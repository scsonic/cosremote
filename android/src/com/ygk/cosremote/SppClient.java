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
 * �T�wbtspp
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
	
	//�s������
	Object dataLock = new Object() ;
	Object btLock = new Object() ; // �B�z�����s�u/�_�u��lock
	
	/**
	 * ����spp client����
	 */
	public SppClient() throws Exception
	{
		boolean error = false ;

		this.btAdapter = BluetoothAdapter.getDefaultAdapter() ;
		
		if ( btAdapter == null || (btAdapter.isEnabled() == false) ) 
		{
			new Exception("����S���ŪީάO���Ұ��Ū޸˸m") ;
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
	 *  �s�u�ܫ��wSpp
	 */
	private void doConnect()
	{
		// ���obtspp�����s�u
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
					Log.e("ERR","�إ�rfcomm���~  " + e.getMessage() + "." ) ;
					running = false; 
				}
	
				Log.i("INFO", "Object client�w�إ�"  ) ;
				btAdapter.cancelDiscovery();
				
				try {
					client.connect();
					Log.i("info", "\n @@@ �S�o��N�O����...Connection established and data link opened...");
					this.state = State.Connected ;
					//this.setState( "�s�u���\" );
				} catch (IOException e) {
					running = false; 
					this.state = State.Fail ;
					Log.e("ERR", "�s�u���Ѫ��� connect() failure " + e.getMessage() + ".");
					try {
						client.close();
					} catch (IOException e2) {
						Log.e("ERR", "�s�u����+1 In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
					}
					//setState("�s�u����" );
				}
				
				// �n���o�G�Ӥ~���
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
			//this.setState( "�s�u����" );
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
	 * �_�u
	 */
	public int disconnect()
	{
		Log.i(TAG, "�@�w��disconnect") ;
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
	 * ���W�h�o��s�u���A
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
