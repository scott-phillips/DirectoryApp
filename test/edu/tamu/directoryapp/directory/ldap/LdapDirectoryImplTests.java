package edu.tamu.directoryapp.directory.ldap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.apache.directory.server.configuration.MutableServerStartupConfiguration;
import org.apache.directory.server.core.partition.impl.btree.MutableBTreePartitionConfiguration;
import org.apache.directory.server.core.schema.bootstrap.ApacheSchema;
import org.apache.directory.server.core.schema.bootstrap.ApachednsSchema;
import org.apache.directory.server.core.schema.bootstrap.AutofsSchema;
import org.apache.directory.server.core.schema.bootstrap.CollectiveSchema;
import org.apache.directory.server.core.schema.bootstrap.CorbaSchema;
import org.apache.directory.server.core.schema.bootstrap.CoreSchema;
import org.apache.directory.server.core.schema.bootstrap.CosineSchema;
import org.apache.directory.server.core.schema.bootstrap.DhcpSchema;
import org.apache.directory.server.core.schema.bootstrap.JavaSchema;
import org.apache.directory.server.core.schema.bootstrap.Krb5kdcSchema;
import org.apache.directory.server.core.schema.bootstrap.MozillaSchema;
import org.apache.directory.server.core.schema.bootstrap.NisSchema;
import org.apache.directory.server.core.schema.bootstrap.SambaSchema;
import org.apache.directory.server.core.schema.bootstrap.SystemSchema;
import org.apache.directory.server.core.schema.bootstrap.TestSchema;
import org.apache.directory.server.protocol.shared.store.LdifFileLoader;
import org.junit.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.ldap.core.DistinguishedName;

import edu.tamu.directoryapp.directory.ldap.LdapDirectoryImpl;
import edu.tamu.directoryapp.model.Department;
import edu.tamu.directoryapp.model.Location;
import edu.tamu.directoryapp.model.Person;


import play.Logger;
import play.Play;
import play.modules.spring.Spring;
import play.test.UnitTest;

/**
 * Test cases for the LDAP directory. This class tests which records are returned by the Directory object, it does not test the specific attributes returned.
 * 
 * @author <a href="http://www.scottphillips.com">Scott Phillips</a>
 * @author Alexey Maslov
 */

public class LdapDirectoryImplTests extends UnitTest {
	
	/** Client interface **/
	private static LdapDirectoryImpl directory;
	
	/**
	 * Create an instance of the Directory object for testing.
	 */
	@BeforeClass
	public static void setupLdap() throws NamingException, IOException {
		// Setup our directory client
		directory = Spring.getBeanOfType(LdapDirectoryImpl.class);
	}
	
	
	
	
	/**
	 * The positive test case.
	 */
	@Test
	public void authenticate() {
		
		assertTrue(directory.authenticate("aggiejack", "test"));
		assertTrue(directory.authenticate("Aggiejack", "test"));
		assertTrue(directory.authenticate("aggieJack", "test"));
		assertTrue(directory.authenticate("AGGIEJACK", "test"));
	}
	
	/**
	 * Inactive accounts should not authenticate either.
	 */
	@Test
	public void authenticateInactiveAccount() {
		
		// Janefonda no longer works here
		assertFalse(directory.authenticate("janefonda", "test"));
	}
	
	/**
	 * Students should not be allowed to authenticate.
	 */
	@Test
	public void authenticateStudent() {
		
		// Students should not be able to login
		assertFalse(directory.authenticate("rosa_parks", "test"));
		
		// Second test to catch graduate students
		assertFalse(directory.authenticate("ice", "test"));
	}
	
	/**
	 * Test bad passwords: capitalization, following, null, blank, etc..
	 */
	@Test
	public void authenticateBadPassword() {
		assertFalse(directory.authenticate("aggiejack", "Test"));
		assertFalse(directory.authenticate("aggiejack", "test1234"));
		assertFalse(directory.authenticate("aggiejack", null));
		assertFalse(directory.authenticate("aggiejack", ""));
		assertFalse(directory.authenticate("aggiejack", "test*"));
		assertFalse(directory.authenticate("aggiejack", "true"));
	}
	
	
	/**
	 * Test bad forms of the netid: null, blank, star, and something following a valid netid.
	 */
	@Test
	public void authenticateBadNetID() {
		assertFalse(directory.authenticate(null, "test"));
		assertFalse(directory.authenticate("", "test"));
		assertFalse(directory.authenticate("*", "test"));
		assertFalse(directory.authenticate("aggiejack123", "test"));
	}
	
	
	
	/**
	 * Test retrieving an person based upon their netid.
	 */
    @Test
    public void getByNetID() {
    	Person person = directory.getByNetID("aggiejack");
    	assertNotNull(person);
    	assertEquals("aggiejack",person.getNetID());    	
    }
    

    /** 
     * If you search for a null netid, make sure no exceptions are thrown.
     */
    @Test
    public void getByNullNetID() {
    	Person person = directory.getByNetID(null);
    	assertNull(person);
    }
    
    /**
     * If you search by a blank NetID, nothing should be returned either.
     */
    @Test
    public void getByEmptyNetID() {
    	Person person = directory.getByNetID("");
    	assertNull(person);
    }
    
    /**
     * If you search by a blank
     */
    @Test
    public void getByNonExistantNetID() {
    	Person person = directory.getByNetID("thisuiddoesnotexist");
    	assertNull(person);
    }
    
    
    
    /**
     * Test the positive search by alaphabet case
     */
    @Test
    public void testSearchByAlphabet() {
    	
    	List<Person> persons = directory.searchByAlphabet('P');
    		
    	assertNotNull(persons);
    	assertEquals(4,persons.size());
    	assertPersons(persons, "bpitt","kryptyk","natalie","putput");
    }
    
    /**
     * Test searching by a non alphabet character
     */
    @Test
    public void testSearchByNonEnglishAlphabet() {
    	
    	List<Person> persons = directory.searchByAlphabet('.');
    	
    	assertNotNull(persons);
    	assertEquals(0,persons.size());
    }
    
    
    /**
     * Test searching, by four cases: Just Query, Just Location, Just Department, and all three fields.
     */
    @Test
    public void testSearch() {
    	
    	List<Person> persons;
    	// Three cases: query, location, department
    	
    	// test just query with a name
    	persons = directory.search("John", null, null);
    	
    	assertNotNull(persons);
    	assertEquals(2,persons.size());
    	assertPersons(persons, "kryptyk", "john.stewart");
    	
    	// test just query with an email address
    	persons = directory.search("john.stewart@library.uni.edu", null, null);
    	
    	assertNotNull(persons);
    	assertEquals(1,persons.size());
    	assertPersons(persons, "john.stewart");
    	

    	// test just location
    	persons = directory.search(null, "Evans", null);
    	
    	assertNotNull(persons);
    	assertEquals(4,persons.size());
    	assertPersons(persons,"aggiejack","ak_glass","scarell","putput");
    	
    	// test just department
    	persons = directory.search(null, null, "ou=Applications Development,ou=Applications,ou=Digital Initiatives");
    	
    	assertNotNull(persons);
    	assertEquals(3,persons.size());
    	assertPersons(persons,"aggiejack","alee","ak_glass");
    	
    	// test them all
    	persons = directory.search("s", "Evans", "ou=Applications Development,ou=Applications,ou=Digital Initiatives");
    	
    	assertNotNull(persons);
    	assertEquals(2,persons.size());
    	assertPersons(persons,"aggiejack","ak_glass");
    }
    
    /**
     * Test Searching when all parameters are null;
     */
    @Test
    public void testSearchNull() {	
    	List<Person> persons = directory.search(null, null, null);
    	
    	assertEquals(23,persons.size());    	
    }
    
    /**
     * Test searching for blank strings 
     */
    @Test
    public void testSearchEmpty() {
    	List<Person> persons = directory.search("", "", "");
    	
    	assertEquals(23,persons.size());    	
    }
    
    /**
     * Test searching for on all four cases for things that do not exist.
     */
    @Test
    public void testSearchBadData() {
    	
    	// test just query
    	List<Person> persons = directory.search("thisdoesnotexist", null, null);
    	
    	assertNotNull(persons);
    	assertEquals(0,persons.size());

    	// test just location
    	persons = directory.search(null, "thisdoesnotexist", null);
    	
    	assertNotNull(persons);
    	assertEquals(0,persons.size());
    	
    	// test just department
    	persons = directory.search(null, null, "thisdoesnotexist");
    	
    	assertNotNull(persons);
    	assertEquals(0,persons.size());
    	
    	// test them all
    	persons = directory.search("thisdoesnotexist", "thisdoesnotexist", "thisdoesnotexist");
    	
    	assertNotNull(persons);
    	assertEquals(0,persons.size());
    }
    
    /**
     * Test that colbert will never return.
     */
    @Test
    public void testNonActiveAccounts() {

    	// Get by NetID
    	Person colbert = directory.getByNetID("colbert");
    	assertNull(colbert);
    	
    	// Search by alaphbet
    	List<Person>persons = directory.searchByAlphabet('C');

    	assertNotNull(persons);
    	assertNotPersons(persons,"colbert");

    	// Search by name
    	persons = directory.search("Colbert", null, null);

    	assertNotNull(persons);
    	assertEquals(0,persons.size());

    	// Search by location
    	persons = directory.search(null, "Evans", null);

    	assertNotNull(persons);
    	assertNotPersons(persons,"colbert");
    	
    	// Search by department
    	persons = directory.search(null, null, "Digital Initiatives");

    	assertNotNull(persons);
    	assertNotPersons(persons,"colbert");
    }
    
    /**
     * Test that student workers are not real.
     */
    @Test
    public void testStudentWorkers() {
    	
    	// Get by NetID
    	Person student = directory.getByNetID("gogita99");
    	assertNull(student);
    	
    	// Check graduate students
    	student = directory.getByNetID("ice");
    	assertNull(student);
    	
    	// Search by alaphbet
    	List<Person>persons = directory.searchByAlphabet('f');

    	assertNotNull(persons);
    	assertNotPersons(persons,"gogita99");

    	// Search by name
    	persons = directory.search("Fuller", null, null);

    	assertNotNull(persons);
    	assertEquals(0,persons.size());

    	// Search by location
    	persons = directory.search(null, "Annex", null);

    	assertNotNull(persons);
    	assertNotPersons(persons,"gogita99");
    	
    	// Search by department
    	persons = directory.search(null, null, "ou=Help Desk,ou=Operations,ou=Digital Initiatives");

    	assertNotNull(persons);
    	assertNotPersons(persons,"gogita99");
    	
    	// There are three non-students in the department.
    	assertPersons(persons,"a-smith","ggeorge","karl");
    	
    }

    
    /**
     * Test the "sponsered accounts" are never returned.
     */
    @Test
    public void testSponseredAccounts() {

    	// Get by NetID
    	Person sponsoredaccount = directory.getByNetID("sponsoredaccount");
    	assertNull(sponsoredaccount);
    	
    	// Search by alaphbet
    	List<Person>persons = directory.searchByAlphabet('A');

    	assertNotNull(persons);
    	assertNotPersons(persons,"sponsoredaccount");

    	// Search by name
    	persons = directory.search("Sponsored", null, null);

    	assertNotNull(persons);
    	assertEquals(0,persons.size());

    	// Search by location
    	persons = directory.search(null, "Annex", null);

    	assertNotNull(persons);
    	assertNotPersons(persons,"sponsoredaccount");
    	
    	// Search by department
    	persons = directory.search(null, null, "Help Desk");

    	assertNotNull(persons);
    	assertNotPersons(persons,"sponsoredaccount");
    }
    
    
    /**
     * Test the list of Departments returned.
     */
    @Test
    public void testGetDepartments() {
    	List<Department> departments = directory.getDepartments();
    	
    	// There are 13 departments defined in the test ldiff, but 3 of them have no members.
    	
    	assertNotNull(departments);
    	assertEquals(10,departments.size());
    	
    	// Check the first one is correct.
    	assertEquals("Applications (APP)",departments.get(0).getLabel());
    	assertEquals("ou=Applications,ou=Digital Initiatives",departments.get(0).getValue());
    	
    	// Check the last one is correct.
    	int lastIndex = departments.size()-1;
    	assertEquals("Windows (OP)",departments.get(lastIndex).getLabel());
    	assertEquals("ou=Windows,ou=Operations,ou=Digital Initiatives",departments.get(lastIndex).getValue());
    }
    
    /**
     * Test the list of Locations returned
     */
    @Test
    public void testGetLocations() {
    	List<Location> locations = directory.getLocations();
    	
    	assertNotNull(locations);
    	assertEquals(7,locations.size());
    	
    	// Check the first one is correct.
    	assertEquals("Sterling C. Evans Library",locations.get(0).getLabel());
    	assertEquals("Evans",locations.get(0).getValue());
    	
    	// Check the last one is correct.
    	assertEquals("Qatar Library",locations.get(6).getLabel());
    	assertEquals("Qatar",locations.get(6).getValue());
    	
    }
    
    /**
     * Assert that all the given ids are found within the list of people. This will *not* check if there are additional people in the list.
     * 
     * @param persons The list of people to search
     * @param netIDs The list of ids expected to be found.
     */
    private void assertPersons(List<Person> persons, String ... netIDs) {

    	for (String id : netIDs) {
    		boolean found = false;
    		String list = "";
    		
    		for (Person person : persons) {
    			list += person.getNetID()+",";
    			if ( id.equals(person.getNetID()) )
    			 found = true;
    		}
    		
    		assertTrue("Unable to find the expected person '"+id+"', from the list: "+list,found);
    	}
    }
    
    /**
     * Assert that the given ids are *not* found within the list of people.
     * 
     * @param persons The list of people to search
     * @param netIDs The list of ids expected to *not* find.
     */
    private void assertNotPersons(List<Person> persons, String ... netIDs) {
    	for (String id : netIDs) {
    		boolean found = false;
    		String list = "";
    		
    		for (Person person : persons) {
    			list += person.getNetID()+",";
    			if ( id.equals(person.getNetID()) )
    			 found = true;
    		}
    		
    		assertFalse("Found an unexpected person '"+id+"', within the list: "+list,found);
    	}
    }
    
    
}
