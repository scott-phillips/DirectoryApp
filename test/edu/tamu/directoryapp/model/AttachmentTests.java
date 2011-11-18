package edu.tamu.directoryapp.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import play.Logger;
import play.test.UnitTest;

/**
 * Test the attachments model.
 * 
 * @author <a href="http://www.scottphillips.com">Scott Phillips</a>
 */
public class AttachmentTests extends UnitTest {
	
	/**
	 * Test that an attachment can be created and has the
	 * correct filename and at least a mimetype.
	 */
	@Test
	public void testCreation() throws IOException {
		
		File file = File.createTempFile("attachment-test", ".dat");
		Page page = new Page("someone");
		
		Attachment attachment = new Attachment(page, file);
		
		assertNotNull(attachment);
		assertNotNull(attachment.data.type());
		assertEquals(file.getName(),attachment.name);
		
		
		// Clean up our resources
		attachment.delete();
		page.delete();
		file.delete();
	}
	
	/**
	 * Test creating an attachment with a bad file.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testBadCreation() throws IOException {
		
		File file = null;
		Page page = new Page("someone");
		
		try {
			new Attachment(page, file);
		} finally {
			page.delete();
		}
	}
	
	/**
	 * Test that deleted attachments are removed from disk.
	 */
	@Test
	public void testDelete() throws IOException {
		
		// Create & delete an attachment
		File file = File.createTempFile("attachment-test", ".dat");
		Page page = new Page("someone");
		Attachment attachment = new Attachment(page, file);
		attachment.delete();
		
		// Verify the file in the asset store was also deleted.
		assertFalse(attachment.data.exists());
		assertFalse(attachment.data.getFile().exists());
		
		// Cleanup
		page.delete();
		file.delete();
	}
	
	/**
	 * Test that file types are properly detected.
	 */
	@Test
	public void testFileDetection() throws IOException {
		
		Map<String,String> types = new HashMap<String,String>();
		types.put(".png","image");
		types.put(".gif","image");
		types.put(".png","image");
		types.put(".tiff","image");
		types.put(".tif","image");
		types.put(".jpeg","image");
		types.put(".jpg","image");
		types.put(".jpe","image");
		types.put(".jif","image");
		types.put(".jfif","image");
		types.put(".jfi","image");
		
		types.put(".pdf", "pdf");

		types.put(".doc", "doc");
		types.put(".dot", "doc");
		types.put(".docx", "doc");
		types.put(".docm", "doc");
		types.put(".dotx", "doc");
		types.put(".dotm", "doc");
		
		types.put(".ppt", "ppt");
		types.put(".pot", "ppt");
		types.put(".pptx", "ppt");
		types.put(".pptm", "ppt");
		types.put(".potx", "ppt");
		types.put(".potm", "ppt");
		types.put(".pps", "ppt");
		types.put(".ppx", "ppt");
		types.put(".ppa", "ppt");
		
		types.put(".txt", "unknown");
		types.put(".odf", "unknown");
		types.put(".html", "unknown");
		types.put(".rtf", "unknown");
		
		// Test each type
		List<String> errors = new ArrayList<String>();
		Page page = new Page("someone");
		for(String extension : types.keySet()) {
			
			File file = File.createTempFile("attachment-test", extension);
			Attachment attachment = new Attachment(page,file);
			
			String detected = "unknown";
			if (attachment.isImage())
				detected = "image";
			else if (attachment.isPDF())
				detected = "pdf";
			else if (attachment.isWord())
				detected = "doc";
			else if (attachment.isPowerPoint())
				detected = "ppt";
			
			// Cleanup
			attachment.delete();
			file.delete();
			
			if (! types.get(extension).equals(detected))
				errors.add("Incorrect file type detected for extension: "+extension+" ( "+types.get(extension)+" =! "+detected+" )");
		}
		page.delete();
		
		if (errors.size() > 0) {
			// There were some failures.
			for (String error : errors) {
				Logger.error(error);
			}
			fail("At least one file type was incorrectly detected, see the log for information on which types failed.");
			
		}
		
		
	}
	
	
	

}
