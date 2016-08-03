package com.ygk.cosremote;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ygk.cosremote.R;
import com.ygk.cosremote.UrlAdapter.UrlAdapterItem;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

/**
 * 關於App的Fragment ~~
 * @author 1310081
 *
 */
public class AppFragment extends Fragment implements OnItemClickListener{

	static public String TAG = "App Fragment" ;
	UrlAdapter urlAdapter = null ;
	ArrayList<UrlAdapterItem> urlList = new ArrayList<UrlAdapterItem>() ;
	ListView listview = null ;
	TextView tvLoading = null ;
	
	static public String LINKS_JSON = "LINKS_JSON" ;

    public AppFragment()
    {
    	// 先備份到Storage中
    	Log.i(TAG, "建了App Fragment") ;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_app, container, false);
        listview = (ListView) rootView.findViewById(R.id.urlListView);
        tvLoading = (TextView) rootView.findViewById(R.id.tvLoading);
        
        urlAdapter = new UrlAdapter(getActivity(), urlList) ;
        listview.setAdapter( this.urlAdapter ) ;
        listview.setOnItemClickListener(this);
        
        if ( urlList.size() <= 0 ) {
        	tvLoading.setVisibility(View.VISIBLE) ;
        }
        else {
        	tvLoading.setVisibility(View.GONE) ;
        }
        
        new Thread(){
        	public void run() {
        		loadUrlFromServer() ;
        	};        	
        }.start() ;
        
        return rootView;
    }

    public void loadUrlFromServer()
    {

    	try {
    		String url = "http://128.199.211.104/mycosalbum/v3/links.php?type=android";
    		String result = Common.http_get(url) ;
    		Log.i(TAG, "links=" + result) ;
    		JSONArray arr = new JSONArray(result);
    		
    		this.urlList.clear() ;
    		
    		for ( int i = 0 ; i < arr.length() ; i++)
    		{
    			JSONObject link = arr.getJSONObject(i);
    			
    			this.urlList.add( 
    					new UrlAdapterItem(
    					link.getString("title"), 
    					link.getString("url"),
    					link.getString("icon"),
    					link.getString("desc"))) ;
    			
    			Log.i("LINKS", "建了:" + link.getString("title") );
    		}
    		
    		

    		
    		getActivity().runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					tvLoading.setVisibility(View.GONE) ;
					urlAdapter.notifyDataSetChanged() ;
				}
			});
    	}
    	catch (Exception ex)
    	{
    		Log.e("LINKS", "無法從json建item list" + ex.getMessage());
    		urlList.clear();
    		Activity activity = getActivity() ;
    		if ( activity != null ) {
	    		getActivity().runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						MainActivity.showQueryError() ;
					}
				});
    		}
    	}
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    	
    	UrlAdapterItem item = this.urlList.get(position);
    	
    	if ( item.url != null)
    	{
    		if ( item.url.startsWith("http"))
    		{
    			Intent i = new Intent(Intent.ACTION_VIEW);
    			i.setData(Uri.parse( item.url ));
    			startActivity(i);
    		}
    		else if ( item.url.contains("@"))
    		{
    			Uri uri = Uri.parse("mailto:" + item.url);
    			Intent it = new Intent(Intent.ACTION_SENDTO, uri);
    			startActivity(it);
    		}
    		else
    		{
    			// do nothing ~~
    		}
    	}
    }
}
