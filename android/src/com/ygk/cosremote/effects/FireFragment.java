package com.ygk.cosremote.effects;

import com.ygk.cosremote.*;
import com.ygk.cosremote.R;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 關於App的Fragment ~~
 * @author 1310081
 *
 */
public class FireFragment extends Fragment{

	static public String TAG = "FireFragment" ;

	boolean isChangingBitmap = false ;
	
	CosRemoteV2 remote ;
	ImageView ivColorPicker ;
	Bitmap bitmap ;
	RelativeLayout rlColorPicker ;
	TextView tvSelectResult ;
	
	int point = 0 ;
	int ledNumber = 64 ;

	int ivWidth ;
	int ivHeight ;
	
	int fireColor ;
	
    public FireFragment()
    {
    	

    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
    	View rootView = inflater.inflate(R.layout.fragment_fire, container, false);
    	ivColorPicker = (ImageView) rootView.findViewById(R.id.ivColorPicker); 
    	rlColorPicker = (RelativeLayout) rootView.findViewById(R.id.rlColorPicker) ;
    	tvSelectResult = (TextView) rootView.findViewById(R.id.tvSelectResult);
    	
    	this.bitmap = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.color_picker);
    	this.loadBitmap(bitmap) ;
    	
    	ivColorPicker.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				
				int x = (int) event.getX() ;
				int y = (int) event.getY() ;
				Log.d(TAG, "得到xy=" + x + "," + y ) ;
				
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
					methodFire(c);
				}
				tvSelectResult.setBackgroundColor(c) ;
				tvSelectResult.setText(String.format("(%d,%d,%d)", r,g,b)) ;
				toHSL( c ) ;

				return true;
			}
			
		});
    	
    	getSppInNewThread() ; 
        return rootView;
        
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
    	    	
    	ivColorPicker.setImageBitmap(bitmap) ;
    }
    
    public void methodFire(int c)
    {
    	remote.setAllPixelColor(c) ;
    }
    
    public void toHSL(int c)
    {
    	float[] hsv = new float[3];
    	Color.RGBToHSV(Color.red(c), Color.green(c), Color.blue(c), hsv);
    	Log.i(TAG, "HSV H=" + hsv[0]); 
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
