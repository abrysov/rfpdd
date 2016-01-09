package com.ntinside.docmodel;

public class DocNote {
	public DocNote(int pos, String text) {
		this.pos = pos;
		this.text = text;
	}
	
	public int getPos() {
		return pos;
	}
	
	public String getText() {
		return text;
	}
	
	private String text;
	private int pos;
}
