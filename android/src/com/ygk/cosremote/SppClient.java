package com.ygk.cosremote;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;
import java.util.Vector;

import org.apache.http.util.ByteArrayBuffer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Message;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;


/*
 * 
 * 固定btspp
 * btspp://000A3A2269CB:1
 */


public class SppClient implements BTSerialInterface
{
	public enum State {
		Init,
		Connecting,
		Fail,
		NoBluetooth,
		Connected, Disconnect 
	}
	static public String TAG = "SppClient" ;
	
	//String btspp = "00:1A:FF:09:00:3C" ;
	String btspp = "00:0A:3A:22:69:CB" ;
	
	private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	boolean running = false ;
	State state = State.Init ;
	
	private BluetoothAdapter btAdapter = null;
	private BluetoothSocket client = null;
  
	public OutputStream dos = null;	
	public InputStream isr = null ;
		
	public int recvDataSize = 0 ;
	public int sendDataSize = 0 ;
	
	//存取控制
	Object dataLock = new Object() ;
	Object btLock = new Object() ; // 處理有關連線/斷線的lock
	
	/**
	 * 產生spp client的啦
	 */
	public SppClient() throws Exception
	{
		boolean error = false ;

		this.btAdapter = BluetoothAdapter.getDefaultAdapter() ;
		
		if ( btAdapter == null || (btAdapter.isEnabled() == false) ) 
		{
			new Exception("手機沒有藍芽或是未啟動藍芽裝置") ;
			state = State.NoBluetooth ;
		}
		Log.i("info", "new spp client ok" ) ;
	}	
	
	
	public void connect(String btAddress)
	{
		this.btspp = btAddress ;
		this.doConnect() ;
	}
	
	/**
	 *  連線至指定Spp
	 */
	private void doConnect()
	{
		// 取得btspp直接連線
		running = true ;
		try
		{
			synchronized ( btLock) 
			{
				BluetoothDevice device = btAdapter.getRemoteDevice(this.btspp);
				try {
					this.state = State.Connecting ;
					client = device.createRfcommSocketToServiceRecord(MY_UUID);
				} catch (Exception e) 
				{
					Log.e("ERR","建立rfcomm錯誤  " + e.getMessage() + "." ) ;
					running = false; 
				}
	
				Log.i("INFO", "Object client已建立"  ) ;
				btAdapter.cancelDiscovery();
				
				try {
					client.connect();
					Log.i("info", "\n @@@ 沒這行就是掛啦...Connection established and data link opened...");
					this.state = State.Connected ;
					//this.setState( "連線成功" );
				} catch (IOException e) {
					running = false; 
					this.state = State.Fail ;
					Log.e("ERR", "連線失敗的啦 connect() failure " + e.getMessage() + ".");
					try {
						client.close();
					} catch (IOException e2) {
						Log.e("ERR", "連線失敗+1 In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
					}
					//setState("連線失敗" );
				}
				
				// 要給這二個才能動
				isr = new BufferedInputStream(client.getInputStream()) ;
				dos = new BufferedOutputStream(client.getOutputStream());
				
				if ( isr == null || dos == null )
				{
					throw new Exception("isr&dos==null XD") ;
				}
			}
		}
		catch (Exception e )
		{
			Log.e("ERR", "no direct Connect :" + e.getMessage()) ;
			//this.setState( "連線失敗" );
			this.state = State.Fail ;
			running = false; 
		}

	}
	
	
	public boolean isConnected()
	{
		if ( this.client == null ) 
			return false ;
		if ( this.client.isConnected() && this.state == State.Connected)
		{
			return true ;
		}
		return false; 
	}
	/**
	 * 斷線
	 */
	public int disconnect()
	{
		Log.i(TAG, "一定有disconnect") ;
		int ret = 0 ;// ok
		this.state = State.Disconnect ;
		this.running = false ;
		
		try 
		{
			if (isr != null) 
			{
				isr.close();
			}
		} 
		catch (Exception e) 
		{
			Log.e(TAG, e.getMessage() ) ;
		}
		try 
		{
			if (dos != null) 
			{
				dos.close();
			}
		} 
		catch (Exception e) 
		{
			Log.e(TAG, e.getMessage() ) ;
		}
		try 
		{
			if (client != null)
			{
				client.close();
			}
		} 
		catch (Exception e) 
		{
			Log.e(TAG, e.getMessage() ) ;
		}
		
		isr = null;
		dos = null;
		client = null;
		btAdapter = null ;
		return ret ;
	}

	
	public synchronized void addSendDataSize()
	{
		this.sendDataSize ++ ;
		if ( this.sendDataSize > Integer.MAX_VALUE -100 )
		{
			this.sendDataSize = 0 ;
		}
	}
	public synchronized int getSendDataSize()
	{
		return this.sendDataSize ;
	}
	public synchronized void addRecvDataSize(int size)
	{
		this.recvDataSize += size ;
	}
	public synchronized int getRecvDataSize()
	{
		return this.recvDataSize ;
	}

	public synchronized void writeByte(int b)
	{
		if ( dos != null )
		{
			try
			{
				dos.write( b ) ;
				//dos.flush() ;
			}
			catch ( Exception ex )
			{
				Log.e("ERR", "write Error" + ex.getMessage() ) ;
			}
			
		}
	}

	/**
	 * 讓上層得到連線狀態
	 * @return
	 */
	public State getState()
	{
		return this.state ;
	}


	@Override
	public void write(byte b) throws IOException {
		this.dos.write(b) ;
		this.dos.flush() ;
	}


	@Override
	public void write(byte[] b) throws IOException{
		this.dos.write(b) ;
		this.dos.flush() ;
	}

	@Override
	public int read(byte []b, int len) throws IOException{
		return this.isr.read(b, 0, len) ;
	}

	@Override
	public void flush() throws IOException{
		this.dos.flush() ;
		
	}


	@Override
	public boolean isConnect() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public int read(byte [] b) throws IOException {
		return this.isr.read(b) ; 
	}
	
	@Override
	public InputStream getInputStream() {
		return this.isr;
	}
	
	@Override
	public OutputStream getOutputStream() {
		return this.dos ;
	}
}
