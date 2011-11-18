package controllers;

import org.junit.Test;

import edu.tamu.directoryapp.directory.ldap.LdapDirectoryImpl;

import play.modules.spring.Spring;
import play.test.UnitTest;

/**
 * Tests for the Authentication Controller. Because there is only one method in the class and it's not a real controller method we can use UnitTests for this case.
 * 
 * @author <a href="http://www.scottphillips.com">Scott Phillips</a>
 */

public class AuthTests extends UnitTest {

	
	/**
	 * Test loggin in with valid credentials
	 */
	@Test
	public void testPositiveAuthentication() {
		// Positive case
		assertTrue(Auth.authenticate("aggiejack", "test"));
		assertTrue(Auth.authenticate("AggieJack", "test"));
		
	}
	
	/**
	 * Test loging in with bad username or password.
	 */
	@Test
	public void testNegativeAuthentication() {
		// Negative case
		assertFalse(Auth.authenticate("aggiejack", "incorrect"));
		assertFalse(Auth.authenticate("incorrect", "test"));
	}

}
