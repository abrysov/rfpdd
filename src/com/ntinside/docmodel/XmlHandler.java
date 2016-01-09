package com.ntinside.docmodel;

public abstract class XmlHandler {
	public static abstract class XmlInfo {
		public abstract String getName();
		public abstract String getValue(String name);
		public abstract String getText();
	}
	
	public abstract void onStartElement(XmlInfo xml);
	public abstract void onEndElement(XmlInfo xml);
	public abstract void onText(XmlInfo xml);
	public abstract boolean isComplete();
}
