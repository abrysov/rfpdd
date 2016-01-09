package com.ntinside.docview;

import com.ntinside.docmodel.DocSearch;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class SearchDocView extends LinearLayout {

	public interface Listener {
		public void onStartSearch();
		public void onEndSearch();
	}
	
	public SearchDocView(Context context) {
		super(context);
		prefs = context.getSharedPreferences(QUERY, Context.MODE_PRIVATE);
		
		setOrientation(VERTICAL);
		setLayoutParams( new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT) );
		
		LinearLayout hl = new LinearLayout(context);
		hl.setOrientation(HORIZONTAL);
		hl.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		hl.setPadding(10, 0, 10, 0);
		
		edit = new EditText(context);
		edit.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1f));
		edit.setHint("Ключевые слова");
		edit.setMaxLines(1);
		edit.setSingleLine();
		edit.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
		edit.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if ((actionId == EditorInfo.IME_ACTION_DONE) ||
					(actionId == EditorInfo.IME_ACTION_SEARCH)	) {
					onSearchClick();
				}
				return false;
			}
		});
		
		String oldText = prefs.getString(QUERY, "");
		edit.setText(oldText);
		
		edit.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				Editor ed = prefs.edit();
				String query = s.toString();
				ed.putString(QUERY, query);
				ed.commit();
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		hl.addView(edit);
		
		button = new Button(context);
		button.setText("Найти");
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onSearchClick();
			}
		});
		hl.addView(button);
		
		addView(hl);
		
		hint = new TextView(context);
		hint.setText("Нажмите кнопку чтобы повторить Поиск");
		hint.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		hint.setPadding(10, 0, 10, 0);
		if (!oldText.equals("")) {
			addView(hint);
		}
		
		ScrollView sv = new ScrollView(context);
		sv.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		searchResult = new SearchResultView(context);
		searchResult.setLayoutParams( new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		sv.addView(searchResult);
		addView(sv);
	}
	
	public void setColorSchema(Colors.ColorSchema schema) {
		setBackgroundColor(schema.getBackgroundColor());
		searchResult.setColorSchema(schema);
		hint.setTextColor(schema.getFontСolor());
	}
	
	public void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
	}
	
	public void setListener(Listener listener) {
		this.listener = listener;
	}
	
	private void onSearchClick() {
		removeView(hint);
		hideKeyboard();
		
		if (listener != null) {
			listener.onStartSearch();
		}
		
		searchResult.clear();
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				doSearch(edit.getText().toString());
				
				post(new Runnable() {
					@Override
					public void run() {
						if (listener != null) {
							listener.onEndSearch();
						}
						
						if (searchResult.getResultsCount() >= LIMIT) {
							Toast.makeText(getContext(), 
									String.format("Количество результатов поиска превышает %d. Уточните ключевые слова", LIMIT), 
									Toast.LENGTH_SHORT).show();
						}
					}
				});
			}
		}).start();
	}
	
	
	
	private void doSearch(String text) {
		//Toast.makeText(getContext(), "onSearchClick", Toast.LENGTH_SHORT).show();
		DocSearch.doit(getContext(), text, new DocSearch.Observer() {
			@Override
			public void onFound(final int linkId, final String text, final String[] words) {
				post(new Runnable() {
					@Override
					public void run() {
						searchResult.addResult(linkId, text, words);
					}
				});
			}
		}, LIMIT);
	}

	private SharedPreferences prefs;
	private static final String QUERY = "searchQuery";
	private EditText edit;
	private SearchResultView searchResult;
	private TextView hint;
	private Button button;
	private Listener listener = null;
	private static int LIMIT = 100;
}
