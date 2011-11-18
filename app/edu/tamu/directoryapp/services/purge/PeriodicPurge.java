package edu.tamu.directoryapp.services.purge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import javax.activation.DataSource;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.commons.mail.SimpleEmail;

import com.sun.istack.internal.ByteArrayDataSource;

import edu.tamu.directoryapp.directory.Directory;
import edu.tamu.directoryapp.directory.ldap.LdapDirectoryImpl;
import edu.tamu.directoryapp.model.Attachment;
import edu.tamu.directoryapp.model.Page;
import edu.tamu.directoryapp.model.Person;
import play.Logger;
import play.Play;
import play.jobs.Every;
import play.jobs.Job;
import play.libs.Mail;
import play.libs.F.Promise;
import play.modules.spring.Spring;
import play.mvc.Mailer;
import play.mvc.Router;

/**
 * Periodically purge users who have left the libraries for more that 30 days.
 * Each day this job will run scanning each page currently in the system. It
 * will check to see if the page is still associated with an active user in the
 * directory. If the page is no longer active then upon the first site it will
 * record the current date. If an inactive page all ready has a date and the
 * purge grace period has expired then the page will be purged from the system.
 * 
 * @author <a href="http://www.scottphillips.com">Scott Phillips</a>
 */
@Every("1d")
public class PeriodicPurge extends Job {

	/** 
	 * Default values for purge notification emails.
	 */
	private final static String DEFAULT_FROM = "noreply@library.tamu.edu";
	private final static String DEFAULT_SUBJECT = "[DirectoryApp] Pending purge notice for {netid}";
	private final static String DEFAULT_MESSAGE = "This is an automatically generated email.\n\nThe DirectoryApp has detected that the user '{netid}' is no longer active with the library. Their page in the employee directory will be purged in {delay} days if no other action is taken. No additional emails will be sent.\n\n{directory}\n\nThe DirectoryApp\n\n";
	
	/**
	 * Default delay between first seeing an inactive page and deleting it.
	 */
	private final static int DEFAULT_DELAY = 90;


	/**
	 * Scan for inactive pages, and if any are found progress them along the purging process.
	 */
	public void doJob() {

		Logger.info("Purge: Starting scan for inactive pages.");
		int delay = getPurgeDelay();
		Calendar delayDate = Calendar.getInstance();
		delayDate.add(Calendar.DATE, -delay);
		Directory directory = Spring.getBeanOfType(Directory.class);
		List<Page> pages = Page.findAll();

		for (Page page : pages) {
			String netid = page.netid;
			Person person = directory.getByNetID(netid);

			if (person == null) {
				// The person is no longer active with the library.
				if (page.inactive == null) {
					// This is the first time we've seen this person as
					// inactive, record the current date.
					page.inactive = Calendar.getInstance();
					page.save();

					Logger.info("Purge: Detected inactive page (netid='"+netid+"') for the first time. The page is scheduled to be purged in "+delay+" days.");
					sendPurgeNotification(page);

				} else if (delayDate.after(page.inactive)) {

					Logger.info("Purge: Perminintaly deleting inactive page (netid='"+netid+"') because it has been inactive for more than "+delay+" days.");

					// This person has been inactive for longer than the grace
					// period. We now delete them.
					page.delete();
				}

			} else {
				// The person is still with the library. Clear any inactive
				// flags if any.
				if (page.inactive != null) {
					page.inactive = null;
					page.save();

					Logger.info("Purge: Previously inactive page (netid='"+netid+"') has become active. This page will no longer be purged.");
				}
			} // if person == null, else
		} // foreach pages
		Logger.info("Purge: Finishing scan for inactive pages.");
	}

	/**
	 * @return The configured purge delay in days.
	 */
	private int getPurgeDelay() {
		try {
			return Integer.valueOf(Play.configuration.getProperty("purge.delay"));
		} catch (NumberFormatException nfe) {
			/** Ignore **/
		}
		return DEFAULT_DELAY;
	}

	/**
	 * Send an email out saying that this person will be purged from the system
	 * 
	 * @param page The page which will be purged in the future.
	 * @throws EmailException 
	 */
	private void sendPurgeNotification(Page page)  {

		// 1. Gather all the configuration settings
		String toEmailList = Play.configuration.getProperty("purge.email",null);

		if (toEmailList == null)
			// Email notification is turned off.
			return;

		String fromEmail = Play.configuration.getProperty("purge.email.from",DEFAULT_FROM);
		String subject = Play.configuration.getProperty("purge.email.subject",DEFAULT_SUBJECT);
		String message = Play.configuration.getProperty("purge.email.message",DEFAULT_MESSAGE);

		// 2. Perform variable substitution:
		//    netid, delay, pageurl
		String directoryAppUrl = Play.configuration.getProperty("application.baseUrl","");
		directoryAppUrl += Router.reverse("Public.index").url;

		subject = subject.replaceAll("\\{netid\\}", page.netid);
		message = message.replaceAll("\\{netid\\}", page.netid);
		subject = subject.replaceAll("\\{delay\\}", String.valueOf(getPurgeDelay()) );
		message = message.replaceAll("\\{delay\\}", String.valueOf(getPurgeDelay()) );
		subject = subject.replaceAll("\\{directory\\}", directoryAppUrl);
		message = message.replaceAll("\\{directory\\}", directoryAppUrl);


		// 3. Send the email
		try {
			MultiPartEmail email = new MultiPartEmail();
			for ( String toEmail : toEmailList.split(",")) {
				email.addTo(toEmail);
			}
			email.setFrom(fromEmail);
			email.setSubject(subject);
			email.setMsg(message);

			// Attach homepage content
			DataSource homepageDS = new ByteArrayDataSource(page.getContent().getBytes(),"text/html");
			email.attach(homepageDS,"homepage.html","Homepage Content", EmailAttachment.ATTACHMENT);

			// Add the portrait picture
			if (page.portrait.exists()) {
				EmailAttachment portrait = new EmailAttachment();
				portrait.setDescription("Homepage Portrait");
				portrait.setName("portrait.jpg");
				portrait.setPath(page.portrait.getFile().getPath());

				email.attach(portrait);
			}

			// Each attachment
			for (Attachment attachment : page.attachments) {

				EmailAttachment file = new EmailAttachment();
				file.setDescription("Homepage File");
				file.setName(attachment.name);
				file.setPath(attachment.data.getFile().getPath());

				email.attach(file);
			}

			Future<Boolean> future = Mail.send(email);
			
			// Wait while the email is sent
			long start = System.currentTimeMillis();
			while (!future.isDone()) {
				if (future.isCancelled())
					throw new RuntimeException("Email task was unexpectidly canceled for "+page.netid);
				if (System.currentTimeMillis() - start > 60000) // 10 seconds
					throw new RuntimeException("Email task took too long to send for "+page.netid);
			};
		} catch (EmailException ee) {
			Logger.error("Purge: Unable to send purge notification because: "+ee);
		}
	}



}
