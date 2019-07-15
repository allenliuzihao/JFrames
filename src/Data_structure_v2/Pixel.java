package Data_structure_v2;

/**
 * This class stores the color information for each pixel in the image. Used this for heap sort purposes
 * 
 * @author allenliu
 *
 */
public class Pixel implements Comparable<Pixel> {
	private int[] data;
	private int numHits;
	public Pixel(int[] data, int numHits)
	{
		this.data = data;
		this.numHits = numHits;
	}
	public int[] getData()
	{
		return this.data;
	}
	public int getNumHits()
	{
		return this.numHits;
	}
	@Override
	public int compareTo(Pixel o) {
		if(this.numHits == o.numHits)
		{
			return 0;
		}
		else if(this.numHits < o.numHits)
		{
			return 1;
		}
		else
		{
			return -1;
		}
	}
	public String toString()
	{
		StringBuilder s = new StringBuilder();
		s.append("Number of hits for this pixel: " + this.numHits + "\nHere is the data : ");
		for(int i = 0; i < data.length; i++)
		{
			s.append(this.data[i]);
		}
		return s.toString();
	}
	
}
