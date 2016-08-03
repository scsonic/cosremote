package com.ygk.cosremote;

import java.util.ArrayList;
import java.util.HashMap;

import com.applidium.shutterbug.downloader.ShutterbugDownloader;
import com.applidium.shutterbug.utils.ShutterbugManager;
import com.ygk.cosremote.R; 
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * 拿來網站、網址的adapter
 */
public class UrlAdapter extends BaseAdapter{
    private Activity activity ;
    static public String TAG = "UrlAdapter" ;
    ArrayList<UrlAdapterItem> urlList ;
    
    HashMap<ImageView, Integer> displayMap= new HashMap<ImageView, Integer>();
    
    static public class UrlAdapterItem{
       	public UrlAdapterItem(String n, String u, String i, String d) {
			this.name = n ;
			this.url = u ;
			this.icon = i ;
			this.description = d ;
		}
    	
    	
    	String name ;
    	String url ;
    	String icon ; // 可以用公開的image reader作讀取 @@
    	String description ; 
    }
    
    // 拿來存 要更新的數字
    Object refreshLock = new Object() ;
    
    /**
     * 需要 更新data set的話 設個flag
     */
    boolean needNotifyDataSetChanged = false ;

    /**
     * 建構子
     * @param _context
     * @param _list
     * @param _mode
     */
    public UrlAdapter(Activity activity, ArrayList<UrlAdapterItem> list) {
    	this.activity = activity ;
    	this.urlList = list ;
    }
    
    
    /**
     * 當adapter要get view時
     * 先顯示loading圖示
     * 等
     */
    @SuppressWarnings("deprecation")
	@SuppressLint("InflateParams")
	public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) this.activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View gridView;
        
        UrlAdapterItem item = this.urlList.get(position) ;
        
        if ( item == null )
        {
        	Log.e(TAG, "item = null return;") ;
        	return null ;
        }
        
        gridView = new View(activity);
        gridView = inflater.inflate(R.layout.urladapter_item, null);

        ImageView flag = (ImageView) gridView.findViewById(R.id.imageView);
        TextView webName = (TextView) gridView.findViewById(R.id.tvWebName);
        TextView webUrl = (TextView) gridView.findViewById(R.id.tvUrl);
	        
        webName.setText( item.name ) ;
        webUrl.setText( item.url + "\n" + item.description ) ;

        if ( item.icon != null && item.icon.equals(""))
        	flag.setImageResource(R.drawable.ic_launcher); 
        else
        	ShutterbugManager.getSharedImageManager(activity).download(item.icon, flag) ;
        
        return gridView;
    }
    
    @Override
    public int getCount() {
        return this.urlList.size() ;
    }

    @Override
    public Object getItem(int position) {
        return this.urlList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}