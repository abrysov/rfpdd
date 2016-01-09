package com.ntinside.docview;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.widget.Toast;

public class BookmarksStore {

	public static void addBookmark(Context context, Bookmark bookmark) {
		Store s = new Store(context);
		List<Bookmark> b = s.load();
		b.add(bookmark);
		s.save(b);
	}
	
	public static List<Bookmark> getBookmarks(Context context) {
		Store s = new Store(context);
		return s.load();
	}
	
	public static void removeBookmark(Context context, int index) {
		Store s = new Store(context);
		List<Bookmark> b = s.load();
		if (b.size() < index) {
			b.remove(index);
		}
		s.save(b);
	}
	
	public static int removeAllBookmarks(Context context) {
		Store s = new Store(context);
		List<Bookmark> b = s.load();
		List<Bookmark> n = new ArrayList<Bookmark>();
		s.save(n);
		return b.size();
	}
	
	public static void removeBookmarkByLinkId(Context context, int linkId) {
		Store s = new Store(context);
		List<Bookmark> b = s.load();
		for(int i = 0; i < b.size(); i++) {
			if (b.get(i).getLinkId() == linkId) {
				b.remove(i);
				break;
			}
		}
		s.save(b);
	}
	
	public static class Bookmark {
		public Bookmark(int linkId, String text, String name) {
			this.linkId = linkId;
			this.text = text;
			this.name = name;
		}
		
		public int getLinkId() {
			return linkId;
		}
		
		public String getText() {
			return text;
		}
		
		public String getName() {
			return name;
		}
		
		private int linkId;
		private String text;
		private String name;
	}
	
	private static class Store {
		public Store(Context context) {
			this.context = context;
			this.prefs = context.getSharedPreferences(BOOKMARKS, Context.MODE_PRIVATE);
		}
		
		public void save(List<Bookmark> bookmarks) {
			if (bookmarks.size() > MAX_BOOKMARKS) {
				Toast.makeText(context, 
						String.format("ѕревышено максимально количество сохран€емых закладок (%d). «акладка не была сохранена", MAX_BOOKMARKS), 
						Toast.LENGTH_LONG).show();
				return;
			}
			
			Editor ed = prefs.edit();
			for(int i = 0; i < bookmarks.size(); i++) {
				Bookmark b = bookmarks.get(i);
				ed.putInt(String.format(BOOKMARK_LINKID, i), b.getLinkId());
				ed.putString(String.format(BOOKMARK_TEXT, i), b.getText());
				ed.putString(String.format(BOOKMARK_NAME, i), b.getName());
			}
			
			ed.putInt(String.format(BOOKMARK_LINKID, bookmarks.size()), -1);
			ed.commit();
		}
		
		public List<Bookmark> load() {
			List<Bookmark> ret = new ArrayList<Bookmark>();
			for(int i = 0; i < MAX_BOOKMARKS; i++) {
				int linkId = prefs.getInt(String.format(BOOKMARK_LINKID, i), -1);
				if (linkId < 0) {
					break;
				}
				String text = prefs.getString(String.format(BOOKMARK_TEXT, i), "");
				String name = prefs.getString(String.format(BOOKMARK_NAME, i), "");
				ret.add(new Bookmark(linkId, text, name));
			}
			return ret;
		}
		
		
		
		private Context context;
		private SharedPreferences prefs;
		private static final String BOOKMARKS = "bookmarks";
		private static final int MAX_BOOKMARKS = 50;
		private static final String BOOKMARK_NAME = "bookmark_name_%d";
		private static final String BOOKMARK_LINKID = "bookmark_linkid_%d";
		private static final String BOOKMARK_TEXT = "bookmark_text_%d";
	}
}
