package edu.tamu.directoryapp.model;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import play.data.validation.Required;
import play.db.jpa.Blob;
import play.db.jpa.Model;
import play.libs.MimeTypes;
import play.mvc.Router;

/**
 * Database model of a file attachment.
 * 
 * @author <a href="http://www.scottphillips.com">Scott Phillips</a>
 */

@Entity
@Table(name = "attachment")
public class Attachment extends Model {

	public Blob data;
	
	@Required
	public String name;
	
    @ManyToOne
    @Required
    public Page page;
	
	/**
	 * Construct a new attachment for the given filename.
	 * 
	 * @param page The page this attachment belongs too.
	 * @param file The file contaning the name & data of the attachment.
	 */
	public Attachment(Page page, File file) throws IOException {
		
		if (page == null || file == null)
			throw new IllegalArgumentException("Unable to create new attachment because either the page or file is null.");
		
		this.page = page;
		this.name = file.getName();
		this.data = new Blob();
		this.data.set(new FileInputStream(file),MimeTypes.getContentType(name));
	}
	
	
	@Override
    public Attachment delete() {
		
		if (this.data.exists())
			this.data.getFile().delete();
		
        super.delete();
        return this;
    }
	
	/**
	 * @return true if the attached file is an image; otherwise false.
	 */
	public boolean isImage() {
		
		if (this.data.exists() && this.data.type().startsWith("image/"))
			return true;
		
		return false;
	}
	
	/** 
	 * @return true if the attached file is a PDF; otherwise false.
	 */
	public boolean isPDF() {
		if (this.data.exists() && this.data.type().contains("pdf"))
			return true;
		
		return false;
	}
	
	/** 
	 * @return true if the attached file is a Word Document; otherwise false.
	 */
	public boolean isWord() {
		if (this.data.exists() && ( this.data.type().contains("msword") || this.data.type().contains("wordprocessingml") ))
			return true;
		
		return false;
	}
	
	/** 
	 * @return true if the attached file is a PowerPoint presentation; otherwise false.
	 */
	public boolean isPowerPoint() {
		if (this.data.exists() && ( this.data.type().contains("ms-powerpoint") || this.data.type().contains("presentationml") ))
			return true;
		
		return false;
	}
	
	
}
