package com.ntinside.docview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ntinside.docview.BookmarksStore.Bookmark;

import android.content.Context;
import android.graphics.Color;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class BookmarksView extends ListView {

	public interface Listener {
		public void onBookmarkClick(Bookmark bookmark);
	}
	
	public BookmarksView(Context context) {
		super(context);
		setDrawSelectorOnTop(false);
		setCacheColorHint(Color.TRANSPARENT);
		setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (listener != null) {
					@SuppressWarnings("unchecked")
					Map<String, ?> item = (Map<String, ?>) getAdapter().getItem(arg2);
					Bookmark bookmark = (Bookmark) item.get("bookmark");
					listener.onBookmarkClick(bookmark);
				}
			}
		});
	}
	
	public void setListener(Listener listener) {
		this.listener = listener;
	}
	
	public void loadBoomarks() {
		list = new ArrayList<Map<String, ?>>();
		fillList();
		setupAdapter();
		
	}
	
	public void reloadBookmarks() {
		SimpleAdapter adapter = (SimpleAdapter) getAdapter();
		list.clear();
		fillList();
		
		if (adapter != null) {
			adapter.notifyDataSetChanged();
		}
	}
	
	public void fillContextMenu(ContextMenu menu, ContextMenuInfo menuInfo) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
		int idx = info.position;
		menu.setHeaderTitle("«‡ÍÎ‡‰Í‡");
		menu.add(GROUP_BOOKMARK_DELETE, ITM_BOOKMARK_DELETE + idx, Menu.NONE, "”‰‡ÎËÚ¸");
		
	}
	
	public void onContextItem(MenuItem item) {
		if (item.getGroupId() == GROUP_BOOKMARK_DELETE) {
			int idx = item.getItemId() - ITM_BOOKMARK_DELETE;
			Map<String, ?> listItem = (Map<String, ?>) list.get(idx);
			Bookmark bookmark = (Bookmark) listItem.get("bookmark");
			BookmarksStore.removeBookmarkByLinkId(getContext(), bookmark.getLinkId());
			reloadBookmarks();
		}
	}
	
	private void setupAdapter() {
		SimpleAdapter adapter = new SimpleAdapter(getContext(), 
        		list, 
        		android.R.layout.simple_list_item_2, 
                new String[] {"title", "description"}, 
                new int[] {android.R.id.text1, android.R.id.text2}) {
			
			 	@Override
		        public View getView(int position, View convertView, ViewGroup parent) {
		            View view =super.getView(position, convertView, parent);
		            TextView textView=(TextView) view.findViewById(android.R.id.text1);
		            textView.setTextColor(schema.getFont—olor());
		            textView=(TextView) view.findViewById(android.R.id.text2);
		            textView.setTextColor(schema.getFont—olor());
		            return view;
		        }
		};
		setAdapter(adapter);
	}
	
	private void fillList() {
		List<Bookmark> bookmarks = BookmarksStore.getBookmarks(getContext());
		Collections.sort(bookmarks, new Comparator<Bookmark>() {
			@Override
			public int compare(Bookmark arg0, Bookmark arg1) {
				if (arg0.getLinkId() < arg1.getLinkId()) {
					return -1;
				} else if (arg0.getLinkId() > arg1.getLinkId()) {
					return 1;
				}
				return 0;
			}
		});
		
		try {
			//List<Map<String, ?>> items = new ArrayList<Map<String, ?>>();
			
			for(Bookmark bookmark: bookmarks) {
				Map<String, Object> map = new HashMap<String, Object>();
		        map.put("title", bookmark.getText());
		        map.put("description", bookmark.getName().equals("") ? "" : "œËÏÂ˜‡ÌËÂ: " + bookmark.getName() );
		        map.put("bookmark", bookmark);
		        list.add(map);
			}

			//return items;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void setColorSchema(Colors.ColorSchema schema) {
		this.schema = schema;
		setBackgroundColor(schema.getBackgroundColor());
		SimpleAdapter adapter = (SimpleAdapter) getAdapter();
		if (adapter != null)
    		adapter.notifyDataSetChanged();
	}

	private Colors.ColorSchema schema;
	private Listener listener = null;
	private List<Map<String, ?>> list = null;
	private static int ITM_BOOKMARK_DELETE = 100;
	private static int GROUP_BOOKMARK_DELETE = 100;
}
