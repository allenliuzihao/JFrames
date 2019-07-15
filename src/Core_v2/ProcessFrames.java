package Core_v2;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import Data_structure_v2.Rectangle;
import Data_structure_v2.UVColorHistogram;


/**
 * Class is implemented to do image processing on all frames of a video. To save the memory and time
 * of processing, I only keep track of two instance of ProcessImage at a time.
 * 
 * @author allenliu
 *
 */
public class ProcessFrames {

	private ProcessImage prev;		//previous template image
	private ProcessImage curr;		//current working image
	private List<String> frames;	//stores the names of all frames
	private int counter;			//use to get the name of each frame (Note frame name must be of form: frame1.jpg, frame2.jpg)
	private String folder;			//contain the folder location
	private List<ProcessImage> memory; 			//contains previous 7 elements; optimization for our motion tracking algorithm
	public static long verifyROIOnTime;
	
	/**
	 * Given the folder path, initialize the ProcessFrame instance
	 * @param url path for folder
	 * @throws IOException
	 */
	public ProcessFrames(String url) throws IOException
	{
		this.frames = listFilesForFolder(new File(url));
		this.counter = 1;
		this.folder = url;
		this.prev = new ProcessImage(url + "/" + frames.get(0));
		this.curr = new ProcessImage(url + "/" + frames.get(1));
		this.memory = new LinkedList<ProcessImage>();
		this.memory.add(prev);
		this.memory.add(curr);
	}

	/**
	 * Select the next two pairs to work with. 
	 * @throws IOException
	 */
	public void next() throws IOException
	{
		this.prev = this.curr;
		this.curr = new ProcessImage(this.folder + "/" + frames.get(++this.counter));
		if(1 + this.memory.size() > 7)
		{
			this.memory.remove(0);
		}
		this.memory.add(this.curr);
	}

	/**
	 * check if we have next pair of frames available
	 * @return true if we have
	 */
	public boolean hasNext()
	{
		return (counter < this.frames.size() - 1) ? true : false;
	}

	/**
	 * return the prev frame and curr frame in the current working set of entire frames
	 * @return list containing curr and prev ProcessImage instance.
	 */
	public List<ProcessImage> getPrevCurrImages()
	{
		List<ProcessImage> li = new ArrayList<ProcessImage>();
		li.add(this.prev);
		li.add(this.curr);
		return li;
	}

	/**
	 * Methods for outputing the current image in current working set of images
	 * @param url path for output
	 * @throws IOException
	 */
	public void outputCurrImg(String url) throws IOException
	{
		this.curr.writeImage(url, "jpg");
	}

	/**
	 * Methods for outputing the previous image in current working set of images
	 * @param url path for output
	 * @throws IOException
	 */
	public void outputPrevImg(String url) throws IOException
	{
		this.prev.writeImage(url, "jpg");
	}
	
	public int numFrames()
	{
		return this.frames.size();
	}

	/**
	 * Given a set of template region in each ProcessImage, move the rectangle box a bit 25px around to determine the best
	 * fit with the previous template in prev image
	 * 
	 * First compute the correlation between the intensity of two bounding box. Then use the correlation of UV color histogram
	 * between two images to verify that this is true.
	 * 
	 */
	public void drawBestFitInPrevOnCurr_Correlation() throws IOException
	{
		List<Rectangle> prevRegions = this.prev.getTemplateRegionsList();
		List<Rectangle> returnedRectangle = new ArrayList<Rectangle>();
		Random rand = new Random();
		for(Rectangle rec : prevRegions)
		{
			ProcessImage bestFit = null;
			Rectangle bestFitRec = rec;
			ProcessImage prev = this.prev.getRectangleImage(rec);
			double highestCorrelation = Double.NEGATIVE_INFINITY;
			int numTrials = rand.nextInt(11) + 10;
			for(int i = 0; i < numTrials; i++)
			{
				int offset = rand.nextInt(21) - 10;
				int x1 = rec.getUpperLeftX();
				int y1 = rec.getUpperLeftY();
				int x2 = rec.getLowerRightX();
				int y2 = rec.getLowerRightY();
				int new_x1 = x1 + offset;
				int new_y1 = y1 + offset;
				int new_x2 = x2 + offset;
				int new_y2 = y2 + offset;
				//quite impossible cases but just in case
				if (new_x1 > this.curr.getDimention()[0] - 1 || new_y1 > this.curr.getDimention()[1] - 1 || new_x2 < 0 || new_y2 < 0)
				{
					throw new IllegalArgumentException("Image is too small!");
				}
				if(new_x1 < 0)
				{
					new_x1 = 0;
					int new_offset = new_x1 - x1;
					new_x2 = x2 + new_offset;
					new_y1 = y1 + new_offset;
					new_y2 = y2 + new_offset;
				}
				if(new_y1 < 0)
				{
					new_y1 = 0;
					int new_offset = new_y1 - y1;
					new_y2 = y2 + new_offset;
					new_x1 = x1 + new_offset;
					new_x2 = x2 + new_offset;
				}
				if (new_x2 > this.curr.getDimention()[0] - 1)
				{
					new_x2 = this.curr.getDimention()[0] - 1;
					int new_offset = new_x2 - x2;
					new_x1 = x1 + new_offset;
					new_y2 = y2 + new_offset;
					new_y1 = y1 + new_offset;
				}
				if(new_y2 > this.curr.getDimention()[1] - 1)
				{	
					new_y2 = this.curr.getDimention()[1] - 1;
					int new_offset = Math.abs(new_y2 - y2);
					new_y1 = y1 + new_offset;
					new_x1 = x1 + new_offset;
					new_x2 = x2 + new_offset;
				}	
				Rectangle new_rec = new Rectangle(new_x1, new_y1, new_x2, new_y2);
				new_rec.setStrokeColor(rec.getStrokeColor());
				ProcessImage curr = this.curr.getRectangleImage(new_rec);
				double correlation = curr.getCorrelationYBetweenImages(prev);
				if(correlation > highestCorrelation)
				{
					highestCorrelation = correlation;
					bestFit = curr;
					bestFitRec = new_rec;
					bestFitRec.setName(rec.getName());
				}
			}
			int[][][] prev_yuv = prev.readImageToYUV();
			int[][][] best_yuv = bestFit.readImageToYUV();
			UVColorHistogram prev_h = new UVColorHistogram();
			UVColorHistogram curr_h = new UVColorHistogram();
			for(int i = 0; i < prev_yuv.length; i++)
			{
				for(int j = 0; j < prev_yuv[i].length; j++)
				{
					Integer[] curr_uv = {prev_yuv[i][j][1], prev_yuv[i][j][2]};
					Integer[] prev_uv = {best_yuv[i][j][1], best_yuv[i][j][2]};
					prev_h.put(prev_uv);
					curr_h.put(curr_uv);
				}
			}
			double correlation =  prev_h.getCorrelation(curr_h);
			if(correlation > 0.70 && verifyROIOnTrack(this.memory, this.curr, bestFitRec))
			{
				this.curr.strokeRectOnImage(bestFitRec);
				returnedRectangle.add(bestFitRec);
			}
		}
		this.curr.setTemplateRegions(returnedRectangle);
	}
	
	/**
	 * Given a set of template region in each ProcessImage, move the rectangle box a bit 25px around to determine the best
	 * fit with the previous template in prev image
	 * 
	 * First compute the correlation between the intensity of two bounding box. Then use the correlation of UV color histogram
	 * between two images to verify that this is true.
	 * 
	 */
	public List<Rectangle> findBestFitInPrevOnCurr_Correlation() throws IOException
	{
		List<Rectangle> prevRegions = this.prev.getTemplateRegionsList();
		List<Rectangle> returnedRectangle = new ArrayList<Rectangle>();
		Random rand = new Random();
		for(Rectangle rec : prevRegions)
		{
			ProcessImage bestFit = null;
			Rectangle bestFitRec = rec;
			ProcessImage prev = this.prev.getRectangleImage(rec);
			double highestCorrelation = Double.NEGATIVE_INFINITY;
			int numTrials = rand.nextInt(11) + 10;
			for(int i = 0; i < numTrials; i++)
			{
				int offset = rand.nextInt(21) - 10;
				int x1 = rec.getUpperLeftX();
				int y1 = rec.getUpperLeftY();
				int x2 = rec.getLowerRightX();
				int y2 = rec.getLowerRightY();
				int new_x1 = x1 + offset;
				int new_y1 = y1 + offset;
				int new_x2 = x2 + offset;
				int new_y2 = y2 + offset;
				//quite impossible cases but just in case
				if (new_x1 > this.curr.getDimention()[0] - 1 || new_y1 > this.curr.getDimention()[1] - 1 || new_x2 < 0 || new_y2 < 0)
				{
					throw new IllegalArgumentException("Image is too small!");
				}
				if(new_x1 < 0)
				{
					new_x1 = 0;
					int new_offset = new_x1 - x1;
					new_x2 = x2 + new_offset;
					new_y1 = y1 + new_offset;
					new_y2 = y2 + new_offset;
				}
				if(new_y1 < 0)
				{
					new_y1 = 0;
					int new_offset = new_y1 - y1;
					new_y2 = y2 + new_offset;
					new_x1 = x1 + new_offset;
					new_x2 = x2 + new_offset;
				}
				if (new_x2 > this.curr.getDimention()[0] - 1)
				{
					new_x2 = this.curr.getDimention()[0] - 1;
					int new_offset = new_x2 - x2;
					new_x1 = x1 + new_offset;
					new_y2 = y2 + new_offset;
					new_y1 = y1 + new_offset;
				}
				if(new_y2 > this.curr.getDimention()[1] - 1)
				{	
					new_y2 = this.curr.getDimention()[1] - 1;
					int new_offset = Math.abs(new_y2 - y2);
					new_y1 = y1 + new_offset;
					new_x1 = x1 + new_offset;
					new_x2 = x2 + new_offset;
				}	
				Rectangle new_rec = new Rectangle(new_x1, new_y1, new_x2, new_y2);
				ProcessImage curr = this.curr.getRectangleImage(new_rec);
				double correlation = curr.getCorrelationYBetweenImages(prev);
				if(correlation > highestCorrelation)
				{
					highestCorrelation = correlation;
					bestFit = curr;
					bestFitRec = new_rec;
					bestFitRec.setName(rec.getName());
				}
			}
			int[][][] prev_yuv = prev.readImageToYUV();
			int[][][] best_yuv = bestFit.readImageToYUV();
			UVColorHistogram prev_h = new UVColorHistogram();
			UVColorHistogram curr_h = new UVColorHistogram();
			for(int i = 0; i < prev_yuv.length; i++)
			{
				for(int j = 0; j < prev_yuv[i].length; j++)
				{
					Integer[] curr_uv = {prev_yuv[i][j][1], prev_yuv[i][j][2]};
					Integer[] prev_uv = {best_yuv[i][j][1], best_yuv[i][j][2]};
					prev_h.put(prev_uv);
					curr_h.put(curr_uv);
				}
			}
			double correlation =  prev_h.getCorrelation(curr_h);
			if(correlation > 0.70 && verifyROIOnTrack(this.memory, this.curr, bestFitRec))
			{
				returnedRectangle.add(bestFitRec);
			}
		}
		return returnedRectangle;
	}
	
	public static List<Rectangle> getCleanEstimatedRectangles(List<Rectangle> estimated, List<Rectangle> detected)
	{
		List<Rectangle> clonePrev = new ArrayList<Rectangle>(estimated);
		for(int i = 0; i < detected.size(); i++)
		{
			List<Rectangle> removeList = new ArrayList<Rectangle>();
			for(int j = 0; j < clonePrev.size(); j++)
			{
				 int x_overlap = Math.max(0, Math.min(detected.get(i).getLowerRightX(), clonePrev.get(j).getLowerRightX()) - Math.max(detected.get(i).getUpperLeftX(), clonePrev.get(j).getUpperLeftX()));
				 int y_overlap = Math.max(0, Math.min(detected.get(i).getLowerRightY(), clonePrev.get(j).getLowerRightY()) - Math.max(detected.get(i).getUpperLeftY(), clonePrev.get(j).getUpperLeftY()));
				 if (x_overlap * y_overlap > 0.5 * (clonePrev.get(j).calcArea()))
				 {
					 removeList.add(clonePrev.get(j));
				 }
			}
			clonePrev.removeAll(removeList);
		}
		return clonePrev;
	}

	/**
	 * Given two process image, one is ranked random number between 4 and 7 before the current processed image
	 * , the second is the current image. The goal for this is to reduce the number of false positive introduced 
	 * by using drawBestFitInPrevOnCurr_Correlation and drawBestFitInPrevOnCurr_Similarity
	 *
	 * @return true if ROI identified by drawBestFitInPrevOnCurr_Correlation or drawBestFitInPrevOnCurr_Correlation correctly
	 * mapped the ROI in current image; false otherwise
	 */
	private static boolean verifyROIOnTrack(List<ProcessImage> memory, ProcessImage currImg, Rectangle curr) throws IOException
	{
		Random rand = new Random();
		int randNum = rand.nextInt(5);
		if(randNum < memory.size() && randNum != memory.size() - 1)
		{
			//read that previous frame first
			ProcessImage prev = memory.get(randNum);
			Rectangle prevRec = prev.getTemplateRegion(curr.getName());
			if(prevRec != null)
			{
				double correlation = prev.getRectangleImage(prevRec).getCorrelationBetweenImages(currImg.getRectangleImage(curr));
				if (correlation < 0.70)
				{
					return false;
				}
			}
		}
		return true;
	}
	/**
	 * Given a set of template region in each ProcessImage, move the rectangle box a bit 25px around to determine the best
	 * fit with the previous template in prev image
	 * 
	 * First compute the similarity between the intensity of two bounding box. Then use the similarity of UV color histogram
	 * between two images to verify that this is true.
	 * 
	 */
	public void drawBestFitInPrevOnCurr_Similarity()
	{
		List<Rectangle> prevRegions = this.prev.getTemplateRegionsList();
		List<Rectangle> returnedRectangle = new ArrayList<Rectangle>();
		Random rand = new Random();
		for(Rectangle rec : prevRegions)
		{
			ProcessImage bestFit = null;
			Rectangle bestFitRec = rec;
			ProcessImage prev = this.prev.getRectangleImage(rec);
			double highestSimilarity = Double.POSITIVE_INFINITY;
			//System.out.println("original: x1" + rec.getUpperLeftX() + " y1: " + rec.getUpperLeftY() + " x2: " + rec.getLowerRightX() + " y2: " + rec.getLowerRightY());
			int numTrials = rand.nextInt(10) + 10;
			for(int i = 0; i < numTrials; i++)
			{
				int offset = rand.nextInt(11) - 5;
				int x1 = rec.getUpperLeftX();
				int y1 = rec.getUpperLeftY();
				int x2 = rec.getLowerRightX();
				int y2 = rec.getLowerRightY();

				int new_x1 = x1 + offset;
				int new_y1 = y1 + offset;
				int new_x2 = x2 + offset;
				int new_y2 = y2 + offset;
				//quite impossible cases but just in case
				if (new_x1 > this.curr.getDimention()[0] - 1 || new_y1 > this.curr.getDimention()[1] - 1 || new_x2 < 0 || new_y2 < 0)
				{
					throw new IllegalArgumentException("Image is too small!");
				}
				if(new_x1 < 0)
				{
					new_x1 = 0;
					int new_offset = new_x1 - x1;
					new_x2 = x2 + new_offset;
					new_y1 = y1 + new_offset;
					new_y2 = y2 + new_offset;
				}
				if(new_y1 < 0)
				{
					new_y1 = 0;
					int new_offset = new_y1 - y1;
					new_y2 = y2 + new_offset;
					new_x1 = x1 + new_offset;
					new_x2 = x2 + new_offset;
				}
				if (new_x2 > this.curr.getDimention()[0] - 1)
				{
					new_x2 = this.curr.getDimention()[0] - 1;
					int new_offset = new_x2 - x2;
					new_x1 = x1 + new_offset;
					new_y2 = y2 + new_offset;
					new_y1 = y1 + new_offset;
				}
				if(new_y2 > this.curr.getDimention()[1] - 1)
				{	
					new_y2 = this.curr.getDimention()[1] - 1;
					int new_offset = Math.abs(new_y2 - y2);
					new_y1 = y1 + new_offset;
					new_x1 = x1 + new_offset;
					new_x2 = x2 + new_offset;
				}	
				//System.out.println("x1: " + new_x1 + " y1: " + new_y1 + " x2: " + new_x2 + " y2: " + new_y2);
				Rectangle new_rec = new Rectangle(new_x1, new_y1, new_x2, new_y2);
				new_rec.setStrokeColor(rec.getStrokeColor());
				ProcessImage curr = this.curr.getRectangleImage(new_rec);
				double similarity = curr.getSimilarityBetweenImage(prev);
				if(similarity < highestSimilarity)
				{
					//System.out.println("Best correlation: " + correlation + " ");
					//System.out.println("Best rectangle" + new_rec);
					highestSimilarity = similarity;
					bestFit = curr;
					bestFitRec = new_rec;
				}
			}
			int[][][] prev_yuv = prev.readImageToYUV();
			int[][][] best_yuv = bestFit.readImageToYUV();
			UVColorHistogram prev_h = new UVColorHistogram();
			UVColorHistogram curr_h = new UVColorHistogram();
			for(int i = 0; i < prev_yuv.length; i++)
			{
				for(int j = 0; j < prev_yuv[i].length; j++)
				{
					Integer[] curr_uv = {prev_yuv[i][j][1], prev_yuv[i][j][2]};
					Integer[] prev_uv = {best_yuv[i][j][1], best_yuv[i][j][2]};
					prev_h.put(prev_uv);
					curr_h.put(curr_uv);
				}
			}
			double similarity =  prev_h.getSimilarity(curr_h);
			if(similarity < 0.1)
			{
				this.curr.strokeRectOnImage(bestFitRec);
				returnedRectangle.add(bestFitRec);
			}
		}
		this.curr.setTemplateRegions(returnedRectangle);
	}
	/**
	 * Recursive helper for displaying all files in the folder
	 * in the order of the number after each files
	 * @param folder path for the folder location
	 * @return list of string containing name of files in the folder
	 */
	private List<String> listFilesForFolder(File folder) {
		int numFiles = 0;
		for (File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				listFilesForFolder(fileEntry);
			} else {
				String filename = fileEntry.getName();
				String extension = filename.substring(filename.lastIndexOf('.') + 1).trim();
				if(extension.equals("jpg") || extension.equals("jpeg") || extension.equals("jpe") || extension.equals("jfif"))
				{
					numFiles++;
				}
			}
		}
		List<String> result = new ArrayList<String>();
		for(int i = 0; i < numFiles; i++)
		{
			result.add("frame" + i + ".jpg");
		}
		return result;
	}
}
