package com.taiva.cts;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.regex.Pattern;
import com.taiva.cts.R;

public class ContactSearch extends ListActivity {
	
    private static final String TAG = "ContactSearch";
    private ProgressDialog myProgressDialog;
    private Cursor c;
    private Uri uri;
    private int matchValue;
    private int r;
    private int searchType;
    private SharedPreferences prefs;
    private InputMethodManager imm;
    private SimpleCursorAdapter sca;
    private String search = "";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        final EditText txtSearch = (EditText) findViewById(R.id.txtSearch);
        final Button btSearch = (Button) findViewById(R.id.btSearch);
                
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        imm = (InputMethodManager) ContactSearch.this.getSystemService(Context.INPUT_METHOD_SERVICE);
        
        searchType = Integer.parseInt(prefs.getString("listPref", "0"));
		
        if (searchType == 0)
		{
			Log.i(TAG, "input phone");
			txtSearch.setInputType(InputType.TYPE_CLASS_PHONE);
		}
		else 
		{
			Log.i(TAG, "input text");
			txtSearch.setInputType(InputType.TYPE_CLASS_TEXT);
		}
       
		// Thực hiện chức năng tra cứu
        btSearch.setOnClickListener(new OnClickListener() {
        	//@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				search = txtSearch.getText().toString().trim();
				final boolean bool;
				Log.i(TAG, "search=" + search);
				
				// check null search 
				if (search.equals("")) 
				{
					Log.i(TAG, "Search is null");
	                    AlertDialog.Builder builder = new AlertDialog.Builder(ContactSearch.this);
	                    builder.setTitle("Thông báo");
	                    builder.setMessage("Nhập thông tin cần tra cứu");
	                    builder.setPositiveButton("Tiếp tục", new DialogInterface.OnClickListener() {
	                        public void onClick(DialogInterface dialog, int which) {
	                            // TODO Auto-generated method stub
	                        	return;
	                        }                        
	                    });
	                    builder.show();
	                    return;
	            }
				
				bool = prefs.getBoolean("chkPref", true);
				if (bool == false)
				{
					matchValue = 0;
				}
				else 
				{
					matchValue = 1;
				}
				
				searchType = Integer.parseInt(prefs.getString("listPref", "0"));
				// check phone number or string
				if (searchType == 0)
				{
					String myPhone = "[0-9]+";
					if (!Pattern.matches(myPhone, search))
					{
						AlertDialog.Builder builder = new AlertDialog.Builder(ContactSearch.this);
	                    builder.setTitle("Thông báo");
	                    builder.setMessage("Dữ liệu nhập vào không hợp lệ");
	                    builder.setPositiveButton("Tiếp tục", new DialogInterface.OnClickListener() {
	                        public void onClick(DialogInterface dialog, int which) {
	                        	txtSearch.setText("");
	                        	return;
	                        }                        
	                    });
	                    builder.show();
	                    return;
					}
				}
				
		        imm.hideSoftInputFromWindow(txtSearch.getWindowToken(), 0);
		        
				myProgressDialog = ProgressDialog.show(ContactSearch.this, "Đang chờ...", "Đang lấy số liệu...", true);
					
				new Thread(){ 
			             public void run(){
			                     try 
			                     {
			                    	r = getContactList(search, searchType, matchValue);
			                    	handler.sendEmptyMessage(r);
			                     } 
			                     catch (Exception e) {
			                             e.printStackTrace();
			                     }
			                     
			             } 
				}.start();
			}
			
		});
        
        final ListView lv = getListView();
        lv.setItemsCanFocus(false);
        lv.setClickable(true);
        registerForContextMenu(lv);
    }
    
    public void onResume()
    {
    	super.onResume();
    	EditText txtSearch = (EditText) findViewById(R.id.txtSearch);
    	searchType = Integer.parseInt(prefs.getString("listPref", "0"));
		if (searchType == 0)
		{
			Log.i(TAG, "input phone");
			txtSearch.setInputType(InputType.TYPE_CLASS_PHONE);
		}
		else 
		{
			Log.i(TAG, "input text");
			txtSearch.setInputType(InputType.TYPE_CLASS_TEXT);
		}
    }
    
    public int getContactList(String search, int searchType, int matchValue)
    {
		// tìm theo số điện thoại
		if (searchType == 0)
		{
			if (search.substring(0, 1).equals("0"))
			{
				search = search.substring(1);
				Log.i(TAG, "search =" + search);
			}
			Log.i(TAG, "searchType = " + searchType + ", search =" + search);
			uri = Uri.parse("content://com.taiva.cts.DataProvider/opt/" + matchValue + "/phone/" + search);
		}
		// tìm theo tên 
		else if (searchType == 1)
		{
			uri = Uri.parse("content://com.taiva.cts.DataProvider/opt/" + matchValue + "/name/" + search);
		}
		// tìm theo địa chỉ 
		else if (searchType == 2)
		{
			uri = Uri.parse("content://com.taiva.cts.DataProvider/opt/" + matchValue + "/address/" + search);
		}
		Log.i(TAG,"uri = " + uri.toString());
	    
		c = getContentResolver().query(uri, new String[]{DataProvider._ID, DataProvider.COLUMN_PHONE, DataProvider.COLUMN_NAME, DataProvider.COLUMN_ADDRESS}, null, null, null);
 		startManagingCursor(c);
   	
		if (c == null)
		{
			return 0;
		}
		return 1;
    }
    
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
       	 	myProgressDialog.dismiss();
       	 	displayResult();
        }
    };
    
    public void displayResult()
    {
    	TextView textViewResult = (TextView) findViewById(R.id.textViewResult);
   	 	textViewResult.setText("");
   	 	if(r > 0)
   	 	{
			Log.i(TAG, "Cursor is not null");
	        String[] from = new String[] { DataProvider.COLUMN_PHONE, DataProvider.COLUMN_NAME, DataProvider.COLUMN_ADDRESS};
	        int[] to = new int[] { R.id.phone, R.id.name, R.id.address};
	        sca = new SimpleCursorAdapter(getBaseContext(),R.layout.contact_list , c, from, to);
	        setListAdapter(sca);
	        textViewResult.setText("Số lượng thuê bao tìm được: " + c.getCount());
	        
		}
		else 
		{
			Log.i(TAG, "Cursor is null");
           	textViewResult.setText(R.string.no_result); 
		}
    }
    
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	menu.add(0, 0, 0, "Cài đặt").setIcon(R.drawable.corpus_edit_icon);
    	return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item)
    {
	    switch (item.getItemId()) 
	    {
	       case 0: 
	    	   Intent myIntent = new Intent(getApplicationContext(), Preferences.class);
               startActivityForResult(myIntent, 0);
	    	   break;
	    }
	    return true;
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) 
    {
      if (v == (ListView) getListView()) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
        Cursor cur = (Cursor) sca.getItem(info.position);
  	  	String phone = cur.getString(1);
        menu.setHeaderTitle(phone);
        String[] menuItems = getResources().getStringArray(R.array.menu);
        for (int i = 0; i<menuItems.length; i++) {
          menu.add(Menu.NONE, i, i, menuItems[i]);
        }
      }
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
      AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
      int menuItemIndex = item.getItemId();
      String[] menuItems = getResources().getStringArray(R.array.menu);
      String menuItemName = menuItems[menuItemIndex];
      Cursor cur = (Cursor) sca.getItem(info.position);
	  String phone = cur.getString(1).trim();
	  final String _id = cur.getString(0);
      
	  switch (menuItemIndex) 
	    {
	    	case 0: // gọi điện thoại
	    		Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone)); 
	    		startActivity(callIntent);
	    		break;
	    	case 1: // gửi tin nhắn
	    		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("smsto:" + phone));
	    		intent.setType("vnd.android-dir/mms-sms");
	    		intent.putExtra("address", phone);
	    		startActivity(intent);
		    	break;
	    	case 2: // chỉnh sửa liên lạc
	    		 Intent intent1 = new Intent(this,Modify.class);
	    		 Bundle bundle = new Bundle();
	    		 bundle.putString("id", cur.getString(0));
	    		 bundle.putString("phone", cur.getString(1));
	    		 bundle.putString("name", cur.getString(2));
	    		 bundle.putString("address", cur.getString(3));
	    		 intent1.putExtras(bundle);
	    		 startActivity(intent1);
	    		break;
	    	case 3: // xóa số
	    		AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
	    		alt_bld.setMessage("Muốn xóa số " + phone  + " phải không?")
	    		.setCancelable(false)
	    		.setPositiveButton("Có", new DialogInterface.OnClickListener() {
	    		public void onClick(DialogInterface dialog, int id) {
	    		// Action for 'Yes' Button
	    			getContentResolver().delete(DataProvider.CONTENT_URI, "_id=?", new String[] {String.valueOf(_id)});
	    			Log.i(TAG, "Đã xóa số: " + _id);
	    			r = getContactList(search, searchType, matchValue);
	    			displayResult();
	    		}
	    		})
	    		.setNegativeButton("Không", new DialogInterface.OnClickListener() {
	    		public void onClick(DialogInterface dialog, int id) {
	    		//  Action for 'NO' Button
	    			dialog.cancel();
	    		}
	    		});
	    		AlertDialog alert = alt_bld.create();
	    		alert.setTitle("Xác nhận");
	    		alert.show();
	    		break;
	    }
		  
      Log.i(TAG, "select: " + menuItemName + ", " + phone);
      return true;
    }
}