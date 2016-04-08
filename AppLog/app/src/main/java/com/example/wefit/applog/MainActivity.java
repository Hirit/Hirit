package com.example.wefit.applog;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, LogService.class);
        startService(intent);

        appName(this);
    }

    private void appName(Context context) {
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
            Log.i("appLog", c.toString());
        }
    }
}
