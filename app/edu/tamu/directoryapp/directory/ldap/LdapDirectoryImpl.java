package edu.tamu.directoryapp.directory.ldap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.naming.Name;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.LikeFilter;
import org.springframework.ldap.filter.NotFilter;
import org.springframework.ldap.filter.OrFilter;
import org.springframework.ldap.filter.PresentFilter;

import edu.tamu.directoryapp.directory.Directory;
import edu.tamu.directoryapp.model.Department;
import edu.tamu.directoryapp.model.Location;
import edu.tamu.directoryapp.model.Person;

import play.Logger;
import play.Play;
import play.jobs.Every;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.modules.spring.Spring;

/**
 * This is an data source type class, meaning it provides access to the ldap
 * based data. There are several methods provided, authenticating a
 * username/password pair along with three forms of ldap search queries.
 * 
 * 
 * @author <a href="http://www.scottphillips.com">Scott Phillips</a>
 * @author James Creel, http://www.jamescreel.net/
 */

public class LdapDirectoryImpl implements Directory {

	/** Private cache of departments, it is expired every hour **/
	private static List<Department> departmentCache = null;
	
	/** Internal LDAP template, this does all the real work **/
	private LdapTemplate ldapTemplate;

	/**
	 * Set the ldap tepmlate
	 * 
	 * @param ldapTemplate
	 *            LDAP template
	 */
	public void setLdapTemplate(LdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}

	/** Map ldap attributes to person attributes **/
	private AttributesMapper personMapper;
	
	/**
	 * Set the attribute mapper to determine how ldap attributes are mapper to
	 * the person model.
	 * 
	 * @param attributeMapper
	 *            LDAP attribute mapper
	 */
	public void setPersonMapper(AttributesMapper personMapper) {
		this.personMapper = personMapper;
	}
	
	/** Map ldap attributes to department attributes **/
	private ContextMapper departmentMapper;
	
	/**
	 * Set the attribute mapper to determine how ldap attributes are mapper to
	 * the department model.
	 * 
	 * @param attributeMapper
	 *            LDAP attribute mapper
	 */
	public void setDepartmentMapper(ContextMapper departmentMapper) {
		this.departmentMapper = departmentMapper;
	}
	

	@Override
	public boolean authenticate(String netid, String password) {

		AndFilter filter = getBaseFilter();
		filter.and(new EqualsFilter("sAMAccountName", netid));

		return ldapTemplate.authenticate("", filter.encode(), password);
	}

	@Override
	public List<Person> search(String query, String location,
			String department) {

		DistinguishedName baseDN = DistinguishedName.EMPTY_PATH;
		AndFilter filter = getBaseFilter();

		if (query != null && query.length() > 0) {
			
			OrFilter nameOrEmail = new OrFilter();
			//nameOrEmail.or(new LikeFilter("eduPersonNickname", "*" + name + "*"));
			nameOrEmail.or(new LikeFilter("mail", "*" + query + "*"));
			nameOrEmail.or(new LikeFilter("displayName", "*" + query + "*"));
			
			filter.and(nameOrEmail);
		}

		if (location != null && location.length() > 0) {
			filter.and(new LikeFilter("postalAddress", location + "*"));
		}
		
		if (department != null && department.length() > 0) {
			
			// Verify that the department is valid.
			List<Department> departments = this.getDepartments();
			for(Department dept : departments) {
				if (department.equalsIgnoreCase(dept.getValue())) {
					// if the name is valid, then add it as the base filter.
					baseDN = dept.getDN();
					break;
				}
			}
			
			//if the department is not valid, no persons can be found in that department.
			if(baseDN.equals(DistinguishedName.EMPTY_PATH))
			{
				return new ArrayList<Person>();
			}
		}

		List<Person> persons = ldapTemplate.search(baseDN, filter.encode(),
				personMapper);
	
		Collections.sort(persons);

		return persons;
	}

	@Override
	public List<Person> searchByAlphabet(char letter) {

		if (letter < 'A' || letter > 'Z')
			return new ArrayList<Person>();
		
		AndFilter filter = getBaseFilter();
		filter.and(new LikeFilter("sn", letter + "*"));

		List<Person> persons = ldapTemplate.search("", filter.encode(),
				personMapper);
		
		Collections.sort(persons);
		
		return persons;
	}

	@Override
	public Person getByNetID(String netid) {

		AndFilter filter = getBaseFilter();
		filter.and(new EqualsFilter("sAMAccountName", netid));

		List<Person> persons = ldapTemplate.search("", filter.encode(),
				personMapper);
		
		if (persons != null && persons.size() > 0)
			return persons.get(0);
		else
			return null;

	}
	
	@Override
	public synchronized List<Location> getLocations() {
		
		// For now this is staticly defined.
		List<Location> locations = new ArrayList<Location>();
		locations.add(new LdapLocationImpl("Sterling C. Evans Library","Evans"));
		locations.add(new LdapLocationImpl("Library Annex","Annex"));
		locations.add(new LdapLocationImpl("Cushing Library","Cushing"));
		locations.add(new LdapLocationImpl("Medical Sciences Library","MSL"));
		locations.add(new LdapLocationImpl("Policy Sciences & Economics Library","PSEL"));
		locations.add(new LdapLocationImpl("West Campus Library","WCL"));
		locations.add(new LdapLocationImpl("Qatar Library","Qatar"));
		
		// Do not sort these alphabetic... we wan't evans to be at the top.
		//Collections.sort(locations);
		
		return locations;
	}
	
	@Override
	public synchronized List<Department> getDepartments() {
		
		// Check if this is cached first.
		if (departmentCache != null)
			return departmentCache;
		
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectclass", "organizationalUnit"));
		filter.and(new PresentFilter("name"));
		filter.and(new PresentFilter("ou"));
		
		// Obtain a list of departments
		List<Department> departments = ldapTemplate.search(DistinguishedName.EMPTY_PATH, filter.encode(),departmentMapper);
		
		// Filter out any empty groups
		Iterator<Department> itr = departments.iterator();
		while (itr.hasNext()) {
			Department department = itr.next();

			if (department.getValue().length() == 0) {
				itr.remove();
				continue;
			}

			AndFilter memberFilter = getBaseFilter();
			List<Person> persons = ldapTemplate.search(department.getDN(), memberFilter.encode(),
					personMapper);
			
			if (persons == null || persons.size() ==0)
				itr.remove();
		}
		
		// Sort the remaining departments;
		Collections.sort(departments);
		
		departmentCache = departments;
		return departmentCache;
	}
	
	
	/**
	 * @return the base filter that trims out students, sponsored accounts,
	 * inactive accounts, and non-people.
	 */
	private static AndFilter getBaseFilter() {
		
		AndFilter filter = new AndFilter();
		filter.and(new EqualsFilter("objectclass", "person"));
		filter.and(new PresentFilter("eduPersonAffiliation"));
		filter.and(new NotFilter(new LikeFilter("description", "*Student*")));
		filter.and(new NotFilter(new LikeFilter("title", "*Student*")));
		filter.and(new EqualsFilter("userAccountControl", "512"));
		
		return filter;
	}
	
	/**
	 * Expire the cache of departments (maybe locations in the future). Immediately after expiring generate a new list 
	 * of departments. It is possible that a web request could come between the expiration and generation, but that
	 * is okay because the generation method handles that case, the first one wins.
	 */
	private void expireCache() {
		departmentCache = null;
		List<Department> departments = getDepartments();
		Logger.info("Directory: Refreshed "+departments.size()+" departments in the cache.");
	}
	
	/**
	 * Periodically expire the cache of departments.
	 */
	@OnApplicationStart
	@Every("1h")
	public class LdapDepartmentCacheManager extends Job {

		// Periodically expire the department cache
		public void doJob() {
			LdapDirectoryImpl directory = Spring.getBeanOfType(LdapDirectoryImpl.class);
			directory.expireCache();
		}
		
	}
	
}
