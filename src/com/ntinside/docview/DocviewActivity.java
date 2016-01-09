package com.ntinside.docview;

import java.util.List;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.ntinside.docmodel.DocGroup;
import com.ntinside.docmodel.DocNote;
import com.ntinside.docmodel.DocRoot;
import com.ntinside.docmodel.DocCache;
import com.ntinside.docmodel.DocSearch;
import com.ntinside.docmodel.DocTools;
import com.ntinside.docview.BookmarksStore.Bookmark;
import com.ntinside.docview.Colors.ColorSchema;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView.OnEditorActionListener;

public class DocviewActivity extends Activity {
	
	public static final String MY_AD_UNIT_ID = "a15191519a139e9";	// a14f748bd093d07

	public static void initFor(Context context, int linkId) {
		initFor(context, linkId, null);
	}
	
	public static void initFor(Context context, int linkId, Integer highlightedLinkId) {
		SharedPreferences prefs = context.getSharedPreferences(PREF, MODE_PRIVATE);
		saveCurrentLink(prefs, linkId);
		saveHighlightedLink(prefs, highlightedLinkId);
		NaviStore.reset(context);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		prefs = getSharedPreferences(PREF, MODE_PRIVATE);
		naviStore = new NaviStore(this);
		
		layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		docView = new Docview(this);
		docView.setListener(new Docview.Listener() {
			@Override
			public void onPageLoadComplete() {
				hideDialog();
			}

			@Override
			public void onUserScrollsToLink(int linkId) {
				saveCurrentLink(linkId);
			}

			@Override
			public void onUserClickToLink(int linkId) {
				//saveCurrentLink(linkId);
				navigate(linkId);
			}

			@Override
			public void onUserClickToNote(int linkId) {
				showNote(linkId);
			}

			@Override
			public void onUserLongClickToLink(int linkId, int x, int y) {
				onLongClick(linkId, x, y);
			}

			@Override
			public void onTouchEvent() {
				if (popup != null) {
					popup.dismiss();
					popup = null;
				}
			}
		});

		docView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
												 LayoutParams.FILL_PARENT));
		
		LinearLayout adLayout = new LinearLayout(this);
		adLayout.setOrientation(LinearLayout.VERTICAL);
		adLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		adLayout.setBackgroundColor(Color.TRANSPARENT);
		
		adView = new AdView(this, AdSize.BANNER, MY_AD_UNIT_ID);
		adLayout.addView(adView);
		
		layout.addView(adLayout);
		layout.addView(docView);
		setContentView(layout);
		initialLoad();
		adView.loadAd(new AdRequest());
	}
	
	private AdView adView;
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		initialLoad();
	}
	
	@Override
	protected void onDestroy() {
		if (adView != null) {
			adView.destroy();
		}
		hideDialog();
		super.onDestroy();
	}
	
	@Override
	protected void onPause() {
		hideDialog();
		super.onPause();
	}
	
	private void initialLoad() {
		reload();
	}
	
	private void reload() {
		ColorSchema schema = Colors.ColorSchemas[Colors.getColorSchema(this)];
		layout.setBackgroundColor(schema.getBackgroundColor());
		docView.setBackgroundColor(schema.getBackgroundColor());
		loadGroup(getGroup(DocCache.getDoc(this)));
	}
	
	private void navigate(int linkId) {
		naviStore.push(new NaviStore.ReturnPoint(loadCurrentLink(), 
												 loadHighlighedLink(), 
												 getTitle().toString()));
		saveCurrentLink(linkId);
		saveHighlightedLink(linkId);
		loadGroup(getGroup(DocCache.getDoc(this)));
	}
	
	private void showNote(int linkId) {
		DocRoot root = DocCache.getDoc(this);
		List<DocNote> notes = root.getNotes();
		for(int i = 0; i < notes.size(); i++) {
			DocNote note = notes.get(i);
			
			if (note.getPos() >= linkId) {
				Toast.makeText(this, note.getText(), Toast.LENGTH_LONG).show();
				return;
			}
		}
	}
	
	private void loadGroup(DocGroup group) {
		if (group != null) {
			showDialog();
			setTitle(group.getName());
			docView.loadFile(group, loadCurrentLink(), loadHighlighedLink(),
					Colors.ColorSchemas[Colors.getColorSchema(this)], loadFontSize() );
		} else {
			setTitle("");
			Toast.makeText(this, "Данные не найдены", Toast.LENGTH_LONG).show();
			docView.clear();
		}
	}

	private DocGroup getGroup(DocRoot root) {
		return DocTools.findChapter(root, loadCurrentLink());
	}
	
	private void showDialog() {
		progressDialog = new ProgressDialog(this);
		progressDialog.setCancelable(true);
		progressDialog.setTitle("Загрузка данных");
		progressDialog.setMessage("Пожалуйста подождите");
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.show();
	}
	
	private void hideDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
			progressDialog = null;
		}
	}
	
	private void saveCurrentLink(int linkId) {
		saveCurrentLink(prefs, linkId);
	}
	
	private void saveHighlightedLink(Integer linkId) {
		saveHighlightedLink(prefs, linkId);
	}
	
	private static void saveCurrentLink(SharedPreferences p, int linkId) {
		Editor ed = p.edit();
		ed.putInt(LINK_ID, linkId);
		ed.commit();
	}
	
	private static void saveHighlightedLink(SharedPreferences p, Integer linkId) {
		Editor ed = p.edit();
		ed.putInt(HIGHLIGHT_LINK_ID, (linkId != null) ? linkId : -1 );
		ed.commit();
	}
	
	private int loadFontSize() {
		return prefs.getInt(FONT_SIZE, 100);
	}
	
	private void saveFontSize(int size) {
		Editor ed = prefs.edit();
		ed.putInt(FONT_SIZE, size);
		ed.commit();
	}
	
	private int loadCurrentLink() {
		int link = prefs.getInt(LINK_ID, -1);
		return link;
	}
	
	private Integer loadHighlighedLink() {
		int val = prefs.getInt(HIGHLIGHT_LINK_ID, -1);
		return (val != -1) ? val : null;
	}
	
	private void revertTo(int count) {
		NaviStore.ReturnPoint pt = naviStore.pop(); 
		for(int i = 1; i <= count; i++) {
			pt = naviStore.pop();
		}
		if (pt != null) {
			saveCurrentLink(pt.getPosLink());
			saveHighlightedLink(pt.getActiveLink());
			loadGroup(getGroup(DocCache.getDoc(this)));			
		} else {
			finish();
		}
	}
	
	private boolean hasBookmark(int linkId) {
		for(Bookmark b : BookmarksStore.getBookmarks(this)) {
			if (linkId == b.getLinkId()) {
				return true;
			}
		}
		return false;
	}
	
	private void addBookmark(int linkId, String text, String name) {
		BookmarksStore.addBookmark(this, new Bookmark(linkId, text, name) );
		docView.highlightBookmark(linkId);
	}
	
	private void onBookmark(int linkId, boolean selected) {
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		
		alert.setTitle("Закладка");
		alert.setCancelable(true);
		
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(10, 0, 10, 0);
		
		TextView msg1 = new TextView(this);
		msg1.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		msg1.setText(
				selected ? "Закладка будет установлена на следующий абзац:" :
				"Закладка будет установлена на абзац, находящийся вверху экрана:");
		layout.addView(msg1);
		
		final int link = linkId;
		String article = DocSearch.getTextFromLink(this, link);
		if (article == null) {
			return;
		}
		if (article.length() > BOOKMARK_TEXT_LENGTH) {
			article = article.substring(0, BOOKMARK_TEXT_LENGTH);
			article += "...";
		}
		final String bookmarkText = article;
		
		TextView msg3 = new TextView(this);
		msg3.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		msg3.setTextAppearance(this, android.R.style.TextAppearance_Medium);
		msg3.setText(article);
		layout.addView(msg3);
		
		final EditText edit = new EditText(this);
		edit.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		edit.setHint("Примечание");
		edit.setMaxLines(1);
		edit.setSingleLine();
		edit.setImeOptions(EditorInfo.IME_ACTION_DONE);
		edit.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					addBookmark(link, bookmarkText, edit.getText().toString());
					if (dialog != null) {
						dialog.dismiss();
					}
				}
				return false;
			}
		});
		layout.addView(edit);
		
		alert.setView(layout);
		
		alert.setPositiveButton("Закладка", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				addBookmark(link, bookmarkText, edit.getText().toString());
			}
		});
		alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Cancel.
			}
		});
		
		dialog = alert.show();
	}
	
	private void onBookmarkRemove(int linkId) {
		
		BookmarksStore.removeBookmarkByLinkId(this, linkId);
		docView.removeBookmark(linkId);
	}
	
	@Override
	public void onBackPressed() {
		revertTo(0);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (!super.onPrepareOptionsMenu(menu))
			return false;
		
		MenuItem item = menu.findItem(IDM_FONT_SIZE_BASE + loadFontSize());
		if (item != null) {
			item.setChecked(true);
		}
		
		Colors.MenuHelper.prepareMenu(menu, this);
		
		for(int i = 0; i < NaviStore.MAX_RETURN_POINTS; i++ ) {
			menu.findItem(IDM_BACK_BASE + i).setVisible(false);
		}
		NaviStore.ReturnPoint[] points = naviStore.getAllReturnPoins();
		for(int idx = 0; idx < points.length; idx++) {
			MenuItem backItem = menu.findItem(IDM_BACK_BASE + idx);
			backItem.setVisible(true);
			backItem.setTitle("назад к: " +
								points[idx].getChapterName());
		}
		
		boolean b = hasBookmark(loadCurrentLink());
		menu.findItem(IDM_BOOKMARK).setVisible(!b);
		menu.findItem(IDM_BOOKMARK_REMOVE).setVisible(b);
		
		return true;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!super.onCreateOptionsMenu(menu))
			return false;
		
		SubMenu fontSize = menu.addSubMenu("Размер шрифта");
		Colors.MenuHelper.createMenu(menu);
		
		for(int i = MIN_FONT_SIZE; i <= MAX_FONT_SIZE; i += 10) {
			fontSize.add(IDMG_FONT_SIZE, IDM_FONT_SIZE_BASE + i, 
					Menu.NONE, String.format("%d%%", i));
		}
		
		fontSize.setGroupCheckable(IDMG_FONT_SIZE, true, true);
	
		SubMenu back = menu.addSubMenu("Назад");
		for(int i = 0; i < NaviStore.MAX_RETURN_POINTS; i++ ) {
			back.add(Menu.NONE, IDM_BACK_BASE + i, Menu.NONE, "").setVisible(false);
		}
		back.add(Menu.NONE, IDM_BACK_ACTIVITY, Menu.NONE, "К выбору раздела");
		
		menu.add(Menu.NONE, IDM_BOOKMARK, Menu.NONE, "Закладка");
		menu.add(Menu.NONE, IDM_BOOKMARK_REMOVE, Menu.NONE, "Удалить закладку");
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		if (item == null)
			return false;
		
		int id = item.getItemId();
		if ((id >= (IDM_FONT_SIZE_BASE + MIN_FONT_SIZE)) && 
			(id <= (IDM_FONT_SIZE_BASE + MAX_FONT_SIZE))) {
			int fontSize = id - IDM_FONT_SIZE_BASE;
			saveFontSize(fontSize);
			reload();
		}
		
		if (Colors.MenuHelper.isColorClick(item, this)) {
			reload();
		}
		
		if (id == IDM_BACK_ACTIVITY) {
			finish();
		}
		for(int i = 0; i < NaviStore.MAX_RETURN_POINTS; i++) {
			if (id == (IDM_BACK_BASE + i)) {
				revertTo(id - IDM_BACK_BASE);
			}
		}
		
		if (id == IDM_BOOKMARK) {
			onBookmark(loadCurrentLink(), false);
		}
		if (id == IDM_BOOKMARK_REMOVE) {
			onBookmarkRemove(loadCurrentLink());
		}

		return true;
	}
	
	private void onLongClick(final int linkId, int x, int y) {
		if (popup != null) {
			popup.dismiss();
			popup = null;
		}
		
		final boolean bookmarked = hasBookmark(linkId);
		Button b = new Button(this);
		b.setText( bookmarked ? "Удалить закладку" : "Закладка");
		b.setFocusable(true);
		b.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 
	    											  ViewGroup.LayoutParams.WRAP_CONTENT));
		b.measure(ViewGroup.LayoutParams.WRAP_CONTENT, 
				  ViewGroup.LayoutParams.WRAP_CONTENT);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (popup != null) {
					popup.dismiss();
					popup = null;
				}
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (bookmarked) {
							onBookmarkRemove(linkId);
						} else {
							onBookmark(linkId, true);
						}
					}
				});
				
			}
		});
		
		popup = new PopupWindow(b, b.getMeasuredWidth(), b.getMeasuredHeight());
		popup.setTouchable(true);
		popup.setOutsideTouchable(true);
		popup.showAtLocation(docView, Gravity.NO_GRAVITY, x, y);
		
		/*Toast.makeText(DocviewActivity.this, 
				String.format("longClick: %d", linkId), 
				Toast.LENGTH_SHORT).show();*/
	}
	
	private LinearLayout layout;
	private Docview docView;
	private ProgressDialog progressDialog = null;
	private static final String PREF = "docview";
	private static final String LINK_ID = "linkId"; 
	private static final String HIGHLIGHT_LINK_ID = "highlightLinkId";
	private static final String FONT_SIZE = "fontSize";
	private SharedPreferences prefs;
	private NaviStore naviStore;
	
	private static final int IDM_FONT_SIZE_BASE = 1000;
	private static final int IDM_BACK_BASE = 3000;
	private static final int IDM_BACK_ACTIVITY = 3500;
	private static final int IDMG_FONT_SIZE = 0;
	private static final int IDM_BOOKMARK = 4000;
	private static final int IDM_BOOKMARK_REMOVE = 4001;
	
	private static final int MIN_FONT_SIZE = 70;
	private static final int MAX_FONT_SIZE = 160;
	
	private static final int BOOKMARK_TEXT_LENGTH = 50;
	private AlertDialog dialog = null;
	private PopupWindow popup = null;
}
