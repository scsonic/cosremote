package com.ygk.cosremote;

import java.util.ArrayList;

import com.ygk.cosremote.R;
import com.ygk.cosremote.effects.AccFragment;
import com.ygk.cosremote.effects.ConfigFragment;
import com.ygk.cosremote.effects.FireFragment;
import com.ygk.cosremote.effects.LaserFragment;
import com.ygk.cosremote.effects.SimpleTestFragment;
import com.ygk.cosremote.effects.IconFragment ;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SelectFragment extends Fragment implements OnItemClickListener {

	static public String TAG = "SelectFragment" ;
	
	ListView lvSelect ;
	public SelectFragment() {
			
	}
	
	
	/**
	 * 暫存用
	 * @author 1310081
	 *
	 */
	static public class SelectPair {
		
		public SelectPair(String t, Fragment f, Bitmap b) {
			this.title = t ;
			this.fragment = f ;
			this.bitmap = b ;
		}
		String title ;
		Fragment fragment ;
		Bitmap bitmap ;
	}
	
	ArrayList<SelectPair> list = new ArrayList<SelectPair>() ;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_select, container,	false);
		
		lvSelect = (ListView) rootView.findViewById(R.id.lvSelect);
		
		if ( list.size() == 0 )
		{
			for ( int i = 0 ; i < SimpleTestFragment.testMethod.length ; i ++ ) 
			{
				list.add(new SelectPair(SimpleTestFragment.testMethod[i], new SimpleTestFragment(i), null)) ;
			}
			list.add(new SelectPair("三軸加速度感測器", new AccFragment(), null)) ;
			list.add(new SelectPair("火焰顏色控制", new FireFragment(), null)) ;
			list.add(new SelectPair("8x8 Led圖示集", new IconFragment(), null)) ;
			list.add(new SelectPair("雷射眼", new LaserFragment(), null)) ;
			
			
		}
		
		
		lvSelect.setAdapter( new SelectAdapter() ) ;
		lvSelect.setOnItemClickListener(this) ;
		
		return rootView;
	}
	
    @Override
    public void onDetach() {
    	//getFragmentManager().beginTransaction().remove(mapFragment).commit() ;
    	//this.mapFragment = null ;
    	super.onDetach();
    }
    
    
    
    @Override
    public void onStop() {
    
    	super.onStop();
    }
    
    public class SelectAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return list.size() ;
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
	        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        View view;
	        
	        if (convertView == null) 
	        {
	        	view = new View(getActivity());
	        	view = inflater.inflate(R.layout.select_item, null);    
	        } else {
	        	view = (View) convertView;
	        }
	        
	        TextView tvTitle = (TextView) view.findViewById(R.id.tvTitle) ;
	        TextView tvSubTitle = (TextView) view.findViewById(R.id.tvSubTitle) ;
	        
	        SelectPair data = (SelectPair) getItem(position);
	        tvTitle.setText( data.title ) ;
	        return view;
		}
    	
    }

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		MainActivity activity = (MainActivity) getActivity() ;
		activity.llTab.setVisibility(View.GONE) ;
		SelectPair data = list.get(position) ;
		android.app.FragmentTransaction trans = getFragmentManager().beginTransaction();
		trans.addToBackStack(null);
		trans.replace(R.id.container, data.fragment ).commit();
		
	}
}