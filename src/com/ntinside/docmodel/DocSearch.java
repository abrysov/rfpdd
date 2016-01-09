package com.ntinside.docmodel;

import android.content.Context;

public class DocSearch {

	public interface Observer {
		public void onFound(int linkId, String text, String[] words);
	}
	
	private static class GetTextLinkResult {
		private String result;
		public void setResult(String result) {
			this.result = result;
		}
		public String getResult() {
			return result;
		}
	}
	
	private static final String SEARCH_FILE = "search.mp3";
	
	public static String getTextFromLink(Context context, final int linkId) {
		final GetTextLinkResult result = new GetTextLinkResult();
		
		try {
			JavaXmlReader.loadXml(context, new XmlHandler() {

				@Override
				public void onStartElement(XmlInfo xml) {
					if (xml.getName().equals("link")) {
						String strId = xml.getValue("id");
						if (strId != null) {
							if ( Integer.parseInt(strId) == linkId ) {
								found = true;
							}
						}
					}
				}

				@Override
				public void onEndElement(XmlInfo xml) {
				}

				@Override
				public void onText(XmlInfo xml) {
					if (found) {
						result.setResult(xml.getText());
						complete = true;
					}
				}

				@Override
				public boolean isComplete() {
					return complete;
				}
				
				private boolean found = false;
				private boolean complete = false;
				
			}, SEARCH_FILE);
		} catch(Exception e) {
			throw new RuntimeException();
		}
		
		return result.getResult();
	}
	
	public static void doit(Context context, String text, final Observer observer, final int limit) {
		final String[] words = text.split(" ");
		if (words.length == 0) {
			return;
		}
		if (words[0].equals("")) {
			return;
		}
		
		final String[] lowerWords = new String[words.length];
		for(int i = 0; i < words.length; i++) {
			lowerWords[i] = words[i].toLowerCase();
		}
		try {
			JavaXmlReader.loadXml(context, new XmlHandler() {

				@Override
				public void onStartElement(XmlInfo xml) {
					if (xml.getName().equals("link")) {
						id = xml.getValue("id");
					}
				}

				@Override
				public void onEndElement(XmlInfo xml) {
				}

				@Override
				public void onText(XmlInfo xml) {
					boolean allFound = true;
					String text = xml.getText();
					String lowerText = text.toLowerCase();
					for(String word: lowerWords) {
						if (!lowerText.contains(word)) {
							allFound = false;
							break;
						} else {
							lowerText = lowerText.substring(lowerText.indexOf(word) + word.length()); 
						}
					}
					
					if (allFound) {
						observer.onFound(Integer.parseInt(id), text, words);
						count++;
					}
				}

				@Override
				public boolean isComplete() {
					return (count >= limit);
				}
				
				private String id = null;
				private int count = 0;
				
			}, SEARCH_FILE);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
