package com.ygk.cosremote;

import com.ygk.cosremote.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity implements Runnable {
	
	static public String TAG = "CosRemoteActivity" ;
	static public boolean needRefresh = true ;
	
	TextView tvTabSelect ;
	TextView tvTabConnect ;
	TextView tvTabLinks ;
	LinearLayout llTab ;
	
	
	PlaceholderFragment mainFragment ;
	AppFragment appFragment ;
	//SelectFragment sFragment ;
	//ConnectFragment cFragment; 
	
	Thread thread = null ;
	boolean running = true ;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		
		MainActivity.activity = this ;
		Common.Init_Common(this) ;
		
		mainFragment = new PlaceholderFragment();
		appFragment = new AppFragment() ;
		//sFragment = new SelectFragment() ;
		//cFragment = new ConnectFragment() ;
		
        this.tvTabSelect = (TextView) findViewById(R.id.tvTabSelect);
        this.tvTabLinks = (TextView) findViewById(R.id.tvTabLinks);
        this.tvTabConnect = (TextView) findViewById(R.id.tvTabConnect);
        this.llTab = (LinearLayout) findViewById(R.id.llTab) ;
        
        tvTabSelect.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				clearFragmentBackStack() ;
				android.app.FragmentTransaction trans = getFragmentManager().beginTransaction();
				//trans.remove(sFragment) ;
				//trans.addToBackStack(null);
				trans.replace(R.id.container, new SelectFragment() ).commit();
			}
		}) ;
        
        tvTabConnect.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				clearFragmentBackStack() ;
				FragmentManager manager = getFragmentManager();
				android.app.FragmentTransaction trans = getFragmentManager().beginTransaction();
				//trans.addToBackStack(null);
				//trans.remove(cFragment); // 移掉舊的
				trans.replace(R.id.container, new ConnectFragment() ).commit();
			}
		}) ;
        
        tvTabLinks.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				clearFragmentBackStack() ;
				android.app.FragmentTransaction trans = getFragmentManager().beginTransaction();
				//trans.addToBackStack(null);
				trans.remove(appFragment); // 移掉舊的
				trans.replace(R.id.container, appFragment ).commit();
			}
		}) ;      
        
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction().add(R.id.container, mainFragment ).commit();
		}
		
		this.thread = new Thread(this);
		//this.thread.start() ;
	}
	
	
	public void run() {
		while ( running )
		{
			Common.sleep(2000) ;
		}
	};
	
	static public Activity activity ;
	
    static public void showQueryError()
    {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
		 
        alertDialog.setTitle("出現錯誤");
        alertDialog.setMessage( "讀取時發生錯誤，可能是網路出錯");
 
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
            	dialog.cancel();
            }
        });
        
        alertDialog.show() ;
    }
    
    /**
     * 切換tab時 把所有back stack清掉
     */
    public void clearFragmentBackStack()
    {
    	android.app.FragmentManager fm = getFragmentManager();
    	for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {    
    	    fm.popBackStack();
    	}
    }
    
    @Override
    public void onBackPressed() {
    	llTab.setVisibility(View.VISIBLE) ;
    	super.onBackPressed();
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		return super.onOptionsItemSelected(item);
	}
	

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment{

		public PlaceholderFragment() {
		}

		static public String TAG = "MainFragment" ;
		
		TextView tvIntro ;
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container, false);
			
			tvIntro = (TextView) rootView.findViewById(R.id.tvIntro) ;
			
			tvIntro.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.facebook.com/MyCosAlbum/"));
					startActivity(browserIntent);
				}
			}) ;
			// just show logo

			return rootView;
		}
		
		@Override
		public void onAttach(Activity activity) {

			super.onAttach(activity);
		}
	}
	
	
	@Override
	protected void onStop() {
		// 加這裡加錯了幹 
		super.onStop();
	}

	
	@SuppressLint("NewApi")
	@Override
	protected void onDestroy() {
		running = false ;
		
		if ( Common.spp != null )
		{
			Common.spp.disconnect() ;
		}
		Common.spp = null ;
		
		
		if ( Common.bleSpp != null )
		{
			if ( Common.isSDK43UP() ) {
				Common.bleSpp.mBluetoothGatt.disconnect() ;
			}
		}

		super.onDestroy();
	}
	
	
}
