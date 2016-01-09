package com.ntinside.docmodel;

import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import android.content.Context;

public class JavaXmlReader {

	public static void loadXml(Context context, XmlHandler handler, String filename) 
			throws XmlPullParserException, IOException {
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        final XmlPullParser xpp = factory.newPullParser();

        InputStream stream = context.getAssets().open(filename);
        try {
	        xpp.setInput( stream, "UTF-8" );

	        XmlHandler.XmlInfo xmlInfo = new XmlHandler.XmlInfo() {
				@Override
				public String getName() {
					return xpp.getName();
				}

				@Override
				public String getValue(String name) {
					return xpp.getAttributeValue("", name);
				}

				@Override
				public String getText() {
					return xpp.getText();
				}
	        };
	        
	        int eventType = xpp.getEventType();
	        while (eventType != XmlPullParser.END_DOCUMENT && !handler.isComplete()) {
	        	if (eventType == XmlPullParser.END_TAG) {
	        		handler.onEndElement(xmlInfo);
	        	} else if (eventType == XmlPullParser.TEXT) {
	        		handler.onText(xmlInfo);
	        	} else if (eventType == XmlPullParser.START_TAG) {
	        		handler.onStartElement(xmlInfo);
	        	}
	        	eventType = xpp.next();
	        }
        } finally {
        	stream.close();
        }
	}
}
