package com.ygk.cosremote;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.graphics.Color;
import android.util.Log;

/**
 * LED控制器V1
 * @author 1310081
 *
 * 0518 
 * 新增 all command
 */
@Deprecated
public class OLD_CosRemoteV1 implements Runnable{

	static public String TAG = "CosRemoteV1" ;
	BufferedInputStream is ;
	BufferedOutputStream os ; // 要給這二個才能動
	int ledNumber = 0 ;
	boolean running = true ;
	boolean showAll = false ;
	Thread thread = null ;
	
	
	// 設定 255 是其他command 其他0-254是ws2812即時控制
	// 因為ws2812吃頻寬 所以才這樣設計
	
	
	int brite = 30 ; //亮度 global
	ByteArrayOutputStream bao = new ByteArrayOutputStream(65536) ;
	
	public ArrayList<WS2812> ledList = new ArrayList<WS2812>() ;
	//SppClient spp = null ; // 沒幹麻  放著
	Queue<byte []> commands = new ConcurrentLinkedQueue<byte []>();
	
	static public class WS2812 {
		int color ;
		int index ;
		int newColor ;
		
		public WS2812(int i) {
			this.index = i ;
		}
	}
	
	
	public OLD_CosRemoteV1(InputStream is, OutputStream os, int ledCount) {
		this.is = new BufferedInputStream(is) ; // 之後操作在這二個東西上
		this.os = new BufferedOutputStream(os) ; 
		
		setLedNumber( ledCount) ;
		thread = new Thread(this) ;
		thread.start() ;
	}

	private void setLedNumber(int ledCount) {
		ledList.clear() ;
		ledNumber = ledCount ;
		for ( int i = 0 ; i < ledNumber; i++)
		{
			ledList.add( new WS2812(i) ) ; // 設成未啟動
		}
	}

	public void setPixelColor(int index, int newColor)
	{
		WS2812 led = ledList.get(index) ;
		led.newColor = newColor ;
	}
	
	/**
	 * 透過特別參數 指定控制所有的ws2812
	 * @param color
	 */
	public void setAllPixelColor( int color)
	{
		byte [] b = new byte[4] ;
		b[0] = (byte) 255 ;
		b[1] = (byte) ( Color.red(color) * this.brite / 255);
		b[2] = (byte) ( Color.green(color) * this.brite / 255);
		b[3] = (byte) ( Color.blue(color) * this.brite / 255);
		commands.add(b) ;
	}
	
	public int getPixelColor(int index)
	{
		WS2812 led = ledList.get(index) ;
		return led.color ;
	}
	
	public void show() 
	{
		// 更新所有LED資料給
		showAll = true ;
	}
	
	
	@Override
	public void run() {
		
		boolean hasChange = false; 
		while ( running )
		{
			try {
				hasChange = false ;
				bao.reset() ;
				
				if ( showAll == false )
				{
					for ( WS2812 led : ledList)
					{
						if ( led.color != led.newColor )
						{
							hasChange = true ;
							bao.write(led.index) ;
							bao.write(Color.red(led.newColor) * this.brite / 255) ;
							bao.write(Color.green(led.newColor) * this.brite / 255) ;
							bao.write(Color.blue(led.newColor)* this.brite / 255 ) ;
							led.color = led.newColor ;
							Log.d(TAG, "Change Color:" + led.index + "," + led.newColor ) ;
						}
					}
				}
				else {
					showAll = false ;
					hasChange = true ;
					if ( showAll == true )
					{
						for ( WS2812 led : ledList)
						{
							led.color = led.newColor ;
							bao.write(led.index) ;
							bao.write(Color.red(led.newColor) * this.brite / 255) ;
							bao.write(Color.green(led.newColor) * this.brite / 255) ;
							bao.write(Color.blue(led.newColor)* this.brite / 255 ) ;
						}
					}
				}
				
				
				if ( hasChange )
				{
					byte[] b = bao.toByteArray() ;
					this.os.write(b) ;
					this.os.flush() ; 
				}
				else
				{
					Common.sleep(10) ;
				}
				
				// 如果有command的話 就要執行
				if ( commands.isEmpty() == false )
				{
					byte [] b = commands.poll() ;
					this.os.write(b) ;
					this.os.flush() ; 
				}
				
				if ( this.is.available() > 0 )
				{
					byte [] t = new byte[this.is.available()] ;
					this.is.read(t) ;
					Log.d(TAG, "讀到的資料:" + new String(t) ) ;
				}				
			}
			catch (Exception ex)
			{
				Log.e(TAG, "發現錯誤:" + ex.getMessage() ) ;
				running = false ;
			}
		}
		
//		try {
//			//this.is.close() ;
//			this.os.close() ;
//		} catch (IOException e) {
//			Log.e(TAG, "is os close fail") ;
//		}

	}
	
	public void stop()
	{
		running = false ;
	}
	


}
