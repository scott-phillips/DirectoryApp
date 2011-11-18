package edu.tamu.directoryapp.model;

import org.junit.Test;

import play.test.UnitTest;

/**
 * Test the link model.
 * 
 * @author <a href="http://www.scottphillips.com">Scott Phillips</a>
 */
public class LinkTests extends UnitTest {

	/**
	 * Test creating a link object.
	 */
	@Test
	public void testCreation() {
		Page page = new Page("someone");
		
		Link link = new Link(page,"url","label");
		assertNotNull(link);
		assertEquals("url",link.url);
		assertEquals("label",link.label);
		
		link.delete();
		page.delete();
	}
	
}
