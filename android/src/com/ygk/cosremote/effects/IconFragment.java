package com.ygk.cosremote.effects;

import java.util.ArrayList;

import com.ygk.cosremote.*;
import com.ygk.cosremote.UrlAdapter.UrlAdapterItem;
import com.ygk.cosremote.R;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 關於App的Fragment ~~
 * @author 1310081
 *
 */
public class IconFragment extends Fragment implements OnItemClickListener{

	static public String TAG = "IconFragment" ;
	ArrayList<Bitmap> iconList = new ArrayList<Bitmap>() ;
	CosRemoteV2 remote ;
	int ledNumber = 64 ;
	
	
			
	
    public IconFragment()
    {
    	

    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
    	View rootView = inflater.inflate(R.layout.fragment_icon, container, false);
        GridView gridview = (GridView) rootView.findViewById(R.id.gridview);
        gridview.setAdapter(new ImageAdapter(getActivity()));

        gridview.setOnItemClickListener(this);
    	
        
        Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "pcsenior.ttf") ;
        int color = Color.WHITE ;
        if ( iconList.size() == 0 ) {
        	//String text = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz~`!@#$%^&*()_+=-1234567890{}[]\\/'\"|;:,.<>" ;
        	String text = "ABCDEFGHIJKLMNOPQRSTUVWXYZ~`!@#$%^&*()_+=-1234567890{}[]\\/'\"|;:,.<>" ;
        	for ( int i = 0 ; i < text.length() ; i++ ) {
        		
        		if ( i % 5 == 0 ) color = Color.WHITE ;
        		if ( i % 5 == 1 ) color = Color.RED ;
        		if ( i % 5 == 2 ) color = Color.GREEN ;
        		if ( i % 5 == 3 ) color = Color.BLUE ;
        		if ( i % 5 == 4 ) color = Color.YELLOW ;
        		
        		iconList.add( textAsBitmap( text.substring(i, i+1) , 8.0f, color, Color.BLACK, font ) );
        	}
        	
        }
    	getSppInNewThread() ; 
        return rootView;
        
    }
    
    public Bitmap textAsBitmap(String text, float textSize, int textColor,int background, Typeface font) {
        Paint paint = new Paint();
        paint.setTextSize(textSize);
        paint.setColor(textColor);
        //paint.setTextAlign(Paint.Align.CENTER);
        paint.setAntiAlias(false) ;
        paint.setTypeface(font) ;
        
        float baseline = -paint.ascent(); // ascent() is negative
        int width = (int) (paint.measureText(text) + 0.5f); // round
        int height = (int) (baseline + paint.descent() + 0.5f);
        width = height = 8 ;
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Log.e(TAG, "@@ icon text wh=" + width + "," + height ) ;
        
        Canvas canvas = new Canvas(image);
        canvas.drawColor(background) ;
        canvas.drawText(text, 0, 6, paint);
        
        return Bitmap.createScaledBitmap(image, 80, 80, false);
        //return image;
    }
    
    public class ImageAdapter extends BaseAdapter {
        private Activity mActivity;

        LayoutInflater inflater ;
        
        public ImageAdapter(Activity act) {
        	mActivity = act;
        	inflater = (LayoutInflater) this.mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
        	
        	return iconList.size() ;
            //return mThumbIds.length;
        }

        public Object getItem(int position) {
            return iconList.get(position) ;
        }

        public long getItemId(int position) {
            return position;
        }

        /*
                gridView = new View(activity);
        

        ImageView flag = (ImageView) gridView.findViewById(R.id.imageView);
        TextView webName = (TextView) gridView.findViewById(R.id.tvWebName);
        TextView webUrl = (TextView) gridView.findViewById(R.id.tvUrl);
        */
	        
        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
        	
        	ImageView ivImage = (ImageView) convertView ;
        	if ( convertView == null ) {
        		ivImage = (ImageView) inflater.inflate(R.layout.item_icon, null);
        	}
        	
        	/*
            ImageView imageView;
            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                imageView = new ImageView(mActivity);
                imageView.setLayoutParams(new GridView.LayoutParams(80, 80));
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setPadding(4, 4, 4, 4);
            } else {
                imageView = (ImageView) convertView;
            }
            */
        	ivImage.setImageBitmap( (Bitmap) getItem(position)) ;
            //imageView.setImageResource(mThumbIds[position]);
            return ivImage;
        }

        // references to our images
        private Integer[] mThumbIds = {
                R.drawable.ic_directions_bus_black_24dp,
                R.drawable.ic_launcher,
                R.drawable.ic_info_outline_black_48dp 
        };
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

    	    	    	int y = Color.YELLOW ;
    	    	    	int g = Color.GREEN ;
    	    	    	for ( int i = 0 ; i < 64 ; i++ ) {
    	    	    		if ( i % 2 == 0 ) {
    	    	    			remote.setPixelColor(i, Color.WHITE ) ;
    	    	    		}
    	    	    		else {
    	    	    			remote.setPixelColor(i, Color.BLACK ) ;
    	    	    		}
    	    	    	}
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

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
		Log.i(TAG, "OnItemClick @@ draw the bitmap @@") ;
		Bitmap b = this.iconList.get(position);
		
		sendBitmap(b) ;
	}

	private void sendBitmap(Bitmap bitmap) {
		
		if ( remote == null ) return ; 
		
		if ( bitmap.getWidth() != 8 || bitmap.getHeight() != 8 ) {
			bitmap =  Bitmap.createScaledBitmap(bitmap, 8, 8, false);
		}
		
		remote.setAllPixelColor(Color.BLACK) ;
		String screen = "\n" ;
		for ( int i = 0 ; i < 8 ; i++ )
		{
			for ( int j = 0 ; j < 8 ; j++ )
			{
				int c = bitmap.getPixel( i, j );
				if ( c == Color.BLACK ) 
				{
					screen += "#" ;
				}
				else
				{
					screen += "O" ;
				}
				
				if ( i % 2 == 0 )
				{
					// 正序
					remote.setPixelColor(i*8 + (j), c ) ;
				}
				else
				{
					// 反序
					remote.setPixelColor(i*8 + (7-j), c ) ;
				}
			}
			screen += "\n" ;
		}
		//Log.e( TAG, screen) ;
	}
}
