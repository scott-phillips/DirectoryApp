package edu.tamu.directoryapp.directory.ldap;

import java.util.Enumeration;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DistinguishedName;


import play.Logger;

/**
 * Map the technical LDAP attributes to their friendly department object.
 * 
 * @author <a href="http://www.scottphillips.com">Scott Phillips</a>
 */

public class LdapDepartmentMapperImpl implements ContextMapper {

	/**
	 *  Map the technical LDAP attributes to their friendly Person object.
	 */
	@Override
	public Object mapFromContext(Object ctx) {

		DirContextAdapter context = (DirContextAdapter) ctx;

		String name = context.getStringAttribute("name");
		DistinguishedName dn = new DistinguishedName(context.getDn());
	
		return new LdapDepartmentImpl(name,dn);
	}
	
}
