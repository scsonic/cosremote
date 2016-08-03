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
public class SimpleTestFragment extends Fragment{

	static public String TAG = "SimpleTestFragment" ;

	boolean isChangingBitmap = false ;
	
	static public String[] testMethod = {"Led燈條跑馬燈-1", "Led燈條跑馬燈-2", "單一顏色變化", "8x8 Led陣列"};
	static public int[] testMethodLedNumber = {30,30,60,64 } ;
	CosRemoteV2 remote ;
	ImageView ivColorPicker ;
	Bitmap bitmap ;
	RelativeLayout rlColorPicker ;
	TextView tvSelectResult ;
	TextView tvMethodName ;
	
	Button btnChangeBitmap ;
	Spinner spTestMethod ;
	int point = 0 ;
	int ledNumber = 60 ;

	int ivWidth ;
	int ivHeight ;
	
	int selectTestMethod = -1 ;
	
	long lastTouchTime = 0 ; // 防止更新太快
	
    public SimpleTestFragment( int method )
    {
    	this.selectTestMethod = method ;
    	this.ledNumber = SimpleTestFragment.testMethodLedNumber[method] ;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
    	View rootView = inflater.inflate(R.layout.fragment_simple_test, container, false);
    	ivColorPicker = (ImageView) rootView.findViewById(R.id.ivColorPicker); 
    	rlColorPicker = (RelativeLayout) rootView.findViewById(R.id.rlColorPicker) ;
    	tvSelectResult = (TextView) rootView.findViewById(R.id.tvSelectResult);
    	btnChangeBitmap = (Button) rootView.findViewById(R.id.btnChangeBitmap);
    	spTestMethod = (Spinner) rootView.findViewById(R.id.spTestMethod);
    	tvMethodName = (TextView) rootView.findViewById(R.id.tvMethodName); 
    	
    	btnChangeBitmap.setOnClickListener(onLoadBitmapListener) ;
    	
    	setSpinner() ;
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
					if ( spTestMethod.getSelectedItemPosition() == 0 )
					{
						//method3(x,y) ;
						method1(c) ;
					}
					else if ( spTestMethod.getSelectedItemPosition() == 1 )
					{	
						method0(c) ;
					}
					else if ( spTestMethod.getSelectedItemPosition() == 2 )
					{	
						method2(c) ;
					}
					else if ( spTestMethod.getSelectedItemPosition() == 3 )
					{	
						method3(x,y) ;
					}
					else
					{
						Log.d(TAG , "沒有對應的select");
					}
				}
				tvSelectResult.setBackgroundColor(c) ;
				tvSelectResult.setText(String.format("(%d,%d,%d)", r,g,b)) ;

				return true;
			}
			
		});
    	
        return rootView;
    }

    
    OnClickListener onLoadBitmapListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("image/*");
			startActivityForResult(intent, 12345);
		}
	};
	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		Log.i(TAG, "onActivity Result" + requestCode + "," + resultCode) ;
		if ( requestCode == 12345 && data != null && data.getData() != null )
	    {
	    	Uri uri = data.getData();  
            Log.e(TAG, "Get URI=" + uri.toString());  
            ContentResolver cr = getActivity().getContentResolver();
            Bitmap bitmap ;
            
            try {  
            	BufferedInputStream bis = new BufferedInputStream(cr.openInputStream(uri));
            	
                // First decode with inJustDecodeBounds=true to check dimensionsspTestMethod
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Config.ARGB_8888;
                options.inJustDecodeBounds = true;
                options.inMutable = true ;
                
                //BitmapFactory.decodeResource(res, resId, options);
                BitmapFactory.decodeStream(bis, null, options) ;
                
                
                // Calculate inSampleSize
                options.inSampleSize = Common.calculateInSampleSize(options, 800, 800);
                bis.close() ;
                bis = new BufferedInputStream( cr.openInputStream(uri) );
                Log.i(TAG, "inSampleSize=" + options.inSampleSize);
                
                // Decode bitmap with inSampleSize set
                options.inJustDecodeBounds = false;
                
                try {
                	bitmap = BitmapFactory.decodeStream(bis, null, options);
                	loadBitmap(bitmap) ;
                }
                catch (OutOfMemoryError error)
                {
                	Common.showAlert("Error", "Image to big, try smaller one") ;
                }

            } catch ( IOException e) {  
                Log.e("Exception", e.getMessage(),e);  
            }  
	    }
        System.gc() ;
		super.onActivityResult(requestCode, resultCode, data);
	}
    
    public void method0(int c)
    {
		try {
			point ++ ;
			point = point % remote.ledNumber ;
			remote.setPixelColor(point, c) ;
		}
		catch (Exception ex)
		{
			Log.e(TAG, "Color Write Error" + ex.getMessage() ) ;
		}
    }
    
    

    public void method1(int c)
    {
		try {
			
			remote.setPixelColor(0, c) ;
			remote.shift(1) ;
			/*
			for ( int i = 1 ; i < remote.ledNumber ; i++) 
			{
				remote.setPixelColor(i, remote.getPixelColor(i-1) ) ;
			}
			*/
		}
		catch (Exception ex)
		{
			Log.e(TAG, "Color Write Error" + ex.getMessage() ) ;
		}
    }
    
    public void method2(int c)
    {
		try {
			remote.setAllPixelColor(c) ;
		}
		catch (Exception ex)
		{
			Log.e(TAG, "Color Write Error" + ex.getMessage() ) ;
		}
    }
    
    
    int [][] matrix = new int[8][8] ;
    /**
     * 8x8 led matrix XD
     * @param x
     * @param y
     */
    public void method3(int x, int y)
    {
		try {
			int c = 0 ;
			int xx = 0 ;
			int yy = 0 ;
			for ( int i = 0 ; i < 8 ; i++)
			{
				for ( int j = 0 ; j < 8 ; j++ )
				{
					xx = x + ( i - 4) * 3 ; // 放大三倍 不然看不出差別
					yy = y + ( j - 4) * 3 ;
					if ( bitmap.getWidth() <= xx || bitmap.getHeight() <= yy )
						c = 0 ;
					else if ( xx < 0 || yy < 0)
						c = 0 ;
					else {
						c = bitmap.getPixel(xx, yy) ;
					}
					
					matrix[i][j] = c ;
				}
			}
			
			// set color
			int p = 0 ;
			for ( int i = 0 ; i < 8 ; i++ )
			{
				for ( int j = 0 ; j < 8 ; j++ )
				{
					if ( i % 2 == 0 )
					{
						// 正序
						remote.setPixelColor(i*8 + (j), matrix[i][j] ) ;
					}
					else
					{
						// 反序
						remote.setPixelColor(i*8 + (7-j), matrix[i][j] ) ;
					}
				}
			}
		}
		catch (Exception ex)
		{
			Log.e(TAG, "Color Write Error" + ex.getMessage() ) ;
		}
    }
    
    
    private void setSpinner() {
    	
    	ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, testMethod);
    	spTestMethod.setAdapter(adapter);
    	
    	if ( this.selectTestMethod != -1 ) {
    		spTestMethod.setSelection(this.selectTestMethod) ;
    		
    		tvMethodName.setText( this.testMethod[ this.selectTestMethod] ) ;
    		spTestMethod.setVisibility(View.GONE) ;
    	}
    	
    	spTestMethod.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if ( remote != null )
				{
					remote.stop();
					remote = null ;
				}
				if ( position == 0 )
					ledNumber = 30 ;
				else if ( position == 1)
					ledNumber = 60 ;
				else if ( position == 2 )
					ledNumber = 60 ;
				else if ( position == 3 )
					ledNumber = 64 ;
				getSppInNewThread() ;
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
    		
		}) ;
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
    	System.gc() ;
    	super.onDetach();
    }
}
