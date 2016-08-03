package com.ygk.cosremote.effects;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.List;

import com.ygk.cosremote.*;
import com.ygk.cosremote.R;

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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * 連接 加速度感測器，只動前x個LED 不然要重寫有的沒的速度又會慢XDXD
 * @author 1310081
 *
 */
public class AccFragment extends Fragment{

	static public String TAG = "ConnectFragment" ;

	boolean isChangingBitmap = false ;
	
	CosRemoteV2 remote ;
	int ledNumber = 64 ;

	TextView tvX ;
	ProgressBar pbX ;
	TextView tvY ;
	ProgressBar pbY ;
	TextView tvZ ;
	ProgressBar pbZ ;
	
	TextView tvColorPreview;
	TextView tvColorText; 
	
	Sensor accelerometerSensor ;
	SensorManager sensorManager ;
	long lastRefreshTime = 0 ;
	
    public AccFragment()
    {
    	

    }
    
    
    int r ;
    int g ;
    int b ;
    private SensorEventListener accelerometerListener = new SensorEventListener(){
    	 
    	 @Override
    	 public void onAccuracyChanged(Sensor arg0, int arg1) {
    	  // TODO Auto-generated method stub
    	  
    	 }
		@Override
		public void onSensorChanged(SensorEvent event) {
			Log.i("ACC", String.valueOf(event.values[0]) );
			
			r = normalize( event.values[0]) ;
			g = normalize( event.values[1]) ;
			b = normalize( event.values[2]) ;
			
			tvX.setText( "X:" + r) ;
			tvY.setText( "Y:" + g) ;
			tvZ.setText( "Z:" + b) ;
			
			pbX.setProgress(r) ;
			pbY.setProgress(g) ;
			pbZ.setProgress(b) ;
			
			pbX.getProgressDrawable().setColorFilter(Color.rgb(r, 0, 0), Mode.SRC_IN);
			pbY.getProgressDrawable().setColorFilter(Color.rgb(0, g, 0), Mode.SRC_IN);
			pbZ.getProgressDrawable().setColorFilter(Color.rgb(0, 0, b), Mode.SRC_IN);
			
			tvColorText.setText("目前顏色" + String.format("#%02x%02x%02x", r, g, b)) ;
			tvColorPreview.setBackgroundColor(Color.rgb(r, g, b));

			updateColor() ;
		}

    };
    
    
    /**
     * 更新顏色 但要用時間區別 不然怕趕不上 太快了
     */
	private void updateColor()
	{
		if ( remote != null)
		{
			if ( System.currentTimeMillis()-lastRefreshTime > 100)
			{
				lastRefreshTime = System.currentTimeMillis();
				// 超過才可以變色

				remote.setAllPixelColor(Color.rgb(r, g, b)) ;
			}
		}
	}
	
	
	/**
	 * 看數值是 -10~+10 中間 
	 * 轉為0~255
	 * @param f
	 * @return
	 */
    public int normalize( float f)
    {
    	return (int) ( (f + 10.0) / 20 * 255) % 256 ;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
    	View rootView = inflater.inflate(R.layout.fragment_acc, container, false);
    	tvX = (TextView) rootView.findViewById(R.id.tvX);
    	pbX = (ProgressBar) rootView.findViewById(R.id.pbX ) ;
    	tvY = (TextView) rootView.findViewById(R.id.tvY);
    	pbY = (ProgressBar) rootView.findViewById(R.id.pbY ) ;
    	tvZ = (TextView) rootView.findViewById(R.id.tvZ);
    	pbZ = (ProgressBar) rootView.findViewById(R.id.pbZ ) ;
    	
    	tvColorText = (TextView) rootView.findViewById(R.id.tvColorText);
    	tvColorPreview = (TextView) rootView.findViewById(R.id.tvColorPreview); 
    	sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
       
        if(sensorList.size() > 0){
	         accelerometerSensor = sensorList.get(0);
	         sensorManager.registerListener(accelerometerListener, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
        }
        else{
        }
   
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
    	if ( accelerometerListener != null)
    	{
    		sensorManager.unregisterListener(accelerometerListener);
    	}
    	if ( this.remote != null)
    	{
    		this.remote.running = false ;
    		this.remote = null ;
    	}
    	System.gc() ;
    	super.onDetach();
    }
}
