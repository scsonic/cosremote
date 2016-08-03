package com.ygk.cosremote.effects;

import java.io.BufferedInputStream;
import java.io.IOException;

import com.ygk.cosremote.*;
import com.ygk.cosremote.R;

import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Bitmap.Config;
import android.graphics.Matrix;
import android.net.Uri;
import android.opengl.Visibility;
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
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * 關於App的Fragment ~~
 * @author 1310081
 *
 */
public class LaserFragment extends Fragment implements Runnable {

	static public String TAG = "LaserFragment" ;
	CosRemoteV2 remote ;
	ImageView ivColorPicker ;
	Bitmap bitmap ;
	RelativeLayout rlColorPicker ;
	TextView tvSelectResult ;
	boolean running = true ;
	
	int point = 0 ;
	int ledNumber = 60 ;

	int ivWidth ;
	int ivHeight ;
	
	int selectTestMethod = -1 ;
	
	long lastTouchTime = 0 ; // 防止更新太快
	int eyeColor = Color.RED ;
	boolean changeEyeColor = false ;
	
	int eyeLength = 5 ;
	int eyeSpeed = 250; // 250ms moveing once ;
	int eyeAt = 0 ; 
	int eyeDirection = 1 ;
	
	Thread thread = null ;
	
    public LaserFragment( )
    {
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
    	View rootView = inflater.inflate(R.layout.fragment_simple_test, container, false);
    	ivColorPicker = (ImageView) rootView.findViewById(R.id.ivColorPicker); 
    	rlColorPicker = (RelativeLayout) rootView.findViewById(R.id.rlColorPicker) ;
    	tvSelectResult = (TextView) rootView.findViewById(R.id.tvSelectResult);
    	
    	getSppInNewThread() ;
    	
    	this.bitmap = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.color_picker);
    	this.loadBitmap(bitmap) ;
    	
    	ivColorPicker.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				
				int x = (int) event.getX() ;
				int y = (int) event.getY() ;
				Log.d(TAG, "得到xy=" + x + "," + y ) ;
				
				if ( System.currentTimeMillis() - lastTouchTime < 50 ) {
					// 太短了 不準更新
					return true ;
				}
				else {
					lastTouchTime = System.currentTimeMillis() ;
				}
				
				// 轉為bitmap xy;
				x = x * bitmap.getWidth() / ivWidth;
				y = y * bitmap.getHeight() / ivHeight ;
				
				Log.d(TAG, "得到 轉換後 xy=" + x + "," + y + "圖大小" + bitmap.getWidth() + bitmap.getHeight() ) ;
				if ( bitmap.getWidth() <= x || bitmap.getHeight() <= y )
					return true ;
				if ( x < 0 || y < 0)
					return true ;
				
				int c = bitmap.getPixel( x, y );
				int r = Color.red(c);
				int b = Color.blue(c);
				int g = Color.green(c);
				if ( remote != null )
				{
					eyeColor = c ;
					changeEyeColor = true ;
				}
				tvSelectResult.setBackgroundColor(c) ;
				tvSelectResult.setText(String.format("(%d,%d,%d)", r,g,b)) ;

				return true;
			}
			
		});
    	
    	this.thread = new Thread(this) ;
    	this.thread.start() ;
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

	public void loadBitmap(Bitmap bitmap)
    {
		this.bitmap = bitmap ;

    	ViewTreeObserver vto = ivColorPicker.getViewTreeObserver();
    	vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
    	    public boolean onPreDraw() {
    	    	ivColorPicker.getViewTreeObserver().removeOnPreDrawListener(this);
    	    	ivHeight = ivColorPicker.getMeasuredHeight();
    	    	ivWidth = ivColorPicker.getMeasuredWidth();
    	    	Log.i(TAG, "ImageView wh=" + ivWidth + "," + ivHeight ) ;
    	        return true;
    	    }
    	});
    	 
    	ivColorPicker.requestLayout() ;
    	ivColorPicker.getLayoutParams().height = 20;

    	
    	ivColorPicker.setImageBitmap(bitmap) ;
    }
    
	public static Bitmap scaleBitmapToWindow(Bitmap bitmapToScale) {   
		
		if(bitmapToScale == null)
		    return null;
		int width = bitmapToScale.getWidth();
		int height = bitmapToScale.getHeight();
		
		int newWidth = 1080 ;
		int newHeight = width * height / 1080 ;
		Matrix matrix = new Matrix();

		matrix.postScale(newWidth / width, newHeight / height);
		return Bitmap.createBitmap(bitmapToScale, 0, 0, bitmapToScale.getWidth(), bitmapToScale.getHeight(), matrix, true);  
	}
	
    @Override
    public void onDetach() {
    	if ( this.remote != null)
    	{
    		this.remote.running = false ;
    		this.remote = null ;
    	}
    	running = false ;
    	System.gc() ;
    	super.onDetach();
    }

    
	@Override
	public void run() {
		
		while ( running ) {
			Common.sleep(this.eyeSpeed) ;
			eyeAt += eyeDirection ;
			
			if (eyeAt == 0 ) {
				eyeDirection = 1 ;
				drawEye() ;
			}
			else if (eyeAt >= ledNumber -1 ){ 
				eyeDirection = -1 ;
				drawEye() ;
			}
			
			remote.shift(eyeDirection) ;
			
			if ( changeEyeColor == true ) {
				eyeAt = 0 ;
				eyeDirection = 1 ;
				
			}
		}
	}
	
	public void drawEye() {
		remote.setAllPixelColor(Color.BLACK) ;
		int c = eyeColor ;
		
		// at = 0 /;
		if ( eyeDirection == 1 ) {
			// draw max-min 5---0
			for ( int i = eyeLength ; i >= 0 ; i -- ) {
				remote.setPixelColor(i, c) ;
				c = darker( c, 0.8f ) ;
			}
		}
		else if ( eyeDirection == -1) {
			// at = len -1 ;
			//draw len...len-5
			for ( int i = ledNumber-eyeLength ; i < ledNumber ; i ++ ) {
				remote.setPixelColor(i, c) ;
				c = darker( c, 0.8f ) ;
			}
		}
	}
	
	public static int darker (int color, float factor) {
	    int a = Color.alpha( color );
	    int r = Color.red( color );
	    int g = Color.green( color );
	    int b = Color.blue( color );

	    return Color.argb( a,
	            Math.max( (int)(r * factor), 0 ),
	            Math.max( (int)(g * factor), 0 ),
	            Math.max( (int)(b * factor), 0 ) );
	}
}
