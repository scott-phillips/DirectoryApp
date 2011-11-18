package edu.tamu.directoryapp.model;

/**
 * An object representing a directory location.
 * 
 * @author <a href="http://www.scottphillips.com">Scott Phillips</a>
 */

public interface Location extends Comparable<Location> {

	/**
	 * @return The location's displayable label
	 */
	public String getLabel();
	
	/**
	 * @return The location's technical value
	 */
	public String getValue();
	
}
