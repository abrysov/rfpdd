package com.ntinside.docview;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class NaviStore {
	
	public static class ReturnPoint {
		public ReturnPoint(Integer posLink, Integer activeLink, String chapterName) {
			this.posLink = posLink;
			this.activeLink = activeLink;
			this.chapterName = chapterName;
		}
		
		public Integer getPosLink() {
			return posLink;
		}
		
		public Integer getActiveLink() {
			return activeLink;
		}
		
		public String getChapterName() {
			return chapterName;
		}
		
		private Integer posLink;
		private Integer activeLink;
		private String chapterName;
	}
	
	public NaviStore(Context context) {
		prefs = getPrefs(context);
		load();
	}
	
	public static void reset(Context context) {
		NaviStore store = new NaviStore(context);
		while(store.pop() != null) {
		}
	}
	
	public ReturnPoint pop() {
		if (returns.size() <= 0)
			return null;
		int idx = returns.size() - 1;
		ReturnPoint pt = returns.get(idx);
		returns.remove(idx);
		
		flush();
		return pt;
	}
	
	public void push(ReturnPoint point) {
		while (returns.size() >= MAX_RETURN_POINTS) {
			returns.remove(0);
		}
		returns.add(point);
		flush();
	}
	
	public ReturnPoint[] getAllReturnPoins() {
		ReturnPoint[] points = new ReturnPoint[returns.size()];
		for(int i = 0; i < returns.size(); i++) {
			points[i] = returns.get( returns.size() - 1 - i );
		}
		return points;
	}
	
	private void flush() {
		Editor ed = prefs.edit();
		for(int i = 0; i < MAX_RETURN_POINTS; i++) {
			ed.putInt(String.format(NAVI_RTPT_LNK, i), -1);
			ed.putInt(String.format(NAVI_RTPT_ACT, i), -1);
			ed.putString(String.format(NAVI_RTPT_CHP, i), null);
		}
		
		for(int i = 0; i < returns.size(); i++) {
			ReturnPoint pt = returns.get(i);
			ed.putInt(String.format(NAVI_RTPT_LNK, i), pt.getPosLink() != null ? pt.getPosLink() : -1);
			ed.putInt(String.format(NAVI_RTPT_ACT, i), pt.getActiveLink() != null ? pt.getActiveLink() : -1);
			ed.putString(String.format(NAVI_RTPT_CHP, i), pt.getChapterName());
		}
		ed.commit();
	}
	
	private void load() {
		for(int i = 0; i < MAX_RETURN_POINTS; i++) {
			int posLink = prefs.getInt(String.format(NAVI_RTPT_LNK, i), -1);
			int activeLink = prefs.getInt(String.format(NAVI_RTPT_ACT, i), -1);
			String chapterName = prefs.getString(String.format(NAVI_RTPT_CHP, i), null);
			if (posLink == -1 && activeLink == -1) {
				break;
			}
			returns.add( new ReturnPoint(posLink >= 0 ? posLink : null, 
										 activeLink >= 0 ? activeLink : null,
										 (chapterName != null ? chapterName : "")) );
		}
	}
	
	private static SharedPreferences getPrefs(Context context) {
		return context.getSharedPreferences(NAVIGATION, Context.MODE_PRIVATE);
	}
	
	private SharedPreferences prefs;
	private ArrayList<ReturnPoint> returns = new ArrayList<ReturnPoint>();
	private static final String NAVIGATION = "navigation";
	private static final String NAVI_RTPT_LNK = "navi_rtpt_%d_pos";
	private static final String NAVI_RTPT_ACT = "navi_rtpt_%d_act";
	private static final String NAVI_RTPT_CHP = "navi_rtpt_%d_chp";
	public static final int MAX_RETURN_POINTS = 10;
	
}
