package edu.tamu.directoryapp.directory.ldap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.naming.NamingException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.ldap.core.DistinguishedName;

import edu.tamu.directoryapp.directory.ldap.LdapDirectoryImpl;
import edu.tamu.directoryapp.directory.ldap.LdapPersonImpl;
import edu.tamu.directoryapp.model.Department;
import edu.tamu.directoryapp.model.Person;

import play.Play;
import play.modules.spring.Spring;
import play.test.UnitTest;

/**
 * Test cases the LDAP based implementation of the Department interface.
 * 
 * @author <a href="http://www.scottphillips.com">Scott Phillips</a>
 */

public class LdapDepartmentImplTests extends UnitTest {
	
	
	
	/** 
	 * Test that sorting works correctly using the compareTo method
	 */
	@Test
	public void testCompareTo() {
		
		Department empty = new LdapDepartmentImpl(null, DistinguishedName.EMPTY_PATH);
		Department a = new LdapDepartmentImpl("Applications", DistinguishedName.immutableDistinguishedName("ou=Applications,ou=Digital Initiatives"));
		Department ap = new LdapDepartmentImpl("Applications Development", DistinguishedName.immutableDistinguishedName("ou=Applications Development,ou=Applications,ou=Digital Initiatives"));		
		Department d = new LdapDepartmentImpl("Digital Initiatives", DistinguishedName.immutableDistinguishedName("ou=Digital Initiatives"));
		Department h = new LdapDepartmentImpl("Help Desk", DistinguishedName.immutableDistinguishedName("ou=Help Desk,ou=Digital Initiatives"));
		Department l = new LdapDepartmentImpl("Low Level Unit", DistinguishedName.immutableDistinguishedName("ou=lowlevel,ou=Digital Initiatives"));
		Department on = new LdapDepartmentImpl("Open Systems", DistinguishedName.immutableDistinguishedName("ou=Open Systems,ou=Digital Initiatives"));
		Department or = new LdapDepartmentImpl("Operations", DistinguishedName.immutableDistinguishedName("ou=Operations,ou=Digital Initiatives"));
		Department w = new LdapDepartmentImpl("Web Services", DistinguishedName.immutableDistinguishedName("ou=Web Services,ou=Digital Initiatives"));

		List<Department> list = new ArrayList<Department>();
		list.add(l);
		list.add(or);
		list.add(a);
		list.add(d);
		list.add(empty);
		list.add(on);
		list.add(h);
		list.add(w);
		list.add(ap);

		
		Collections.sort(list);
		
		
		// Check the sort
		assertEquals(a,list.get(0));
		assertEquals(ap,list.get(1));
		assertEquals(d,list.get(2));
		assertEquals(h,list.get(3));
		assertEquals(l,list.get(4));
		assertEquals(on,list.get(5));
		assertEquals(or,list.get(6));
		assertEquals(w,list.get(7));
		assertEquals(empty,list.get(8));

	}
	
	
	/**
	 * Test the label and group methods
	 */
	@Test
	public void testGetLabelandGroup() {
		
		Department dept = new LdapDepartmentImpl("Applications Development", DistinguishedName.immutableDistinguishedName("ou=Applications Development,ou=Applications,ou=Digital Initiatives"));		
		
		assertEquals("APP",dept.getGroupAbbreviation());
		assertEquals("Applications Development (APP)",dept.getLabel());
		
		dept = new LdapDepartmentImpl("Low Level Unit", DistinguishedName.immutableDistinguishedName("ou=lowlevel,ou=Digital Initiatives"));
		
		assertNull(dept.getGroupAbbreviation());
		assertEquals("Low Level Unit",dept.getLabel());

	}
}
