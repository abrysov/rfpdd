package com.ntinside.docview;

import com.ntinside.docmodel.DocCache;
import com.ntinside.docmodel.DocGroup;
import com.ntinside.docmodel.DocTools;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

public class ContentActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		layout = new LinearLayout(this);
		layout.setLayoutParams( new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT) );
		layout.setOrientation(LinearLayout.VERTICAL);
		
		contents = new ContentsView(this);
		contents.setLayoutParams( new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		
		layout.addView(contents);
		
		setContentView(layout);
		
		updateColors();
		
		int linkId = getIntent().getExtras().getInt(LINK_ID);
		DocGroup group = DocTools.findGroup(DocCache.getDoc(this), linkId);
		setTitle( group.getName() );
		contents.setGroups(group.getGroups());
		contents.setListener(new ContentsView.Listener() {
			@Override
			public void onGroupClick(DocGroup group) {
				if (group.getFilename() != null) {
					DocviewActivity.initFor(ContentActivity.this, group.getFirst());
					Intent intent = new Intent(ContentActivity.this, DocviewActivity.class);
					startActivity(intent);
				} else {
					Intent intent = new Intent(ContentActivity.this, ContentActivity.class);
					intent.putExtra(ContentActivity.LINK_ID, group.getFirst() );
					startActivity(intent);
				}
			}
		});
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
    	updateColors();
    	super.onResume();
    }

    void updateColors() {
    	schema = Colors.ColorSchemas[Colors.getColorSchema(this)];
    	layout.setBackgroundColor(schema.getBackgroundColor());
    	contents.setColorSchema(schema);
    }
    
    private LinearLayout layout;
    private Colors.ColorSchema schema;
    private ContentsView contents;
    public static final String LINK_ID = "linkId";
}
