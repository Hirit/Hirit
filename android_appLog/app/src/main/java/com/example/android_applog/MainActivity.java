package com.example.android_applog;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.ActivityManager.RecentTaskInfo;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // isRunningTask(this);
        while(true) {
            appName(this);
            try {
                Thread.sleep(5000);
            }catch(InterruptedException e){
                System.out.println(e.getMessage()); //sleep 메소드가 발생하는 InterruptedException
            }
        }
        // taskStack(this);
        // taskStack2(this);
    }

    public static void isRunningTask(Context context) {
        ActivityManager actMng = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> task = actMng.getRunningTasks(1);
        ComponentName componentInfo = task.get(0).topActivity;

        Log.d("appLogTest1 ", componentInfo.getPackageName());
        Log.d("appLogTest1 ", task.get(0).topActivity.getClassName());
    }

    public static void appName(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService( Context.ACTIVITY_SERVICE );
        PackageManager pm = context.getPackageManager();
        ActivityManager.RunningAppProcessInfo appProcess = activityManager.getRunningAppProcesses().get(0);

        if(appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
            CharSequence c = null;
            try {
                c = pm.getApplicationLabel(pm.getApplicationInfo(appProcess.processName, PackageManager.GET_META_DATA));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            Log.i("appLogTest55", c.toString());
        }
    }
}
