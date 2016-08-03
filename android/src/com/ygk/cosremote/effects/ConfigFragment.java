package com.ygk.cosremote.effects;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.List;

import com.ygk.cosremote.*;

import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Bitmap.Config;
import android.graphics.Matrix;
import android.graphics.PorterDuff.Mode;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

/**
 * 連接 加速度感測器，只動前x個LED 不然要重寫有的沒的速度又會慢XDXD
 * @author 1310081
 *
 */
public class ConfigFragment extends Fragment{

	static public String TAG = "ConfigFragment" ;

	CosRemoteV2 remote ;
	int ledNumber = 64 ;
	
	Switch swDebug ;
	SeekBar sbPixelCount ;
	SeekBar sbBrightness ;
	
	
    public ConfigFragment()
    {
    	

    }
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
    	View rootView = inflater.inflate(R.layout.fragment_config, container, false);

    	swDebug = (Switch) rootView.findViewById(R.id.swDebug ) ;
    	sbPixelCount = (SeekBar) rootView.findViewById(R.id.sbPixelCount ) ;
    	sbBrightness = (SeekBar) rootView.findViewById(R.id.sbBrightness) ;
    	
    	swDebug.setOnCheckedChangeListener( new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if ( remote != null ) {
					remote.sendDebug(isChecked) ;
				}
			}
		}) ;
    	
    	sbBrightness.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if ( remote != null ) {
					CosRemoteV2.brightness = progress ;
					remote.sendBrightness() ;
				}
			}
		}) ;
    	
    	sbPixelCount.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if ( remote != null ) {
					remote.stop() ;
					remote = null ;
				}
				ledNumber = progress ;
				getSppInNewThread();
			}
		}) ;
    	
        getSppInNewThread() ;
        return rootView;
    }
    
    public void getSppInNewThread()
    {
    	new Thread(){
    		@Override
    		public void run() {
    	    	try {
    	    		
    	    		BTSerialInterface spp = Common.getSppClient() ;
    	    		if ( spp != null)
    	    		{
    	    			remote = new CosRemoteV2( spp.getInputStream(), spp.getOutputStream(), ledNumber) ;
    	    			Log.i(TAG, "連線到Spp~~") ;
    	    			
    	    		}
    	    		else
    	    		{
    	    			Log.e(TAG, "spp is not connect @@" ) ;
    	    			remote = null ;
    	    		}
    	    		
    	    	}
    	    	catch (Exception ex)
    	    	{
    	    		remote = null ;
    	    		Common.showAlert("連線錯誤", "請先配對藍芽並啟動藍芽電源" + ex.getMessage()) ;
    	    	}
    		}
    	}.start() ;
    }

    @Override
    public void onDetach() {
    	if ( this.remote != null)
    	{
    		this.remote.running = false ;
    		this.remote = null ;
    	}
    	System.gc() ;
    	super.onDetach();
    }
}
