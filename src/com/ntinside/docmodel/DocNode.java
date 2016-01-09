package com.ntinside.docmodel;

import java.util.ArrayList;
import java.util.List;

public abstract class DocNode {
	
	public abstract DocNode getParent();
	
	public List<DocGroup> getGroups() {
		return groups;
	}
	
	void addGroup(DocGroup group) {
		groups.add(group);
	}
	
	private ArrayList<DocGroup> groups = new ArrayList<DocGroup>();
}
