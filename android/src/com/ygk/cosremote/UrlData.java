package com.ygk.cosremote;

import org.json.JSONObject;

import android.util.Log;

public class UrlData {

	
	String url ;
	String icon ;
	String title ;
	String description ;
	
	public UrlData(String _url, String _icon, String _title, String _desc) {
		this.url = _url ;
		this.icon = _icon ;
		this.title = _title ;
		this.description = _desc ;
	}
	
	public UrlData( JSONObject obj)
	{
		try {
			this.url = obj.getString("url") ;
			this.icon = obj.getString("icon") ;
			this.title = obj.getString("title") ;
			this.description = obj.getString("desc") ;
		}
		catch (Exception ex)
		{
			Log.e("UrlData", "json parse error urldata:" + ex.getMessage()) ;
		}
	}

}
