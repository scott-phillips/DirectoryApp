package edu.tamu.directoryapp.directory.ldap;

import javax.mail.internet.InternetAddress;

import edu.tamu.directoryapp.model.Person;


/**
 * Implementation of an LDAP-based person object.
 * 
 * @author <a href="http://www.scottphillips.com">Scott Phillips</a>
 */

public class LdapPersonImpl implements Person{

	/** Internal person attributes **/
	private String netID;
	private String firstName;
	private String lastName;
	private String displayName;
	private String nickname;
	private String title;
	private String position;
	private String department;
	private String email;
	private String phone;
	private String building;
	private String office;
	
	/**
	 * Protected constructor
	 */
	protected LdapPersonImpl(String uid, String firstName, String lastName,
			String displayName, String nickname, String title, String position,
			String department, String email, String phone, String building,
			String office) {
		this.netID = uid;
		this.firstName = firstName;
		this.lastName = lastName;
		this.displayName = displayName;
		this.nickname = nickname;
		this.title = title;
		this.position = position;
		this.department = department;
		this.email = email;
		this.phone = phone;
		this.building = building;
		this.office = office;
	}
	
	@Override
	public String getNetID() {
		return netID;
	}
	
	@Override
	public String getFirstName() {
		return firstName;
	}
	
	@Override
	public String getLastName() {
		return lastName;
	}
	
	@Override
	public String getDisplayName() {
		return displayName;
	}
	
	@Override
	public String getNickname() {
		return nickname;
	}
	
	@Override
	public String getPreferredFirstDisplayName(){
		if(nickname == null)
		{
			return firstName;
		}
		else return nickname;
	}
	
	@Override
	public String getFormattedName() {
		/*
		 *	If a person has a nickname field then: sn, nickname 
			Otherwise show: sn, givenName 
		 */
		String lastNameString;
		if(lastName == null)
		{
			lastNameString = "";
		}
		else
		{
			lastNameString = lastName + ", ";
		}
		
		String firstNameString;
		if(getPreferredFirstDisplayName() == null)
		{
			firstNameString = "";
		}
		else
		{
			firstNameString = getPreferredFirstDisplayName();
		}
		
		return lastNameString + firstNameString;
	}
	
	@Override
	public String getTitle() {
		return title;
	}
	
	@Override
	public String getPosition() {
		return position;
	}
	
	@Override
	public String getDepartment() {
		return department;
	}
	
	@Override
	public String getEmail() {
		return email;
	}
	
	@Override
	public String getPhone() {
		String phoneString = "";
		String extensionString = "";
		
		//if the phone number contains the string " or ", then we assume that it is in fact two phone numbers of the standard 10 digit variety.
		if(phone!= null && phone.contains(" or "))
		{
			phoneString = phone.split("\\sor\\s")[0].replaceAll("\\D", "");
			String phoneString2 = phone.split("\\sor\\s")[1].replaceAll("\\D", "");
			return "(" + phoneString.substring(0, 3) + ") " + phoneString.substring(3,6) + "-" + phoneString.substring(6,10) + " or (" + phoneString2.substring(0, 3) + ") " + phoneString2.substring(3,6) + "-" + phoneString2.substring(6,10);
		}			
		
		
		//if the phone number ends with non-digit characters that include alphabetic and possibly punctuated text (such as "ext. " or "EXT:" or "intercom ") followed directly by more numbers, then we assume the number has an extension.
		if(phone!= null && phone.matches(".+[a-zA-Z]+\\D*\\d+"))
		{
			//make a delimiter where the alphabetic text appears
			String splitPhone = phone.replaceFirst("[a-zA-Z]+\\D*", "SPLITLOCATION");
			phoneString = splitPhone.split("SPLITLOCATION")[0];
			extensionString = " ext: " + splitPhone.split("SPLITLOCATION")[1];
		}
		else if(phone!= null)
		{
			phoneString = phone;			
		}

		//start by stripping non-digit characters from the phone string
		phoneString = phoneString.replaceAll("\\D", "");
		/*
		 * 3+7+3=13 means local with unprefixed extension, 3+8=11 means Qatar, 3+7=10 means local w/ area code, 7 means local w/o area code, 5 means internal.
		 */
		switch(phoneString.length())
		{
		case 13:
		extensionString = " ext: " + phoneString.substring(10,13);
		phoneString = "(" + phoneString.substring(0, 3) + ") " + phoneString.substring(3,6) + "-" + phoneString.substring(6,10);		
		break;
		
		case 11:
		phoneString = "+" + phoneString.substring(0, 3) + " " + phoneString.substring(3, 7) + "-" + phoneString.substring(7,11) + " (Qatar)";
		break;
		
		case 10:
		phoneString = "(" + phoneString.substring(0, 3) + ") " + phoneString.substring(3,6) + "-" + phoneString.substring(6,10);
		break;
		
		case 7:
		phoneString = "(979) " + phoneString.substring(0, 3) + "-" + phoneString.substring(3, 7);
		break;
		
		case 5:
		phoneString = "(979) 45" + phoneString.substring(0, 1) + "-" + phoneString.substring(1, 5);
		break;
		
		default: 
	
		}
		
		return phoneString + extensionString;
	}
	
	@Override
	public String getBuilding() {
		return building;
	}
	
	@Override
	public String getOfficeNumber() {
		return office;
	}

	@Override
	public int compareTo(Person other) {
		// Take care of null last names.
		if (this.lastName == null && other.getLastName() == null)
			return 0;
		if (this.lastName == null)
			return 1;
		if (other.getLastName() == null)
			return -1;
		
		if (this.lastName.equals(other.getLastName())) {
			// We have the same last name. Fall back to first name comparison.
			if (this.getPreferredFirstDisplayName() == null && other.getPreferredFirstDisplayName() == null)
				return 0;
			if (this.getPreferredFirstDisplayName() == null)
				return 1;
			if (other.getPreferredFirstDisplayName() == null)
				return -1;
			
			return this.getPreferredFirstDisplayName().compareTo(other.getPreferredFirstDisplayName());
		} else {
			// Sort based upon their last name.
			return this.lastName.compareTo(other.getLastName());
		}
	}
	
	@Override
	public String toString() {
		return this.netID + ": " + getFormattedName();
	}
	
}
