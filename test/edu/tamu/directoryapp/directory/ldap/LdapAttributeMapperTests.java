package edu.tamu.directoryapp.directory.ldap;

import java.io.IOException;

import javax.naming.NamingException;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.tamu.directoryapp.directory.ldap.LdapDirectoryImpl;
import edu.tamu.directoryapp.directory.ldap.LdapPersonImpl;
import edu.tamu.directoryapp.model.Person;

import play.modules.spring.Spring;
import play.test.UnitTest;

/**
 * Test cases for the attribute mapper to check that the correct ldap fields are mapped to the person object.
 * 
 * @author <a href="http://www.scottphillips.com">Scott Phillips</a>
 */

public class LdapAttributeMapperTests extends UnitTest {

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
	 * Just the basic simple case.
	 */
	@Test
	public void testSimpleCase() {
		
		Person person = directory.getByNetID("aggiejack");
		
		assertNotNull(person);
		
		assertEquals("Evans",person.getBuilding());
		assertEquals("Applications Development",person.getDepartment());
		assertEquals("Jack Daniels",person.getDisplayName());
		assertEquals(null, person.getNickname());
		assertEquals("Daniels, Jack", person.getFormattedName());
		assertEquals("jack@uni.edu",person.getEmail()); 
		assertEquals("Jack",person.getFirstName());
		assertEquals("Daniels",person.getLastName());
		assertEquals("3.722",person.getOfficeNumber());
		assertEquals("(979) 458-7234",person.getPhone());
		assertEquals("Staff",person.getPosition());
		assertEquals("Program Coordinator",person.getTitle());
		assertEquals("aggiejack",person.getNetID());
	}
	
	/**
	 * The case where someone has a nickname.
	 */
	@Test
	public void testNickName() {
		
		Person person = directory.getByNetID("kryptyk");
		
		assertNotNull(person);
		
		assertEquals("Annex",person.getBuilding());
		assertEquals("Applications Services",person.getDepartment());
		assertEquals("John Picard",person.getDisplayName());
		assertEquals("kryptyk@library.uni.edu",person.getEmail());
		assertEquals("Johnny",person.getNickname());
		assertEquals("John",person.getFirstName());
		assertEquals("Picard, Johnny", person.getFormattedName());
		assertEquals("Picard",person.getLastName());
		assertEquals("Annex 945",person.getOfficeNumber());
		assertEquals("(979) 458-1234",person.getPhone());
		assertEquals("Staff",person.getPosition());
		assertEquals("Software Developer",person.getTitle());
		assertEquals("kryptyk",person.getNetID());
	}
	
	/**
	 * The case where someone is a faculty member.
	 */
	@Test
	public void testFaculty() {
		
		Person person = directory.getByNetID("gossling");
		
		assertNotNull(person);
		
		assertEquals("Annex",person.getBuilding());
		assertEquals("Web Services",person.getDepartment());
		assertEquals("James Gossling",person.getDisplayName());
		assertEquals("gossling@library.uni.edu",person.getEmail());
		assertEquals("James",person.getFirstName());
		assertEquals("Gossling",person.getLastName());
		assertEquals("8.296",person.getOfficeNumber());
		assertEquals("(929) 845-0001",person.getPhone());
		assertEquals("Assistant Professor",person.getPosition());
		assertEquals("Digital Experience Designer",person.getTitle());
		assertEquals("gossling",person.getNetID());
		
		
	}
	
	
}
