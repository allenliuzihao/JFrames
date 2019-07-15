package testing_v2;


import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import Core_v2.ProcessFrames;
import Core_v2.ProcessImage;
import Data_structure_v2.Rectangle;
import Utility_v2.Constants;

public class TestCore {
	public static String input_dir = Constants.input_dir;
	public static String output_dir = Constants.output_dir;
	public static void main(String[] args)
	{
		testImprovementsOnShort();
	}
	private static void testImprovementsOnShort()
	{
		int counter = 1;
		long correlation_time = 0;
		try
		{
			long startTime = System.currentTimeMillis();
			ProcessFrames frames = new ProcessFrames(input_dir);
			List<ProcessImage> images = frames.getPrevCurrImages();
			ProcessImage prev = images.get(0);
			Rectangle rec = new Rectangle(410, 51, 470, 121);
			rec.setName("a");
			rec.setStrokeColor(Constants.COLOR_RED);
			prev.strokeRectOnImage(rec);
			List<Rectangle> l = new ArrayList<Rectangle>();
			l.add(rec);
			prev.setTemplateRegions(l);
			frames.outputPrevImg(output_dir + "/frame" + (counter++) + ".jpg");
			while(frames.hasNext())
			{
				long start = System.currentTimeMillis();
				frames.drawBestFitInPrevOnCurr_Correlation();
				long end = System.currentTimeMillis();
				correlation_time += (end - start);
				frames.outputCurrImg(output_dir + "/frame" + (counter++) + ".jpg");
				frames.next();
			}
			frames.outputCurrImg(output_dir + "/frame" + (counter++) + ".jpg");
			long endTime = System.currentTimeMillis();
			System.out.println("Took "+(endTime - startTime) + " s"); 
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("verify ROI Time: " + ProcessFrames.verifyROIOnTime);
		System.out.println("Correlation Time: " + correlation_time);
	}
	private static void testImprovementsOnShort1()
	{
		int counter = 1;
		try
		{
			long startTime = System.currentTimeMillis();
			ProcessFrames frames = new ProcessFrames(input_dir);
			List<ProcessImage> images = frames.getPrevCurrImages();
			ProcessImage prev = images.get(0);
			Rectangle rec1 = new Rectangle(81, 71, 151, 161);
			Rectangle rec2 = new Rectangle(365, 125, 428, 198);
			rec1.setName("a");
			rec2.setName("b");
			rec1.setStrokeColor(Constants.COLOR_RED);
			rec2.setStrokeColor(Constants.COLOR_RED);
			prev.strokeRectOnImage(rec1);
			prev.strokeRectOnImage(rec2);
			List<Rectangle> l = new ArrayList<Rectangle>();
			l.add(rec1);
			l.add(rec2);
			prev.setTemplateRegions(l);
			frames.outputPrevImg(output_dir + "/frame" + (counter++) + ".jpg");
			while(frames.hasNext())
			{
				frames.drawBestFitInPrevOnCurr_Correlation();
				frames.outputCurrImg(output_dir + "/frame" + (counter++) + ".jpg");
				frames.next();
			}
			frames.outputCurrImg(output_dir + "/frame" + (counter++) + ".jpg");
			long endTime = System.currentTimeMillis();
			System.out.println("Took "+(endTime - startTime) + " s"); 
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	private static void testIteratingFrames()
	{
		try
		{
			long startTime = System.currentTimeMillis();
			ProcessFrames frames = new ProcessFrames(input_dir);
			while(frames.hasNext())
			{
				frames.next();
			}
			long endTime = System.currentTimeMillis();
			System.out.println("Took "+(endTime - startTime) + " s"); 
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	private static void testProcessFrames()
	{
		try
		{
			ProcessFrames frames = new ProcessFrames(input_dir);
			List<ProcessImage> images = frames.getPrevCurrImages();
			ProcessImage prev = images.get(0);
			ProcessImage curr = images.get(1);
			List<Rectangle> l = new ArrayList<Rectangle>();
			l.add(new Rectangle(34, 110, 114, 198));
			prev.setTemplateRegions(l);
			frames.drawBestFitInPrevOnCurr_Correlation();
			prev.writeImage(input_dir +"/frame_prev.jpg" , "jpg");
			curr.writeImage(input_dir + "/frame_curr.jpg",  "jpg");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	private static void testExtractRectangleImage()
	{
		try
		{
			ProcessImage img1 = new ProcessImage(input_dir+"/frame1.jpg");
			Rectangle rec = new Rectangle(200, 200, 250, 250);
			ProcessImage recImg = img1.getRectangleImage(rec);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	private static void testBlurRectangleOnImage()
	{
		try
		{
			ProcessImage img1 = new ProcessImage(input_dir+"/frame1.jpg");
			Rectangle rec = new Rectangle(200, 200, 250, 250);
			img1.blurRectOnImage(rec);
			img1.writeImage(input_dir + "/frame1_blur.jpg",  "jpg");

		}catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	private static void testStrokingRectangleOnImage()
	{
		try
		{
			ProcessImage img1 = new ProcessImage(input_dir+"/frame1.jpg");
			Rectangle rec = new Rectangle(200, 200, 250, 250);
			rec.setStrokeColor(Constants.COLOR_RED);
			img1.strokeRectOnImage(rec);
			img1.writeImage(input_dir + "/frame1_out.jpg",  "jpg");

		}catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	private static void testGetSimilarityBetweenImages()
	{
		try
		{
			ProcessImage img1 = new ProcessImage(input_dir+"/frame1.jpg");
			ProcessImage img2 = new ProcessImage(input_dir + "/frame1.jpg");
			System.out.println(img1.getCorrelationYBetweenImages(img2));
			System.out.println(img1.getSimilarityBetweenImage(img2));

		}catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	private static void testYUVInfo()
	{
		try
		{
			ProcessImage img1 = new ProcessImage(input_dir + "/frame1.jpg");
			int[][][] yuv = img1.readImageToYUV();
			printImageDataArray(yuv);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	private static void testGetRGBInfo(ProcessImage img)
	{
		int[][][] rgb = img.getRGBData();
		int[] dimension = img.getDimention();
		for (int i = 0; i < dimension[0]; i++)
		{
			for (int j = 0; j < dimension[1]; j++)
			{
				System.out.println("R: " + rgb[i][j][0] + " G: " + rgb[i][j][1] + " B: " + rgb[i][j][2]);
			}
			System.out.println();
		}
	}
	private static void getImageType()
	{
		try
		{
			ProcessImage img = new ProcessImage(input_dir+"/frame1.jpg");
			System.out.println("Type of image: " + getImageType(img.getImage().getType()));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	private static void printImageDataArray(int[][][] data)
	{
		for(int i = 0; i < data.length; i++)
		{
			for(int j = 0; j < data[i].length; j++)
			{
				System.out.println("first: " + data[i][j][0] + " second: " + data[i][j][1] + " third: " + data[i][j][2]);
			}
		}
	}
	private static String getImageType(int type)
	{
		switch(type){
		case BufferedImage.TYPE_3BYTE_BGR: return "TYPE_3BYTE_BGR";
		case BufferedImage.TYPE_4BYTE_ABGR: return "4BYTE_ABGR";
		case BufferedImage.TYPE_4BYTE_ABGR_PRE: return "4BYTE_ABGR_PRE";
		default: return "Nothing so far";
		}
	}
}
