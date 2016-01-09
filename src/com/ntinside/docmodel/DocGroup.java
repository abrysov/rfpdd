package com.ntinside.docmodel;

public class DocGroup extends DocNode {

	public DocGroup(DocNode parent, String name, int first, int last) {
		this.parent = parent;
		this.name = name;
		this.first = first;
		this.last = last;
	}
	
	public String getFilename() {
		return filename;
	}
	
	public String getName() {
		return name;
	}
	
	public int getFirst() {
		return first;
	}
	
	public int getLast() {
		return last;
	}
	
	@Override
	public DocNode getParent() {
		return parent;
	}
	
	void setFilename(String filename) {
		this.filename = filename;
	}
	
	private DocNode parent;
	private int first;
	private int last;
	private String name;
	private String filename;
}
