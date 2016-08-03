package com.ygk.cosremote;

import android.app.Application;
import android.util.Log;

/**
 * ���Fcatch �S�����쪺ex�@��
 * @author 1310081
 *
 */
public class MyApplication extends Application {
	
	static public String TAG = "MyApplication" ;
	
	public void onCreate() {
		
		Log.i(TAG ,"���Ұʦۭqapplication @@") ;
		// Setup handler for uncaught exceptions.
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread thread, Throwable e) {
				handleUncaughtException(thread, e);
			}
		});
	}

	public void handleUncaughtException(Thread thread, Throwable e) {
		e.printStackTrace(); 
		
		Log.i("MyApplication", "Handle uncatch exception: " + e.getMessage());
		String stacktrace = "" ;
		for ( StackTraceElement st: e.getStackTrace())
		{
			stacktrace += st.toString() + "\n" ;
		}
		
		String output = "uncatch exception:" + e.getMessage() + "\nStackTrace:" + stacktrace  ;

		Common.showAlert("�o���Y�����~", output) ;
		
		if ( Common.spp != null )
		{
			Common.spp.disconnect() ;
		}
		System.exit(1); // kill off the crashed app
	}

}