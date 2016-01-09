package com.ntinside.docview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.ntinside.docmodel.DocCache;
import com.ntinside.docmodel.DocGroup;
import com.ntinside.docmodel.DocTools;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;


public class SearchResultView extends LinearLayout {

	public SearchResultView(Context context) {
		super(context);
		setOrientation(VERTICAL);
	}
	
	public void clear() {
		removeAllViews();
	}
	
	public void addResult(int linkId, String text, String[] words) {
		addView(new SearchListView(getContext(), new SearchListItem(linkId, text, words), schema));
	}
	
	public void invalidate() {
	}
	
	public int getResultsCount() {
		return getChildCount();
	}
	
	public void setColorSchema(Colors.ColorSchema schema) {
		this.schema = schema;
		for(int i = 0; i < getChildCount(); i++) {
			SearchListView v = (SearchListView) getChildAt(i);
			v.setColorSchema(schema);
		}
	}
	
	private Colors.ColorSchema schema = null;
	
	private static class SearchListItem {
		public SearchListItem(int linkId, String text, String[] words) {
			this.linkId = linkId;
			this.text = text;
			this.words = words;
		}
		
		public String[] getWords() {
			return words;
		}
		
		public String getText() {
			return text;
		}
		
		public int getLinkId() {
			return linkId;
		}
		
		private int linkId;
		private String text;
		private String[] words;
	}
	
	private static class SearchListView extends LinearLayout {
		public SearchListView(Context context, SearchListItem item, Colors.ColorSchema schema) {
			super(context);
			setOrientation(VERTICAL);
			padding();
			//setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			textView = new TextView(context);
			textViewLocation = new TextView(context);
			addView(textView);
			addView(textViewLocation);
			setItem(item);
			setColorSchema(schema);
		}
		
		public void setItem(final SearchListItem item) {
			String text = item.getText();
			ArrayList<ConcatInfo> concats = new ArrayList<ConcatInfo>();
			for(int i = 0; i < item.getWords().length; i++) {
				concats.add(new ConcatInfo(text, item.getWords()[i]));
			}
			sortByFirstPos(concats);
			concats = unionNearest(concats);
			
			String result = "";
			if (concats.get(0).prefixRequired()) {
				result = "...";
			}
			for(int j = 0; j < concats.size() - 1; j++) {
				result += text.substring(concats.get(j).firstPos(), concats.get(j).lastPos());
				result += "...";
			}
			
			result += text.substring(concats.get(concats.size()-1).firstPos(), 
									 concats.get(concats.size()-1).lastPos());
			if (concats.get(concats.size()-1).suffixRequired()) {
				result += "...";
			}

			for(String word: item.getWords()) {
				int index = result.toLowerCase().indexOf(word.toLowerCase());
				if (index >= 0) {
					String repl = result.substring(index, index + word.length());
					result = result.replace( repl, String.format("<font color='red'>%s</font>", repl));
				}
			}
			
			//String styledText = "This is <font color='red'>simple</font>.";
			textView.setText(Html.fromHtml(result), TextView.BufferType.SPANNABLE);
			
			DocGroup group = DocTools.findChapter(DocCache.getDoc(getContext()), item.getLinkId());
			if (group != null) {
				textViewLocation.setText( String.format("Õ‡È‰ÂÌÓ ‚: %s", group.getName()) );
			}
			
			setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					DocviewActivity.initFor(getContext(), item.getLinkId(), item.getLinkId());
					getContext().startActivity(new Intent(getContext(), DocviewActivity.class));
					
				}
			});
			//textView.setText( result );
		}
		
		public void setColorSchema(Colors.ColorSchema schema) {
			if (schema != null) {
				textView.setTextColor(schema.getFont—olor());
				textViewLocation.setTextColor(schema.getFont—olor());
				setBackgroundColor(schema.getBackgroundColor());
			}
		}
		
		@Override
		public boolean onTouchEvent(MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				setBackgroundDrawable(getContext().getResources().getDrawable(android.R.drawable.list_selector_background) );
				padding();
			} else if (event.getAction() == MotionEvent.ACTION_UP || 
					event.getAction() == MotionEvent.ACTION_CANCEL ) {
				setBackgroundDrawable(null);
				padding();
			}
			return super.onTouchEvent(event);
		}
		
		private void sortByFirstPos(ArrayList<ConcatInfo> concats) {
			 Collections.sort(concats, new Comparator<ConcatInfo>() {
				@Override
				public int compare(ConcatInfo arg0, ConcatInfo arg1) {
					if (arg0.firstPos() < arg1.firstPos()) {
						return -1;
					} else if (arg0.firstPos() > arg1.firstPos()) {
						return 1;
					} else {
						return 0;
					}
				}
			 });
		}
		
		private ArrayList<ConcatInfo> unionNearest(ArrayList<ConcatInfo> concats) {
			ArrayList<ConcatInfo> result = new ArrayList<ConcatInfo>();
			for(int i = 0; i < concats.size(); i++) {
				if (result.size() > 0) {
					ConcatInfo last = result.get(result.size() - 1);
					if (last.lastPos() + WIDTH >= concats.get(i).firstPos()) {
						last.setLastPos(concats.get(i).lastPos());
						last.setSuffixRequired(concats.get(i).suffixRequired());
					} else {
						result.add(concats.get(i));
					}
				}
				else {
					result.add(concats.get(i));
				}
			}
			return result;
		}
		
		private void padding() {
			setPadding(10, 8, 10, 8);
		}
		
		private TextView textView;
		private TextView textViewLocation;
		private static final int WIDTH = 40;
		
		private static class ConcatInfo {
			public ConcatInfo(String text, String word) {
				int index = text.toLowerCase().indexOf(word.toLowerCase());
				if (index <= 0) {
					// WTF ???
				}
				
				firstPos = 0;
				lastPos = text.length() - 1;
				
				if (index > WIDTH) {
					prefix = true;
					firstPos = index - WIDTH;
				}
				int w = text.length();
				if (firstPos + WIDTH*2 < w) {
					w = firstPos + WIDTH*2;
					suffix = true;
				}
				lastPos = w;
			}
			
			public boolean prefixRequired() {
				return prefix;
			}
			
			public boolean suffixRequired() {
				return suffix;
			}
			
			public int firstPos() {
				return firstPos;
			}
			
			public int lastPos() {
				return lastPos;
			}
			
			public void setLastPos(int lastPos) {
				this.lastPos = lastPos;
			}
			
			public void setSuffixRequired(boolean required) {
				this.suffix = required;
			}
			
			private int firstPos, lastPos;
			private boolean suffix = false, prefix = false;
		}
	}
}
