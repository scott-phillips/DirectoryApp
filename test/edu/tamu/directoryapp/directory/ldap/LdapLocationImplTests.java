package edu.tamu.directoryapp.directory.ldap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.emory.mathcs.backport.java.util.Collections;
import edu.tamu.directoryapp.directory.ldap.LdapDirectoryImpl;
import edu.tamu.directoryapp.directory.ldap.LdapPersonImpl;
import edu.tamu.directoryapp.model.Department;
import edu.tamu.directoryapp.model.Location;
import edu.tamu.directoryapp.model.Person;

import play.modules.spring.Spring;
import play.test.UnitTest;

/**
 * Test cases the LDAP based implementation of the Department interface.
 * 
 * @author <a href="http://www.scottphillips.com">Scott Phillips</a>
 */

public class LdapLocationImplTests extends UnitTest {
	
	
	/** 
	 * Test that sorting works correctly using the compareTo method
	 */
	@Test
	public void testCompareTo() {
		
		
		Location n = new LdapLocationImpl(null, null);
		Location a = new LdapLocationImpl("a", null);
		Location b = new LdapLocationImpl("b", null);

		List<Location> list = new ArrayList<Location>();
		list.add(b);
		list.add(n);
		list.add(a);

		Collections.sort(list);
		
		// Check the sort
		assertEquals(a,list.get(0));
		assertEquals(b,list.get(1));
		assertEquals(n,list.get(2));
	}
	
	
}
