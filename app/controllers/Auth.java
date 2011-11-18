package controllers;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;

import play.Play;
import play.libs.Crypto;
import play.modules.spring.Spring;
import play.mvc.Http;
import play.mvc.Router;
import controllers.Secure.Security;
import edu.tamu.directoryapp.directory.ldap.LdapDirectoryImpl;

/**
 * 
 * Authentication provider handles verify credentials with the ldap directory.
 * 
 * @author <a href="http://www.scottphillips.com">Scott Phillips</a>
 * 
 */

public class Auth extends Secure.Security {
	
	
	/** Validate the username and password by quering ldap **/
	public static boolean authenticate(String netid, String password) {

		// Authenticate the person
		LdapDirectoryImpl directory = Spring
				.getBeanOfType(LdapDirectoryImpl.class);
		boolean result = directory.authenticate(netid, password);
		
		// If they weren't going anywhere in particular send them to the root of the directory.
		if (result) {
			if (flash.get("url") == null) {
				Map<String,Object> routeArgs = new HashMap<String,Object>();
				routeArgs.put("netid", netid);
				flash.put("url", Router.reverse("Admin.editHomepage",routeArgs).url);
			}

			session.remove("admin");
			if (isAdmin(netid)) {
				session.put("admin", true);
			}
		}
		
		// Return the authentication result.
		return result;
	}
	
	
	/**
	 * A helper method to determine whether a particular user is an admin ("netid.admin" config parameter)
	 * @param netid
	 * @return
	 */
	public static boolean isAdmin(String netid) 
	{
		// See if we have any admins to begin with		
		String adminString = Play.configuration.getProperty("admin.netids");
		
		if (adminString == null || adminString.length() == 0)
			return false;

		// Split the admin string over commas and compare each one to the target netid
		String[] admins = adminString.split(",");
		for (String admin : admins) {
			if (netid.equalsIgnoreCase(admin.trim()))
				return true;
		}
	
		return false;
	}
	

}
