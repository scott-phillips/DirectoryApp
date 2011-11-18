package controllers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.tamu.directoryapp.model.Attachment;
import edu.tamu.directoryapp.model.Link;
import edu.tamu.directoryapp.model.Page;

import play.Logger;
import play.Play;
import play.db.DB;
import play.db.jpa.Blob;
import play.db.jpa.JPA;
import play.mvc.Http;
import play.mvc.Router;
import play.mvc.Http.Cookie;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.test.FunctionalTest;

/**
 * Functional tests for the CKEditor file browser and uploader integration.
 * 
 * @author <a href="http://www.scottphillips.com">Scott Phillips</a>
 */

public class CKEditorTests extends FunctionalTest {
	
	/** 
	 * Before every test clear out aggiejack's page
	 */
	@Before
	@After
	public void clearAggieJack() {
		Page page = Page.find("netid = ?", "aggiejack").first();
		if (page != null)
			page.delete();
		clearJPASession();
	}
	
	/**
	 * Verify that we can not browse someone elses attachments.
	 */
	@Test
	public void testBrowsingSomeoneElsesAttachments() {
		
		// Resolve the URLs we will be visiting.
		Response response;
		Map<String,Object> routeArgs = new HashMap<String,Object>();
		routeArgs.put("netid", "aggiejack");
		final String AGGIEJACK_BROWSE_URL = Router.reverse("CKEditor.browse",routeArgs).url;
		final String LOGIN_URL = Router.reverse("Secure.login").url;

		{ 	// View the browse URL and verify everything pertinant is there.
			response = GET(AGGIEJACK_BROWSE_URL);
			assertStatus(403,response);
		}
		
	}
	
	/**
	 * Verify that we can not upload to someone elses attachments.
	 */
	@Test
	public void testUploadingToSomeoneElesesAttachments() throws IOException {
		
		Page page = new Page("aggiejack");
		page.save();
		clearJPASession();
		
		
		final File file = createTempFile(".dat");
		
		// Resolve the URLs we will be visiting.
		Response response;
		Map<String,Object> routeArgs = new HashMap<String,Object>();
		routeArgs.put("netid", "aggiejack");
		final String AGGIEJACK_BROWSE_URL = Router.reverse("CKEditor.upload",routeArgs).url;

		{ 
			// Upload an attachment and verify that it was received.
			Map<String,String> params = new HashMap<String,String>();
			params.put("CKEditorFuncNum", "99");
			Map<String,File> files = new HashMap<String,File>();
			files.put("upload", file);
			
			
			response = POST(AGGIEJACK_BROWSE_URL,params,files);
			assertStatus(403,response);
		}
		
		// Check that the file was uploaded correctly.
		page = Page.find("netid = ?", "aggiejack").first();
		assertNotNull(page);
		assertEquals(0,page.attachments.size());
		
		// Cleanup
		file.delete();
	}
	
	/**
	 * Test browsing files using the integrated CKEditor browser. Verify that the
	 * correct files are listed when the type is specified and when it is not.
	 */
	@Test
	public void testBrowsingFiles() throws IOException {
		
		Page page = new Page("aggiejack");
		final File png = createTempFile(".png");
		final File doc = createTempFile(".doc");
		final File pdf = createTempFile(".pdf");
		final File ppt = createTempFile(".ppt");
		final File txt = createTempFile(".txt");
		page.addAttachment(png);
		page.addAttachment(doc);
		page.addAttachment(pdf);
		page.addAttachment(ppt);
		page.addAttachment(txt);
		page.save();
		clearJPASession();
		
		
		// Resolve the URLs we will be visiting.
		Response response;
		Map<String,Object> routeArgs = new HashMap<String,Object>();
		routeArgs.put("netid", "aggiejack");
		final String AGGIEJACK_BROWSE_URL = Router.reverse("CKEditor.browse",routeArgs).url;
		final String LOGIN_URL = Router.reverse("Secure.login").url;

		{ // Login and verify it was successfull
			Map<String,String> params = new HashMap<String,String>();
			params.put("username", "aggiejack");
			params.put("password", "test");
			response = POST(LOGIN_URL, params);
			assertStatus(302,response);
			assertTrue(response.cookies.get("PLAY_SESSION").value.contains("aggiejack"));
		}

		{ 	// View the browse URL and verify everything pertinant is there.
			response = GET(AGGIEJACK_BROWSE_URL);
			assertStatus(200,response);
			assertContentMatch("<title>File Browser</title>",response);
		
			// Check one of each of the file times is present.
			assertContentMatch("icon image",response);
			assertContentMatch("icon PDF",response);
			assertContentMatch("icon word",response);
			assertContentMatch("icon powerpoint",response);
			assertContentMatch("icon file",response);
			
			// Assert each of the file names are present.
			assertContentMatch(page.attachments.get(0).name,response);
			assertContentMatch(page.attachments.get(1).name,response);
			assertContentMatch(page.attachments.get(2).name,response);
			assertContentMatch(page.attachments.get(3).name,response);
			assertContentMatch(page.attachments.get(4).name,response);
		}
		
		{ 	// View the browse URL again, but this time only for images.
			response = GET(AGGIEJACK_BROWSE_URL+"?type=images");
			assertStatus(200,response);
			assertContentMatch("<title>File Browser</title>",response);
		
			// Check one of each of the file times is present.
			assertContentMatch("icon image",response);
			assertFalse(getContent(response).contains("icon PDF"));
			assertFalse(getContent(response).contains("icon word"));
			assertFalse(getContent(response).contains("icon powerpoint"));
			assertFalse(getContent(response).contains("icon file"));
			
			// Assert each of the file names are present.
			assertContentMatch(page.attachments.get(0).name,response);
			assertFalse(getContent(response).contains(page.attachments.get(1).name));
			assertFalse(getContent(response).contains(page.attachments.get(2).name));
			assertFalse(getContent(response).contains(page.attachments.get(3).name));
			assertFalse(getContent(response).contains(page.attachments.get(4).name));
		}
		
		png.delete();
		doc.delete();
		pdf.delete();
		ppt.delete();
		txt.delete();
		
	}
	
	/**
	 * Test uploading a new file through the CKEditor file uploader.
	 */
	@Test
	public void testUploadingFiles() throws IOException {
		
		final File file = createTempFile(".dat");
		
		// Resolve the URLs we will be visiting.
		Response response;
		Map<String,Object> routeArgs = new HashMap<String,Object>();
		routeArgs.put("netid", "aggiejack");
		final String AGGIEJACK_BROWSE_URL = Router.reverse("CKEditor.upload",routeArgs).url;
		final String LOGIN_URL = Router.reverse("Secure.login").url;
		
		{ // Login and verify it was successfull
			Map<String,String> params = new HashMap<String,String>();
			params.put("username", "aggiejack");
			params.put("password", "test");
			response = POST(LOGIN_URL, params);
			assertStatus(302,response);
			assertTrue(response.cookies.get("PLAY_SESSION").value.contains("aggiejack"));
		}
		
		{	// Upload an attachment and verify that it was received.
			Map<String,String> params = new HashMap<String,String>();
			params.put("CKEditorFuncNum", "99");
			Map<String,File> files = new HashMap<String,File>();
			files.put("upload", file);
			response = POST(AGGIEJACK_BROWSE_URL,params,files);
			assertStatus(200,response);
			
			assertContentMatch("<title>File Upload</title>",response);
			assertContentMatch("window.parent.CKEDITOR.tools.callFunction\\( '99',",response);
			assertContentMatch(file.getName(),response);
		}

		clearJPASession();
		
		// Check that the file was uploaded correctly.
		Page page = Page.find("netid = ?", "aggiejack").first();
		assertNotNull(page);
		Attachment attachment = page.findAttachment(file.getName());
		assertNotNull(attachment);
	}
	
	
	
	/**
	 * Test browsing files using the integrated CKEditor browser. Verify that the
	 * correct files are listed when the type is specified and when it is not.
	 */
	@Test
	public void testAdminBrowsingFiles() throws IOException {
		
		Page page = new Page("aggiejack");
		final File png = createTempFile(".png");
		final File doc = createTempFile(".doc");
		final File pdf = createTempFile(".pdf");
		final File ppt = createTempFile(".ppt");
		final File txt = createTempFile(".txt");
		page.addAttachment(png);
		page.addAttachment(doc);
		page.addAttachment(pdf);
		page.addAttachment(ppt);
		page.addAttachment(txt);
		page.save();
		clearJPASession();
		
		
		// Resolve the URLs we will be visiting.
		Response response;
		Map<String,Object> routeArgs = new HashMap<String,Object>();
		routeArgs.put("netid", "aggiejack");
		final String AGGIEJACK_BROWSE_URL = Router.reverse("CKEditor.browse",routeArgs).url;
		final String LOGIN_URL = Router.reverse("Secure.login").url;

		{ // Login and verify it was successful
			Map<String,String> params = new HashMap<String,String>();
			params.put("username", "putput");
			params.put("password", "test");
			response = POST(LOGIN_URL, params);
			assertStatus(302,response);
			assertTrue(response.cookies.get("PLAY_SESSION").value.contains("putput"));
		}

		{ 	// View the browse URL and verify everything pertinent is there.
			response = GET(AGGIEJACK_BROWSE_URL);
			assertStatus(200,response);
			assertContentMatch("<title>File Browser</title>",response);
		
			// Check one of each of the file times is present.
			assertContentMatch("icon image",response);
			assertContentMatch("icon PDF",response);
			assertContentMatch("icon word",response);
			assertContentMatch("icon powerpoint",response);
			assertContentMatch("icon file",response);
			
			// Assert each of the file names are present.
			assertContentMatch(page.attachments.get(0).name,response);
			assertContentMatch(page.attachments.get(1).name,response);
			assertContentMatch(page.attachments.get(2).name,response);
			assertContentMatch(page.attachments.get(3).name,response);
			assertContentMatch(page.attachments.get(4).name,response);
		}
		
		{ 	// View the browse URL again, but this time only for images.
			response = GET(AGGIEJACK_BROWSE_URL+"?type=images");
			assertStatus(200,response);
			assertContentMatch("<title>File Browser</title>",response);
		
			// Check one of each of the file times is present.
			assertContentMatch("icon image",response);
			assertFalse(getContent(response).contains("icon PDF"));
			assertFalse(getContent(response).contains("icon word"));
			assertFalse(getContent(response).contains("icon powerpoint"));
			assertFalse(getContent(response).contains("icon file"));
			
			// Assert each of the file names are present.
			assertContentMatch(page.attachments.get(0).name,response);
			assertFalse(getContent(response).contains(page.attachments.get(1).name));
			assertFalse(getContent(response).contains(page.attachments.get(2).name));
			assertFalse(getContent(response).contains(page.attachments.get(3).name));
			assertFalse(getContent(response).contains(page.attachments.get(4).name));
		}
		
		png.delete();
		doc.delete();
		pdf.delete();
		ppt.delete();
		txt.delete();
		
	}
	
	/**
	 * Test uploading a new file through the CKEditor file uploader.
	 */
	@Test
	public void testAdminUploadingFiles() throws IOException {
		
		final File file = createTempFile(".dat");
		
		// Resolve the URLs we will be visiting.
		Response response;
		Map<String,Object> routeArgs = new HashMap<String,Object>();
		routeArgs.put("netid", "aggiejack");
		final String AGGIEJACK_BROWSE_URL = Router.reverse("CKEditor.upload",routeArgs).url;
		final String LOGIN_URL = Router.reverse("Secure.login").url;
		
		{ // Login and verify it was successfull
			Map<String,String> params = new HashMap<String,String>();
			params.put("username", "putput");
			params.put("password", "test");
			response = POST(LOGIN_URL, params);
			assertStatus(302,response);
			assertTrue(response.cookies.get("PLAY_SESSION").value.contains("putput"));
		}
		
		{	// Upload an attachment and verify that it was received.
			Map<String,String> params = new HashMap<String,String>();
			params.put("CKEditorFuncNum", "99");
			Map<String,File> files = new HashMap<String,File>();
			files.put("upload", file);
			response = POST(AGGIEJACK_BROWSE_URL,params,files);
			assertStatus(200,response);
			
			assertContentMatch("<title>File Upload</title>",response);
			assertContentMatch("window.parent.CKEDITOR.tools.callFunction\\( '99',",response);
			assertContentMatch(file.getName(),response);
		}

		clearJPASession();
		
		// Check that the file was uploaded correctly.
		Page page = Page.find("netid = ?", "aggiejack").first();
		assertNotNull(page);
		Attachment attachment = page.findAttachment(file.getName());
		assertNotNull(attachment);
	}
	
	
	/**
	 * Create a temporary file with a unique name and some data in it.
	 * 
	 * @param extension The extension of the file (include the dot!)
	 * @return A temporary file.
	 */
	private File createTempFile(String extension) throws IOException {
		final File file = File.createTempFile("upload-", extension);
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(12);
		fos.close();
		return file;
	}

}
