package Utility_v2;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import Data_structure_v2.Rectangle;

public class ReadWrite {
	public static List<String> readEachLine(String line)
	{
		List<String> result = new ArrayList<String>();
		char[] c = line.toCharArray();
		int charCount = 0;
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < c.length; i++)
		{
			if(c[i] == '(' || c[i] == ')')
			{
				charCount++;
				if(charCount == 2)
				{
					result.add(sb.toString());
					charCount = 0;
					sb = new StringBuilder();
				}
				continue;
			}
			sb.append(c[i]);
		}
		return result;
	}
	public static List<Rectangle> readCoordinate(List<String> curr)
	{
		List<Rectangle> rects = new ArrayList<Rectangle>();
		for(String s : curr)
		{
			String[] result = s.split(",");
			Rectangle currRec = new Rectangle(Integer.parseInt(result[0]), Integer.parseInt(result[1]), Integer.parseInt(result[2]), Integer.parseInt(result[3]));
			rects.add(currRec);
		}
		return rects;
	}
	public static void writeRectEachLine(List<Rectangle> eachLine, PrintWriter p)
	{
		for(Rectangle rec : eachLine)
		{
			p.print("(" + rec.getUpperLeftX() + "," + rec.getUpperLeftY() + "," + rec.getLowerRightX() + "," + rec.getLowerRightY() + ")");
		}
		p.println();
	}
}
