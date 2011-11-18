package edu.tamu.directoryapp.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import play.Play;
import play.test.UnitTest;

/**
 * Test the page model object.
 * 
 * @author <a href="http://www.scottphillips.com">Scott Phillips</a>
 */
public class PageTests extends UnitTest {

	
	/** 
	 * Before every test clear out aggiejack's and somone's page
	 */
	@Before
	public void clearPages() {
		String[] names = {"aggiejack","someone","some1","some2","someone23"};
		for (String name : names) {
			Page page = Page.find("netid = ?", name).first();
			if (page != null)
				page.delete();
		}
		clearJPASession();
	}
	
	/**
	 * Test that creating a page works.
	 */
	@Test
	public void testCreation() {

		Page page = new Page("aggiejack");
		assertEquals("aggiejack", page.getNetID());
		assertEquals(Page.DEFAULT_TEMPLATE, page.getContent());
		
		// DEV-56, add published flags
		assertTrue(page.content_published);
		assertTrue(page.portrait_published);

		// be nice and cleanup
		page.delete();
	}

	/**
	 * Test that setContent will 1) update the content and 2) revert the content
	 * back to the template if empty.
	 */
	@Test
	public void testSettingPageContent() {

		Page page = new Page("aggiejack");
		assertEquals("aggiejack", page.getNetID());
		assertEquals(Page.DEFAULT_TEMPLATE, page.getContent());

		page.setContent("I changed the content");
		assertEquals("I changed the content", page.getContent());

		page.setContent("   ");
		assertEquals(Page.DEFAULT_TEMPLATE, page.getContent());

		// be nice and cleanup
		page.delete();

	}
	
	
	/**
	 * Test basic positive case adding / removing / finding attachments.
	 */
	@Test
	public void testAttachments() throws IOException {
		
		// Create the page & attachments
		File file1 = File.createTempFile("file-", ".dat");
		File file2 = File.createTempFile("file-", ".dat");
		File file3 = File.createTempFile("file-", ".dat");
		
		Page page = new Page("someone");
		page.addAttachment(file1);
		page.addAttachment(file2);
		page.addAttachment(file3);
		
		assertNotNull(page.attachments);
		assertEquals(3,page.attachments.size());

		page.save();
		
		// Check the attachments
		Attachment attach1 = page.findAttachment(file1.getName());
		assertNotNull(attach1);
		assertEquals(file1.getName(),attach1.name);
		assertNotNull(attach1.data);
		assertNotNull(attach1.data.type());
		
		Attachment attach2 = page.findAttachment(file2.getName());
		assertNotNull(attach2);
		assertEquals(file2.getName(),attach2.name);
		assertNotNull(attach2.data);
		assertNotNull(attach2.data.type());
		
		Attachment attach3 = page.findAttachment(file3.getName());
		assertNotNull(attach3);
		assertEquals(file3.getName(),attach3.name);
		assertNotNull(attach3.data);
		assertNotNull(attach3.data.type());
		
		// Check removing attachments
		page.removeAttachment(attach2);
		
		assertNotNull(page.attachments);
		assertEquals(2,page.attachments.size());

		page.removeAttachment(attach1);
		assertNotNull(page.attachments);
		assertEquals(1,page.attachments.size());

		// Check that the only one left is attachment 1
		attach3 = page.findAttachment(file3.getName());
		assertNotNull(attach3);
		assertEquals(file3.getName(),attach3.name);
		assertNotNull(attach3.data);
		assertNotNull(attach3.data.type());

		// Remove the last one
		page.removeAttachment(attach3);
		assertNotNull(page.attachments);
		assertEquals(0,page.attachments.size());
		
		// Cleanup 
		page.delete();
	}
	
	/**
	 * Test searches for attachments which don't exist.
	 */
	@Test
	public void testFindBadAttachments() {
		
		Page page = new Page("someone");
		
		Attachment attach = page.findAttachment(null);
		assertNull(attach);
		
		attach = page.findAttachment("");
		assertNull(attach);
		
		attach = page.findAttachment("thisdoesnotexist");
		assertNull(attach);
		
		page.delete();
	}
	
	/**
	 * Test that you can not add a null attachment.
	 * @throws IOException
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testBadCreateAttachment() throws IOException {
		
		File file = File.createTempFile("illegal-attachment-filename", "dat");
		Page page =  new Page("someone");
		try {		
			page.addAttachment(null);			
		} finally {
			// make sure we cleanup
			page.delete();
		}
	}
	
	/**
	 * Test that you can not add an attachment without a file extension.
	 * @throws IOException
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testBadCreateAttachmentWithoutExtension() throws IOException {
		
		File file = File.createTempFile("illegal-attachment-filename", "dat");
		Page page =  new Page("someone");
		try {		
			page.addAttachment(file);			
		} finally {
			// make sure we cleanup
			page.delete();
			file.delete();
		}
	}
	
	/**
	 * Test that attachment names are unque to each page object.
	 */
	@Test
	public void testAttachmentNamesAreUnique() throws IOException {
	
		File file1 = File.createTempFile("fileone-", ".dat");
		
		Page page1 = new Page("some1");
		Page page2 = new Page("some2");
		
		// Test adding an attachment twice, should fail because the name is allready used.
		Attachment attach1 = page1.addAttachment(file1);
		assertNotNull(attach1);
		page1.save();
		try {
			page1.addAttachment(file1);
			fail("Expected adding a file as an attachment to throw an IllegalStateException.");
		} catch (IllegalArgumentException iae) {
			// we expect an exception here.
		}
		
		Attachment attach2 = page2.addAttachment(file1);
		assertNotNull(attach2);
		assertTrue(attach1.id != attach2.id);
		page2.save();
		
		// Test find them.
		Attachment find1 = page1.findAttachment(file1.getName());
		Attachment find2 = page2.findAttachment(file1.getName());
		assertNotNull(find1);
		assertNotNull(find2);
		assertTrue(find1.id != find2.id);
		assertTrue(find1.id == attach1.id);
		assertTrue(find2.id == attach2.id);
		
		// Cleanup
		page1.delete();
		page2.delete();
		file1.delete();	
	}
	
	/**
	 * Test removing attchment with bad arguments
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testRemoveBadAttachment() {
		
		Page page = new Page("someone");
		try {
			page.removeAttachment(null);
		} finally {
			// make sure we cleanup.
			page.delete();
		}
	}
	
	
	/**
	 * Test the basic positive case for adding / removing links 
	 */
	@Test
	public void testLinks() {
		
		Page page = new Page("someone");
		
		// Add two links
		Link link1 = page.addLink("url1", "label1");
		Link link2 = page.addLink("url2", "label2");
		
		assertNotNull(link1);
		assertNotNull(link2);
		
		assertNotNull(page.links);
		assertEquals(2,page.links.size());
		
		// Check that the two links are correct.
		link1 = page.links.get(0);
		assertNotNull(link1);
		assertEquals("url1",link1.url);
		assertEquals("label1",link1.label);
		
		link2 = page.links.get(1);
		assertNotNull(link2);
		assertEquals("url2",link2.url);
		assertEquals("label2",link2.label);
		
		// Remove a link and check that everything is correct.
		page.removeLink(link1);
		assertNotNull(page.links);
		assertEquals(1,page.links.size());
		
		link2 = page.links.get(0);
		assertNotNull(link2);
		assertEquals("url2",link2.url);
		assertEquals("label2",link2.label);
		
		// Finaly remove the last link and check that there are none.
		page.removeLink(link2);
		assertNotNull(page.links);
		assertEquals(0,page.links.size());
		
		// Cleanup
		page.delete();
		
	}
	
	/**
	 * Test to make sure the list of links preserves it's order.
	 */
	@Test
	public void testLinksPreservingOrder() { 
		
		
		Page page = new Page("someone");
		
		// Add a bunch of page links & save
		page.addLink("url1", "label1");
		page.addLink("url2", "label2");
		page.addLink("url3", "label3");
		page.addLink("url4", "label4");
		page.addLink("url5", "label5");
		page.addLink("url6", "label6");
		page.addLink("url7", "label7");
		page.addLink("url8", "label8");
		page.addLink("url9", "label9");
		page.save();
		
		// Check that the links order is correct after reloading
		page = Page.find("netid = ?", "someone").first();
		assertNotNull(page);
		assertNotNull(page.links);
		assertEquals(9,page.links.size());
		
		assertEquals("url1",page.links.get(0).url);
		assertEquals("url2",page.links.get(1).url);
		assertEquals("url3",page.links.get(2).url);
		assertEquals("url4",page.links.get(3).url);
		assertEquals("url5",page.links.get(4).url);
		assertEquals("url6",page.links.get(5).url);
		assertEquals("url7",page.links.get(6).url);
		assertEquals("url8",page.links.get(7).url);
		assertEquals("url9",page.links.get(8).url);
		
		// Modify the links
		page.removeLink(page.links.get(5));
		page.addLink("url0", "label0");
		page.save();
		
		
		// Recheck the order.
		page = Page.find("netid = ?", "someone").first();
		assertNotNull(page);
		assertNotNull(page.links);
		assertEquals(9,page.links.size());
		
		assertEquals("url1",page.links.get(0).url);
		assertEquals("url2",page.links.get(1).url);
		assertEquals("url3",page.links.get(2).url);
		assertEquals("url4",page.links.get(3).url);
		assertEquals("url5",page.links.get(4).url);
		assertEquals("url7",page.links.get(5).url);
		assertEquals("url8",page.links.get(6).url);
		assertEquals("url9",page.links.get(7).url);
		assertEquals("url0",page.links.get(8).url);
		
		page.delete();
	}
	
	/**
	 * Test both positive variants of the reorderLink method both the string and list based method.
	 */
	@Test
	public void testLinkReorder() {
		Page page = new Page("someone");
		
		// Add a bunch of page links & save
		Link l1 = page.addLink("url1", "label1");
		Link l2 = page.addLink("url2", "label2");
		Link l3 = page.addLink("url3", "label3");
		page.save();
		
		// Reorder the links using the string method
		String reOrderString = l2.id+","+l3.id+","+l1.id;
		page.reorderLinks(reOrderString);
		assertEquals(l2.id, page.links.get(0).id);
		assertEquals(l3.id, page.links.get(1).id);
		assertEquals(l1.id, page.links.get(2).id);
		
		page.save();
		
		// Check that the links persists past a save & reload
		page = Page.find("netid = ?", "someone").first();
		assertEquals(l2.id, page.links.get(0).id);
		assertEquals(l3.id, page.links.get(1).id);
		assertEquals(l1.id, page.links.get(2).id);
		
		
		
		// Reorder the links using the List<Long> method
		List<Long> reOrderList = new ArrayList<Long>();
		reOrderList.add(l1.id);
		reOrderList.add(l2.id);
		reOrderList.add(l3.id);
		page.reorderLinks(reOrderList);
		assertEquals(l1.id, page.links.get(0).id);
		assertEquals(l2.id, page.links.get(1).id);
		assertEquals(l3.id, page.links.get(2).id);
		
		
		// Check that the link order persists past a save & reload
		page = Page.find("netid = ?", "someone").first();
		assertEquals(l1.id, page.links.get(0).id);
		assertEquals(l2.id, page.links.get(1).id);
		assertEquals(l3.id, page.links.get(2).id);
		
		// Cleanup
		page.delete();
	}
	
	/**
	 * Test giving the reorder methods bad data
	 */
	@Test
	public void testbadReorder() {
		
		Page page = new Page("someone");
		Link l1 = page.addLink("url1", "label1");
		Link l2 = page.addLink("url2", "label2");
		Link l3 = page.addLink("url3", "label3");
		page.save();
		
		// Null for String based
		try {
			page.reorderLinks((String) null);
			fail("reorderLinks should throw an IllegalArgumentException when presented with a null parameter");
		} catch (IllegalArgumentException iae) {
			// this is expected.
		}
		
		// Null for list based
		try {
			page.reorderLinks((List<Long>) null);
			fail("reorderLinks should throw an IllegalArgumentException when presented with a null parameter");
		} catch (IllegalArgumentException iae) {
			// this is expected.
		}
		
		// Unparsable
		try {
			page.reorderLinks("abc");
			fail("reorderLinks should throw an IllegalArgumentException when presented with an unparceable parameter");
		} catch (NumberFormatException iae) {
			// this is expected.
		}
		// Unparsable
		try {
			page.reorderLinks("a,b,c");
			fail("reorderLinks should throw an IllegalArgumentException when presented with an unparceable parameter");
		} catch (NumberFormatException iae) {
			// this is expected.
		}
		
		// Unparsable
		try {
			page.reorderLinks("1,,2,3");
			fail("reorderLinks should throw an IllegalArgumentException when presented with an unparceable parameter");
		} catch (NumberFormatException iae) {
			// this is expected.
		}
		
		// Giving random numbers should not produce an error, just the order of the list may not be determined.
		page.reorderLinks("95,63,21");
	}
	
	
	/**
	 * Positive test cases for setting and resetting a portrait image.
	 */
	@Test
	public void testSetPortrait() throws IOException {
		File png = File.createTempFile("image-", ".png");
		File gif = File.createTempFile("image-", ".gif");
		File jpg = File.createTempFile("image-", ".jpg");
		
		Page page = new Page("someone");
		page.setPortrait(png);
		page.save();
		clearJPASession();
		page = Page.find("netid = ?", "someone").first();
		assertTrue(page.portrait.getFile().exists());
		assertEquals("image/png",page.portrait.type());
		
		page.setPortrait(gif);
		page.save();
		clearJPASession();
		page = Page.find("netid = ?", "someone").first();
		assertTrue(page.portrait.getFile().exists());
		assertEquals("image/gif",page.portrait.type());
		
		
		page.setPortrait(jpg);
		page.save();
		clearJPASession();
		page = Page.find("netid = ?", "someone").first();
		assertTrue(page.portrait.getFile().exists());
		assertEquals("image/jpeg",page.portrait.type());
		
		page.delete();
		png.delete();
		gif.delete();
		jpg.delete();
	}
	
	/**
	 * Negative test cases for setting a portrait image.
	 * @throws IOException 
	 */
	@Test
	public void testBadSetPortrait() throws IOException {
		
		File file = File.createTempFile("file-", ".dat");
		
		Page page = new Page("someone");
		try {
			page.setPortrait(null);
			fail("setPortrait() did not throw an exception with null input.");
		} catch (IllegalArgumentException iae) {
			// expected
		}
		
		try {
			page.setPortrait(file);
			fail("setPortrait() allowed a non-image file to be used.");
		} catch (IllegalArgumentException iae) {
			// expected;
		}
		
		page.delete();
		file.delete();
	}
	
	
	/** 
	 * Test the get/set content methods
	 */
	@Test
	public void testGetContent() {
		
		Page page = new Page("someone");
		page.content = "";
		assertEquals(Page.DEFAULT_TEMPLATE,page.getContent());
		
		page.setContent(" ");
		assertEquals(Page.DEFAULT_TEMPLATE,page.content);
		
		page.setContent("I changed the content");
		assertEquals("I changed the content",page.content);
		
		page.delete();
	}
	
	/**
	 * Test the get/set operation for inactive date. Specifically check that it handles the null attribute correctly.
	 */
	@Test
	public void testInactiveDate() {
		
		Page page = new Page("someone");
		assertNull(page.inactive);
		page.save();
		
		clearJPASession();
		
		page = Page.find("netid = ?", "someone").first();
		assertNull(page.inactive);
		page.inactive = Calendar.getInstance();
		assertNotNull(page.inactive);
		page.save();
		
		clearJPASession();
		
		page = Page.find("netid = ?", "someone").first();
		assertNotNull(page.inactive);		
		page.inactive = null;
		assertNull(page.inactive);
		page.save();
		
		clearJPASession();
		
		page = Page.find("netid = ?", "someone").first();
		assertNull(page.inactive);		
		
		page.delete();
		
	}
	
	
	/**
	 * Test that save cascades
	 * @throws IOException 
	 */
	@Test
	public void testSave() throws IOException {
		
		// Create the page & attachments
		File file = File.createTempFile("file-", ".dat");
				
		Page page = new Page("someone23");
		Attachment attach = page.addAttachment(file);
		Link link = page.addLink("url","label");
		
		assertFalse(page.isPersistent());
		assertFalse(attach.isPersistent());
		assertFalse(link.isPersistent());
		
		
		page.save();
		
		
		assertTrue(page.isPersistent());
		assertTrue(attach.isPersistent());
		assertTrue(link.isPersistent());
		
		
		// Cleanup
		page.removeAttachment(attach);
		page.removeLink(link);
		page.delete();
	}
	

}
