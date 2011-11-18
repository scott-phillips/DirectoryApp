package edu.tamu.directoryapp.model;

import java.util.List;

import javax.naming.Name;

import org.springframework.ldap.core.DistinguishedName;

/**
 * An object representing a directory department.
 * 
 * @author <a href="http://www.scottphillips.com">Scott Phillips</a>
 */
public interface Department extends Comparable<Department> {
	
	/**
	 * @return The department's name.
	 */
	public String getName();
	
	/**
	 * @return The department's displayable label including the name and group of the department.
	 */
	public String getLabel();
	
	
	/**
	 * @return The department's distinguished name as a verified name object.
	 */
	public DistinguishedName getDN();
	
	/**
	 * @return The department's distinguished name as an unverified string.
	 */
	public String getValue();
	
	/**
	 * @return The name of this department's top-level ancestor department name
	 */
	public String getGroupAbbreviation();
}
