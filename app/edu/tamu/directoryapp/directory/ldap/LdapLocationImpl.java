package edu.tamu.directoryapp.directory.ldap;

import edu.tamu.directoryapp.model.Department;
import edu.tamu.directoryapp.model.Location;

/**
 * A class representing an LDAP directory location.
 * 
 * @author <a href="http://www.scottphillips.com">Scott Phillips</a>
 */
public class LdapLocationImpl implements Location {

	/** Private variables to store the label and value **/
	private final String label;
	private final String value;
	
	/**
	 * Construct a new LDAP Location
	 * @param label The displayable label of this location
	 * @param value The technical value of this location.
	 */
	public LdapLocationImpl(String label, String value) {
		this.label = label;
		this.value = value;
	}
	
	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public int compareTo(Location other) {
		
		// Handle the cumbersome null case
		if (this.label == null && other.getLabel() == null)
			return 0;
		if (this.label == null)
			return 1;
		if (other.getLabel() == null)
			return -1;
		
		// Now just campare strings
		return this.label.compareTo(other.getLabel());
	}
	
	@Override
	public String toString() {
		return this.label;
	}
	
}
