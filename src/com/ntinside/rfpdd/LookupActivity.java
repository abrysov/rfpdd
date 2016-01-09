package com.ntinside.rfpdd;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ntinside.docmodel.DocCache;
import com.ntinside.docmodel.DocGroup;
import com.ntinside.docmodel.DocRoot;
import com.ntinside.docmodel.JavaXmlReader;
import com.ntinside.docmodel.XmlHandler;
import com.ntinside.docview.DocviewActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Class usage example:
 * 			Intent intent = new Intent();
			intent.setComponent(new ComponentName("com.ntinside.rfpdd", 
												  "com.ntinside.rfpdd.LookupActivity"));
			intent.putExtra("objType", "admission_list");
			intent.putExtra("objId", "7.4");
			
			startActivity(intent);
 */

/**
 * Validating that application is installed
 * private boolean isCallable(Intent intent) {  
        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent,   
            PackageManager.MATCH_DEFAULT_ONLY);  
        return list.size() > 0;  
   }
 *
 */

public class LookupActivity extends Activity {

	private static final String INTENT_OBJ_TYPE	= "objType";
	private static final String INTENT_OBJ_ID = "objId";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		root = DocCache.getDoc(this);
		Bundle bundle = getIntent().getExtras();
		Integer link = findLink(bundle.getString(INTENT_OBJ_TYPE), bundle.getString(INTENT_OBJ_ID));
		if (link != null) {
			DocviewActivity.initFor(this, link, link);
			startActivity(new Intent(this, DocviewActivity.class));
		} else {
			Toast.makeText(this, "—сылка не найдена!", Toast.LENGTH_LONG).show();
		}
		
		/*//testRoadRule();
		//testAdmissionList();
		//testRoadMarking();
		//testRoadSigns();
		if (true) {throw new RuntimeException("test: ok");}*/
		
		finish();
	}
	
	private Integer findLink(String objType, String objId) {
		
		if (objType.equals("road_rule")) {
			return findRoadRule(objId);
		} else if (objType.equals("admission_list")) {
			return findAdmissionList(objId);
		} else if (objType.equals("road_sign") || objType.equals("road_sign_plate") ) {
			return findRoadSign(objId);
		} else if (objType.equals("road_marking")) {
			return findRoadMarking(objId);
		}
		return null;
	}
	
	private Integer findRoadRule(String objId) {
		return simpleFindInDoc(objId, root.getGroups().get(0), false);
	}
	
	private Integer findAdmissionList(String objId) {
		return simpleFindInDoc(objId, root.getGroups().get(3).getGroups().get(1), false);
	}
	
	private Integer findRoadSign(String objId) {
		return simpleFindInDoc(objId, root.getGroups().get(1), true);
	}
	
	private Integer findRoadMarking(String objId) {
		return simpleFindInDoc(objId, root.getGroups().get(2), true);
	}
	
	private static class ResultHolder {
		public ResultHolder(DocGroup group, String objId, boolean extend) {
			this.group = group;
			this.objId = objId;
			this.extend = extend;
		}
		
		public boolean isInDoc(int linkId) {
			return ((group.getFirst() <= linkId) &&
					(linkId <= group.getLast()));
		}
		
		public void setResult(int result) {
			this.result = result;
		}
		
		public Integer getResult() {
			return result;
		}
		
		public boolean checkText(String text) {
			if (extend) {
				return hasNumberInComplexName(text, objId);
			} else {
				return text.startsWith(objId);
			}
		}
		
		public static boolean hasNumberInComplexName(String text, String substr) {
			if (text.startsWith(substr)) {
				return true;
			}
			
			Pattern p = Pattern.compile("([\\d\\.]+), ([\\d\\.]+).*");
			Matcher m = p.matcher(text);
			if (m.find()) {
				if (m.group(2).equals(substr)) {
					return true;
				}
			}
			
			p = Pattern.compile("([\\d\\.]+) \".*?\", ([\\d\\.]+).*");
			m = p.matcher(text);
			if (m.find()) {
				if (m.group(2).equals(substr)) {
					return true;
				}
			}
			
			
			p = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+) - (\\d+)\\.(\\d+)\\.(\\d+).*");
			m = p.matcher(text);
			if (m.find()) {
				int from = Integer.parseInt(m.group(3));
				int till = Integer.parseInt(m.group(6));
				for(int i = from; i <= till; i++) {
					String check = String.format("%s.%s.%d", m.group(1), m.group(2), i );
					if (check.equals(substr)) 
						return true;
				}
			}
					
			return false;
		}

		
		private Integer result = null;
		private DocGroup group;
		private String objId;
		private boolean extend; 
	}
	
	private Integer simpleFindInDoc(String objId, DocGroup group, boolean extend) {
		final ResultHolder result = new ResultHolder(group, objId, extend);
		
		try {
			JavaXmlReader.loadXml(this, new XmlHandler() {
	
				@Override
				public void onStartElement(XmlInfo xml) {
					if (xml.getName().equals("link")) {
						currentLink = Integer.parseInt(xml.getValue("id"));
					}
				}
	
				@Override
				public void onEndElement(XmlInfo xml) {
				}
	
				@Override
				public void onText(XmlInfo xml) {
					if (result.isInDoc(currentLink)) {
						if (result.checkText(xml.getText())) {
							result.setResult(currentLink);
						}
					}
				}
	
				@Override
				public boolean isComplete() {
					return (result.getResult() != null);
				}
				
				private int currentLink = 0;
			}, SEARCH_FILE);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		return result.getResult();
	}
	
	/*void testRoadRule() {
		String[] items = new String[] { "1.2","6.2","8.1","8.2","8.4","1.4","10.3","11.4","12.4","12.5","13.5","13.11","13.12","13.10","13.9","10.2","19.1","2.1.2","8.5","8.11","9.7","12.2","6.15","13.3","13.4","13.1","18.3","20.4","3.1","8.6","8.8","9.4","11.1","16.1","12.1","21.2","21.4","21.1","9.1","6.3","9.5","3.2","17.2","20.3","6.14","8.10","6.10","8.9","3.4","15.3","19.10","13.7","8.3","11.7","6.8","14.1","14.2","2.7","9.3","13.2","17.1","17.4","23.4","2.5","18.2","6.7","1.3","19.2","2.3.1","9.8","11.5","22.8","10.1","12.3","13.8","19.4","11.3","6.12","6.9","13.6","4.3","19.7","9.9","3.5","7.2","10.5","7.1","8.12","16.2","22.9","6.13","18.1","20.2","3.3","15.4","19.5","9.10","9.11","11.2","2.6","9.6","17.3","15.2","19.11","14.4","23.5","2.3.3","24.4","6.4","2.3.2","23.3","7.3","15.5","4.2","19.3","10.4","14.6","2.1.1","2.4","22.1","8.7","6.11","5.2","22.4","22.5","19.8","22.6"  };
		for(String item: items) {
			Integer link = findRoadRule(item);
			if (link == null) {
				//Toast.makeText(this, , Toast.LENGTH_SHORT).show();
				throw new RuntimeException(String.format("Error for: %s", item));
			}
		}
	}
	
	void testAdmissionList() {
		String[] items = new String[] { "7.4","5.1","3.3","3.2","3.4","2.1","7.7","6.1","6.2","6.5","1.5","7.6","5.5","6.3","7.2","7.3","4.1","1.4"  };
		for(String item: items) {
			Integer link = findAdmissionList(item);
			if (link == null) {
				//Toast.makeText(this, , Toast.LENGTH_SHORT).show();
				throw new RuntimeException(String.format("Error for: %s", item));
			}
		}
	}
	
	void testRoadMarking() {
		String[] items = new String[] { "1.7","1.19","1.9","1.18","1.12","1.17","1.21","2.5","1.20","1.13","1.11","1.4","1.5","1.14.1","1.14.2","2.1.1","2.1.3","1.2.1","1.23","1.2.2","2.7","1.16.3","1.10","1.25","2.6","1.24.2","1.15","2.2","1.22","1.3","1.1","1.6"  };
		for(String item: items) {
			Integer link = findRoadMarking(item);
			if (link == null) {
				//Toast.makeText(this, , Toast.LENGTH_SHORT).show();
				throw new RuntimeException(String.format("Error for: %s", item));
			}
		}
	}
	
	void testRoadSigns() {
		String[] items = new String[] { "4.1.4","6.21.1","5.29","5.30","4.6","2.3.1","2.1","8.13","5.21","2.7","5.33","5.15.1","2.4","6.4","5.1","2.5","3.24","5.5","5.14","6.14.2","6.15.1","7.11","1.12.2","6.19.1","5.15.5","8.16","1.18","1.15","3.28","5.24.1","6.3.1","4.1.1","3.25","3.18.2","3.2","3.3","3.1","3.27","5.7.2","5.13.1","3.19","6.8.2","1.22","5.19.1","5.19.2","4.3","4.1.6","3.18.1","5.3","6.2","5.10","6.8.1","1.13","1.14","2.6","3.4","6.15.2","5.6","1.21","1.2","1.4.1","1.11.1","1.11.2","4.5","3.10","5.15.6","5.13.2","1.6","5.27","5.28","5.25","1.25","5.15.2","1.12.1","3.17.2","5.26","1.16","3.30","5.23.1","5.23.2","5.7.1","2.3.3","3.17.3","3.17.1","7.12","6.16","3.21","3.7","5.8","5.11","3.26","1.29","1.5","3.16","4.1.2","3.20","3.31","3.23","2.2","1.20.2","4.4","1.3.2","5.16","1.17","5.20","3.29","6.14.1","6.13","3.22","5.15.4","1.34.3","6.3.2","8.1.1","2.3.2","7.10","7.9","4.2.2","1.28","5.22","1.1","1.4.3","1.4.6","4.1.3","1.31","6.8.3","1.24","8.6.4","7.14","4.8.3","8.4.1","3.12","3.13","3.11","8.3.3","5.15.3","8.6.2","8.14","8.4.3","8.6.3","8.6.6","8.6.9","8.2.1","8.21.2","8.1.2","8.2.2","8.10","8.12","8.6.1","8.2.4","8.2.3","8.5.1","8.5.2","8.18","8.6","8.11"  };
		for(String item: items) {
			Integer link = findRoadSign(item);
			if (link == null) {
				//Toast.makeText(this, , Toast.LENGTH_SHORT).show();
				throw new RuntimeException(String.format("Error for: %s", item));
			}
		}
	}*/
	
	private DocRoot root;
	private static final String SEARCH_FILE = "search.mp3";
}
