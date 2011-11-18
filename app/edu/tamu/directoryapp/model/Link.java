package edu.tamu.directoryapp.model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import play.data.validation.Required;
import play.data.validation.URL;
import play.db.jpa.Model;

/**
 * Database model that represents a side bar HTTP link.
 * 
 * @author <a href="http://www.scottphillips.com">Scott Phillips</a>
 */

@Entity
@Table(name="link")
public class Link extends Model {
	
	@ManyToOne
	@Required
	public Page page;

	@Required
	public String url;
	
	@Required
	public String label;
	
	@Required
	public Long sortOrder;
	
	/**
	 * Construct a new link object.
	 * 
	 * @param page The page this link applies too
	 * @param url The URL of the the link
	 * @param label The displaybale label of the link.
	 */
	public Link(Page page, String url, String label) {
		this.page = page;
		this.url = url;
		this.label = label;
		this.sortOrder = 0L;
	}
	
}
