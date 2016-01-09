package com.ntinside.docview;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;

import com.ntinside.docmodel.DocGroup;
import com.ntinside.docview.BookmarksStore.Bookmark;
import com.ntinside.docview.Colors.ColorSchema;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

public class Docview extends WebView {

	public interface Listener {
		public void onPageLoadComplete();
		public void onUserScrollsToLink(int linkId);
		public void onUserClickToLink(int linkId);
		public void onUserClickToNote(int linkId);
		public void onUserLongClickToLink(int linkId, int x, int y);
		public void onTouchEvent();
	}
	
	public static class JavaScriptInterface {
		public JavaScriptInterface(Docview context) {
			this.context = context;
		}
		
		private Docview context;
		
		// JS interface
		public void setSelectedLink(String link) {
			if (!String.format("%s", link).equals("")) {
				context.onScrolledToLink(link);
			}
		}
		
		public void onElementLookuped(String link) {
			if (!String.format("%s", link).equals("")) {
				context.onElementLookuped(link);
			}
		}
	}
	
	public Docview(Context context) {
		super(context);
		
		getSettings().setJavaScriptEnabled(true);
		
		setWebViewClient(new WebViewClient() {
			@Override 
	        public boolean shouldOverrideUrlLoading(WebView view, String url) {
				onLinkClick(url);
				return true;
			}
			
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				onPageLoadComplete();
			}
		});
		
		setVerticalScrollBarEnabled(true);
	    addJavascriptInterface(new JavaScriptInterface(this), "Java");
	    
	    setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				onUserLongClick();
				return false;
			}
		});
	}
	
	
	public void loadFile(DocGroup group, int goLink, Integer highlightLink,
						ColorSchema schema, int fontSize ) {
		this.goLink = goLink;
		this.highlightLink = highlightLink;
		this.docGroup = group;
		
		try {
			StringBuilder builder = new StringBuilder();
			
			InputStream stream = getContext().getAssets().open(group.getFilename());
			BufferedInputStream bis = new BufferedInputStream(stream);
			
			byte[] data = new byte[bis.available()];
			bis.read(data);
			bis.close();
			stream.close();
			
			
			builder.append("<html><body>");
			builder.append(getCSS(schema, fontSize));
			builder.append(new String(data, "UTF-8"));
			builder.append(getJavascript());
			builder.append("</body></html>");
			
			String html = builder.toString();
		    loadDataWithBaseURL(null, html, "text/html", "utf-8", "about:blank");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void clear() {
		loadDataWithBaseURL(null, "", "text/html", "utf-8", "about:blank");
	}
	
	public void setListener(Listener listener) {
		this.listener = listener;
	}
	
	public void highlightBookmark(int linkId) {
		String jsCall = String.format("javascript:highlightBookmark('link_%d');", linkId);
		loadUrl(jsCall);
	}
	
	public void removeBookmark(int linkId) {
		String jsCall = String.format("javascript:removeBookmark('link_%d');", linkId);
		loadUrl(jsCall);
	}
	
	private void onLinkClick(String url) {
		if (url.startsWith("link_")) {
			int clickLink = Integer.parseInt(url.replace("link_", ""));
			if (listener != null) {
				listener.onUserClickToLink(clickLink);
			}
			return;
		}
		if (url.startsWith("note_")) {
			int clickNote = Integer.parseInt(url.replace("note_", ""));
			if (listener != null) {
				listener.onUserClickToNote(clickNote);
			}
			return;
		}
	}

	private void onPageLoadComplete() {
		post(new Runnable() {
			@Override
			public void run() {
				String jsCall;
				
				if (highlightLink != null) {
					jsCall = String.format("javascript:highlightLink('link_%d');", highlightLink);
					loadUrl(jsCall);
				}
				
				for(Bookmark b : BookmarksStore.getBookmarks(getContext())) {
					int linkId = b.getLinkId();
					if ((docGroup.getFirst() <= linkId) && 
						(linkId <= docGroup.getLast())) {
						highlightBookmark(linkId);
					}
				}
				
				jsCall = String.format("javascript:goLink('link_%d');", goLink);
				loadUrl(jsCall);
				
				if (listener != null) {
					listener.onPageLoadComplete();
				}
			}
		});
	}
	
	private void onScrolledToLink(String link) {
		if (link.startsWith("link_")) {
			int currentLink = Integer.parseInt(link.replace("link_", ""));
			if (goLink != currentLink) {
				goLink = currentLink;
				if (listener != null) {
					listener.onUserScrollsToLink(goLink);
				}
			}
		}
	}
	
	private String getCSS(ColorSchema schema, int fontSize) {
		String link = String.format("#%s", Colors.asString(schema.getLinkColor()));
		String note = String.format("#%s", Colors.asString(schema.getNoteColor()));
		String font = String.format("#%s", Colors.asString(schema.getFontÑolor()));
		String bg = String.format("#%s", Colors.asString(schema.getBackgroundColor()));
		
		return "<style>" +
			".active {padding-left: 5px; margin: 0; border-left: 5px solid " + link + "; display: block; }" +
			".bookmark {padding-right: 5px; margin: 0; border-right: 5px solid #ff0000; display: block; }" +
			".link {color: " + link + ";}" +
			".note {color: " + note + ";}" +
			"body {color: " + font + "; background: " + bg + "; font-size: " + String.format("%d%%", fontSize)  + ";}" +
			"</style>";
	}
	
	private String getJavascript() {
		return "<script type=\"text/javascript\">" +
				"function goLink(linkId) {" +
				"   var obj = document.getElementById(linkId); " +
				"   if (obj != null) { window.scrollTo(0, obj.offsetTop); }" +
				"}" +
				"" +
				"function highlightLink(linkId) {" +
				"   var obj = document.getElementById(linkId); " +
				"   if (obj != null) { " +
				"       obj.className = obj.className + ' active'; " +
				"	}" +
				"}" +
				"" +
				"function highlightBookmark(linkId) {" +
				"   var obj = document.getElementById(linkId); " +
				"   if (obj != null) { " +
				"       obj.className = obj.className + ' bookmark'; " +
				"	}" +
				"}" +
				"" +
				"function removeBookmark(linkId) {" +
				"   var obj = document.getElementById(linkId); " +
				"   if (obj != null) { " +
				"       var cl = obj.className ? obj.className : '';" +
				"       cl = cl.replace('bookmark', '');  " +
				"       obj.className = cl; " +
				"	}" +
				"}" +
				"" +
				"function lookupElement(x, y) {" +
				"	var element = document.elementFromPoint(x, y);" +
				"   if (element != null) { " +
				"      if (!element.id) {element = element.parentNode;} " +
				"      if (!element.id) {element = element.parentNode;} " +
				"      if (element.id) {" +
				"          Java.onElementLookuped(element.id);}" +
				"   }" +
				"}" +
				"" +
				"function saveCurrentPos() {" +
				"	var element = document.elementFromPoint(30, 15);" + //window.pageYOffset
				"   if (element != null) { " +
				"      if (!element.id) {element = element.parentNode;} " +
				"      if (element.id) {" +
				"          Java.setSelectedLink(element.id);}" +
				"   }" +
				"}" +
				"" +
				"window.onscroll = function() { saveCurrentPos(); };" +
				"" +
				"function onLinkClick(elid) {" +
				"  Java.setSelectedLink(elid);" + 
				"  return true;" +
				"}" +
				"</script>";
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_DOWN || 
			ev.getAction() == MotionEvent.ACTION_CANCEL) {
			if (listener != null) {
				listener.onTouchEvent();
			}
		}
		if (ev.getAction() == MotionEvent.ACTION_DOWN || 
			ev.getAction() == MotionEvent.ACTION_MOVE) {
			x = (int) ev.getX();
			y = (int) ev.getY();
		}
		
		return super.onTouchEvent(ev);
	}
	
	private void onUserLongClick() {
		String jsCall = String.format("javascript:lookupElement(%d, %d);", x, y);
		loadUrl(jsCall);
	}
	
	private void onElementLookuped(String link) {
		if (link.startsWith("link_")) {
			int linkId = Integer.parseInt(link.replace("link_", ""));
			if (listener != null) {
				listener.onUserLongClickToLink(linkId, x, y);
			}
		}
	}
	
	private int goLink;
	private Integer highlightLink = null;
	private Listener listener = null;
	private DocGroup docGroup = null;
	private int x = 0, y = 0;
}
