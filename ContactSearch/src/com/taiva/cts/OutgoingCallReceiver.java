package com.taiva.cts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

public class OutgoingCallReceiver extends BroadcastReceiver {

		private String phoneNumber;
		private String strInfo;
		private Toast toast;
		private SharedPreferences prefs;
		
        @Override
        public void onReceive(Context context, Intent intent) {
                Bundle bundle = intent.getExtras();
                if(null == bundle)
                        return;
                
                prefs = PreferenceManager.getDefaultSharedPreferences(context);
            	if (prefs.getBoolean("chkOutgoing", true) == true)
            	{
            		phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
                    Log.i("OutgoingCallReceiver",phoneNumber);
                    Log.i("OutgoingCallReceiver",bundle.toString());
                    if (phoneNumber != null)
                    {
	                    phoneNumber = phoneNumber.substring(1);
		        		Uri uri = Uri.parse("content://com.taiva.cts.DataProvider/opt/0/phone/" + phoneNumber);
		        		Log.i("OutgoingCallReceiver","uri = " + uri.toString());
		        		Cursor r = context.getContentResolver().query(uri,null,null,null,null);
		 
		        		//Only notify when info is found
		        		if (r != null && r.getCount() > 0)
		        		{
		        			r.moveToFirst();
		        			StringBuilder info = new StringBuilder();
		        			info.append("Tên: " + r.getString(2) + "\n");
		        			info.append("Địa chỉ: " + r.getString(3));
		        			r.close();
		        			
		        			strInfo = info.toString();
		        			Log.i("OutgoingCallReceiver","info = " + strInfo);
		        			toast = Toast.makeText(context, strInfo, Toast.LENGTH_LONG);
		        			toast.setGravity(Gravity.TOP, 0, 80);
		        			fireLongToast();
		        		}
                    }
            	}
        }
        
        private void fireLongToast() {
        	
        	Thread t = new Thread() {
        		public void run() {
        			int count = 0;
        		    try {
	        		    	while (true && count < 6) {
	        		    		toast.show();
	        		            sleep(1850);
	        		            count++;
	        	            }
        		    } 
        		    catch (Exception e) {
        		        	Log.e("OutgoingCallReceiver", "", e);
        		    }
        		}
        	};
        	t.start();
      }
}
