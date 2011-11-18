package controllers;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
 * This controller manages integration with the CKEditor's file upload and browsing capabilities.
 * 
 * @author <a href="http://www.scottphillips.com">Scott Phillips</a>
 */

@With(Secure.class)
public class CKEditor extends Controller {

	/**
	 * Check that people can only access there page, and no one else. This will
	 * be run before any other action within this class.
	 */
	@Before
	static void checkNetID() throws Throwable {
		
		// If they haven't logged in yet then bail, the @With annotation will direct them to login.
		if (session.get("username") == null)
			forbidden();
		
		// Check they they can only access their homepage.
		Object object = params.get("netid");
		
		// Check that the user is accessing there current homepage,
		if (object instanceof String) {
			String netid = (String) object;
			if (netid != null &&
				netid.length() > 0 &&
				netid.equalsIgnoreCase(session.get("username")) || session.get("admin") != null) {
				
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
	 * Browse the list of attachments via the CKEditor. 
	 */
	public static void browse(String netid, String type, String CKEditorFuncNum) throws Throwable {
		Page page = Page.find("netid = ?", netid).first();
		if (page == null) {
			page = new Page(netid);
		}
		
		List<Attachment> attachments = page.attachments;
		
		if ("images".equals(type)) {
			// Only display images
			attachments = new ArrayList<Attachment>();
			for (Attachment attachment : page.attachments) {
				if (attachment.isImage())
					attachments.add(attachment);
			}
		}
		
		
		
		renderTemplate("CKEditor/browse.html",attachments,type,CKEditorFuncNum);
	}
	
	
	/** 
	 * Upload an attachment via the CKEditor. The file is stored server side, then a simple HTML page is
	 * displayed that contains a bit of javascript which loads the uploaded file's URL into the CKEditor.
	 * 
	 */
	public static void upload(String netid, String CKEditorFuncNum, File upload) throws Throwable {
		
		Page page = Page.find("netid = ?", netid).first();
		if (page == null) {
			page = new Page(netid);
		}
		
		if ( upload != null ) {
			Attachment attachment = page.addAttachment(upload);
			page.save();
			
			renderTemplate("CKEditor/upload.html",attachment, CKEditorFuncNum);
		}
		
		error("No file found when uploading");
	}

}
