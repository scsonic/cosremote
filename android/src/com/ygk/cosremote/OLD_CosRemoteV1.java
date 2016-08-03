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
@Deprecated
public class OLD_CosRemoteV1 implements Runnable{

	static public String TAG = "CosRemoteV1" ;
	BufferedInputStream is ;
	BufferedOutputStream os ; // �n���o�G�Ӥ~���
	int ledNumber = 0 ;
	boolean running = true ;
	boolean showAll = false ;
	Thread thread = null ;
	
	
	// �]�w 255 �O��Lcommand ��L0-254�Ows2812�Y�ɱ���
	// �]��ws2812�Y�W�e �ҥH�~�o�˳]�p
	
	
	int brite = 30 ; //�G�� global
	ByteArrayOutputStream bao = new ByteArrayOutputStream(65536) ;
	
	public ArrayList<WS2812> ledList = new ArrayList<WS2812>() ;
	//SppClient spp = null ; // �S�F��  ���
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
		this.is = new BufferedInputStream(is) ; // ����ާ@�b�o�G�ӪF��W
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
			ledList.add( new WS2812(i) ) ; // �]�����Ұ�
		}
	}

	public void setPixelColor(int index, int newColor)
	{
		WS2812 led = ledList.get(index) ;
		led.newColor = newColor ;
	}
	
	/**
	 * �z�L�S�O�Ѽ� ���w����Ҧ���ws2812
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
				
				// �p�G��command���� �N�n����
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
					Log.d(TAG, "Ū�쪺���:" + new String(t) ) ;
				}				
			}
			catch (Exception ex)
			{
				Log.e(TAG, "�o�{���~:" + ex.getMessage() ) ;
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
