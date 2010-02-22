package uk.ac.ebi.mydas.model;

import java.net.URL;

public class DasLink {

	private URL href;
	private String text;
	public DasLink(URL href, String text) {
		this.href = href;
		this.text = text;
	}
	public URL getHref() {
		return href;
	}
	public void setHref(URL href) {
		this.href = href;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	
	

}
