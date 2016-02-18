package com.example.hirit;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.app.Fragment;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Browser;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
			.add(R.id.container, new PlaceholderFragment())
			.commit();
		}
		Process process;
		try {
			process = Runtime.getRuntime().exec( "logcat -c");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		getBrowserHist();

		if ( isExternalStorageWritable() ) {

			File appDirectory = new File( Environment.getExternalStorageDirectory() + "/hirit" );
			File logDirectory = new File( appDirectory + "/log" );
			File logFile = new File( logDirectory, "logcat" + System.currentTimeMillis() + ".txt" );

			// create app folder
			if ( !appDirectory.exists() ) {
				appDirectory.mkdir();
			}

			// create log folder
			if ( !logDirectory.exists() ) {
				logDirectory.mkdir();
			}

			// clear the previous logcat and then write the new one to the file
			try {
				//Process process = Runtime.getRuntime().exec( "logcat -c");
				process = Runtime.getRuntime().exec( "logcat -f " + logFile + " history *:s");
			} catch ( IOException e ) {
				e.printStackTrace();
			}

		} else if ( isExternalStorageReadable() ) {
			// only readable
		} else {
			// not accessible
		}
	}

	/* Checks if external storage is available for read and write */
	public boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if ( Environment.MEDIA_MOUNTED.equals( state ) ) {
			return true;
		}
		return false;
	}
	public boolean isExternalStorageReadable() {
		String state = Environment.getExternalStorageState();
		if ( Environment.MEDIA_MOUNTED.equals( state ) ||
				Environment.MEDIA_MOUNTED_READ_ONLY.equals( state ) ) {
			return true;
		}
		return false;
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container, false);
			return rootView;
		}
	}

	@SuppressWarnings("deprecation")
	public void getBrowserHist()  {
		/*Cursor mCur = managedQuery(Browser.BOOKMARKS_URI,
				Browser.HISTORY_PROJECTION, null, null, null);
		mCur.moveToLast();
		if (mCur.moveToLast() && mCur.getCount() > 0) {
			//while (mCur.isAfterLast() == false) {
			Log.v("titleIdx", mCur
					.getString(Browser.HISTORY_PROJECTION_TITLE_INDEX));
			Log.v("urlIdx", mCur.getString(Browser.HISTORY_PROJECTION_URL_INDEX));
			//mCur.moveToNext();
			//}
		}*/
		String sel = Browser.BookmarkColumns.BOOKMARK + " = 0";
		Cursor mCur = managedQuery(Browser.BOOKMARKS_URI, Browser.HISTORY_PROJECTION, sel, null, null);
		int j = mCur.getCount();
		String[] mTitles = new String[j];
		String[] murls = new String[j];
		long[] date=new long[j];
		mCur.moveToFirst();
		int i=j-1;

		if (mCur.moveToFirst() && mCur.getCount() > 0) {
			int titleIdx = mCur.getColumnIndex(Browser.BookmarkColumns.TITLE);
			int urlIdx = mCur.getColumnIndex(Browser.BookmarkColumns.URL);
			int dateIdx=mCur.getColumnIndex(Browser.BookmarkColumns.DATE);

			while (mCur.isAfterLast() == false ) {
				mTitles[i]=mCur.getString(titleIdx);
				murls[i]=mCur.getString(urlIdx);
				date[i]=mCur.getLong(dateIdx);
				Log.v("history", mTitles[i] + "	" + murls[i] + "	" + date[i]);
				i--;
				mCur.moveToNext();
			}
		}
	}
}
