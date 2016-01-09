package com.ntinside.docmodel;
import android.content.Context;

public class DocCache {
	public static DocRoot getDoc(Context context) {
		if (root == null) {
			DocXmlHandler h = new DocXmlHandler();
			try {
				JavaXmlReader.loadXml(context, h, "files.mp3");
				root = h.getDocRoot();
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		return root;
	}
	
	public static String getVersion(Context context) {
		try {
			VersionXmlHander h = new VersionXmlHander();
			JavaXmlReader.loadXml(context, h, "version.mp3");
			return h.getVersion();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static DocRoot root = null;
}
