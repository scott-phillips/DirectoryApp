package edu.tamu.directoryapp.directory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.LikeFilter;
import org.springframework.ldap.filter.NotFilter;

import edu.tamu.directoryapp.model.Department;
import edu.tamu.directoryapp.model.Location;
import edu.tamu.directoryapp.model.Person;

/**
 * An interface for accessing Person information from a directory of people. 
 * 
 * @author <a href="http://www.scottphillips.com">Scott Phillips</a>
 */

public interface Directory {

	/**
	 * Authenticate a person object
	 * 
	 * @param netid The netid, or username, of the person attempting to authenticate. The field is case *insensitive*
	 * @param password The user's password.
	 * @return True of the credentials are valid, false otherwise.
	 */
	public boolean authenticate(String netid, String password);
	
	
	/**
	 * Search the directory based upon a query, location, and department.
	 * 
	 * @param query The query against the LDAP displayName and mail fields
	 * @param location The location, such as "Evans", "Annex", or "MSL"
	 * @param department The department's full name.
	 * @return A list of people that match the query
	 */
	public List<Person> search(String query, String location,
			String department);

	/**
	 * Search by alphabet. In all cases this method will return a list of person objects, however the list may be empty.
	 * 
	 * @param letter An uppercase letter between A and Z.
	 * @return A list of people who's surname starts with the provided letter.
	 */
	public List<Person> searchByAlphabet(char letter);

	/**
	 * Return a directory person with the exact netid.
	 * 
	 * @param netid The netid of the person, case insensitive
	 * @return the person object if found, otherwise null
	 */
	public Person getByNetID(String netid);
	
	
	/**
	 * Return a list of all the locations contained within the LDAP directory.
	 * 
	 * @return List of all locations.
	 */
	public List<Location> getLocations();
	
	
	/**
	 * Return a list of all the departments contained within the LDAP directory. 
	 * 
	 * @return List of all departments.
	 */
	public List<Department> getDepartments();
	
}
