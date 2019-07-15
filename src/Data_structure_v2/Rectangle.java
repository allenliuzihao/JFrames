package Data_structure_v2;

import java.util.UUID;

import Utility_v2.Constants;

/**
 * Use this class to represent retangle regions on the ProcessImage
 * 
 * @author allenliu
 *
 */
public class Rectangle extends Shape{
	
	/* Upper left coordinate */
	private int x1;
	private int y1;
	
	private int x2;
	private int y2;
	
	private int strokeColor;
	private int fillColor;
	private int thickness;
	
	private String name;
	
	/* Lower right coordinate */
	
	public Rectangle(int x1, int y1, int x2, int y2)
	{
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		this.strokeColor = Constants.COLOR_BLACK;
		this.fillColor = Constants.COLOR_BLACK;
		this.thickness = 1;
		String rawUIUD = UUID.randomUUID().toString();
		this.name = rawUIUD.replace("-", "");
	}
	public Rectangle(int x1, int y1, int x2, int y2, int thickness)
	{
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		this.strokeColor = Constants.COLOR_BLACK;
		this.fillColor = Constants.COLOR_BLACK;
		this.thickness = thickness;
		String rawUIUD = UUID.randomUUID().toString();
		this.name = rawUIUD.replace("-", "");
	}

	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getName()
	{
		return this.name;
	}
	public void setThickness(int thick)
	{
		this.thickness = thick;
	}
	public int getThickness()
	{
		return this.thickness;
	}
	public int getStrokeColor()
	{
		return this.strokeColor;
	}
	public void setStrokeColor(int color)
	{
		this.strokeColor = color;
	}
	public void setFillColor(int color)
	{
		this.fillColor = color;
	}
	public int getUpperLeftX()
	{
		return this.x1;
	}
	public int getUpperLeftY()
	{
		return this.y1;
	}
	public int getLowerRightX()
	{
		return this.x2;
	}
	public int getLowerRightY()
	{
		return this.y2;
	}
	public int getWidth()
	{
		return Math.abs(this.x1 - this.x2); 
	}
	
	public int getHeight()
	{
		return Math.abs(this.y1 - this.y2);
	}
	
	public double distanceTo(Rectangle other)
	{
		double midx_rec1 = (this.x1 + this.x2) / 2;
		double midy_rec1 = (this.y1 + this.y2) / 2;
		double midx_rec2 = (other.x1 + other.x2) / 2;
		double midy_rec2 = (other.y1 + other.y2) / 2;
		double first_part = Math.pow(midx_rec1 - midx_rec2, 2);
		double second_part = Math.pow(midy_rec1 - midy_rec2, 2);
		return Math.sqrt(first_part + second_part);
	}
	
	public String toString()
	{
		StringBuilder s = new StringBuilder();
		s.append("name: " + this.name);
		s.append("x1: " + this.x1 + " y1 : " + this.y1 + " x2: " + this.x2 + " y2: " + this.y2);
		return s.toString();
	}
	
	@Override
	public double calcArea() {
		return this.getWidth() * this.getHeight();
	}
	
}
