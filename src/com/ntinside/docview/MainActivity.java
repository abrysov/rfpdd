package com.ntinside.docview;

import java.util.List;

import com.ntinside.docmodel.DocCache;
import com.ntinside.docmodel.DocGroup;
import com.ntinside.docview.BookmarksStore.Bookmark;
import com.ntinside.docview.BookmarksView.Listener;
import com.ntinside.rfpdd.R;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabWidget;
import android.widget.TextView;

public class MainActivity extends TabActivity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		if (!isTaskRoot()) {
			finish();
			return;
			//startActivity(new Intent(this, ContentActivity.class));
		}
		
		MigrateMgr.doMigrate(this);
		
		host = new TabHost(this, null);
		host.setId(android.R.id.tabhost);
		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		host.addView(ll);
		
		TabWidget tabWidget = new TabWidget(this);
		tabWidget.setId(android.R.id.tabs);
		ll.addView(tabWidget);
		
		frame = new FrameLayout(this);
		frame.setId(android.R.id.tabcontent);
		frame.setLayoutParams( new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		ll.addView(frame);
		
		contents = new ContentsView(this);
		contents.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		//frame.addView(contents);
		
		versionLayout = new LinearLayout(this);
		versionLayout.setOrientation(LinearLayout.VERTICAL);
		versionLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		
		version = new TextView(this);
		version.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		version.setText(DocCache.getVersion(this));
		version.setTextAppearance(this, android.R.style.TextAppearance_Medium);
		version.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
		versionLayout.addView(version);
		
		Button ticketButton = new Button(this);
		ticketButton.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		ticketButton.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
		ticketButton.setText("Проверь свои знания ПДД!\nВсе билеты по ПДД бесплатно");
		ticketButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onTicketsClick();
			}
		});
		versionLayout.addView(ticketButton);
		
		
		searchView = new SearchDocView(this);
		searchView.setListener( new SearchDocView.Listener() {
			
			@Override
			public void onStartSearch() {
				if (progressDialog != null) {
					progressDialog.dismiss();
				}
			
				progressDialog = new ProgressDialog(MainActivity.this);
				progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				progressDialog.setCancelable(false);
				progressDialog.setTitle("Идет поиск");
				progressDialog.setMessage("Пожалуйста подождите");
				progressDialog.show();
			}
			
			@Override
			public void onEndSearch() {
				if (progressDialog != null) {
					progressDialog.dismiss();
					progressDialog = null;
				}
				
			}
		});
		
		bookmarksView = new BookmarksView(this);
		
		setContentView(host);
		
		host.setOnTabChangedListener(new OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabId) {
				searchView.hideKeyboard();
			}
		});
		
		createTabs(host);
		updateColors();
		
		contents.setGroups(DocCache.getDoc(this).getGroups());
		contents.setListener(new ContentsView.Listener() {
			@Override
			public void onGroupClick(DocGroup group) {
				if (group.getFilename() == null) {
					Intent intent = new Intent(MainActivity.this, ContentActivity.class);
					intent.putExtra(ContentActivity.LINK_ID, group.getFirst() );
					startActivity(intent);
				} else {
					DocviewActivity.initFor(MainActivity.this, group.getFirst());
					Intent intent = new Intent(MainActivity.this, DocviewActivity.class);
					startActivity(intent);
				}
			}
		});
		
		bookmarksView.loadBoomarks();
		bookmarksView.setListener(new Listener() {
			@Override
			public void onBookmarkClick(Bookmark bookmark) {
				DocviewActivity.initFor(MainActivity.this, bookmark.getLinkId());
				Intent intent = new Intent(MainActivity.this, DocviewActivity.class);
				startActivity(intent);
			}
		});
		registerForContextMenu(bookmarksView);
	}
	
	private void createTabs(TabHost host) {
		Drawable icon = null;
		
		icon = getResources().getDrawable(R.drawable.ic_tab_contents);		
		host.addTab(host.newTabSpec("Contents").setIndicator("Разделы", icon).setContent(new TabContentFactory() {
			@Override
			public View createTabContent(String tag) {
				return contents;
			}
		}));
		
		icon = getResources().getDrawable(R.drawable.ic_tab_bookmarks);
		host.addTab(host.newTabSpec("Bookmarks").setIndicator("Закладки", icon).setContent(new TabContentFactory() {
			@Override
			public View createTabContent(String tag) {
				return bookmarksView;
			}
		}));
		
		icon = getResources().getDrawable(R.drawable.ic_tab_search);
		host.addTab(host.newTabSpec("Search").setIndicator("Поиск", icon).setContent(new TabContentFactory() {
			@Override
			public View createTabContent(String tag) {
				return searchView;
			}
		}));
		
		icon = getResources().getDrawable(R.drawable.ic_tab_version);
		host.addTab(host.newTabSpec("About").setIndicator("Версия", icon).setContent(new TabContentFactory() {
			@Override
			public View createTabContent(String tag) {
				return versionLayout;
			}
		}));
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	if (!super.onCreateOptionsMenu(menu)) {
    		return false;
    	}
		Colors.MenuHelper.createMenu(menu);
    	return true;
    }
    
    @Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (!super.onPrepareOptionsMenu(menu))
			return false;
		Colors.MenuHelper.prepareMenu(menu, this);
		return true;
    }
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		if (Colors.MenuHelper.isColorClick(item, this)) {
			updateColors();
		}
		return true;
    }
    
    @Override
    protected void onResume() {
    	bookmarksView.reloadBookmarks();
    	updateColors();
    	super.onResume();
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	super.onCreateContextMenu(menu, v, menuInfo);
    	bookmarksView.fillContextMenu(menu, menuInfo);
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	bookmarksView.onContextItem(item);
    	return super.onContextItemSelected(item);
    }
    
    private void onTicketsClick() {
		try {
			PackageManager pm = getPackageManager();
			ApplicationInfo ai = pm.getApplicationInfo("com.ntinside.droidpdd2012", 0);
			Intent intent = pm.getLaunchIntentForPackage(ai.packageName);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			
			if (!isCallable(intent)) {
				throw new RuntimeException("Not installed");
			}
			startActivity(intent);
				
		} catch (Exception e) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Приложение \"Билеты ПДД\" не установлено. Перейти в Google Play?")
                   .setCancelable(false)
                   .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                    	   Intent intent = new Intent(Intent.ACTION_VIEW);
                    	   intent.setData(Uri.parse("market://details?id=com.ntinside.droidpdd2012"));
                    	   intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    	   startActivity(intent);
                       }
                   })
                   .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                       }
                   });
            AlertDialog alert = builder.create();
            alert.show();
		}
    }
    
	private boolean isCallable(Intent intent) {
		List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent,   
	            PackageManager.MATCH_DEFAULT_ONLY);  
	        return list.size() > 0;
	}
    
    void updateColors() {
    	schema = Colors.ColorSchemas[Colors.getColorSchema(this)];
    	frame.setBackgroundColor(schema.getBackgroundColor());
    	contents.setColorSchema(schema);
    	searchView.setColorSchema(schema);
    	bookmarksView.setColorSchema(schema);
    	
    	versionLayout.setBackgroundColor(schema.getBackgroundColor());
    	version.setBackgroundColor(schema.getBackgroundColor());
    	version.setTextColor(schema.getFontСolor());
    }
    
    private TabHost host;
    private FrameLayout frame;
    private Colors.ColorSchema schema;
    private ContentsView contents;
    private TextView version; 
    private SearchDocView searchView;
    private BookmarksView bookmarksView;
    private ProgressDialog progressDialog = null;
    private LinearLayout versionLayout; 
}
