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
 * Functional tests for verify that the administrative controller works as expected.
 * 
 * @author <a href="http://www.scottphillips.com">Scott Phillips</a>
 */

public class AdminTests extends FunctionalTest {

	private static String ESCAPED_DEFAULT_TEMPLATE = ".*Professional Responsibilities.*";
	

	/** Fixed a bug in the default version of this method. It dosn't follow redirects properly **/
	public static Response GET(Object url, boolean followRedirect) {
		Response response = GET(url);
		if (Http.StatusCode.FOUND == response.status && followRedirect) {
			String redirectedTo = response.getHeader("Location");
			response = GET(redirectedTo);
		}
		return response;
	}
	
	
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
	 * Test the positive case for editing your homepage. Specifically it tests the following properties:
	 * 
	 * 1) That you can edit your home page without logging in first.
	 * 
	 * 2) That after logging in you will be forwarded back to editing your home page.
	 * 
	 * 3) That the default template will be applied when editing your homepage.
	 * 
	 * 4) That you can change the content of your homepage.
	 * 
	 * 5) That if you remove all the content from your homepage it will reset to the default template.
	 */
	@Test
	public void testEditingHomepage() {

		// Resolve the URLs we will be visiting.
		Response response;
		Map<String,Object> routeArgs = new HashMap<String,Object>();
		routeArgs.put("netid", "aggiejack");
		final String AGGIEJACK_EDIT_URL = Router.reverse("Admin.editHomepage",routeArgs).url;
		final String LOGIN_URL = Router.reverse("Secure.login").url;

			{ // First go straight to the edit page and make sure it forces us to login.			
				response = GET(AGGIEJACK_EDIT_URL,true);
				assertStatus(200,response);
				assertContentMatch("<title>Login</title>",response);
			}

			{ // Login and verify it was successfull
				Map<String,String> params = new HashMap<String,String>();
				params.put("username", "aggiejack");
				params.put("password", "test");
				response = POST(LOGIN_URL, params);
				assertStatus(302,response);
				assertTrue(response.cookies.get("PLAY_SESSION").value.contains("aggiejack"));
				assertHeaderEquals("Location", AGGIEJACK_EDIT_URL, response); 
			}

			{ // View the edit homepage for real this time and make sure it shows the default
				//template because one should not exist allready.
				response = GET(AGGIEJACK_EDIT_URL);
				assertStatus(200,response);
				assertContentMatch("<title>Edit Homepage</title>",response);
				assertContentMatch("<textarea[^>]*>"+ESCAPED_DEFAULT_TEMPLATE+"</textarea>",response);
				
				// DEV-58 show edit link
				assertContentMatch(Play.configuration.getProperty("ldap.edit.url"),response);
			}

			{	// Change the homepage content and verify that it was saved.
				Map<String,String> params = new HashMap<String,String>();
				params.put("content", "I changed the page");
				response = POST(AGGIEJACK_EDIT_URL,params);
				assertContentMatch("<title>Edit Homepage</title>",response);
				assertContentMatch("<textarea[^>]*>I changed the page</textarea>",response);
				
				clearJPASession();
				Page page = Page.find("netid = ?", "aggiejack").first();
				assertEquals("I changed the page",page.getContent());
			}

			{   // remove all the homepage content
				Map<String,String> params = new HashMap<String,String>();
				params.put("content", "");
				response = POST(AGGIEJACK_EDIT_URL,params);
				assertContentMatch("<title>Edit Homepage</title>",response);
				assertContentMatch("<textarea[^>]*>"+ESCAPED_DEFAULT_TEMPLATE+"</textarea>",response);
			}
	}

	/**
	 * Verify that you are not able to edit other people's pages.
	 */
	@Test
	public void testForbiddenWhenEditingSomeoneElsesPage() {

		// Resolve the URLs we will be visiting.
		Response response;
		Map<String,Object> routeArgs = new HashMap<String,Object>();
		routeArgs.put("netid", "putput");
		final String PUTPUT_EDIT_URL = Router.reverse("Admin.editHomepage",routeArgs).url;
		routeArgs = new HashMap<String,Object>();
		routeArgs.put("netid", "aggiejack");
		final String AGGIEJACK_EDIT_URL = Router.reverse("Admin.editHomepage",routeArgs).url;
		routeArgs.put("netid", "thisdoesnotexist");
		final String THISDOESNOTEXIST_EDIT_URL = Router.reverse("Admin.editHomepage",routeArgs).url;
		final String LOGIN_URL = Router.reverse("Secure.login").url;

		{
			// First Login
			Map<String, String> params = new HashMap<String, String>();
			params.put("username", "aggiejack");
			params.put("password", "test");
			response = POST(LOGIN_URL, params);
			assertStatus(302, response);
			assertTrue(response.cookies.get("PLAY_SESSION").value
					.contains("aggiejack"));
			//DEV-61, redirect location changed to the person's homepage.
			assertHeaderEquals("Location", AGGIEJACK_EDIT_URL, response);
		}

		{
			// Attempt to edit someone elses homepage
			response = GET(PUTPUT_EDIT_URL);
			assertStatus(403, response);
		}

		{
			// Attempt to edit a non existant homepage
			response = GET(THISDOESNOTEXIST_EDIT_URL);
			assertStatus(403, response);
		}

	}
	
	
	
	/**
	 * Test the positive case for an admin editing someone else's homepage. 
	 */
	@Test
	public void testAdminEditingHomepage() {

		// Resolve the URLs we will be visiting.
		Response response;
		Map<String,Object> routeArgs = new HashMap<String,Object>();
		routeArgs.put("netid", "aggiejack");
		final String AGGIEJACK_EDIT_URL = Router.reverse("Admin.editHomepage",routeArgs).url;
		final String LOGIN_URL = Router.reverse("Secure.login").url;

			{ // First go straight to the edit page and make sure it forces us to login.			
				response = GET(AGGIEJACK_EDIT_URL,true);
				assertStatus(200,response);
				assertContentMatch("<title>Login</title>",response);
			}

			{ // Login with admin username and password and verify it was successfull
				Map<String,String> params = new HashMap<String,String>();
				params.put("username", "putput");
				params.put("password", "test");
				response = POST(LOGIN_URL, params);
				assertStatus(302,response);
				assertTrue(response.cookies.get("PLAY_SESSION").value.contains("putput"));
				assertHeaderEquals("Location", AGGIEJACK_EDIT_URL, response); 
			}

			{ // View the edit homepage for real this time and make sure it shows the default
				//template because one should not exist allready.
				response = GET(AGGIEJACK_EDIT_URL);
				assertStatus(200,response);
				assertContentMatch("<title>Edit Homepage</title>",response);
				assertContentMatch("<textarea[^>]*>"+ESCAPED_DEFAULT_TEMPLATE+"</textarea>",response);
			}

			{	// Change the homepage content and verify that it was saved.
				Map<String,String> params = new HashMap<String,String>();
				params.put("content", "I changed someone else's page");
				response = POST(AGGIEJACK_EDIT_URL,params);
				assertContentMatch("<title>Edit Homepage</title>",response);
				assertContentMatch("<textarea[^>]*>I changed someone else's page</textarea>",response);
				
				clearJPASession();
				Page page = Page.find("netid = ?", "aggiejack").first();
				assertEquals("I changed someone else's page",page.getContent());
			}
	}
	
	
	/**
	 * Test that an admin can add and delete other people's portraits
	 */
	@Test
	public void testAdminPortraits() throws IOException {
		
		// Resolve the URLs we will be visiting.
		Response response;

		// Create a file and fill it with some data.
		final File file = File.createTempFile("portrait-", ".png");
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(12);
		fos.close();

		try {
			Map<String,Object> routeArgs = new HashMap<String,Object>();
			routeArgs.put("netid", "aggiejack");
			final String AGGIEJACK_EDIT_URL = Router.reverse("Admin.editHomepage",routeArgs).url;
			final String LOGIN_URL = Router.reverse("Secure.login").url;

			routeArgs = new HashMap<String,Object>();
			routeArgs.put("netid", "aggiejack");
			final String VIEW_URL = Router.reverse("Public.viewPortrait",routeArgs).url;



			{ // Login and verify it was successfull
				Map<String,String> params = new HashMap<String,String>();
				params.put("username", "putput");
				params.put("password", "test");
				response = POST(LOGIN_URL, params);
				assertStatus(302,response);
				assertTrue(response.cookies.get("PLAY_SESSION").value.contains("putput"));
			}

			{ // View the edit homepage for real this time and make sure it shows the default
				//template because one should not exist allready.
				response = GET(AGGIEJACK_EDIT_URL);
				assertStatus(200,response);
				assertContentMatch("<title>Edit Homepage</title>",response);
				assertContentMatch("<textarea[^>]*>"+ESCAPED_DEFAULT_TEMPLATE+"</textarea>",response);
			}

			{	// Upload a portrait and verify that it was received.
				Map<String,String> params = new HashMap<String,String>();
				params.put("content", "I changed the page");
				Map<String,File> files = new HashMap<String,File>();
				files.put("uploadPortrait", file);
				response = POST(AGGIEJACK_EDIT_URL,params,files);
				assertStatus(200,response);
				assertContentMatch("<title>Edit Homepage</title>",response);
				assertContentMatch("<textarea[^>]*>I changed the page</textarea>",response);
				assertContentMatch(VIEW_URL,response);


				Page page = Page.find("netid = ?", "aggiejack").first();
				assertNotNull(page);
				assertNotNull(page.portrait);
				assertTrue(page.portrait.exists());
			}
		} finally {
			// delete the file on the client's side
			file.delete(); 
		}
	}
	
	
	/**
	 * Test that you can add and delete attachments
	 */
	@Test
	public void testAdminAttachments() throws IOException {
		
		// Resolve the URLs we will be visiting.
		Response response;
		
		// Create a file and fill it with some data.
		final File file = File.createTempFile("upload-", ".dat");
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(12);
		fos.close();
		try {
			Map<String,Object> routeArgs = new HashMap<String,Object>();
			routeArgs.put("netid", "aggiejack");
			final String AGGIEJACK_EDIT_URL = Router.reverse("Admin.editHomepage",routeArgs).url;
			final String LOGIN_URL = Router.reverse("Secure.login").url;
	
			routeArgs = new HashMap<String,Object>();
			routeArgs.put("netid", "aggiejack");
			routeArgs.put("name", file.getName());
			final String DELETE_URL = Router.reverse("Public.viewAttachment",routeArgs).url;
	
	
	
			{ // Login and verify it was successfull
				Map<String,String> params = new HashMap<String,String>();
				params.put("username", "putput");
				params.put("password", "test");
				response = POST(LOGIN_URL, params);
				assertStatus(302,response);
				assertTrue(response.cookies.get("PLAY_SESSION").value.contains("putput"));
			}
	
			{ // View the edit homepage for real this time and make sure it shows the default
				//template because one should not exist allready.
				response = GET(AGGIEJACK_EDIT_URL);
				assertStatus(200,response);
				assertContentMatch("<title>Edit Homepage</title>",response);
				assertContentMatch("<textarea[^>]*>"+ESCAPED_DEFAULT_TEMPLATE+"</textarea>",response);
			}
	
			{	// Upload an attachment and verify that it was received.
				Map<String,String> params = new HashMap<String,String>();
				params.put("content", "I changed the page");
				Map<String,File> files = new HashMap<String,File>();
				files.put("uploadAttachment", file);
				response = POST(AGGIEJACK_EDIT_URL,params,files);
				assertStatus(200,response);
				assertContentMatch("<title>Edit Homepage</title>",response);
				assertContentMatch("<textarea[^>]*>I changed the page</textarea>",response);
				assertContentMatch(file.getName(),response);
	
	
				Page page = Page.find("netid = ?", "aggiejack").first();
				assertNotNull(page);
				Attachment attachment = page.findAttachment(file.getName());
				assertNotNull(attachment);
			}
	
			{   // Delete the attachment
				response = DELETE(DELETE_URL);
				assertStatus(200,response);
				assertContentMatch("\\{\"status\":\"success\"\\}",response);
	
				Page page = Page.find("netid = ?", "aggiejack").first();
				assertNotNull(page);
				assertEquals(0,page.attachments.size());
			}
		} finally {
			// delete the file on the client's side
			file.delete(); 
		}
	}
	
	
	
	/**
	 * Test that you can add and remove links.
	 */
	@Test
	public void testAdminLinks() {
		
		// Resolve the URLs we will be visiting.
		Response response;
		
		Map<String,Object> routeArgs = new HashMap<String,Object>();
		routeArgs.put("netid", "aggiejack");
		final String AGGIEJACK_EDIT_URL = Router.reverse("Admin.editHomepage",routeArgs).url;
		final String AGGIEJACK_LINK_URL = Router.reverse("Admin.addLink",routeArgs).url;
		final String LOGIN_URL = Router.reverse("Secure.login").url;
		
		Map<String,Http.Cookie> savedCookies;
		
		{ // Login and verify it was successfull
			Map<String,String> params = new HashMap<String,String>();
			params.put("username", "putput");
			params.put("password", "test");
			response = POST(LOGIN_URL, params);
			assertStatus(302,response);
			assertTrue(response.cookies.get("PLAY_SESSION").value.contains("putput"));
			
			savedCookies = response.cookies;
		}

		{ 
			// Create a page and add a new link.
			Page page = new Page("aggiejack");
			page.save();
			
			// PUT requests don't preserve the cookies
			Request request = newRequest();
			request.cookies = savedCookies;
			response = PUT(request,AGGIEJACK_LINK_URL+"?url=url1&label=label1", null, "");
			assertStatus(200,response);
			assertContentMatch("\\{\"status\":\"success\"",response);

			clearJPASession();
			
			page = Page.find("netid = ?", "aggiejack").first();
			assertEquals(1,page.links.size());
		}

		{ 
			// Add a second link.
			Request request = newRequest();
			request.cookies = savedCookies;
			response = PUT(request,AGGIEJACK_LINK_URL+"?url=url2&label=label2", null, "");
			assertStatus(200,response);
			assertContentMatch("\\{\"status\":\"success\"",response);
			
			clearJPASession();
			Page page = Page.find("netid = ?", "aggiejack").first();
			assertEquals(2,page.links.size());
		}
		
		{ 
			// Reorder the links
			Page page = Page.find("netid = ?", "aggiejack").first();
			List<Link> links = page.getLinks();
			
			String order = links.get(1).id+","+links.get(0).id;
			
			Map<String,String> params = new HashMap<String,String>();
			params.put("order",order);
			response = POST(AGGIEJACK_LINK_URL,params);
			assertStatus(200,response);
			assertContentMatch("\\{\"status\":\"success\"",response);
			
			clearJPASession();
			page = Page.find("netid = ?", "aggiejack").first();
			assertEquals(2,page.links.size());
			assertEquals("url2",page.links.get(0).url);
			assertEquals("url1",page.links.get(1).url);
		}
		
		{ 
			// Delete a link
			Page page = Page.find("netid = ?", "aggiejack").first();
			List<Link> links = page.getLinks();
			response = DELETE(AGGIEJACK_LINK_URL+"?linkid="+links.get(0).id);
			assertStatus(200,response);
			assertContentMatch("\\{\"status\":\"success\"",response);
			
			clearJPASession();
			page = Page.find("netid = ?", "aggiejack").first();
			assertEquals(1,page.links.size());
			assertEquals("url1",page.links.get(0).url);
		}
	}
	
	
	/**
	 * Test publishing and unpublishing the homepage content and portrait picture.
	 */
	@Test
	public void testAdminPublishFlag() {

		// Resolve the URLs we will be visiting.
		Response response;
		Map<String,Object> routeArgs = new HashMap<String,Object>();
		routeArgs.put("netid", "aggiejack");
		final String AGGIEJACK_EDIT_URL = Router.reverse("Admin.editHomepage",routeArgs).url;
		final String LOGIN_URL = Router.reverse("Secure.login").url;

		{ // Login and verify it was successfull
			Map<String,String> params = new HashMap<String,String>();
			params.put("username", "putput");
			params.put("password", "test");
			response = POST(LOGIN_URL, params);
			assertStatus(302,response);
			assertTrue(response.cookies.get("PLAY_SESSION").value.contains("putput"));
		}

		{ // View the edit homepage for real this time and make sure it shows the default
			//template because one should not exist allready.
			response = GET(AGGIEJACK_EDIT_URL);
			assertStatus(200,response);
			assertContentMatch("<title>Edit Homepage</title>",response);
			assertContentMatch("<textarea[^>]*>"+ESCAPED_DEFAULT_TEMPLATE+"</textarea>",response);
			
			// Check that the flags default to on.
			assertContentMatch("<input[^>]*content_published[^>]*checked[^>]*>",response);
			assertContentMatch("<input[^>]*portrait_published[^>]*checked[^>]*>",response);
		}

		{	// Turn off the content published flag
			Map<String,String> params = new HashMap<String,String>();
			params.put("content", "I changed the page");
			// Ommiting content_published turns it off.
			params.put("portrait_published","true");
			response = POST(AGGIEJACK_EDIT_URL,params);
			assertContentMatch("<title>Edit Homepage</title>",response);
			assertContentMatch("<textarea[^>]*>I changed the page</textarea>",response);
			
			// Check that content published is off, while portrait is still on.
			assertContentMatch("<input[^>]*content_published[^c>]*[^>]*>",response);
			assertContentMatch("<input[^>]*portrait_published[^>]*checked[^>]*>",response);
			
			clearJPASession();
			Page page = Page.find("netid = ?", "aggiejack").first();
			assertFalse(page.content_published);
			assertTrue(page.portrait_published);
			assertEquals("I changed the page",page.getContent());
		}
		
		{	// Toggle the portrait published flag.
			Map<String,String> params = new HashMap<String,String>();
			params.put("content", "I changed the page");
			// Ommiting portrait published
			params.put("content_published","true");
			response = POST(AGGIEJACK_EDIT_URL,params);
			assertContentMatch("<title>Edit Homepage</title>",response);
			assertContentMatch("<textarea[^>]*>I changed the page</textarea>",response);
			
			// Check that content published is off, while portrait is still on.
			assertContentMatch("<input[^>]*content_published[^>]*checked[^>]*>",response);
			assertContentMatch("<input[^>]*portrait_published[^c>]*[^>]*>",response);
			
			clearJPASession();
			Page page = Page.find("netid = ?", "aggiejack").first();
			assertTrue(page.content_published);
			assertFalse(page.portrait_published);
			assertEquals("I changed the page",page.getContent());
		}
		
		{	// Turn both flags off
			Map<String,String> params = new HashMap<String,String>();
			params.put("content", "I changed the page");
			// Ommiting both content & portrait published
			response = POST(AGGIEJACK_EDIT_URL,params);
			assertContentMatch("<title>Edit Homepage</title>",response);
			assertContentMatch("<textarea[^>]*>I changed the page</textarea>",response);
			
			// Check that content published is off, while portrait is still on.
			assertContentMatch("<input[^>]*content_published[^c>]*[^>]*>",response);
			assertContentMatch("<input[^>]*portrait_published[^c>]*[^>]*>",response);
			
			clearJPASession();
			Page page = Page.find("netid = ?", "aggiejack").first();
			assertFalse(page.content_published);
			assertFalse(page.portrait_published);
			assertEquals("I changed the page",page.getContent());
		}
		
	}

	
	/**
	 * Test that you can add and delete attachments
	 */
	@Test
	public void testAttachments() throws IOException {
		
		// Resolve the URLs we will be visiting.
		Response response;
		
		// Create a file and fill it with some data.
		final File file = File.createTempFile("upload-", ".dat");
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(12);
		fos.close();
		try {
			Map<String,Object> routeArgs = new HashMap<String,Object>();
			routeArgs.put("netid", "aggiejack");
			final String AGGIEJACK_EDIT_URL = Router.reverse("Admin.editHomepage",routeArgs).url;
			final String LOGIN_URL = Router.reverse("Secure.login").url;
	
			routeArgs = new HashMap<String,Object>();
			routeArgs.put("netid", "aggiejack");
			routeArgs.put("name", file.getName());
			final String DELETE_URL = Router.reverse("Public.viewAttachment",routeArgs).url;
	
	
	
			{ // Login and verify it was successfull
				Map<String,String> params = new HashMap<String,String>();
				params.put("username", "aggiejack");
				params.put("password", "test");
				response = POST(LOGIN_URL, params);
				assertStatus(302,response);
				assertTrue(response.cookies.get("PLAY_SESSION").value.contains("aggiejack"));
			}
	
			{ // View the edit homepage for real this time and make sure it shows the default
				//template because one should not exist allready.
				response = GET(AGGIEJACK_EDIT_URL);
				assertStatus(200,response);
				assertContentMatch("<title>Edit Homepage</title>",response);
				assertContentMatch("<textarea[^>]*>"+ESCAPED_DEFAULT_TEMPLATE+"</textarea>",response);
			}
	
			{	// Upload an attachment and verify that it was received.
				Map<String,String> params = new HashMap<String,String>();
				params.put("content", "I changed the page");
				Map<String,File> files = new HashMap<String,File>();
				files.put("uploadAttachment", file);
				response = POST(AGGIEJACK_EDIT_URL,params,files);
				assertStatus(200,response);
				assertContentMatch("<title>Edit Homepage</title>",response);
				assertContentMatch("<textarea[^>]*>I changed the page</textarea>",response);
				assertContentMatch(file.getName(),response);
	
	
				Page page = Page.find("netid = ?", "aggiejack").first();
				assertNotNull(page);
				Attachment attachment = page.findAttachment(file.getName());
				assertNotNull(attachment);
			}
	
			{   // Delete the attachment
				response = DELETE(DELETE_URL);
				assertStatus(200,response);
				assertContentMatch("\\{\"status\":\"success\"\\}",response);
	
				Page page = Page.find("netid = ?", "aggiejack").first();
				assertNotNull(page);
				assertEquals(0,page.attachments.size());
			}
		} finally {
			// delete the file on the client's side
			file.delete(); 
		}
	}
	
	
	/**
	 * Test that you can add and delete attachments
	 */
	@Test
	public void testPortraits() throws IOException {
		
		// Resolve the URLs we will be visiting.
		Response response;

		// Create a file and fill it with some data.
		final File file = File.createTempFile("portrait-", ".png");
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(12);
		fos.close();

		try {
			Map<String,Object> routeArgs = new HashMap<String,Object>();
			routeArgs.put("netid", "aggiejack");
			final String AGGIEJACK_EDIT_URL = Router.reverse("Admin.editHomepage",routeArgs).url;
			final String LOGIN_URL = Router.reverse("Secure.login").url;

			routeArgs = new HashMap<String,Object>();
			routeArgs.put("netid", "aggiejack");
			final String VIEW_URL = Router.reverse("Public.viewPortrait",routeArgs).url;



			{ // Login and verify it was successfull
				Map<String,String> params = new HashMap<String,String>();
				params.put("username", "aggiejack");
				params.put("password", "test");
				response = POST(LOGIN_URL, params);
				assertStatus(302,response);
				assertTrue(response.cookies.get("PLAY_SESSION").value.contains("aggiejack"));
			}

			{ // View the edit homepage for real this time and make sure it shows the default
				//template because one should not exist allready.
				response = GET(AGGIEJACK_EDIT_URL);
				assertStatus(200,response);
				assertContentMatch("<title>Edit Homepage</title>",response);
				assertContentMatch("<textarea[^>]*>"+ESCAPED_DEFAULT_TEMPLATE+"</textarea>",response);
			}

			{	// Upload an attachment and verify that it was received.
				Map<String,String> params = new HashMap<String,String>();
				params.put("content", "I changed the page");
				Map<String,File> files = new HashMap<String,File>();
				files.put("uploadPortrait", file);
				response = POST(AGGIEJACK_EDIT_URL,params,files);
				assertStatus(200,response);
				assertContentMatch("<title>Edit Homepage</title>",response);
				assertContentMatch("<textarea[^>]*>I changed the page</textarea>",response);
				assertContentMatch(VIEW_URL,response);


				Page page = Page.find("netid = ?", "aggiejack").first();
				assertNotNull(page);
				assertNotNull(page.portrait);
				assertTrue(page.portrait.exists());
			}
		} finally {
			// delete the file on the client's side
			file.delete(); 
		}
	}
	
	
	/**
	 * Test that you can add and remove links.
	 */
	@Test
	public void testLinks() {
		
		// Resolve the URLs we will be visiting.
		Response response;
		
		Map<String,Object> routeArgs = new HashMap<String,Object>();
		routeArgs.put("netid", "aggiejack");
		final String AGGIEJACK_EDIT_URL = Router.reverse("Admin.editHomepage",routeArgs).url;
		final String AGGIEJACK_LINK_URL = Router.reverse("Admin.addLink",routeArgs).url;
		final String LOGIN_URL = Router.reverse("Secure.login").url;
		
		Map<String,Http.Cookie> savedCookies;
		
		{ // Login and verify it was successfull
			Map<String,String> params = new HashMap<String,String>();
			params.put("username", "aggiejack");
			params.put("password", "test");
			response = POST(LOGIN_URL, params);
			assertStatus(302,response);
			assertTrue(response.cookies.get("PLAY_SESSION").value.contains("aggiejack"));
			
			savedCookies = response.cookies;
		}

		{ 
			// Create a page and add a new link.
			Page page = new Page("aggiejack");
			page.save();
			
			// PUT requests don't preserve the cookies
			Request request = newRequest();
			request.cookies = savedCookies;
			response = PUT(request,AGGIEJACK_LINK_URL+"?url=url1&label=label1", null, "");
			assertStatus(200,response);
			assertContentMatch("\\{\"status\":\"success\"",response);

			clearJPASession();
			
			page = Page.find("netid = ?", "aggiejack").first();
			assertEquals(1,page.links.size());
		}

		{ 
			// Add a second link.
			Request request = newRequest();
			request.cookies = savedCookies;
			response = PUT(request,AGGIEJACK_LINK_URL+"?url=url2&label=label2", null, "");
			assertStatus(200,response);
			assertContentMatch("\\{\"status\":\"success\"",response);
			
			clearJPASession();
			Page page = Page.find("netid = ?", "aggiejack").first();
			assertEquals(2,page.links.size());
		}
		
		{ 
			// Reorder the links
			Page page = Page.find("netid = ?", "aggiejack").first();
			List<Link> links = page.getLinks();
			
			String order = links.get(1).id+","+links.get(0).id;
			
			Map<String,String> params = new HashMap<String,String>();
			params.put("order",order);
			response = POST(AGGIEJACK_LINK_URL,params);
			assertStatus(200,response);
			assertContentMatch("\\{\"status\":\"success\"",response);
			
			clearJPASession();
			page = Page.find("netid = ?", "aggiejack").first();
			assertEquals(2,page.links.size());
			assertEquals("url2",page.links.get(0).url);
			assertEquals("url1",page.links.get(1).url);
		}
		
		{ 
			// Delete a link
			Page page = Page.find("netid = ?", "aggiejack").first();
			List<Link> links = page.getLinks();
			response = DELETE(AGGIEJACK_LINK_URL+"?linkid="+links.get(0).id);
			assertStatus(200,response);
			assertContentMatch("\\{\"status\":\"success\"",response);
			
			clearJPASession();
			page = Page.find("netid = ?", "aggiejack").first();
			assertEquals(1,page.links.size());
			assertEquals("url1",page.links.get(0).url);
		}
	}
	
	
	/**
	 * Test publishing and unpublishing the homepage content and portrait picture.
	 */
	@Test
	public void testPublishFlag() {

		// Resolve the URLs we will be visiting.
		Response response;
		Map<String,Object> routeArgs = new HashMap<String,Object>();
		routeArgs.put("netid", "aggiejack");
		final String AGGIEJACK_EDIT_URL = Router.reverse("Admin.editHomepage",routeArgs).url;
		final String LOGIN_URL = Router.reverse("Secure.login").url;

		{ // Login and verify it was successfull
			Map<String,String> params = new HashMap<String,String>();
			params.put("username", "aggiejack");
			params.put("password", "test");
			response = POST(LOGIN_URL, params);
			assertStatus(302,response);
			assertTrue(response.cookies.get("PLAY_SESSION").value.contains("aggiejack"));
		}

		{ // View the edit homepage for real this time and make sure it shows the default
			//template because one should not exist allready.
			response = GET(AGGIEJACK_EDIT_URL);
			assertStatus(200,response);
			assertContentMatch("<title>Edit Homepage</title>",response);
			assertContentMatch("<textarea[^>]*>"+ESCAPED_DEFAULT_TEMPLATE+"</textarea>",response);
			
			// Check that the flags default to on.
			assertContentMatch("<input[^>]*content_published[^>]*checked[^>]*>",response);
			assertContentMatch("<input[^>]*portrait_published[^>]*checked[^>]*>",response);
		}

		{	// Turn off the content published flag
			Map<String,String> params = new HashMap<String,String>();
			params.put("content", "I changed the page");
			// Ommiting content_published turns it off.
			params.put("portrait_published","true");
			response = POST(AGGIEJACK_EDIT_URL,params);
			assertContentMatch("<title>Edit Homepage</title>",response);
			assertContentMatch("<textarea[^>]*>I changed the page</textarea>",response);
			
			// Check that content published is off, while portrait is still on.
			assertContentMatch("<input[^>]*content_published[^c>]*[^>]*>",response);
			assertContentMatch("<input[^>]*portrait_published[^>]*checked[^>]*>",response);
			
			clearJPASession();
			Page page = Page.find("netid = ?", "aggiejack").first();
			assertFalse(page.content_published);
			assertTrue(page.portrait_published);
			assertEquals("I changed the page",page.getContent());
		}
		
		{	// Toggle the portrait published flag.
			Map<String,String> params = new HashMap<String,String>();
			params.put("content", "I changed the page");
			// Ommiting portrait published
			params.put("content_published","true");
			response = POST(AGGIEJACK_EDIT_URL,params);
			assertContentMatch("<title>Edit Homepage</title>",response);
			assertContentMatch("<textarea[^>]*>I changed the page</textarea>",response);
			
			// Check that content published is off, while portrait is still on.
			assertContentMatch("<input[^>]*content_published[^>]*checked[^>]*>",response);
			assertContentMatch("<input[^>]*portrait_published[^c>]*[^>]*>",response);
			
			clearJPASession();
			Page page = Page.find("netid = ?", "aggiejack").first();
			assertTrue(page.content_published);
			assertFalse(page.portrait_published);
			assertEquals("I changed the page",page.getContent());
		}
		
		{	// Turn both flags off
			Map<String,String> params = new HashMap<String,String>();
			params.put("content", "I changed the page");
			// Ommiting both content & portrait published
			response = POST(AGGIEJACK_EDIT_URL,params);
			assertContentMatch("<title>Edit Homepage</title>",response);
			assertContentMatch("<textarea[^>]*>I changed the page</textarea>",response);
			
			// Check that content published is off, while portrait is still on.
			assertContentMatch("<input[^>]*content_published[^c>]*[^>]*>",response);
			assertContentMatch("<input[^>]*portrait_published[^c>]*[^>]*>",response);
			
			clearJPASession();
			Page page = Page.find("netid = ?", "aggiejack").first();
			assertFalse(page.content_published);
			assertFalse(page.portrait_published);
			assertEquals("I changed the page",page.getContent());
		}
		
	}

	
	
	

}
