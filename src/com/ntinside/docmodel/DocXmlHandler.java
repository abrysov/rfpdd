package com.ntinside.docmodel;

public class DocXmlHandler extends XmlHandler {
	@Override
	public void onStartElement(XmlInfo xml) {
		
		if (xml.getName().equals("group")) {
			if (!xml.getValue("name").equals("root")) {
				DocGroup docGroup = new DocGroup(currentNode, xml.getValue("name"),
									Integer.parseInt(xml.getValue("first")),
									Integer.parseInt(xml.getValue("last")));
				currentNode.addGroup(docGroup);
				group = docGroup;
			} else {
				root = new DocRoot();
				group = root;
			}
		}
		if (xml.getName().equals("html")) {
			((DocGroup) group).setFilename(xml.getValue("filename"));
		}
		if (xml.getName().equals("groups")) {
			currentNode = group;
		}
		if (xml.getName().equals("note")) {
			DocNote note = new DocNote(Integer.parseInt(xml.getValue("pos")),
									   xml.getValue("text"));
			root.addNote(note);
		}
	}

	@Override
	public void onEndElement(XmlInfo xml) {
		if (xml.getName().equals("groups")) {
			currentNode = currentNode.getParent();
		}
	}

	@Override
	public void onText(XmlInfo xml) {
	}
	
	@Override
	public boolean isComplete() {
		return false;
	}
	
	public DocRoot getDocRoot() {
		return root;
	}
	
	private DocRoot root;
	private DocNode currentNode = null;
	private DocNode group = null;
	
}
