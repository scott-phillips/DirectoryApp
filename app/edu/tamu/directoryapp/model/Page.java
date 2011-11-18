package edu.tamu.directoryapp.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import java.util.Collections;

import play.Logger;
import play.data.validation.Required;
import play.db.jpa.Blob;
import play.db.jpa.Model;
import play.libs.Images;
import play.libs.MimeTypes;

/**
 * Database model that represents a person homepage.
 * 
 * @author <a href="http://www.scottphillips.com">Scott Phillips</a>
 *
 */

@Entity
@Table(name = "page")
public class Page extends Model {
	
	public static final String DEFAULT_TEMPLATE = "<h2>Professional Responsibilities</h2><h2>Lorem ipsum dolor sit amet Librarian</h2><ul><li>Subject Specialist sit amet pretium tellus augue at quam </li><li>Subject Specialist nascetur ridiculus mus</li><li>Subject Specialist et sem ac justo rutrum sollicitudin</li></ul><h2>Affiliations</h2><p>Proin a dui eu orci euismod aliquam. Integer in ipsum in lacus hendrerit aliquam. Lorem ipsum dolor sit amet, consectetuer adipiscing elit</p><h2>Biography</h2><p>Sed tristique mauris aliquam magna. Morbi metus. Suspendisse justo sem, vulputate eleifend, porta vel, vulputate eu, libero. Proin quam orci, condimentum et, rutrum viverra, ullamcorper ac, diam. Sed tempus augue sed arcu. Proin nec nibh id nisl vestibulum auctor. Etiam eleifend porta velit. Suspendisse ac est et eros sagittis fringilla. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas.&nbsp;</p><h2>Research Interests</h2><p><strong>Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos hymenaeos. </strong>Curabitur eget sem at nunc interdum ultrices. Etiam dictum pellentesque justo. Sed lacus purus, iaculis a, tincidunt eget, luctus nec, justo. Pellentesque posuere placerat velit. In sed eros vel ligula ornare scelerisque. Ut tincidunt magna ac massa.</p><h2>Selected Papers</h2><ul><li>Mauris pharetra, sem nec tempor mollis, quam velit tempor lacus, ac commodo purus ante ut mauris. <br /></li><li>Aliquam erat volutpat. Duis tortor lorem, varius quis, accumsan at, accumsan sit amet, urna. Praesent posuere risus non est. Donec eleifend. Suspendisse potenti.</li></ul><h2>Books</h2><ul><li>The Old Man and the Sea</li><li>In Our Time</li><li>Farewell to Arms</li></ul><h2>Collections</h2><p>Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Cras nec leo. Donec in libero. Donec convallis metus ac diam. Nam dui. Maecenas lacus metus, euismod scelerisque, ornare vitae, adipiscing et, nisl. Morbi facilisis ornare diam. Nunc ac metus. Etiam ligula erat, faucibus a, ornare vel, ornare quis, purus. Vestibulum sem est, venenatis quis, commodo quis, nonummy sed, orci. Donec cursus pellentesque ligula. Ut at ligula id tellus tristique pharetra. Aliquam erat volutpat. Quisque eleifend ultrices diam.</p><p>Duis tortor lorem, varius quis, accumsan at, accumsan sit amet, urna. Praesent posuere risus non est. Donec eleifend. Suspendisse potenti. Etiam vulputate justo in mi. Ut pulvinar, justo eu lacinia iaculis, eros massa vestibulum turpis, vel mattis nisi turpis nec metus. Ut tincidunt placerat nisl. Maecenas dolor.</p>";
	
	@Required
	public String netid;
	
	public Boolean content_published;
	public Boolean portrait_published;
	
	@Lob
	public String content;
	
	public Blob portrait;
	
	@OneToMany( mappedBy="page", cascade=CascadeType.ALL)
	public List<Link> links;
	
	@OneToMany( mappedBy="page", cascade=CascadeType.ALL)
	public List<Attachment> attachments;
	
	// This is the date we first saw that the person is no longer active
	@Column( nullable=true)
	@Temporal(TemporalType.TIMESTAMP)
	public Calendar inactive;
	
	/** 
	 * Construct a new page object
	 * 
	 * @param netid The netid of the person who own's this page.
	 */
	public Page(String netid) {
		this.netid = netid;
		this.inactive = null;
		this.content_published = true;
		this.portrait_published = true;
		this.content = DEFAULT_TEMPLATE;
		this.portrait = new Blob();
		this.links = new ArrayList<Link>();
		this.attachments = new ArrayList<Attachment>();
	}
	
	/** 
	 * @return the netid of the person who owns this page.
	 */
	public String getNetID() {
		return this.netid;
	}
	
	
	/**
	 * @return The page's HTML content, or the default template if there is none.
	 */
	public String getContent() {
		
		if (this.content == null || this.content.trim().length() == 0)
			return DEFAULT_TEMPLATE;
		
		return this.content;
	}
	
	/**
	 * Set page content. If the content is empty then it will be
	 * replaced with the default template.
	 * 
	 * @param content The new content for the page.
	 */
	public void setContent(String content) {
		this.content = content;
		if (this.content == null || this.content.trim().length() == 0)
			this.content = null;
	}
	

	/**
	 * Find an attachment with a particular name. Relative to this page.
	 * 
	 * @param name The name of the attachment.
	 * @return The attachment found.
	 */
	public Attachment findAttachment(String name) {
		return Attachment.find("page_id = ? and name = ?", this.id, name).first();
	}
	
	/**
	 * Add a new file as an attachment. The name of the file will be used as the name of the attachment and it's contents will be stored on the filesystem for later retrieval.
	 * 
	 * @param file The file to add as an attachment.
	 * @return The new attachment.
	 */
	public Attachment addAttachment(File file) throws IOException {
		if (file == null)
			throw new IllegalArgumentException("Unable to attach a null file.");
		
		if (this.findAttachment(file.getName()) != null)
			throw new IllegalArgumentException("An attachment with the name is allready present.");
		
		if ( ! Pattern.matches(".+\\..+",file.getName())) 
			throw new IllegalArgumentException("Invalid filename, names must contain an extension such as .pdf, .png, .doc, etc...");
		
		Attachment attachment = new Attachment(this,file);
		
		this.attachments.add(attachment);
		return attachment;
	}
	
	/**
	 * Remove the attachment from the page and delete it's contents.
	 * 
	 * @param attachment The attachment to delete.
	 */
	public void removeAttachment(Attachment attachment) {
		if (attachment == null)
			throw new IllegalArgumentException("Unable to remove null attachment.");
			
		this.attachments.remove(attachment);
		attachment.delete();
	}
	
	
	/**
	 * Retrieve the list of links and ensure they are in the correct sortable order.
	 * 
	 * @return The sorted list of links.
	 */
	public List<Link> getLinks() {
		Collections.sort(this.links,new LinkComparator());
		return this.links;
	}
	
	
	/**
	 * Add a new to the end of the list with the specified url and label.
	 * 
	 * @param url The url of the link.
	 * @param label The label of the link.
	 */
	public Link addLink(String url, String label) {
		Link link = new Link(this,url,label);
		
		this.links.add(link);
		
		return link;
	}
	
	/**
	 * Remove the provided link from the list of links, and then delete the actualy link.
	 * 
	 * @param link The link to remove and delete the link.
	 */
	public void removeLink(Link link) {
		this.links.remove(link);
		link.delete();
	}
	
	
	
	/**
	 * Recieve a list of link ids formatted as a single string of
	 * comma seperated ids. Then re-arrange the links contained on
	 * this page with the given set of IDs.
	 * 
	 * @param order A comma seperated list of link ids.
	 */
	public void reorderLinks(String order) {
		if (order == null)
			throw new IllegalArgumentException();
		
		String[] orderStrings = order.split(",");
		List<Long> orderIDs = new ArrayList<Long>();
		for (String orderString : orderStrings)
			orderIDs.add(Long.valueOf(orderString));

		reorderLinks(orderIDs);
	}
	
	/**
	 * Reorder the links associated with this object according to the position of the links id in the passed list.
	 * 
	 * @param order A list of link ids.
	 */
	public void reorderLinks(List<Long> order) {
		if (order == null)
			throw new IllegalArgumentException();
		
		Comparator<Link> comparator = new LinkComparatorUsingExplicitOrder(order);
		List<Link> links = this.links;
		Collections.sort(links, comparator);
		
		// Persist the order in the database.
		long index = 0L;
		for(Link link : links) {
			link.sortOrder = index++;
			link.save();
		}
	}
	
	/**
	 * Set the user's portrait. The portrait must be an image file.
	 * 
	 * @param file An image file to be used a user's portrait
	 */
	public void setPortrait(File file) throws FileNotFoundException {

		if (file == null)
			throw new IllegalArgumentException("Unable set the portrait to a null file.");

		String type = MimeTypes.getContentType(file.getName());

		// image/gif, image/jpeg, image/png, image/tiff
		if ( type == null || ! type.startsWith("image/"))
			throw new IllegalArgumentException("Portraits must be an image.");

		// Delete the old portrait on disk.
		if (portrait.exists())
			portrait.getFile().delete();
		
		// Create a new blob and set it's data
		portrait = new Blob();
		portrait.set(new FileInputStream(file), type);
	}
	
	
	/**
	 * Save the page and all it's links / attachments.
	 */
	public Page save() {
		super.save();
		
		for(Attachment attachment : this.attachments)
			attachment.save();
				
		for(Link link : this.links)
			link.save();
				
				
		return this;
	}
	
	
	
	/**
	 * Private comparator class to handle sorting of a set of Links.
	 *
	 */
	private static class LinkComparator implements Comparator<Link> {
		
		@Override
		public int compare(Link a, Link b) {
			// if a > b: 1
			// if a < b: -1
			// if a = b: 0
		
			if (a.sortOrder == b.sortOrder) 
				return 0;
			else if (a.sortOrder > b.sortOrder)
				return 1;
			else 
				return 0;
		}
	} // LinkComparator
	
	/**
	 * Private comparator class to handle sorting of a set of Links using an explicit ordering provided.
	 *
	 */
	private static class LinkComparatorUsingExplicitOrder implements Comparator<Link> {
		
		/** A list of order ids for how links should be sorted **/
		private List<Long> orderIDs;
		
		/**
		 * Construct a new comparator which will sort links based upon the order of their IDs in this list.
		 * @param orderIDs A list of link order
		 */
		public LinkComparatorUsingExplicitOrder(List<Long> orderIDs) {
			this.orderIDs = orderIDs;
		}
		
		@Override
		public int compare(Link a, Link b) {
			// if a > b: 1
			// if a < b: -1
			// if a = b: 0
			
			if (a.id == b.id) {
				return 0;
			} else if (orderIDs.indexOf(a.id) > orderIDs.indexOf(b.id)) {
				return 1;
			} else {
				return -1;
			}
		}
	} // LinkComparatorUsingExplicitOrder
	
	@Override
    public Page delete() {
		
		if (this.portrait.exists())
			this.portrait.getFile().delete();
		
		for(Attachment attachment : this.attachments) {
			attachment.data.getFile().delete();
		}
		
        super.delete();
        return this;
    }
}
