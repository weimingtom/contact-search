package com.taiva.cts;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class Modify extends Activity {
	private long id;
	private EditText phone;
	private EditText name;
	private EditText address;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	 // TODO Auto-generated method stub
	 	super.onCreate(savedInstanceState);
	 	setContentView(R.layout.modify);
	 	phone = (EditText)findViewById(R.id.txtPhone);
	 	name = (EditText)findViewById(R.id.txtName);
	 	address = (EditText)findViewById(R.id.txtAddress);
	
	 	Bundle bundle = this.getIntent().getExtras();
	    
	 	id = Long.parseLong(bundle.getString("id"));
	 	if (id > 0)
	 	{
		 	phone.setText(bundle.getString("phone"));
		 	name.setText(bundle.getString("name"));
		 	address.setText(bundle.getString("address"));
	 	}
	 	Button btSave = (Button)findViewById(R.id.btSave);
	 	btSave.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				modifyData(new String[] {String.valueOf(id)});
				finish();
			}
		});
	 	
	}
	
	private int modifyData(String[] selectionArgs)
	{
		Integer i = 0;
		
		String _phone = phone.getText().toString().trim();
		String _name = name.getText().toString().trim();
		String _address = address.getText().toString().trim();
		
		if (_phone.equals("")) 
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(Modify.this);
            builder.setTitle("Thông báo");
            builder.setMessage("Số điện thoại không có rồi, có phải chưa nhập hay không?");
            builder.setPositiveButton("Nhập lại", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	// TODO Auto-generated method stub
                return;
                }                        
            });
            builder.show();
        }
		
		if (_name.equals("")) 
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(Modify.this);
            builder.setTitle("Thông báo");
            builder.setMessage("Tên không có rồi, có phải chưa nhập hay không?");
            builder.setPositiveButton("Nhập lại", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	// TODO Auto-generated method stub
                return;
                }                        
            });
            builder.show();
        }
		
		if (_address.equals("")) 
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(Modify.this);
            builder.setTitle("Thông báo");
            builder.setMessage("Địa chỉ không có rồi, có phải chưa nhập hay không?");
            builder.setPositiveButton("Nhập lại", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	// TODO Auto-generated method stub
                return;
                }                        
            });
            builder.show();
        }
		
		ContentValues c = new ContentValues();
		c.put("phone", Integer.parseInt(_phone));
		c.put("name", _name);
		c.put("address", _address);
		
		if (id > 0)
		{
			i = getContentResolver().update(DataProvider.CONTENT_URI, c, "_id=?", selectionArgs);
		}
		else 
		{
			getContentResolver().insert(DataProvider.CONTENT_URI, c);
		}
		return i;
	}
}