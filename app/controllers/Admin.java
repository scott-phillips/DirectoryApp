package controllers;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import edu.tamu.directoryapp.directory.Directory;
import edu.tamu.directoryapp.model.Attachment;
import edu.tamu.directoryapp.model.Link;
import edu.tamu.directoryapp.model.Page;
import edu.tamu.directoryapp.model.Person;
import play.Logger;
import play.Play;
import play.db.jpa.Blob;
import play.libs.MimeTypes;
import play.modules.spring.Spring;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Router;
import play.mvc.Router.Route;
import play.mvc.With;
import play.mvc.results.RenderStatic;


/**
 * The administrative controller allows users to edit their content within the directory app. They can 
 * edit their page's content, manage attachments, and additional links.
 * 
 * @author <a href="http://www.scottphillips.com">Scott Phillips</a>
 */

@With(Secure.class)
public class Admin extends Controller {

	/**
	 * Check that people can only access there page, and no one else. This will
	 * be run before any other action within this class.
	 */
	@Before
	static void checkNetID() throws Throwable {
		
		// If they haven't logged in yet then bail, the @With annotation will direct them to login.
		if (session.get("username") == null)
			return;
		
		// Check they they can only access their homepage.
		Object object = params.get("netid");
		
		// Check that the user is accessing their current homepage,
		// or is one of the privileged group
		if (object instanceof String) {
			String netid = (String) object;
			if (netid != null &&
				netid.length() > 0 &&
				(netid.equalsIgnoreCase(session.get("username")) || session.get("admin") != null )) {
				
				// Let's save the user object to for the render engine.
				Directory directory = Spring.getBeanOfType(Directory.class);
				Person person = directory.getByNetID(netid);
				
				if (person != null) {
					renderArgs.put("user", person);
					return;
				} // If the person has been found.
			} // if the netid matches the person who logged in.
		} // If netid a string

		// Otherwise, they are forbidden!
		forbidden();
	}
	
	
	/**
	 * Edit a person's homepage. The user can update the HTML content of their page and/or upload attachments to their page that can be included in the HTML.
	 * 
	 * @param netid The netid of the person who's having their content edited.
	 * @param uploadAttachment A new attachment to upload to the page.
	 */
	public static void editHomepage(String netid, File uploadAttachment, File uploadPortrait) throws Throwable {
		flash.clear();
		Page page = Page.find("netid = ?", netid).first();
		
		if (page == null) {
			page = new Page(netid);
		}
		
		// Save Page HTML content
		if ( "POST".equals(request.method) ) {

			if (uploadAttachment != null) {
				try {
					Attachment attachment = page.addAttachment(uploadAttachment);
					flash.success("Uploaded file attachment: "+attachment.name);
				} catch (IllegalArgumentException iae) {
					flash.error("Failed to upload file attachment: "+iae.getMessage());
				}
				page.save();
			}
			
			if (uploadPortrait != null) {
				try {
					page.setPortrait(uploadPortrait);
				} catch (IllegalArgumentException iae) {
					flash.error("Failed to upload portrait: "+iae.getMessage());
				}
				page.save();
			}

			// Save the HTML content & publish flags
			page.setContent(params.get("content"));
			
			if (params.get("content_published") != null)
				page.content_published = true;
			else 
				page.content_published = false;
			
			if (params.get("portrait_published") != null)
				page.portrait_published = true;
			else 
				page.portrait_published = false;
			
			page.save();
		}
		
		String baseAttachmentURL = request.secure ? "https://" : "http://";
		baseAttachmentURL += request.host;
		
		renderTemplate("Admin/edit.html",page,baseAttachmentURL);
	}
	
	/**
	 * Remove an attachment.
	 * 
	 * @param netid The netid of the person who owns the attachment.
	 * @param name The name of the attachment to be removed.
	 */
	public static void removeAttachment(String netid, String name) throws Throwable {
		
		Page page = Page.find("netid = ?", netid).first();
		notFoundIfNull(page);
		
		Attachment attachment = page.findAttachment(name);
		notFoundIfNull(attachment);
		
		page.removeAttachment(attachment);
		renderJSON("{\"status\":\"success\"}");
	}
	
	
	/**
	 * Add a new link to the end of the list.
	 * 
	 * @param netid The netid of the person who owns the page this link will be attached too.
	 * @param url The url of the new link.
	 * @param label The display label of the new link.
	 */
	public static void addLink(String netid, String url, String label) {
		
		Page page = Page.find("netid = ?", netid).first();
		if (page == null)
			page = new Page(netid);
		
		Link link = page.addLink(url,label);
		page.save();
		
		renderJSON("{\"status\":\"success\",\"id\":\""+link.id+"\"}");
	}
	
	/**
	 * Remove an existing link from the page.
	 * 
	 * @param netid The netid of the person who owns the page this link is attached too.
	 * @param linkid The unique id of the link.
	 */
	public static void removeLink(String netid, long linkid) {
		
		Page page = Page.find("netid = ?", netid).first();
		notFoundIfNull(page);
		
		Link link = Link.find("page_id = ? and id = ?",page.id,linkid).first();
		notFoundIfNull(page);
		
		page.removeLink(link);
		page.save();
		renderJSON("{\"status\":\"success\"}");
	}
	
	/**
	 * Reorder the list of links 
	 * 
	 * @param netid The netid of the person who owns the page these links are sorted for.
	 * @param order A comma seperated list of link ids
	 */
	public static void reorderLinks(String netid, String order) {

		Page page = Page.find("netid = ?", netid).first();
		notFoundIfNull(page);
		
		page.reorderLinks(order);
		page.save();
		renderJSON("{\"status\":\"success\"}");
	}

}
