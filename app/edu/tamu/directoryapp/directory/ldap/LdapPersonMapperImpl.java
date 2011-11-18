package edu.tamu.directoryapp.directory.ldap;

import java.util.Enumeration;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.springframework.ldap.core.AttributesMapper;


import play.Logger;

/**
 * Map the technical LDAP attributes to their friendly Person object.
 * 
 * @author <a href="http://www.scottphillips.com">Scott Phillips</a>
 * @author Alexey Maslov
 */

public class LdapPersonMapperImpl implements AttributesMapper {

	/**
	 *  Map the technical LDAP attributes to their friendly Person object.
	 */
	@Override
	public Object mapFromAttributes(Attributes attributes)
			throws javax.naming.NamingException {

//		System.out.println("----------------------------------------");
//		Enumeration<String> e = attributes.getIDs();		
//		while (e.hasMoreElements()) {
//			String name = e.nextElement();
//			Object value = attributes.get(name).get();
//			System.out.println("Recieved an attribute: "+name+" = "+value);
//			
//		}
		
		String netID = findAttribute(attributes, "sAMAccountName");
		String firstName = findAttribute(attributes, "givenName");
		String lastName = findAttribute(attributes, "sn");
		String displayName = findAttribute(attributes, "displayName");
		String nickname = findAttribute(attributes, "eduPersonNickname");
		String title = findAttribute(attributes, "title");
		String position = findAttribute(attributes, "eduPersonAffiliation");
		String department = findAttribute(attributes, "department");
		String email = findAttribute(attributes, "mail");
		String phone = findAttribute(attributes, "telephoneNumber");
		String building = findAttribute(attributes, "postalAddress");
		String office = findAttribute(attributes, "physicalDeliveryOfficeName");
		
		return new LdapPersonImpl(netID, firstName, lastName, displayName, nickname, title,
				position, department, email, phone, building, office);
	}

	/**
	 * Retrieve a single attribute, protecting against nulls and non-string
	 * datatypes.
	 * 
	 * @param attributes
	 *            The attribute object to retrieve values from.
	 * @param name
	 *            The name of the attribute to look for.
	 * @return The plain string-based value of the attribute, or null if not
	 *         found or otherwise invalid.
	 * @throws NamingException
	 */
	private String findAttribute(Attributes attributes, String name) {
		Attribute attribute = attributes.get(name);

		String value = null;
		try {
			if (attribute != null) {
				Object object = attribute.get();
				if (object instanceof String)
					value = (String) object;
			}
		} catch (NamingException ne) {
			Logger.warn("Encountered exception while looking for attribute: "
					+ name, ne);
		}
		return value;
	}

}
