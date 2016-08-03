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
 * LED���V1
 * @author 1310081
 *
 * 0518 
 * �s�W all command
 */
public class CosRemoteV2 implements Runnable{

	static public String TAG = "CosRemoteV2" ;
	BufferedInputStream is ;
	BufferedOutputStream os ; // �n���o�G�Ӥ~���
	public int ledNumber = 0 ;
	public boolean running = true ;
	boolean showAll = false ;
	Thread thread = null ;
	
	static public int COMMAND = 252 ;
	static public int CMD_PIXEL_COUNT = 1 ;
	static public int CMD_BRITENESS = 2 ;
	static public int CMD_DEBUG = 3; 
	
	 
	static public int ALLLED = 255;
	static public int SKIP = 254;
	static public int SHIFT = 253;
	
	// �]�w 255 �O��Lcommand ��L0-254�Ows2812�Y�ɱ���
	// �]��ws2812�Y�W�e �ҥH�~�o�˳]�p
	
	
	static public int brightness = 200 ; //�G�� global
	ByteArrayOutputStream bao = new ByteArrayOutputStream(65536) ;
	
	public ArrayList<WS2812> ledList = new ArrayList<WS2812>() ;
	//SppClient spp = null ; // �S�F��  ���
	Queue<byte []> commands = new ConcurrentLinkedQueue<byte []>();
	
	static public class WS2812 {
		int color ;
		int index ;
		//int newColor ;
		boolean isChange = false ;
		
		public WS2812(int i) {
			this.index = i ;
		}
	}
	
	
	public CosRemoteV2(InputStream is, OutputStream os, int ledCount) {
		this.is = new BufferedInputStream(is) ; // ����ާ@�b�o�G�ӪF��W
		this.os = new BufferedOutputStream(os) ; 
		
		
		setLedNumber( ledCount) ;
		sendLedNumber() ;
		sendBrightness() ;
		
		thread = new Thread(this) ;
		thread.start() ;
	}

	public void setLedNumber(int ledCount) {

		ledList.clear() ;
		ledNumber = ledCount ;
		for ( int i = 0 ; i < ledNumber; i++)
		{
			ledList.add( new WS2812(i) ) ; // �]�����Ұ�
		}
	}

	public void setPixelColor(int index, int newColor)
	{
		WS2812 led = ledList.get(index) ;
		//led.newColor = newColor ;
		led.color = newColor ;
		led.isChange = true ;
	}
	
	/**
	 * �z�L�S�O�Ѽ� ���w����Ҧ���ws2812
	 * @param color
	 */
	public void setAllPixelColor( int color)
	{
		byte [] b = new byte[4] ;
		b[0] = (byte) ALLLED ;
		b[1] = (byte) ( Color.red(color) );
		b[2] = (byte) ( Color.green(color) );
		b[3] = (byte) ( Color.blue(color) );
		
		for ( WS2812 led: ledList ) {
			//led.newColor = color ;
			led.color = color ;
		}
		commands.add(b) ;
	}
	
	/**
	 * �첾
	 * @param color
	 */
	public void shift( int shift )
	{
		if ( shift < 0 ) {
			shift += this.ledNumber % this.ledNumber;
		}
		byte [] b = new byte[2] ;
		b[0] = (byte) SHIFT ;
		b[1] = (byte) ( shift );
		commands.add(b) ;
	}
	
	public void sendBrightness() {
		byte [] b = new byte[3] ;
		b[0] = (byte) COMMAND ;
		b[1] = (byte) CMD_BRITENESS ;
		b[2] = (byte) brightness ;
		
		commands.add(b) ;
	}
	
	public void sendLedNumber() { 
		byte [] b = new byte[3] ;
		b[0] = (byte) COMMAND ;
		b[1] = (byte) CMD_PIXEL_COUNT ;
		b[2] = (byte) this.ledNumber ;
		
		commands.add(b) ;
	}
	
	
	
	public void sendDebug(boolean data) { 
		byte [] b = new byte[3] ;
		b[0] = (byte) COMMAND ;
		b[1] = (byte) CMD_DEBUG ;
		if ( data )
			b[2] = (byte) 1 ;
		else 
			b[2] = (byte) 0 ;
		
		commands.add(b) ;
	}
	
	public int getPixelColor(int index)
	{
		WS2812 led = ledList.get(index) ;
		return led.color ;
	}
	
	public void show() 
	{
		// ��s�Ҧ�LED��Ƶ�
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
				
				for ( WS2812 led : ledList)
				{
					if ( led.isChange == true ) {
						led.isChange = false ;
						
						bao.write(led.index) ;
						bao.write(Color.red(led.color) ) ;
						bao.write(Color.green(led.color) ) ;
						bao.write(Color.blue(led.color)) ;
						
						hasChange = true ;
					}
				}
				/*
				if ( showAll == false )
				{
					for ( WS2812 led : ledList)
					{
						if ( led.color != led.newColor )
						{
							hasChange = true ;
							bao.write(led.index) ;
							bao.write(Color.red(led.newColor) * this.brightness / 255) ;
							bao.write(Color.green(led.newColor) * this.brightness / 255) ;
							bao.write(Color.blue(led.newColor)* this.brightness / 255 ) ;
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
							bao.write(Color.red(led.newColor) * this.brightness / 255) ;
							bao.write(Color.green(led.newColor) * this.brightness / 255) ;
							bao.write(Color.blue(led.newColor)* this.brightness / 255 ) ;
						}
					}
				}
				*/
				
				
				if ( hasChange )
				{
					byte[] b = bao.toByteArray() ;
					
					for ( int i = 0 ; i < b.length ; i++ ) {
						Log.i(TAG, "_" + b[i] ) ;
					}
					this.os.write(b) ;
					this.os.flush() ; 
				}
				else
				{
					Common.sleep(10) ;
				}
				
				// �p�G��command���� �N�n����
				if ( commands.isEmpty() == false )
				{
					byte [] b = commands.poll() ;
					this.os.write(b) ;
					this.os.flush() ; 
				}
				
				
				try {
					if ( this.is != null && this.is.available() > 0 )
					{
						byte [] t = new byte[this.is.available()] ;
						this.is.read(t) ;
						Log.e(TAG, "Ū�쪺��� len:" + t.length) ;
					}				
				}
				catch (Exception ex) {
					Log.e(TAG, "�Y��IS�����~" + ex.getMessage() ) ;
					//this.is = null ;
				}
				
				
				if ( Common.bleSpp != null ) {
					Common.bleSpp.flush() ;
				}
			}
			catch (Exception ex)
			{
				Log.e(TAG, "�o�{���~:" + ex.getMessage() + ex.getStackTrace() ) ;
				for (StackTraceElement  st: ex.getStackTrace())
				{
					Log.e(TAG, "st:" + st.toString()) ;
				}
				running = false ;
			}
		}

	}
	
	public void stop()
	{
		running = false ;
	}
	


}
