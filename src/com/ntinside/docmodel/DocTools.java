package com.ntinside.docmodel;

import java.util.List;

public class DocTools {
	public static DocGroup findChapter(DocRoot root, int linkId) {
		for(DocGroup group: root.getGroups()) {
			DocGroup g = findChapter(group, linkId);
			if (g != null) {
				return g;
			}
		}
		
		if (root.getGroups().size() > 0 && (linkId < root.getGroups().get(0).getFirst()) ) {
			return getFirstGroupWithFile(root.getGroups().get(0));
		}
		return null;
	}
	
	private static DocGroup findChapter(DocGroup group, int linkId) {
		if (group.getFirst() <= linkId &&
			linkId <= group.getLast()) {
			if (group.getFilename() != null) {
				return group;
			} else {
				for(DocGroup c: group.getGroups()) {
					DocGroup found = findChapter(c, linkId);
					if (found != null) {
						return found;
					}
				}
				return getFirstGroupWithFile(group);
			}
		} else {
			return null;
		}
	}
	
	private static DocGroup getFirstGroupWithFile(DocGroup group) {
		if (group.getFilename() != null) {
			return group;
		}
		if (group.getGroups().size() > 0 )
			return getFirstGroupWithFile(group.getGroups().get(0));
		
		return null;
	}
	
	public static DocGroup findGroup(DocRoot root, Integer firstId) {
		if (firstId == null) {
			return null;
		}
		for(DocGroup g: root.getGroups()) {
			DocGroup f = findGroup(g, firstId);
			if (f != null)
				return f;
		}
		return null;
	}
	
	private static DocGroup findGroup(DocGroup group, Integer firstId) {
		if (firstId == group.getFirst()) {
			return group;
		}
		for(DocGroup g: group.getGroups()) {
			DocGroup f = findGroup(g, firstId);
			if (f != null) {
				return f;
			}
		}
		return null;
	}
	
	public static List<DocGroup> getChildGroups(DocRoot root, Integer firstId) {
		if (firstId == null) {
			return root.getGroups();
		}
		DocGroup g = findGroup(root, firstId);
		return (g != null) ? g.getGroups() : null;
	}

}
