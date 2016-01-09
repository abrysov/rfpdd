package com.ntinside.docmodel;

public class VersionXmlHander extends XmlHandler {

	@Override
	public void onStartElement(XmlInfo xml) {
		if (xml.getName().equals("version")) {
			inVersion = true;
		}
		
	}

	@Override
	public void onEndElement(XmlInfo xml) {
		if (xml.getName().equals("version")) {
			inVersion = false;
		}
		
	}

	@Override
	public void onText(XmlInfo xml) {
		if (inVersion) {
			version += xml.getText();
		}
	}
	
	@Override
	public boolean isComplete() {
		// TODO Auto-generated method stub
		return false;
	}
	
	public String getVersion() {
		return version;
	}
	
	private boolean inVersion = false;
	private String version = "";
	
}
