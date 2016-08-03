package com.ygk.cosremote;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Locale;
import javax.net.ssl.HttpsURLConnection;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.BitmapFactory;
import android.util.Log;

public class Common
{
	static public SppClient spp = null ;
	static public BLEClient bleSpp = null ;
	
    static public String TAG = "Common" ;
    static public Activity activity ;
    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.TAIWAN);
    
    static public void Init_Common(Activity activity)
    {
    	Common.activity = activity ;
    }
    
    static public void sleep(int ms)
    {
    	try {
			Thread.sleep(ms) ;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    static public BTSerialInterface getSppClient()
    {
    	if ( spp != null )
    	{
    		return spp ;
    	}
    	if ( bleSpp != null )
    	{
    		return bleSpp ;
    	}
    	else {
    		try {
				spp = new SppClient() ;
				spp.connect( Common.getLastBTDevice() ) ;
			} catch (Exception e) {
				Log.e(TAG, "Spp Can't Connect" + e.getMessage()) ;
				return null ;
			}
    	}
    	return spp ;
    	
    }
    
    static public String getServerUrl()
    {
    	return "http://128.199.211.104/cosremote/" ;
    }

    
    
    /**
     * 備份root路徑 
     */
    static public boolean writeSharePerf(String tag, String data)
    {
    	if ( data == null ) return false ;
    	
        try {
            SharedPreferences pref = activity.getSharedPreferences(tag, Context.MODE_PRIVATE);
            Editor editor = pref.edit() ;
            editor.putString(tag, data ) ;
            editor.commit() ;
            editor.apply() ;
            return true; 
        }
        catch (Exception ex)
        {
        	Log.e("SharePref" , "存檔失敗" + ex.getMessage()) ;
            return false ;
        }
        
    }
    
    static public String LastBTDevice = "LastBTDevice" ;
    static public String getLastBTDevice()
    {
    	return Common.readSharePerf(LastBTDevice) ;
    }
    
    static public void saveLastBTDevice(String address)
    {
    	Common.writeSharePerf(LastBTDevice, address) ;
    }
    
    /**
     * 備份root路徑 
     */
    static public String readSharePerf( String tag)
    {
        SharedPreferences pref = activity.getSharedPreferences(tag, Context.MODE_PRIVATE);
        String data = pref.getString(tag, "") ;
        
        if ( data.equalsIgnoreCase(""))
            return null ;
        else
            return data ;
    }
    
    
    static public String tempTitle, tempMsg ;
    static public void showAlert(String title, String msg)
    {
    	tempTitle = title ;
    	tempMsg = msg ;
    	activity.runOnUiThread( new Runnable() {
			
			@Override
			public void run() {
				AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
				 
		        alertDialog.setTitle(tempTitle);
		        alertDialog.setMessage( tempMsg );
		 
		        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog,int which) {
		            	dialog.cancel();
		            }
		        });
		        
		        alertDialog.show() ;
			}
		});

    }

    static public String http_get(String _url) throws IOException 
    {      
        int responseCode = -1 ;
        try {
            URL url = new URL(_url) ;
            
            ByteArrayOutputStream tmpOut = new ByteArrayOutputStream();
            

            InputStream is;
            if ( _url.startsWith("https") )
            {
            	HttpsURLConnection httpsConnection = (HttpsURLConnection) url.openConnection() ;
            	httpsConnection.setRequestMethod("GET") ;
            	
            	is = httpsConnection.getInputStream() ;
            }
            else
            {
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                is = con.getInputStream() ;
            }
            

            if ( is != null)
            {
                byte[] buf = new byte[512];
                int len = -1 ;
                while (true) {
                    len = is.read(buf);
                    if (len == -1) {
                        break;
                    }
                    tmpOut.write(buf, 0, len);
                }
                tmpOut.close();
                is.close();
            }
            else
            {
                //Log.e(TAG, "is = null") ;
            }

            String result ;
            try {
                result = new String(tmpOut.toByteArray(), "UTF-8") ;
                //Log.i(TAG, url + " raw data:" + result ) ;
                return result;
            }
            catch (Exception ex)
            {
                //Log.e(TAG, "result to String error:" + ex.getMessage()) ;
                return "";
            }
            
            
        } catch (IOException e) {
            //Log.e(TAG, e.getMessage() + ", responseCode=" + responseCode ) ;
            throw new IOException("網路出錯" + e.getMessage()) ;
        }
    }
    
	static public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) 
    {
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;
	    
	    if (height > reqHeight || width > reqWidth) {
	
	        final int halfHeight = height / 2;
	        final int halfWidth = width / 2;
	
	        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
	        // height and width larger than the requested height and width.
	        while ((halfHeight / inSampleSize) > reqHeight
	                && (halfWidth / inSampleSize) > reqWidth) {
	            inSampleSize *= 2;
	        }
	    }

	    return inSampleSize;
	}
	
	static public boolean isSDK43UP()
	{
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
		     return true; 
		}
		return false;
	}
}
