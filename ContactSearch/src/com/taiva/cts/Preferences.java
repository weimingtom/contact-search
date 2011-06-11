package com.taiva.cts;
 
import com.taiva.cts.R;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.widget.Toast;
 
public class Preferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	
	    public static SharedPreferences mShareRefs;   
	    private ListPreference list;
	    private CheckBoxPreference chk;
	    private CheckBoxPreference chkIncoming;
	    private CheckBoxPreference chkOutgoing;
	    
        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                addPreferencesFromResource(R.xml.preferences);
                
                list = (ListPreference) findPreference("listPref");
                chk = (CheckBoxPreference) findPreference("chkPref");
                chkIncoming = (CheckBoxPreference) findPreference("chkIncoming");
                chkOutgoing = (CheckBoxPreference) findPreference("chkOutgoing");
        }
        
        @Override
        protected void onResume() {
            super.onResume();
            list.setSummary(list.getEntry());
            chk.setSummary(chk.isChecked()? "Đang tìm gần đúng" : "Đang tìm chính xác" );
            chkIncoming.setSummary(R.string.incoming_des);
            chkOutgoing.setSummary(R.string.outgoing_des);
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        protected void onPause() {
            super.onPause();          
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);    
        }

        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals("listPref")) {
            	list.setSummary(list.getEntry());
            	Toast.makeText(getBaseContext(), "Đã lưu loại tìm", Toast.LENGTH_SHORT).show();
            }
            else if (key.equals("chkPref")) {
            	chk.setSummary(chk.isChecked()? "Đang tìm gần đúng" : "Đang tìm chính xác" );
            	Toast.makeText(getBaseContext(), "Đã lưu kiểu tìm", Toast.LENGTH_SHORT).show();
            }
            else if (key.equals("chkIncoming")) {
            	Toast.makeText(getBaseContext(), "Đã lưu", Toast.LENGTH_SHORT).show();
            }
            else if (key.equals("chkOutgoing")) {
            	Toast.makeText(getBaseContext(), "Đã lưu", Toast.LENGTH_SHORT).show();
            }
        }


}