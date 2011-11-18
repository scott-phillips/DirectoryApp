package controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.tamu.directoryapp.directory.Directory;
import edu.tamu.directoryapp.directory.ldap.LdapDirectoryImpl;
import edu.tamu.directoryapp.model.Attachment;
import edu.tamu.directoryapp.model.Department;
import edu.tamu.directoryapp.model.Location;
import edu.tamu.directoryapp.model.Page;
import edu.tamu.directoryapp.model.Person;
import play.db.jpa.Blob;
import play.modules.spring.Spring;
import play.mvc.Controller;

/**
 * Public controller for viewing directory information.
 * 
 * @author Alexey Maslov
 * @author <a href="http://www.scottphillips.com">Scott Phillips</a>
 */

public class Public extends Controller 
{
	
	/**
	 * Front page, should just display the alphabet listing and search form. 
	 */
	public static void index() {
		Directory directory = Spring.getBeanOfType(LdapDirectoryImpl.class);
		List<Department> departments = directory.getDepartments();
		List<Location> locations = directory.getLocations();
		
		renderTemplate("Public/directory.html", departments, locations);
	}
	
	/**
	 * Browse page, should just display the standard stuff and the matching letter 
	 */
	public static void browse(String letter) 
	{
		Directory directory = Spring.getBeanOfType(LdapDirectoryImpl.class);
		List<Person> people = directory.searchByAlphabet(letter.toUpperCase().charAt(0));
		List<Department> departments = directory.getDepartments();
		List<Location> locations = directory.getLocations();
		
		Map<String, Page> pages = new HashMap<String, Page>();
		for (Person person : people) {
			Page page = Page.find("netid = ?", person.getNetID()).first();
			if (page != null)
				pages.put(person.getNetID(), page);
		}
				
		renderTemplate("Public/directory.html",people, letter, pages, departments, locations);
	}
	
	/**
	 * Search results page, should display the standard stuff and the search results 
	 */
	public static void search(String nameField, String locationField, String deptField) 
	{
		Directory directory = Spring.getBeanOfType(LdapDirectoryImpl.class);
		List<Person> people = directory.search(nameField, locationField, deptField);
		List<Department> departments = directory.getDepartments();
		List<Location> locations = directory.getLocations();
		
		Map<String, Page> pages = new HashMap<String, Page>();
		if(people != null)
		{
			for (Person person : people) {
				Page page = Page.find("netid = ?", person.getNetID()).first();
				if (page != null)
					pages.put(person.getNetID(), page);
			}
		}
		
		renderTemplate("Public/directory.html",people, nameField, locationField, deptField, pages, departments, locations);
	}
	
	public static void viewPage(String netid) 
	{
		Directory directory = Spring.getBeanOfType(LdapDirectoryImpl.class);
		Person person = directory.getByNetID(netid);
		notFoundIfNull(person);
				
		Page page = Page.find("netid = ?", netid).first();
		notFoundIfNull(page);
		
		if (page.content_published != null && page.content_published) {
			render(person, page);
		} else {
			forbidden();
		}
	}
	
	/**
	 * Publicly view an attachment.
	 * 
	 * @param netid The netid of the person who owns the attachment. 
	 * @param name The name of the attachment.
	 */
	public static void viewAttachment(String netid, String name) {
		Page page = Page.find("netid = ?", netid).first();
		notFoundIfNull(page);
		Attachment attachment = page.findAttachment(name);
		notFoundIfNull(attachment);
		
		response.setContentTypeIfNotSet(attachment.data.type());
		renderBinary(attachment.data.get());
	}
	
	/**
	 * Publicly view a person's profile picture.
	 * 
	 * @param netid The netid of the person's profile picture.
	 */
	public static void viewPortrait(String netid) {
		Page page = Page.find("netid = ?", netid).first();
		notFoundIfNull(page);
		
		Blob portrait = page.portrait;
		if (!portrait.exists())
			notFound();

		// Check if the portrait is published
		if ((page.portrait_published != null && page.portrait_published) ||
			netid.equals(session.get("username"))) {
			response.setContentTypeIfNotSet(portrait.type());
			renderBinary(portrait.get());
		} else {
			forbidden();
		}
	}

}
