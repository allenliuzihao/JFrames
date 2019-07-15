package Data_structure_v2;

import java.io.IOException;

/**
 * API for Histogram data-structure. Super class for all other used histogram
 * @author allenliu
 *
 */
public abstract class Histogram<E> {
	
	/**
	 * Put an element in histogram
	 * @param item
	 */
	public abstract void put(E[] item);
	
	/**
	 * Print the stat for the current Histogram
	 */
	public abstract void printStat();
	
	/**
	 * get number of element in the histogram
	 * @return size of elements
	 */
	public abstract int getElementsSize();
	
	/**
	 * Get correlation between two histogram
	 * @param other
	 * @return
	 */
	public abstract double getCorrelation(Histogram<E> other);
	
	/**
	 * Get similarity between two histogram
	 * @param other
	 * @return
	 */
	public abstract double getSimilarity(Histogram<E> other);
	
}
