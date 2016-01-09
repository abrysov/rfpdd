package com.ntinside.docmodel;

import java.util.ArrayList;
import java.util.List;

public class DocRoot extends DocNode {
	
	public List<DocNote> getNotes() {
		return notes;
	}
	
	@Override
	public DocNode getParent() {
		return null;
	}
	
	void addNote(DocNote note) {
		notes.add(note);
	}
	
	private ArrayList<DocNote> notes = new ArrayList<DocNote>();
}
