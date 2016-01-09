package com.ntinside.docview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.ntinside.docmodel.DocGroup;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class ContentsView extends ListView {

	public interface Listener {
		public void onGroupClick(DocGroup group);
	}
	
	public ContentsView(Context context) {
		super(context);
		init();
	}
	
	public ContentsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public void setGroups(List<DocGroup> groups) {
		setupAdapter(groups);
	}
	
	public void setListener(Listener listener) {
		this.listener = listener;
	}
	
	private void init() {
		setDrawSelectorOnTop(false);
		setCacheColorHint(Color.TRANSPARENT);
		setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (listener != null) {
					@SuppressWarnings("unchecked")
					Map<String, ?> item = (Map<String, ?>) getAdapter().getItem(arg2);
					DocGroup group = (DocGroup) item.get("group");
					listener.onGroupClick(group);
				}
			}
		});
	}
	
	private void setupAdapter(List<DocGroup> groups) {
		SimpleAdapter adapter = new SimpleAdapter(getContext(), 
        		createList(groups), 
        		android.R.layout.simple_list_item_2, 
                new String[] {"title", "description"}, 
                new int[] {android.R.id.text1, android.R.id.text2}) {
			
			 	@Override
		        public View getView(int position, View convertView, ViewGroup parent) {
		            View view =super.getView(position, convertView, parent);
		            TextView textView=(TextView) view.findViewById(android.R.id.text1);
		            textView.setTextColor(schema.getFont—olor());
		            return view;
		        }
		};
		setAdapter(adapter);
	}
	
	private List<Map<String, ?>> createList(List<DocGroup> groups) {
		try {
			List<Map<String, ?>> items = new ArrayList<Map<String, ?>>();
			
			for(DocGroup group: groups) {
				Map<String, Object> map = new HashMap<String, Object>();
		        map.put("title", group.getName());
		        map.put("description", "");
		        map.put("group", group);
		        items.add(map);
			}

			return items;
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
}
