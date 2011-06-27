package com.taiva.cts;

import java.util.ArrayList;

import com.taiva.cts.ContactSearch.ManagedCursor;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.util.Log;

public class DataProvider extends ContentProvider {
	public static final String TAG = "ContactSearch-DataProvider";
	public static final String PROVIDER_NAME = "com.taiva.cts.DataProvider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + PROVIDER_NAME);
	
	public static final String _ID = "_id";
	public static final String COLUMN_PHONE = "phone";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_ADDRESS = "address";

	private static final int SEARCH_PHONE = 0;
	private static final int SEARCH_NAME = 1;
	private static final int SEARCH_ADDRESS = 2; 
	
	private SQLiteDatabase mCurrentDB = null;
	
	private final ArrayList<ContactSearch.ManagedCursor> mManagedCursors = new ArrayList<ContactSearch.ManagedCursor>();
	 
	private static final UriMatcher uriMatcher;
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(PROVIDER_NAME, "opt/*/_id/*", SEARCH_PHONE);
		uriMatcher.addURI(PROVIDER_NAME, "opt/*/name/*", SEARCH_NAME);
		uriMatcher.addURI(PROVIDER_NAME, "opt/*/address/*", SEARCH_ADDRESS);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return deleteContact(selectionArgs);
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		long rowId = insertContact(values);
		if (rowId > 0)
		{
			Uri uri1 = ContentUris.withAppendedId(CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(uri1, null);
			return uri1;
		}
		throw new SQLException("Failed to insert row into " + uri);
	}
	
	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
	    return updateContact(values, selectionArgs);
	}

	public void openDatabase()
	{
		String dbPath = "/sdcard/ContactSearch/data.db";
		Log.i(TAG,"Data path = " + dbPath);

		//Close the database first if is was opened
		if (mCurrentDB != null)
		{
			if (mCurrentDB.isOpen())
			{
				mCurrentDB.close();
			}
		}
		
        try
        {
        	mCurrentDB = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS|SQLiteDatabase.OPEN_READWRITE);
        	Log.i(TAG,"Đã kết nối");
        }
        catch (SQLiteException ex)
        {
        	mCurrentDB = null;
			Log.i(TAG, "Chưa kết nối");
        }
	}
	
	public void closeDatabase()
	{
		if (mCurrentDB != null)
		{
			if (mCurrentDB.isOpen())
			{
				mCurrentDB.close();
			}
		}
	}
	
	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		return false;
	}

	
	private Cursor getDataFromNumber(int a, String number)
	{
		String query = "";
		if (a==0)
		{
			query = "SELECT _id, _id as phone, name, address FROM contact WHERE _id MATCH '" + number+ "'";
		}
		else if (a==1) 
		{
			query = "SELECT _id, _id as phone, name, address FROM contact WHERE _id like '%" + number + "%'";
		}
		else 
		{
			query = "SELECT _id, _id as phone, name, address FROM contact WHERE _id MATCH '" + number + "*' ORDER BY _id ASC";
		}
		Log.i(TAG, "query = " + query);
		
		if (mCurrentDB == null)
		{
			openDatabase();
		}
		Cursor r = mCurrentDB.rawQuery(query, null);
	    startManagingCursor(r);
		Log.i(TAG, query);
		Log.i(TAG, "column=" + r.getColumnCount() + ", row=" + r.getCount());
		return r;
	}
	
	public int deleteContact(String[] selectionArgs)
	{
		if (mCurrentDB == null)
		{
			openDatabase();
		}
		return mCurrentDB.delete("contact", "_id=?", selectionArgs);
	}
	
	public long insertContact(ContentValues values)
	{
		if (mCurrentDB == null)
		{
			openDatabase();
		}
		return mCurrentDB.insert("contact", null, values);
	}
	
	public int updateContact(ContentValues values, String[] selectionArgs)
	{
		if (mCurrentDB == null)
		{
			openDatabase();
		}
		return mCurrentDB.update("contact", values, "_id=?", selectionArgs);
	}

	private Cursor getDataFromName(int a, String name)
	{
		String query = "";
		query = "SELECT _id, _id as phone, name, address FROM contact WHERE name MATCH '" + name + "'";
		query = query + " UNION SELECT _id, _id as phone, name, address FROM contact WHERE raw_name MATCH '" + name.toLowerCase() + "' ORDER BY name";
		Log.i(TAG, "query = " + query);
		
		if (mCurrentDB == null)
		{
			openDatabase();
		}
		
		Cursor r = mCurrentDB.rawQuery(query, null);
		startManagingCursor(r);
		Log.i(TAG, query);
		Log.i(TAG, "column=" + r.getColumnCount() + ", row=" + r.getCount());
		return r;
	}
	
	private Cursor getDataFromAddress(int a, String address)
	{
		String query = "";
		query = "SELECT _id, _id as phone, name, address FROM contact WHERE address MATCH '" + address + "'";
		query = query + " UNION SELECT _id, _id as phone, name, address FROM contact WHERE raw_address MATCH '" + address.toLowerCase() + "' ORDER BY address";
		Log.i(TAG, "query = " + query);

		if (mCurrentDB == null)
		{
			openDatabase();
		}
		
		Cursor r = mCurrentDB.rawQuery(query, null);
		startManagingCursor(r);
		Log.i(TAG, query);
		Log.i(TAG, "column=" + r.getColumnCount() + ", row=" + r.getCount());
		return r;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub

		String name;
		String phone;
		String address;
		int opt;
		Cursor c;
		
		switch (uriMatcher.match(uri))
		{
			case SEARCH_PHONE:
				try
				{
					phone = uri.getPathSegments().get(3);
					opt = Integer.parseInt(uri.getPathSegments().get(1));
					Log.i(TAG,"SEARCH_PHONE = " + phone);
					
					if (phone != null)
					{
						c = getDataFromNumber(opt, phone);
						//c.setNotificationUri(getContext().getContentResolver(), uri);
						if (c.getCount() > 0)
						{
							Log.i(TAG, "Tìm thấy số: " + phone);
						}
						else  
						{
							Log.i(TAG, "Không tìm thấy số: " + phone);
							c = null;
						}	
					}
					else
					{
						c = null;
						Log.i(TAG,"Không có dữ liệu tìm kiếm");
					}
					return c;
					
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
					return null;
				}
			case SEARCH_NAME:
				try
				{
					name = uri.getPathSegments().get(3);
					opt = Integer.parseInt(uri.getPathSegments().get(1));
					Log.i(TAG,"SEARCH_NAME = " + name);
					
					if (name != null)
					{
						c = getDataFromName(opt, name);
						//c.setNotificationUri(getContext().getContentResolver(), uri);
						if (c.getCount() > 0)
						{
							Log.i(TAG, "Tìm thấy tên: " + name);
						}
						else  
						{
							Log.i(TAG, "Không tìm thấy địa chỉ: " + name);
							c = null;
						}
					}
					else
					{
						c = null;
						Log.i(TAG,"Không có dữ liệu tìm kiếm");
					}
					return c;
					
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
					return null;
				}
			case SEARCH_ADDRESS:
				try
				{
					address = uri.getPathSegments().get(3);
					opt = Integer.parseInt(uri.getPathSegments().get(1));
					Log.i(TAG,"SEARCH_ADDRESS: " + address);
					
					if (address != null)
					{
						c = getDataFromAddress(opt, address);
						//c.setNotificationUri(getContext().getContentResolver(), uri);
						if (c.getCount() > 0)
						{
							Log.i(TAG, "Tìm thấy địa chỉ: " + address);
						}
						else  
						{
							Log.i(TAG, "Không tìm thấy địa chỉ: " + address);
							c = null;
						}
					}
					else
					{
						c = null;
						Log.i(TAG,"Không có dữ liệu tìm kiếm");
					}
					return c;
					
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
					return null;
				}
	        default:
	        	Log.e(TAG,"Không hỗ trợ URI: " + uri);
	        	return null;
		}
	}
	
	public void startManagingCursor(Cursor c) {
        synchronized (mManagedCursors) {
            mManagedCursors.add(new ManagedCursor(c));
        }
    }
    
    public void stopManagingCursor(Cursor c) {
        synchronized (mManagedCursors) {
            final int N = mManagedCursors.size();
            for (int i=0; i<N; i++) {
                ManagedCursor mc = mManagedCursors.get(i);
                if (mc.mCursor == c) {
                    mManagedCursors.remove(i);
                    break;
                }
            }
        }
    }

}
