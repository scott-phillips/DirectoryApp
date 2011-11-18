package controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import edu.tamu.directoryapp.model.Page;

import play.Logger;
import play.Play;
import play.mvc.Before;
import play.mvc.Http.Response;
import play.mvc.Router;
import play.test.Fixtures;
import play.test.FunctionalTest;

/**
 * Test the public controller
 * 
 * @author Alexey Maslov
 * @author <a href="http://www.scottphillips.com">Scott Phillips</a>
 */

public class PublicTests extends FunctionalTest 
{
	@Before
	public void setUp() {
		Page page = Page.find("netid = ?", "putput").first();
		if (page != null)
			page.delete();
		clearJPASession();			
	}

	@Test
	public void testIndex() 
	{
		Response response = GET(Router.reverse("Public.index").url);
		assertStatus(200,response);
		assertContentMatch("<title>Library Directory</title>",response);	
		//ensure that the clear form button is present
		assertContentMatch("<input type=\"button\" value=\"Clear Form\" onclick=\"clearForm\\(this.form\\);\"/>", response);

		// DEV-60 login link should say Staff Login
		assertContentMatch("Staff Login", response);
	}

	@Test
	public void testLocationsDropDown() 
	{
		Response response = GET(Router.reverse("Public.index").url);
		assertStatus(200,response);
		assertContentMatch("<title>Library Directory</title>",response);

		// Check that the main library & qatar are present.
		assertContentMatch("<option\\s+value=\"Evans\">Sterling C. Evans Library</option>",response);
		assertContentMatch("<option\\s+value=\"Qatar\">Qatar Library</option>",response);		
	}

	@Test
	public void testDepartmentsDropDown() 
	{
		Response response = GET(Router.reverse("Public.index").url);
		assertStatus(200,response);
		assertContentMatch("<title>Library Directory</title>",response);

		// Check that the Applications Development & Windows
		assertContentMatch("<option\\s+value=\"ou=Applications,ou=Digital Initiatives\">Applications \\(APP\\)</option>",response);
		assertContentMatch("<option\\s+value=\"ou=Windows,ou=Operations,ou=Digital Initiatives\">Windows \\(OP\\)</option>",response);		
	}


	@Test
	public void testDepartments() 
	{
		Response response = GET(Router.reverse("Public.index").url);
		assertStatus(200,response);
		assertContentMatch("<title>Library Directory</title>",response);		
	}

	@Test
	public void testLocationLinks()
	{
		// Check that evans appears
		{
			Map<String,String> searchParams = new HashMap<String,String>();
			searchParams.put("locationField", "Evans");
			String searchURL = Router.reverse("Public.search").url;
			Response response = POST(searchURL, searchParams);
			assertStatus(200,response);
			assertNotNull(Play.configuration.getProperty("location.link.evans"));
			assertContentMatch(Play.configuration.getProperty("location.link.evans"),response);
		}

		// Check that WCL appears (since we had a bug where it didn't). Also check the url directly
		{
			Map<String,String> searchParams = new HashMap<String,String>();
			searchParams.put("locationField", "WCL");
			String searchURL = Router.reverse("Public.search").url;
			Response response = POST(searchURL, searchParams);
			assertStatus(200,response);
			assertContentMatch("<a href=\"http://wcl\\.library\\.tamu\\.edu/about/directions/\">WCL</a>",response);
		}
	}

	@Test
	public void testBrowse() 
	{

		Map<String,Object> routeArgs = new HashMap<String,Object>();
		routeArgs.put("letter", "C");

		Response response = GET(Router.reverse("Public.browse",routeArgs).url);
		assertStatus(200,response);
		assertContentMatch("<title>Library Directory</title>",response);		
		assertContentMatch("Clipper Chipper",response);
		assertContentMatch("<a href=\"mailto:clipper@library\\.uni\\.edu\">clipper@library\\.uni\\.edu</a>", response);
		assertContentMatch("George Clooney",response);
		assertContentMatch("<a href=\"mailto:gclooney@library\\.uni\\.edu\">gclooney@library\\.uni\\.edu</a>", response);
		assertContentMatch("Steven Carell",response);
		assertContentMatch("<a href=\"mailto:scarell@library\\.uni\\.edu\">scarell@library\\.uni\\.edu</a>", response);


		// DEV-59, Verify that title is displayed before position everywhere
		assertContentMatch("<span\\s*class=\"label\">Title:</span>\\s*<span\\s*class=\"content\">[^>]+</span>\\s*</div>\\s*<div\\s*class=\"extra-item\">\\s*<span\\s*class=\"label\">Position:</span>\\s*<span\\s*class=\"content\">[^>]+</span>",response);

	}

	@Test
	public void testSearch() 
	{
		Page page = new Page("putput");
		page.setContent("<p>I am puting the U back in Putin</p>");
		page.save();

		clearJPASession();

		Map<String, Object> routeArgs = new HashMap<String, Object>();
		routeArgs.put("netid", "putput");
		String putinProfileURL = Router.reverse("Public.viewPage", routeArgs).url;

		Map<String,String> searchParams = new HashMap<String,String>();
		searchParams.put("nameField", "Putin");
		String searchURL = Router.reverse("Public.search").url;

		Response response = POST(searchURL, searchParams);
		assertStatus(200,response);
		assertContentMatch("<title>Library Directory</title>",response);
		assertContentMatch(putinProfileURL, response);

		// Cleanup
		page = Page.find("netid = ?", "putput").first();
		page.delete();
	}

	@Test
	public void testPage() throws IOException
	{
		// 1. Get Putin's page
		Page page = new Page("putput");

		// 2. Add an attachment
		final File file = File.createTempFile("test-", ".dat");
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(12);
		fos.close();
		page.addAttachment(file);

		// 2.1 Add a portrait
		final File portrait = File.createTempFile("port-", ".png");
		fos = new FileOutputStream(portrait);
		fos.write(37);
		fos.close();
		page.setPortrait(portrait);

		// 2.2!!! Save dat stuff, yo
		page.save();

		clearJPASession();		

		// 3. Build the URL's for the profile page, attachment and portrait
		Map<String, Object> routeArgs = new HashMap<String, Object>();
		routeArgs.put("netid", "putput");
		String putinProfileURL = Router.reverse("Public.viewPage", routeArgs).url;

		routeArgs = new HashMap<String, Object>();
		routeArgs.put("netid", "putput");
		routeArgs.put("name", file.getName());
		String attachmentURL = Router.reverse("Public.viewAttachment", routeArgs).url;

		routeArgs = new HashMap<String, Object>();
		routeArgs.put("netid", "putput");
		String portraitURL = Router.reverse("Public.viewPortrait", routeArgs).url;

		// 4. GET the page content and verify that the attachment exists
		Response response = GET(putinProfileURL);
		assertStatus(200,response);
		assertContentMatch(portraitURL, response);

		// Verify that Title and Position are displayed in that order.
		assertContentMatch("Experience Designer",response);
		assertContentMatch("Staff",response);
		assertContentMatch("Experience Designer[^S]*Staff",response);

		// 5. Verify the portrait and attachment URLS
		response = GET(portraitURL);
		assertStatus(200,response);

		response = GET(attachmentURL);
		assertStatus(200,response);


		// Cleanup
		page = Page.find("netid = ?", "putput").first();
		page.delete();
	}

	@Test
	public void testUnpublishedPage() throws IOException
	{
		// 0. Define the URLS
		final String PUTPUT_SEARCH_URL = Router.reverse("Public.search").url;
		Map<String, Object> routeArgs = new HashMap<String, Object>();
		routeArgs.put("netid", "putput");
		final String PUTPUT_PAGE_URL = Router.reverse("Public.viewPage", routeArgs).url;
		final String LOGIN_URL = Router.reverse("Secure.login").url;


		// 1. Create PutPut's page and unpublish it.
		Page page = new Page("putput");
		page.content = "I shall not be seen.";
		page.content_published = false;
		page.portrait_published = true;
		page.save();

		clearJPASession();	

		// 2. Check that the profile link does not appear from a directory list
		{ 
			Map<String,String> searchParams = new HashMap<String,String>();
			searchParams.put("nameField", "Putin");
			Response response = POST(PUTPUT_SEARCH_URL,searchParams);
			assertStatus(200,response);
			assertContentMatch("<title>Library Directory</title>",response);
			assertFalse(getContent(response).contains(PUTPUT_PAGE_URL));
		}


		// 3. View the page un-authenticated
		{
			Response response = GET(PUTPUT_PAGE_URL);
			assertStatus(403, response);
		}

		// 4. Authenticate as putput
		{
			Map<String, String> params = new HashMap<String, String>();
			params.put("username", "putput");
			params.put("password", "test");
			Response response = POST(LOGIN_URL, params);
			assertStatus(302, response);
			assertTrue(response.cookies.get("PLAY_SESSION").value
					.contains("putput"));
		}

		// 5. Review the page, now authenticated
		{
			Response response = GET(PUTPUT_PAGE_URL);
			assertStatus(403, response);
		}

		// Cleanup
		page = Page.find("netid = ?", "putput").first();
		page.delete();
	}

	@Test
	public void testUnpublishedPortrait() throws IOException
	{

		// 1. Create PutPut's page & portrait and then unpublish the portrait.
		Page page = new Page("putput");
		page.content = "My face shall not be seen.";
		page.content_published = true;
		final File file = File.createTempFile("test-", ".png");
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(12);
		fos.close();
		page.setPortrait(file);
		page.portrait_published = false;
		page.save();

		clearJPASession();	


		// 2. Define the URLS
		final String PUTPUT_SEARCH_URL = Router.reverse("Public.search").url;
		Map<String, Object> routeArgs = new HashMap<String, Object>();
		routeArgs.put("netid", "putput");
		final String PUTPUT_PAGE_URL = Router.reverse("Public.viewPage", routeArgs).url;
		final String PUTPUT_PORTRAIT_URL = Router.reverse("Public.viewPortrait", routeArgs).url;
		final String LOGIN_URL = Router.reverse("Secure.login").url;



		// 3. Check that the portrait link does not appear from a directory list
		{ 
			Map<String,String> searchParams = new HashMap<String,String>();
			searchParams.put("nameField", "Putin");
			Response response = POST(PUTPUT_SEARCH_URL,searchParams);
			assertStatus(200,response);
			assertContentMatch("<title>Library Directory</title>",response);

			// the portrait url should not appear
			assertFalse(getContent(response).contains("src=\""+PUTPUT_PORTRAIT_URL));
		}

		// 4. View the page, un-authenticated
		{
			Response response = GET(PUTPUT_PAGE_URL);
			assertStatus(200, response);
			assertContentMatch("My face shall not be seen.",response);
			// There should not be a link to the portrait url.
			assertFalse(getContent(response).contains(PUTPUT_PORTRAIT_URL));
		}

		// 5. View the portrait, un-authenticated
		{
			Response response = GET(PUTPUT_PORTRAIT_URL);
			assertStatus(403, response);
		}

		// 6. Authenticate as putput
		{
			Map<String, String> params = new HashMap<String, String>();
			params.put("username", "putput");
			params.put("password", "test");
			Response response = POST(LOGIN_URL, params);
			assertStatus(302, response);
			assertTrue(response.cookies.get("PLAY_SESSION").value
					.contains("putput"));
		}

		// 7. Review the page, now authenticated
		{
			Response response = GET(PUTPUT_PAGE_URL);
			assertStatus(200, response);

			// Check that their is a message saying the page is unpublished.
			assertContentMatch("My face shall not be seen.",response);

			// there should still be no link to the portrait.
			assertFalse(getContent(response).contains(PUTPUT_PORTRAIT_URL));
		}

		// 8. View the portrait, now authenticated
		{
			Response response = GET(PUTPUT_PORTRAIT_URL);
			// We should now be able to see the portrait fine.
			assertStatus(200, response);
		}

		// Cleanup
		page = Page.find("netid = ?", "putput").first();
		page.delete();
	}


	@Test
	public void testUnpublishedSearch() 
	{
		// 0. Create the page
		Page page = new Page("putput");
		page.setContent("<p>I am puting the U back in Putin</p>");
		page.content_published = false;
		page.save();

		clearJPASession();

		// 1. Define the URLS
		Map<String, Object> routeArgs = new HashMap<String, Object>();
		routeArgs.put("netid", "putput");
		final String PUTPUT_PAGE_URL = Router.reverse("Public.viewPage", routeArgs).url;

		Map<String,String> searchParams = new HashMap<String,String>();
		searchParams.put("nameField", "Putin");
		final String SEARCH_URL = Router.reverse("Public.search").url;

		// 2. View the results table
		Response response = POST(SEARCH_URL, searchParams);
		assertStatus(200,response);
		assertContentMatch("<title>Library Directory</title>",response);
		
		// There should not be any link to a putput homepage because it is unpublished.
		assertFalse(getContent(response).contains(PUTPUT_PAGE_URL));


		// Cleanup
		page = Page.find("netid = ?", "putput").first();
		page.delete();
	}
	
	
	@Test
	public void testAdminEdit() 
	{
		final String LOGIN_URL = Router.reverse("Secure.login").url;
		Response response;
		
		// 0. Get a browse result list url 
		Map<String,Object> routeArgs = new HashMap<String,Object>();
		routeArgs.put("letter", "C");
		final String browseString = Router.reverse("Public.browse",routeArgs).url;

		{ // 1. Verify admin edit links don't show up for unauthenticated people
			response = GET(browseString);
			assertStatus(200,response);
			assertFalse(getContent(response).contains("[Edit]"));
		}
		
		{ // 2. Login as admin and verify it was successfull
			Map<String,String> params = new HashMap<String,String>();
			params.put("username", "putput");
			params.put("password", "test");
			response = POST(LOGIN_URL, params);
			assertStatus(302,response);
			assertTrue(response.cookies.get("PLAY_SESSION").value.contains("putput"));
		}
		
		{ // 3. Generate the browse list again and verify that the links now display
			response = GET(browseString);
			assertStatus(200,response);
			
			assertContentMatch("\\[Edit\\]", response);
		}
	}
	
	
	@Test
	public void testEdit() 
	{
		final String LOGIN_URL = Router.reverse("Secure.login").url;
		Response response;
		
		// 0. Get a browse result list url 
		Map<String,Object> routeArgs = new HashMap<String,Object>();
		routeArgs.put("letter", "D");
		final String browseString = Router.reverse("Public.browse",routeArgs).url;

		{ // 1. Verify admin edit links don't show up for unauthenticated people
			response = GET(browseString);
			assertStatus(200,response);
			assertFalse(getContent(response).contains("[Edit]"));
		}
		
		{ // 2. Login as admin and verify it was successfull
			Map<String,String> params = new HashMap<String,String>();
			params.put("username", "aggiejack");
			params.put("password", "test");
			response = POST(LOGIN_URL, params);
			assertStatus(302,response);
			assertTrue(response.cookies.get("PLAY_SESSION").value.contains("aggiejack"));
		}
		
		{ // 3. Generate the browse list again and verify that the links now display
			response = GET(browseString);
			assertStatus(200,response);
			
			assertContentMatch("jack\\@uni.edu[^\\@]*\\[Edit\\]", response);
		}
	}
	
	
	@Test
	public void testNullpublishedSearch() 
	{
		// 0. Create the page
		Page page = new Page("putput");
		page.setContent("<p>I am puting the U back in nUll</p>");
		page.content_published = null;
		page.portrait_published = null;
		page.save();

		clearJPASession();

		// 1. Define the URLS
		Map<String, Object> routeArgs = new HashMap<String, Object>();
		routeArgs.put("netid", "putput");
		final String PUTPUT_PAGE_URL = Router.reverse("Public.viewPage", routeArgs).url;
		final String PUTPUT_PORTRAIT_URL = Router.reverse("Public.viewPortrait", routeArgs).url;

		Map<String,String> searchParams = new HashMap<String,String>();
		searchParams.put("nameField", "Putin");
		final String SEARCH_URL = Router.reverse("Public.search").url;

		// 2. View the results table
		Response response = POST(SEARCH_URL, searchParams);
		assertStatus(200,response);
		assertContentMatch("<title>Library Directory</title>",response);
		assertFalse(getContent(response).contains(PUTPUT_PAGE_URL));
		assertFalse(getContent(response).contains(PUTPUT_PORTRAIT_URL));


		// Cleanup
		page = Page.find("netid = ?", "putput").first();
		page.delete();
	}


}
