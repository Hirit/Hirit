package com.example.hirit;

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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

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
		String sel = Browser.BookmarkColumns.BOOKMARK + " = 0";
		Cursor mCur = managedQuery(Browser.BOOKMARKS_URI, Browser.HISTORY_PROJECTION, sel, null, null);
		int j = mCur.getCount();
		String[] mTitles = new String[j];
		String[] murls = new String[j];
		long[] date=new long[j];
		mCur.moveToFirst();
		int i=0;

		if (mCur.moveToLast() && mCur.getCount() > 0) {
			int titleIdx = mCur.getColumnIndex(Browser.BookmarkColumns.TITLE);
			int urlIdx = mCur.getColumnIndex(Browser.BookmarkColumns.URL);
			int dateIdx=mCur.getColumnIndex(Browser.BookmarkColumns.DATE);

			while (mCur.isBeforeFirst() == false ) {
				mTitles[i]=mCur.getString(titleIdx);
				murls[i]=mCur.getString(urlIdx);
				date[i]=mCur.getLong(dateIdx);
				Log.v("history", mTitles[i] + "	" + murls[i] + "	" + getDate(date[i], "yyyy/MM/dd hh:mm"));
				i++;
				mCur.moveToPrevious();
			}
		}
	}
	public static String getDate(long milliSeconds, String dateFormat)
	{
		// Create a DateFormatter object for displaying date in specified format.
		SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

		// Create a calendar object that will convert the date and time value in milliseconds to date.
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(milliSeconds);
		return formatter.format(calendar.getTime());
	}
}
