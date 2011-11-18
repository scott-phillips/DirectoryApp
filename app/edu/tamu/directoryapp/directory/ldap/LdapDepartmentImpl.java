	package edu.tamu.directoryapp.directory.ldap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.Name;

import org.springframework.ldap.core.DistinguishedName;

import play.Logger;
import play.Play;

import edu.tamu.directoryapp.model.Department;
import edu.tamu.directoryapp.model.Person;

/**
 * A class representing an LDAP directory department.
 * 
 * @author <a href="http://www.scottphillips.com">Scott Phillips</a>
 * @author James Creel
 */
public class LdapDepartmentImpl implements Department{

	/** Private variables to store the name and value **/
	private final String group;
	private final String name;
	private final DistinguishedName dn;

	/**
	 * Construct a new Department.
	 * 
	 * @param label The displayable label for the department.
	 * @param dn The distinguished name for the department.
	 */
	public LdapDepartmentImpl(String name, DistinguishedName dn) {
		this.name = name;
		this.dn = dn.immutableDistinguishedName();
		this.group = this.getGroupAbbreviation();
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getLabel() {
		String group = this.getGroupAbbreviation();
		if (group != null)
			return this.name + " ("+group+")";
		else
			return this.name;
	}
	
	@Override
	public DistinguishedName getDN() {
		return dn;
	}
	
	@Override
	public String getValue() {
		return dn.toString();
	}

	@Override
	public int compareTo(Department other) {
		
		// Handle the cumbersome null case
		if (this.name == null && other.getName() == null)
			return 0;
		if (this.name == null)
			return 1;
		if (other.getName() == null)
			return -1;
		
		return this.name.compareTo(other.getName());
	}
	
	@Override
	public String toString() {
		return this.getLabel();
	}	
	
	@Override
	public String getGroupAbbreviation() {
		
		// Check if we have the value cached
		if (this.group != null)
			return this.group;
		
		// Determine the group
		Map<String,String> departmentGroups = getDepartmentGroups();
		for(String ou : this.getOrganizationalUnits())
		{
			if(departmentGroups.containsKey(ou))
				return departmentGroups.get(ou);
		}
		return null;
	}
	
	
	
	/**
	 * @return A list of organizational units starting with the most specific
	 * and becoming more general.
	 */
	private List<String> getOrganizationalUnits()
	{
		List<String> names = new ArrayList<String>();
		for(String n : dn.toString().split("ou="))
		{
			n=n.replace(",", "");
			if(!n.isEmpty())
				names.add(n);
		}
		return names;
	}
	
	/**
	 * @return The map of department group names and their abbreviation from
	 * the main configuration file.
	 */
	private static Map<String,String> getDepartmentGroups() {
		
		Map<String,String> departmentGroups = new HashMap<String,String>();
		Set<Object> keys = Play.configuration.keySet();
		for(Object keyObj : keys) {
			if (keyObj instanceof String) {
				String key = (String) keyObj;
				if (key.startsWith("ldap.department.groups.")) {
					
					String abbreviation = key.substring("ldap.department.groups.".length());
					String name = Play.configuration.getProperty(key);
					
					departmentGroups.put(name, abbreviation);
				}
			}
		}
		return departmentGroups;
	}
}
