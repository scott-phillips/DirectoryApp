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
import edu.tamu.directoryapp.model.Person;

import play.modules.spring.Spring;
import play.test.UnitTest;

/**
 * Test cases the LDAP based implementation of the person interface.
 * 
 * @author <a href="http://www.scottphillips.com">Scott Phillips</a>
 */

public class LdapPersonTests extends UnitTest {
	
	
	/** 
	 * Test that sorting works correctly using the compareTo method
	 */
	@Test
	public void testCompareTo() {
		
		// construct a list and test the sorting.
		Person aa = new LdapPersonImpl(null,"a","a", null, null,null,null,null,null,null,null,null);
		Person ba = new LdapPersonImpl(null,"a","b", null, null,null,null,null,null,null,null,null);
		Person an = new LdapPersonImpl(null,null,"a", null, null,null,null,null,null,null,null,null);
		Person ab = new LdapPersonImpl(null,"b","a", null, null,null,null,null,null,null,null,null);
		Person nn = new LdapPersonImpl(null,null,null, null, null,null,null,null,null,null,null,null);
		Person acnick = new LdapPersonImpl(null,"a","a", null, "c",null,null,null,null,null,null,null);
		Person bbnick = new LdapPersonImpl(null,"a","b", null, "b",null,null,null,null,null,null,null);
		Person cbnick = new LdapPersonImpl(null,"b","c", null, "b",null,null,null,null,null,null,null);
		
		List<Person> list = new ArrayList<Person>();
		list.add(nn);
		list.add(nn);
		list.add(an);
		list.add(aa);
		list.add(ba);
		list.add(an);
		list.add(ab);
		list.add(acnick);
		list.add(bbnick);
		list.add(cbnick);
		
		Collections.sort(list);
		
		// Check the sort
		assertEquals(aa,list.get(0));
		assertEquals(ab,list.get(1));
		assertEquals(acnick, list.get(2));
		assertEquals(an,list.get(3));
		assertEquals(an,list.get(4));
		assertEquals(ba,list.get(5));
		assertEquals(bbnick, list.get(6));
		assertEquals(cbnick, list.get(7));
		assertEquals(nn,list.get(8));
		assertEquals(nn,list.get(9));
	}
	
	/**
	 * Test that the phone number parser works properly
	 */
	@Test
	public void testGetPhone() {
		Person qatarWithExtension = new LdapPersonImpl(null,"a","a", null, null,null,null,null,null,"+ (974) 457.81234, extension 9283",null,null);
		Person internalNumber = new LdapPersonImpl(null,"b","a", null, null,null,null,null,null,"12345",null,null);
		Person noAreaCode = new LdapPersonImpl(null,"c","a", null, null,null,null,null,null,"872.1234",null,null);
		Person oddlyFormatted = new LdapPersonImpl(null,"d","a", null, null,null,null,null,null,"9,7,9 458! 5.1(2@2",null,null);
		Person oddlyFormattedWithExtension = new LdapPersonImpl(null,"e","a", null, null,null,null,null,null,"9,7,9 458! 5.1(2@2being of intercom denumeration%%%645",null,null);
		Person nullPhone = new LdapPersonImpl(null,"f","a", null, null,null,null,null,null,null,null,null);
		
		//Some problematic cases found in live ldap
		Person brendaRosas =    new LdapPersonImpl(null,"Rosas","Brenda",    null, null,null,null,null,null,"9794581302124",null,null);
		//Person chroustDavid =   new LdapPersonImpl(null,"Chroust","David",   null, null,null,null,null,null,"9794581265125",null,null);
		//Person dotsonFrances =  new LdapPersonImpl(null,"Dotson","Frances",  null, null,null,null,null,null,"9794581371156",null,null);
		Person tabacaruSimona = new LdapPersonImpl(null,"Tabacaru","Simona", null, null,null,null,null,null,"979.845.5453 or 979.845.8850",null,null);
		
		assertEquals(qatarWithExtension.getPhone(), "+974 4578-1234 (Qatar) ext: 9283");
		assertEquals(internalNumber.getPhone(), "(979) 451-2345");
		assertEquals(noAreaCode.getPhone(), "(979) 872-1234");
		assertEquals(oddlyFormatted.getPhone(), "(979) 458-5122");
		assertEquals(oddlyFormattedWithExtension.getPhone(), "(979) 458-5122 ext: 645");
		assertEquals(nullPhone.getPhone(), "");
		
		assertEquals(brendaRosas.getPhone(), "(979) 458-1302 ext: 124");
		assertEquals(tabacaruSimona.getPhone(), "(979) 845-5453 or (979) 845-8850");
	}
}
