package com.ntinside.docview;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.widget.Toast;

public class MigrateMgr {

	public static void doMigrate(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
		int lastMigrate = prefs.getInt(PREF_LAST_MIGRATE, 0);
		int currentVer = getCurrentVer(context);
		
		if ((currentVer != -1) && (lastMigrate != currentVer)) {
			Editor ed = prefs.edit();

			// Cleanup bookmarks
			int deletedCount = BookmarksStore.removeAllBookmarks(context);
			
			// Save migrated version
			ed.putInt(PREF_LAST_MIGRATE, currentVer);
			ed.commit();

			if (deletedCount > 0) {
				Toast.makeText(context, 
						String.format("Текст документа был обновлен. Некоторые закладки потеряли актуальность и были удалены (%d)", deletedCount), 
						Toast.LENGTH_LONG).show();
			}
		}
	}
	
	private static int getCurrentVer(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			return -1;
		}
	}
	
	private static final String PREFS = "migrateMgr";
	private static final String PREF_LAST_MIGRATE = "lastMigrate";
}
