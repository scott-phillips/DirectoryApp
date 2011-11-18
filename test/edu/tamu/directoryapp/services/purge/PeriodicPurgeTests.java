package edu.tamu.directoryapp.services.purge;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.tamu.directoryapp.model.Page;
import play.Play;
import play.libs.F.Promise;
import play.libs.Mail;
import play.test.UnitTest;

/**
 * Test the periodic purging code.
 * 
 * @author <a href="http://www.scottphillips.com">Scott Phillips</a>
 */

public class PeriodicPurgeTests extends UnitTest{
	
	
	/**
	 * Test that an inactive date is set for users who do not exist in the
	 * directory
	 */
	@Test
	public void testSetPurgeDate() {
		
		// Create a page for someone who does not exist in the directory.
		Page page = new Page("SomeoneWhoDoesNotExist");
		page.save();
		
		// Run the purge job
		new PeriodicPurge().doJob();
		
		// Check that a purge date was set.
		page = Page.find("netid = ?", "SomeoneWhoDoesNotExist").first();
		assertNotNull(page);
		assertNotNull(page.inactive);
		
		// check that the date is between the start of today and now.
		Calendar cal = Calendar.getInstance();
		assertTrue(page.inactive.before(cal));
		cal.add(Calendar.DATE,-1);
		assertTrue(page.inactive.after(cal)) ;
	}
	
	/**
	 * Test that when an inactive date is set, and the delay time has expired the page is deleted.
	 */
	@Test
	public void testPurgePage() {
		
		int purgeDelay = Integer.valueOf(Play.configuration.getProperty("purge.delay"));
		
		// Create a page for someone who does not exist in the directory.
		Page page = new Page("SomeoneWhoDoesNotExist");
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -purgeDelay);
		page.inactive = cal;
		page.save();
		
		// Run the purge job
		new PeriodicPurge().doJob();
		
		// Check that the person was removed
		page = Page.find("netid = ?", "SomeoneWhoDoesNotExist").first();
		assertNull(page);
	}

	/**
	 * Test that if a user who was once inactive returns their inactive date is cleared.
	 */
	@Test
	public void testUnPurgePage() {
		
		int purgeDelay = Integer.valueOf(Play.configuration.getProperty("purge.delay"));
		
		// Create a page for someone who exists
		Page page = new Page("aggiejack");
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -purgeDelay);
		page.inactive = cal;
		page.save();
		
		// Run the purge job
		new PeriodicPurge().doJob();
		
		// Check that the person's inactive date was reset.
		page = Page.find("netid = ?", "aggiejack").first();
		assertNotNull(page);
		assertNull(page.inactive);
	}
	

	/**
	 * Test that someone who has always been active remains so
	 */
	@Test
	public void testAlwaysActive() {
		
		// Create a page for someone who exists
		Page page = new Page("aggiejack");
		page.save();
		
		// Run the purge job
		new PeriodicPurge().doJob();
		
		// Check that the person's inactive date was reset.
		page = Page.find("netid = ?", "aggiejack").first();
		assertNotNull(page);
		assertNull(page.inactive);
		
		
	}
	/**
	 * Test that someone who has always been active remains so
	 */
	@Test
	public void testInactiveButNotYet() {
		
		int purgeDelay = Integer.valueOf(Play.configuration.getProperty("purge.delay"));
		purgeDelay--;
		
		// Create a page for someone who exists
		Page page = new Page("SomeoneWhoDoesNotExist");
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -purgeDelay);
		page.inactive = cal;
		page.save();
		
		// Run the purge job
		new PeriodicPurge().doJob();
		
		// Check that the person's inactive date was reset.
		page = Page.find("netid = ?", "SomeoneWhoDoesNotExist").first();
		assertNotNull(page);
		assertNotNull(page.inactive);
		assertTrue(page.inactive.equals(cal));
		
	}
	
	/**
	 * Test that someone who has always been active remains so
	 */
	@Test
	public void testInactiveButReturnedBeforeDelay() {
		
		int purgeDelay = Integer.valueOf(Play.configuration.getProperty("purge.delay"));
		purgeDelay--;
		
		// Create a page for someone who exists
		Page page = new Page("aggiejack");
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -purgeDelay);
		page.inactive = cal;
		page.save();
		
		// Run the purge job
		new PeriodicPurge().doJob();
		
		// Check that the person's inactive date was reset.
		page = Page.find("netid = ?", "aggiejack").first();
		assertNotNull(page);
		assertNull(page.inactive);
	}
	
	/**
	 * Test the purge email
	 */
	@Test
	public void testPurgeEmailNotification() throws IOException, InterruptedException {
		
		Mail.Mock.reset();
		
		File portrait = createTempFile(".jpg");
		File attachment1 = createTempFile(".pdf");
		File attachment2 = createTempFile(".txt");
		
		// Create a page for someone who does not exist in the directory.
		Page page = new Page("SomeoneWhoDoesNotExist");
		page.setPortrait(portrait);
		page.addAttachment(attachment1);
		page.addAttachment(attachment2);
		page.setContent("I will be emailed");
		page.save();
		
		// Run the purge job
		new PeriodicPurge().doJob();
		
		String emailList = Play.configuration.getProperty("purge.email");
		String email = Mail.Mock.getLastMessageReceivedBy(emailList.split(",")[0]);
		assertTrue(email.contains(page.netid));
		assertTrue(email.contains("homepage.html"));
		assertTrue(email.contains("portrait.jpg"));
		assertTrue(email.contains(attachment1.getName()));
		assertTrue(email.contains(attachment2.getName()));
		
		
		portrait.delete();
		attachment1.delete();
		attachment2.delete();
		
	}
	
	
	/** 
	 * Before every test clear out any test page
	 */
	@Before
	@After
	public void clearPages() {
		Page page = Page.find("netid = ?", "aggiejack").first();
		if (page != null)
			page.delete();
		
		page = Page.find("netid = ?", "SomeoneWhoDoesNotExist").first();
		if (page != null)
			page.delete();
		clearJPASession();
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
		fos.write(78);
		fos.close();
		return file;
	}
	
}
