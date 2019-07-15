package Core_v2;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

import javax.imageio.ImageIO;

import Data_structure_v2.Rectangle;
import Utility_v2.Formula;


/**
 * This class represent each frame in the program and corresponding available operations we can do to them
 * 
 * @author Allen Liu
 *
 */
public class ProcessImage {
	/**
	 * Constants for converting the RGB to YUV and YUV to RGB
	 */
	private static final double R_WEIGHT = 0.299;
	private static final double B_WEIGHT = 0.114;
	private static final double G_WEIGHT = 0.587;
	private static final double U_MAX = 0.436;
	private static final double V_MAX = 0.615;



	/**
	 * Constants for option in getAverage
	 */
	public static final String R_RGB = "r";
	public static final String G_RGB = "g";
	public static final String B_RGB = "b";
	public static final String Y_YUV = "y";
	public static final String U_YUV = "u";
	public static final String V_YUV = "v";

	public static final String OUTFILE_TYPE_JPG = "jpg";
	public static final String OUTFILE_TYPE_PNG = "png";

	public static final int CREATE_FILE_COLOR_SPACE_RGB = 1;
	public static final int CREATE_FILE_COLOR_SPACE_YUV = 2;

	private BufferedImage img;	//private instance holding the actual image. See more on BufferedImage in Java official doc
	private int width, height;			//width and height for the image
	private String url;					//url for the image
	private String name;		//name of this image: frame1.jpg
	
	private Hashtable<String, Rectangle> templateRegions;	//store rectangles on this image that serves as template for next image 
	private List<Rectangle> templateRegionsList;
	/**
	 * Given the url (relative or absolute), initialize a ProcessImage object
	 * 
	 * ProcessImage, as name suggested, allows individual to do image operations on the image specified
	 * 
	 * @param url	relative or absolute path for the location of image
	 * @throws IOException
	 */
	public ProcessImage(String url) throws IOException
	{
		this.img = readImage(url);
		this.width = this.img.getWidth();
		this.height = this.img.getHeight();
		this.url = url;
		this.templateRegions = new Hashtable<String, Rectangle>();
		this.templateRegionsList = null;
		this.name = url.substring(url.lastIndexOf('/') + 1, url.lastIndexOf('.'));
	}
	/**
	 * Given the BufferedImage, create an instance of ProcessImage
	 * @param img	BufferedImage for instance of ProcessImage. This is the actual image data stored in the 
	 * 				process image
	 */
	public ProcessImage(BufferedImage img)
	{
		this.img = img;
		this.width = img.getWidth();
		this.height = img.getHeight();
		this.url = this.name = null;
	}
	
	/**
	 * Return the name of this image
	 * @return name of the image
	 */
	public String getName()
	{
		return this.name;
	}

	/**
	 * set which region in this image that we want to use it as template to do the template 
	 * matching with the next image; used for motion tracking optimization
	 * @param regions specify the rectangle region we select as template
	 */
	public void setTemplateRegions(List<Rectangle> regions)
	{
		if(this.templateRegions.isEmpty())
		{
			for(Rectangle rec : regions)
			{
				this.templateRegions.put(rec.getName(), rec);
			}
		}
		this.templateRegionsList = regions;
	}
	
	/**
	 * Given the name of the rectangle, return the rectangle itself in this instance of ProcessImage
	 * @param name name of the rectangle
	 * @return return the rectangle with the name
	 */
	public Rectangle getTemplateRegion(String name)
	{
		return this.templateRegions.get(name);
	}
	
	/**
	 * getter for template region
	 * @return a list of templates in current image
	 */
	public List<Rectangle> getTemplateRegionsList()
	{
		return this.templateRegionsList;
	}
	
	/**
	 * Given the rectangle coordinate, draw out the rectangles in the image
	 * @param rect specified rectangle location on the image
	 */
	public void strokeRectOnImage(Rectangle rec)
	{
		rectangleSanityCheck(rec);
		int x1 = rec.getUpperLeftX();
		int x2 = rec.getLowerRightX();
		int y1 = rec.getUpperLeftY();
		int y2 = rec.getLowerRightY();
		Color c = Formula.convertConsToColor(rec.getStrokeColor());
		int[] rgb = {c.getRed(), c.getGreen(), c.getBlue()};
		WritableRaster w = this.img.getRaster();
		for(int i = x1; i <= x2; i++)
		{
			w.setPixel(i, y1, rgb);
			w.setPixel(i, y2, rgb);
		}
		for(int j = y1; j <= y2; j++)
		{
			w.setPixel(x1, j, rgb);
			w.setPixel(x2, j, rgb);
		}
	}
	
	/**
	 * Blur the rectangle area. This is a native way of blurring the image.
	 * Basically sums up all image data, R, G, B, respectively and then take their
	 * average respectively as substitute for original image data
	 * 
	 * @param rec specified rectangle location on the image
	 */
	public void blurRectOnImage(Rectangle rec)
	{
		rectangleSanityCheck(rec);
		int x1 = rec.getUpperLeftX();
		int x2 = rec.getLowerRightX();
		int y1 = rec.getUpperLeftY();
		int y2 = rec.getLowerRightY();
		double avg_red = 0.0;
		double avg_green = 0.0;
		double avg_blue = 0.0;
		for(int i = x1; i <= x2; i++)
		{
			for(int j = y1; j <= y2; j++)
			{
				Color c = new Color(img.getRGB(i, j));
				avg_red += c.getRed();
				avg_green += c.getGreen();
				avg_blue += c.getBlue();
			}
		}
		avg_red /= (rec.getHeight() * rec.getWidth());
		avg_green /= (rec.getHeight() * rec.getWidth());
		avg_blue /= (rec.getHeight() * rec.getWidth());
		WritableRaster w = this.img.getRaster();
		int[] avg = { (int) avg_red, (int) avg_green, (int) avg_blue};
		for(int i = x1; i <= x2; i++)
		{
			for(int j = y1; j <= y2; j++)
			{
				w.setPixel(i, j, avg);
			}
		}
	}
	
	/**
	 * Given other image, return the similarity between this image to the other image
	 * 
	 * The similarity here is defined by the average absolute difference between two images y component
	 * 
	 * @param other the other image to be processed, note the other image's dimension must match the current dimension
	 * 			Else, the IllegalArgumentException will be thrown
	 * @return the similarity of y component in the YUV color space for current image
	 */

	public double getSimilarityBetweenImage(ProcessImage other)
	{
		if(this.width != other.width || this.height != other.height)
		{
			throw new IllegalArgumentException("Make sure you use images that are of same dimension");
		}
		int[][][] first = this.readImageToYUV();
		int[][][] second = other.readImageToYUV();
		int sum = 0;
		for(int row = 0; row < this.width; row++)
		{
			for(int col = 0; col < this.height; col++)
			{
				sum += Math.abs(first[row][col][0] - second[row][col][0]);
			}
		}
		return (double) sum / (this.width * this.height);
	}
	
	/**
	 * Given an rectangle, output the image of interest
	 * @param rec specified extracted region in the image
	 * @return ProcessImage with specified rectangle region
	 */
	public ProcessImage getRectangleImage(Rectangle rec)
	{
		rectangleSanityCheck(rec);
		BufferedImage img = new BufferedImage(rec.getWidth(), rec.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
		WritableRaster w = img.getRaster();
		for(int i = 0; i < rec.getWidth(); i++)
		{
			for(int j = 0; j < rec.getHeight(); j++)
			{
				Color c = new Color(this.img.getRGB(i + rec.getUpperLeftX(), j + rec.getUpperLeftY()));
				int[] rgb = {c.getRed(), c.getGreen(), c.getBlue()};
				w.setPixel(i, j, rgb);
			}
		}
		return new ProcessImage(img);
	}

	/**
	 * Given the option, return the average of the value in image based on that option.
	 * @param option choose from y, u, v, r, g, b (case insensitive).
	 * @return average of y, u, v, r, g, or b value in the image
	 */
	public double getAverage(String option)
	{
		int index = -1;
		boolean yuv = false;
		if (option.equalsIgnoreCase("r") || option.equalsIgnoreCase("y"))
		{
			index = 0;
			if(option.equalsIgnoreCase("y"))
				yuv = true;
		}
		else if (option.equalsIgnoreCase("u") || option.equalsIgnoreCase("g"))
		{
			index = 1;
			if(option.equalsIgnoreCase("u"))
				yuv = true;
		}
		else if (option.equalsIgnoreCase("v") || option.equals("b"))
		{
			index = 2;
			if(option.equalsIgnoreCase("v"))
				yuv = true;
		}
		else
		{
			throw new IllegalArgumentException("The option must be y, u, v, r, g, b!");
		}
		int[][][] data = null;
		int total = 0;
		if(!yuv)
		{
			data = this.getRGBData();
		}
		else
		{
			data = this.readImageToYUV();
		}
		for(int row = 0; row < this.width; row++)
		{
			for(int col = 0; col < this.height; col++)
			{
				total += data[row][col][index];
			}
		}
		return (double) total / (this.width * this.height);
	}

	/**
	 * This is to test the validity of the getYUVImage method. Basically it takes intensity image and UV image.
	 * Combine them to restore the original image (Note that this method has to be called using intensity image)
	 * @param colorImg UV image
	 * @return restored image
	 */
	public BufferedImage restoreToNormal(ProcessImage colorImg)
	{
		int[][][] intensity = this.readImageToYUV();
		int[][][] color = colorImg.readImageToYUV();
		BufferedImage copy = deepCopy(this.img);		//based the intensity image as template
		WritableRaster w = copy.getRaster();
		for(int row = 0; row < this.width; row++)
		{
			for(int col = 0; col < this.height; col++)
			{
				int R, G, B;
				R = (int)(intensity[row][col][0] + 1.140 * color[row][col][2]);
				G = (int)(intensity[row][col][0] -0.395 * color[row][col][1] - 0.581 * color[row][col][2]);
				B = (int)(intensity[row][col][0] + 2.032 * color[row][col][1]);
				int[] arr = {R, G, B};
				arr = preventOverflow(arr);
				w.setPixel(row, col, arr);
			}
		}
		return copy;
	}
	/**
	 * Given the option, return the kind of image corresponds to that option
	 * @param option Either "Y" or "UV". Y means image that has UV component to be 0 in each
	 * 				pixel. UV means image that has Y component to be 0 in each pixel
	 * @return Image correspond to the option
	 */
	public BufferedImage getYUVImage(String option)
	{
		int[][][] yuv = this.readImageToYUV();
		BufferedImage copy = deepCopy(this.img);
		WritableRaster w = copy.getRaster();
		for(int row = 0; row < this.width; row++)
		{
			for(int col = 0; col < this.height; col++)
			{
				int R, G, B;
				R = G = B = 0;
				if(option.equals("Y"))
				{
					R = G = B = yuv[row][col][0];	
				}
				else if(option.equals("UV"))
				{
					R = (int)( 1.140 * yuv[row][col][2]);
					G = (int)(-0.395 * yuv[row][col][1] - 0.581 * yuv[row][col][2]);
					B = (int)(2.032 * yuv[row][col][1]);
				}
				int[] arr = {R, G, B};
				arr = preventOverflow(arr);
				w.setPixel(row, col, arr);
			}
		}
		return copy;
	}
	/**
	 * As name suggested, return the 3D array that contains rgb data. To access this data,
	 * use the data[row][col][0 ~ 2]
	 * @return 3d data array that contains RGB data
	 */
	public int[][][] getRGBData()
	{
		int[][][] rgb = new int[this.width][this.height][3];
		for (int row = 0; row < this.width; row++)
		{
			for(int col = 0; col < this.height; col++)
			{
				Color color_img = new Color(this.img.getRGB(row, col));
				int R = color_img.getRed();
				int G = color_img.getGreen();
				int B = color_img.getBlue();
				int[] curr = {R, G, B};
				rgb[row][col] = curr;
			}
		}
		return rgb;
	}
	/**
	 * get dimension of the image
	 * @return [width, height]
	 */
	public int[] getDimention()
	{
		int [] returned = {this.width, this.height};
		return returned;
	}
	/**
	 * get the image as bufferedImage
	 * @return bufferedImage
	 */
	public BufferedImage getImage()
	{
		return this.img;
	}

	/**
	 * Write the current image to a specified format
	 *  
	 * 		Note that only JPEG is supported right now
	 * 
	 * 
	 * @param url	output location, relative or absolute
	 * @param type	type of output, specified in this class by OUTFILE_TYPE_JPG
	 * @throws IOException
	 */
	public void writeImage(String url, String type) throws IOException
	{
		if(type.equalsIgnoreCase(OUTFILE_TYPE_JPG))
		{
			ImageIO.write(this.img, "jpg", new File(url));
		}
	}

	/**
	 * convert the image to yuv color space
	 * @return 3 d array containing the yuv data for current image
	 */
	public int[][][] readImageToYUV()
	{
		this.width = img.getWidth();
		this.height = img.getHeight();
		int[][][] yuv_img = new int[this.width][this.height][3];
		for (int row = 0; row < this.width; row++) {
			for (int col = 0; col < this.height; col++) {
				Color color_img = new Color(img.getRGB(row, col));
				yuv_img[row][col] = rgb2yuv(color_img);
			}
		}
		return yuv_img;
	}

	/**
	 * Given two images, return the correlation between those two images. Two images must be of same size
	 * 
	 * The cross correlation is calculated using cos = dot_product(u, v) / (len(u) * len(v))
	 * 
	 * Note that since all yuv value are positive, I subtract mean from each obtained doc product. The cross correlation calculated therefore is always
	 * between -1 and 1. The more similar two images are, the closer the cross correlation is to
	 * 1.
	 * 
	 * @param img1
	 * @param img2
	 * @return correlation between those two images
	 */
	public double getCorrelationYBetweenImages(ProcessImage other)
	{
		int width_img1 = this.width, height_img1 = this.height;
		int width_img2 = other.getDimention()[0], height_img2 = other.getDimention()[1];
		if(width_img1 != width_img2 || height_img1 != height_img2)
		{
			throw new IllegalArgumentException("Two images must be of same size");
		}
		int multiplication_result = width_img1 * height_img1 * 3;
		double[] yuv_img1 = new double[multiplication_result];
		double[] yuv_img2 = new double[multiplication_result];
		double sum_yuv_img1 = 0.0, sum_yuv_img2 = 0.0, avg_yuv_img1 = 0.0, avg_yuv_img2 = 0.0;
		int index1 = 0, index2 = 0;
		for (int row = 0; row < width_img1; row++) {
			for (int col = 0; col < height_img1; col++) {
				Color color_img1 = new Color(this.img.getRGB(row, col));
				Color color_img2 = new Color(other.getImage().getRGB(row, col));
				int y1 = rgb2yuv(color_img1)[0];
				//int u1 = rgb2yuv(color_img1)[1];
				//int v1 = rgb2yuv(color_img1)[2];
				int y2 = rgb2yuv(color_img2)[0];
				//int u2 = rgb2yuv(color_img2)[1];
				//int v2 = rgb2yuv(color_img2)[2];
				yuv_img1[index1++] = y1;
				//yuv_img1[index1++] = u1;
				//yuv_img1[index1++] = v1;
				yuv_img2[index2++] = y2;
				//yuv_img2[index2++] = u2;
				//yuv_img2[index2++] = v2;
				sum_yuv_img1 += y1;
				sum_yuv_img2 += y2;
			}
		}
		avg_yuv_img1 = Math.ceil((double) sum_yuv_img1 / multiplication_result);		
		avg_yuv_img2 = Math.ceil((double) sum_yuv_img2 / multiplication_result);
		//naive implementation to find the cos(theta)
		double inner_product = 0.0;
		for (int i = 0; i < multiplication_result; i++)
		{
			inner_product += (yuv_img1[i] - avg_yuv_img1) * (yuv_img2[i] - avg_yuv_img2);
		}
		double inner_img1 = 0.0, inner_img2 = 0.0;
		for(int i = 0; i < yuv_img1.length; i++)
		{
			inner_img1 += Math.pow(yuv_img1[i] -avg_yuv_img1 , 2.0);
			inner_img2 += Math.pow(yuv_img2[i] - avg_yuv_img2, 2.0);
		}
		double length_product = Math.sqrt(inner_img1) * Math.sqrt(inner_img2);
		return inner_product / length_product;
	}

	public double getCorrelationBetweenImages(ProcessImage other)
	{
		int width_img1 = this.width, height_img1 = this.height;
		int width_img2 = other.getDimention()[0], height_img2 = other.getDimention()[1];
		if(width_img1 != width_img2 || height_img1 != height_img2)
		{
			throw new IllegalArgumentException("Two images must be of same size");
		}
		int multiplication_result = width_img1 * height_img1 * 3;
		double[] yuv_img1 = new double[multiplication_result];
		double[] yuv_img2 = new double[multiplication_result];
		double sum_yuv_img1 = 0.0, sum_yuv_img2 = 0.0, avg_yuv_img1 = 0.0, avg_yuv_img2 = 0.0;
		int index1 = 0, index2 = 0;
		for (int row = 0; row < width_img1; row++) {
			for (int col = 0; col < height_img1; col++) {
				Color color_img1 = new Color(this.img.getRGB(row, col));
				Color color_img2 = new Color(other.getImage().getRGB(row, col));
				int y1 = rgb2yuv(color_img1)[0];
				int u1 = rgb2yuv(color_img1)[1];
				int v1 = rgb2yuv(color_img1)[2];
				int y2 = rgb2yuv(color_img2)[0];
				int u2 = rgb2yuv(color_img2)[1];
				int v2 = rgb2yuv(color_img2)[2];
				yuv_img1[index1++] = y1;
				yuv_img1[index1++] = u1;
				yuv_img1[index1++] = v1;
				yuv_img2[index2++] = y2;
				yuv_img2[index2++] = u2;
				yuv_img2[index2++] = v2;
				sum_yuv_img1 += y1;
				sum_yuv_img2 += y2;
			}
		}
		avg_yuv_img1 = Math.ceil((double) sum_yuv_img1 / multiplication_result);		
		avg_yuv_img2 = Math.ceil((double) sum_yuv_img2 / multiplication_result);
		//naive implementation to find the cos(theta)
		double inner_product = 0.0;
		for (int i = 0; i < multiplication_result; i++)
		{
			inner_product += (yuv_img1[i] - avg_yuv_img1) * (yuv_img2[i] - avg_yuv_img2);
		}
		double inner_img1 = 0.0, inner_img2 = 0.0;
		for(int i = 0; i < yuv_img1.length; i++)
		{
			inner_img1 += Math.pow(yuv_img1[i] -avg_yuv_img1 , 2.0);
			inner_img2 += Math.pow(yuv_img2[i] - avg_yuv_img2, 2.0);
		}
		double length_product = Math.sqrt(inner_img1) * Math.sqrt(inner_img2);
		return inner_product / length_product;
	}

	
/**
	 * Given color space, data array, and dimension, return an image with only the color specified
	 * int data array. (Right now mainly for debugging purposes)
	 * 
	 * @param color_space can be either CREATE_FILE_COLOR_SPACE_RGB, or CREATE_FILE_COLOR_SPACE_YUV
	 * @param data 3d array that specifies the R, G, B value if color space is RGB or Y, U, V if color space
	 * 		is Y, U, V
	 * @param width width of the image
	 * @param height height of the image
	 * @return the created image
	 */
	public static ProcessImage createOneColorImage(int color_space, int[] data, int width, int height)
	{
		int[] rgb = null;
		switch(color_space)
		{
		case CREATE_FILE_COLOR_SPACE_RGB:
			break;
		case CREATE_FILE_COLOR_SPACE_YUV:
			rgb = yuv2rgb(data);
			break;
		default:
			throw new IllegalArgumentException("Only support YUV and RGB right now!");
		}
		BufferedImage ret = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		WritableRaster raster = ret.getRaster();
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				raster.setPixel(i, j, rgb);
			}
		}
		ProcessImage img = new ProcessImage(ret);
		return img;
	}

	/**
	 * Print the name and URL for the current working image
	 */
	public String toString()
	{
		String message = "name: " + this.name + " url: " + this.url;
		return message;
	}
	
	/**
	 * helper to convert rbg color mode to yuv color mode
	 * @param Color the color component
	 * @return a vector with YUV value
	 */
	private static int[] rgb2yuv(Color c) 
	{
		int r = c.getRed(), g = c.getGreen(), b = c.getBlue();
		int y = (int)(R_WEIGHT * r + G_WEIGHT * g + B_WEIGHT * b);
		int u = (int)((b - y) * 0.492f); 
		int v = (int)((r - y) * 0.877f);
		int[] yuv = new int[3];
		yuv[0]= y;
		yuv[1]= u;
		yuv[2]= v;
		return yuv;
	}

	/**
	 * helper to convert yuv color mode to rgb color mode
	 * @param yuv yuv array that contains the color
	 * @return a vector with rgb value
	 */
	private static int[] yuv2rgb(int[] yuv) 
	{
		int y = yuv[0], u = yuv[1], v = yuv[2];
		int r = (int)(y + 1.140 * v);
		int g = (int)(y - 0.395 * u - 0.581 * v);
		int b = (int)(y + 2.032 * u);
		int[] rgb = {r, g, b};
		int[] cleaned = preventOverflow(rgb);
		return cleaned;
	}
	/**
	 * This method is critical as the formula for converting YUV to RGB or RGB to YUV is not ganranteed to be within
	 * the range of RGB value.
	 * @param RGB raw RGB data
	 * @return valid and useable RGB data
	 */
	private static int[] preventOverflow(int[] RGB)
	{
		int R = RGB[0];
		int G = RGB[1];
		int B = RGB[2];
		if(R > 255)
		{
			R = 255;
		}
		else if (R < 0)
		{
			R = 0;
		}
		if(G > 255)
		{
			G = 255;
		}
		else if (G <0)
		{
			G = 0;
		}
		if (B > 255)
		{
			B = 255;
		}
		else if (B < 0)
		{
			B = 0;
		}
		int[] returned = {R, G, B};
		return returned;
	}
	/**
	 * Check if the rectangle is valid. Valid means if it is within the bounds of image
	 * @param rec region of interest on the image
	 */
	private void rectangleSanityCheck(Rectangle rec)
	{
		int x1 = rec.getUpperLeftX();
		int x2 = rec.getLowerRightX();
		int y1 = rec.getUpperLeftY();
		int y2 = rec.getLowerRightY();
		if (x1 < 0 || y1 < 0 || x2 < 0 || y2 < 0)
		{
			throw new IllegalArgumentException("The coordinate must be non-negative!");
		}
		if(x1 > this.width || x2 > this.width || y1 > this.height || y2 > this.height)
		{
			throw new IllegalArgumentException("The coordinate must be within the dimension of the image");
		}
		if (x1 > x2 || y1 > y2)
		{
			throw new IllegalArgumentException("upper left coordinate must be left of and up to lower right coordinate");
		}
	}
	
	/**
	 * Give an image URL, read it as BufferedImage
	 * @param url
	 * @return BufferedImage
	 */
	private static BufferedImage readImage(String url) throws IOException
	{
		BufferedImage img = ImageIO.read(new File(url));
		return img;
	}

	/**
	 * Given a target of a BufferedImage instance, return a full copy of it
	 * @param bi target BufferedImage
	 * @return a deep copy of bi
	 */
	private static BufferedImage deepCopy(BufferedImage bi) {
		ColorModel cm = bi.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = bi.copyData(null);
		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}
}
