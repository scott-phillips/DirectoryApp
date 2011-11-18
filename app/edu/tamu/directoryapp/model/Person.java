package edu.tamu.directoryapp.model;

/**
 * The person object represents an immutable person from a directory source.
 * 
 * @author <a href="http://www.scottphillips.com">Scott Phillips</a>
 */
public interface Person extends Comparable<Person> {

	/**
	 * @return The unique netid for the person.
	 */
	public String getNetID();
	
	/**
	 * @return The first name of the person
	 */
	public String getFirstName();
	
	/**
	 * @return The last name of the person
	 */
	public String getLastName();
	
	/**
	 * @return The full display name of the person, this may be slightly different that just first and last names.
	 */
	public String getDisplayName();
	
	/**
	 * @return The nickname of the person
	 */
	public String getNickname();
	
	/**
	 * @return The preferred first display name of the person: nickname if given, firstName if otherwise.
	 */
	public String getPreferredFirstDisplayName();
	
	/**
	 * @return The formatted name of the person for display in the directory
	 */
	public String getFormattedName();
	
	/**
	 * @return The title of the person, everyone has a title.
	 */
	public String getTitle();
	
	/**
	 * @return The position of the person, this will be something like "Staff" or "Assistant Professor"
	 */
	public String getPosition();
	
	/**
	 * @return The most immediate department that the person belongs too.
	 */
	public String getDepartment();
	
	/**
	 * @return The prefered email address of the person
	 */
	public String getEmail();
	
	/**
	 * @return The office phone number of the person
	 */
	public String getPhone();
	
	/**
	 * @return The building where the person's office is located. The format can vary a bit from "Evans", "Evans Library", "Annex", etc...
	 */
	public String getBuilding();
	
	/**
	 * @return The office number of the person's office, the format varies widely.
	 */
	public String getOfficeNumber();

	
	
}
