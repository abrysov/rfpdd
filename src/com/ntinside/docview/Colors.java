package com.ntinside.docview;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

public class Colors {

	public static interface ColorSchema {
		public int getBackgroundColor();
		public int getFont—olor();
		public int getLinkColor();
		public int getNoteColor();
		public int getId();
		public String getName();
	}
	
	public static String colorSchema = "÷‚ÂÚÓ‚‡ˇ ÒıÂÏ‡";
	public static String blackColorSchema = "“ÂÏÌ˚È ÙÓÌ";
	public static String whiteColorSchema = "—‚ÂÚÎ˚È ÙÓÌ";
	
	public static ColorSchema[] ColorSchemas = { new BlackColorSchema(), 
		 										 new WhiteColorSchema() };
	
	public static int getColorSchema(Context context) {
		int schema = context.getSharedPreferences(COLORS, Context.MODE_PRIVATE)
				.getInt(CURRENT_COLOR_SCHEMA, ColorSchemas[0].getId());
		boolean include = false;
		for(ColorSchema s: ColorSchemas) {
			if (schema == s.getId()) {
				include = true;
			}
		}
		return (include ? schema : ColorSchemas[0].getId());
	}
	
	public static void setColorSchema(Context context, int schema) {
		SharedPreferences prefs = context.getSharedPreferences(COLORS, Context.MODE_PRIVATE);
		Editor ed = prefs.edit();
		ed.putInt(CURRENT_COLOR_SCHEMA, schema);
		ed.commit();
	}
	
	public static MenuHelper MenuHelper = new MenuHelper();
	
	public static class MenuHelper {
		public void createMenu(Menu menu) {
			SubMenu colors = menu.addSubMenu(colorSchema);
			for(ColorSchema s: Colors.ColorSchemas) {
				colors.add(IDMG_COLORS, 
						IDM_COLORS_BASE + s.getId(), Menu.NONE,
						s.getName());
			}
			colors.setGroupCheckable(IDMG_COLORS, true, true);
		}
		
		public void prepareMenu(Menu menu, Context context) {
			MenuItem item = menu.findItem(IDM_COLORS_BASE + Colors.getColorSchema(context));
			if (item != null) {
				item.setChecked(true);
			}
		}
		
		public boolean isColorClick(MenuItem item, Context context) {
			int id = item.getItemId();
			for(ColorSchema s: Colors.ColorSchemas) {
				if (id == (IDM_COLORS_BASE + s.getId()) ) {
					Colors.setColorSchema(context, id - IDM_COLORS_BASE);
					return true;
				}
			}
			return false;
		}
		
		private static final int IDMG_COLORS= 100;
		private static final int IDM_COLORS_BASE = 9000;
	}
	
	public static String asString(int color) {
		 return String.format("%06X", (0xFFFFFF & color));
	}
	
	private static class BlackColorSchema implements ColorSchema {
		@Override
		public int getBackgroundColor() {
			return Color.rgb(0x0C, 0x0C, 0x0C);
		}

		@Override
		public int getFont—olor() {
			return Color.WHITE;
		}
		
		@Override
		public int getLinkColor() {
			return Color.GREEN;
		}
		
		@Override
		public int getNoteColor() {
			return Color.YELLOW;
		}
		
		@Override
		public int getId() {
			return 0;
		}

		@Override
		public String getName() {
			return blackColorSchema;
		}
	}
	
	private static class WhiteColorSchema implements ColorSchema {
		@Override
		public int getBackgroundColor() {
			return Color.rgb(0xD0, 0xD0, 0xD0);
		}

		@Override
		public int getFont—olor() {
			return Color.BLACK;
		}

		@Override
		public int getId() {
			return 1;
		}
		
		@Override
		public int getLinkColor() {
			return Color.BLUE;
		}
		
		public int getNoteColor() {
			return Color.DKGRAY;
		}

		@Override
		public String getName() {
			return whiteColorSchema;
		}
	}
	
	private static final String COLORS = "colors";
	private static final String CURRENT_COLOR_SCHEMA = "currentColorSchema";
}
