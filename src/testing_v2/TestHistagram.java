package testing_v2;

import java.util.List;

import Utility_v2.Constants;

import Core_v2.ProcessImage;
import Data_structure_v2.Pixel;
import Data_structure_v2.UVColorHistogram;

public class TestHistagram 
{
	public static String dir = Constants.input_dir;
	public static void main(String[] args)
	{
		UVColorHistogram h = new UVColorHistogram();
		UVColorHistogram h1 = new UVColorHistogram();
		try
		{
			ProcessImage img1 = new ProcessImage(dir + "/frame1.jpg");
			ProcessImage img2 = new ProcessImage(dir + "/frame2.jpg");
			
			int[][][] yuv = img1.readImageToYUV();
			int[][][] yuv1 = img2.readImageToYUV();
			for(int i = 0; i < yuv.length; i++)
			{
				for(int j = 0; j < yuv[i].length; j++)
				{
					Integer[] color = {new Integer(yuv[i][j][1]), new Integer(yuv[i][j][2])};
					h.put(color);
					Integer[] color1 = {new Integer(yuv1[i][j][1]), new Integer(yuv1[i][j][2])};
					h1.put(color1);
				}
			}
			//System.out.println("Similarity: " + h.getSimilarity(h1));
			//System.out.println("Correlation: " + h.getCorrelation(h1));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
}
