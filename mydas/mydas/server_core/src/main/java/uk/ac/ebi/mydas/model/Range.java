package uk.ac.ebi.mydas.model;


public class Range{

	private Integer from = null;
	private Integer to = null;

	public Range( Integer start, Integer end ){
		this.from = start;
		this.to = end;
	}


	public boolean contains( Integer value ){
		return ((from <= value)  && (value  >= to));
	}
	
	public Integer getFrom(){
		return from;
	}
	public Integer getTo(){
		return to;
	}
}