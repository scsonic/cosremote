package com.ygk.cosremote;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.entity.InputStreamEntity;

import com.ygk.cosremote.R;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class BLEClient implements LeScanCallback, BTSerialInterface {

	static public String TAG = "BLEClient" ;
	Activity activity ;
	Object writeLock = new Object() ;
	
	static public int MAX_BLE_LENGTH = 16 ;
	
	private static BluetoothGattCharacteristic mSCharacteristic, mModelNumberCharacteristic, mSerialPortCharacteristic, mCommandCharacteristic;
	public static final String SerialPortUUID=       "0000dfb1-0000-1000-8000-00805f9b34fb";
	public static final String CommandUUID=          "0000dfb2-0000-1000-8000-00805f9b34fb";
    public static final String ModelNumberStringUUID="00002a24-0000-1000-8000-00805f9b34fb";
	
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    
	private LeDeviceListAdapter mLeDeviceListAdapter=null;
	private BluetoothAdapter mBluetoothAdapter;
	AlertDialog mScanDeviceDialog;
	BluetoothGatt mBluetoothGatt;
	
	
	byte [] inputBuffer = new byte[65536] ;
	ByteArrayOutputStream bao = new ByteArrayOutputStream(65536) ;
	
	PipedOutputStream pipeBTRead = new PipedOutputStream() ;
	PipedInputStream pipeToApp = new PipedInputStream() ;
	
	String mDeviceName ;
	String mDeviceAddress ;
	
	boolean isWriting = false ;
	int lastState = BluetoothGatt.STATE_DISCONNECTED ;
	
	
	public BLEClient(Activity activity) {
		this.activity = activity ;
		
		try {
			pipeBTRead.connect(pipeToApp) ;
		}
		catch (Exception ex) { 
			Log.e(TAG, "Connect Pipe Error" + ex.getMessage() )  ;
		} ;
		ByteArrayInputStream test = new ByteArrayInputStream(inputBuffer) ;
				
	}

	@SuppressLint("NewApi")
	boolean initiate()
	{
		if (!activity.getPackageManager().hasSystemFeature( PackageManager.FEATURE_BLUETOOTH_LE)) {
			return false;
		}
		final BluetoothManager bluetoothManager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();
	
		// Checks if Bluetooth is supported on the device.
		if (mBluetoothAdapter == null) {
			return false;
		}
		
		mBluetoothAdapter.startLeScan(this);
		return true;
	}
	
    public void onCreateProcess()
    {
    	if(!initiate())
		{
			Toast.makeText(activity, "Not Support BLE",Toast.LENGTH_SHORT).show();
			activity.finish();
		}
		// Initializes list view adapter.
		mLeDeviceListAdapter = new LeDeviceListAdapter();
		// Initializes and show the scan Device Dialog
		mScanDeviceDialog = new AlertDialog.Builder(activity)
		.setTitle("BLE Device Scan...").setAdapter(mLeDeviceListAdapter, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				final BluetoothDevice device = mLeDeviceListAdapter.getDevice(which);
				if (device == null)
					return;
				Log.e(TAG, "onListItemClick " + device.getName().toString());
				Log.e(TAG, "Device Name:"+device.getName() + "   " + "Device Name:" + device.getAddress());
				
				mDeviceName=device.getName().toString();
				mDeviceAddress=device.getAddress().toString();
				
		        if(mDeviceName.equals("No Device Available") && mDeviceAddress.equals("No Address Available"))
		        {
		        	Log.e(TAG, mDeviceName + "@" + mDeviceAddress );
		        }
		        else{
		        	Log.e(TAG, "開始連線  XXD") ;
		        	Log.e(TAG, "device.connectGatt connect");
		    		//synchronized(this)
		    		{
		    			mBluetoothGatt = device.connectGatt(activity, false, mGattCallback);
		    		}
		        }
		        dialog.cancel() ;
			}
		})
		.setOnCancelListener(new DialogInterface.OnCancelListener(){

			@Override
			public void onCancel(DialogInterface arg0) {
				//System.out.println("mBluetoothAdapter.stopLeScan");
				mScanDeviceDialog.dismiss();
			}
		}).create();
		
    }
    

    private void getGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        
        mModelNumberCharacteristic=null;
        mSerialPortCharacteristic=null;
        mCommandCharacteristic=null;
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            uuid = gattService.getUuid().toString();
            System.out.println("displayGattServices + uuid="+uuid);
            
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                uuid = gattCharacteristic.getUuid().toString();
                
                if(uuid.equals(ModelNumberStringUUID)){
                	mModelNumberCharacteristic=gattCharacteristic;
                	System.out.println("mModelNumberCharacteristic  "+mModelNumberCharacteristic.getUuid().toString());
                }
                else if(uuid.equals(SerialPortUUID)){
                	mSerialPortCharacteristic = gattCharacteristic;
                	System.out.println("mSerialPortCharacteristic  "+mSerialPortCharacteristic.getUuid().toString());
//                    updateConnectionState(R.string.comm_establish);
                }
                else if(uuid.equals(CommandUUID)){
                	mCommandCharacteristic = gattCharacteristic;
                	System.out.println("mSerialPortCharacteristic  "+mSerialPortCharacteristic.getUuid().toString());
//                    updateConnectionState(R.string.comm_establish);
                }
            }
            mGattCharacteristics.add(charas);
        }
        
        if (mModelNumberCharacteristic==null || mSerialPortCharacteristic==null || mCommandCharacteristic==null) {
			Toast.makeText(activity, "連線失敗，你拿的可能不是",Toast.LENGTH_SHORT).show();
		}
        else {
        	
        	mSCharacteristic=mSerialPortCharacteristic;
        	mBluetoothGatt.setCharacteristicNotification(mSCharacteristic, true);
        	mBluetoothGatt.readCharacteristic(mSCharacteristic) ;
		}
        
    }
	static class ViewHolder {
		TextView deviceName;
		TextView deviceAddress;
	}
	
	
	private class LeDeviceListAdapter extends BaseAdapter {
		private ArrayList<BluetoothDevice> mLeDevices;
		private LayoutInflater mInflator;

		public LeDeviceListAdapter() {
			super();
			mLeDevices = new ArrayList<BluetoothDevice>();
			mInflator =  activity.getLayoutInflater();
		}

		public void addDevice(BluetoothDevice device) {
			if (!mLeDevices.contains(device)) {
				mLeDevices.add(device);
			}
		}

		public BluetoothDevice getDevice(int position) {
			return mLeDevices.get(position);
		}

		public void clear() {
			mLeDevices.clear();
		}

		@Override
		public int getCount() {
			return mLeDevices.size();
		}

		@Override
		public Object getItem(int i) {
			return mLeDevices.get(i);
		}

		@Override
		public long getItemId(int i) {
			return i;
		}


		
		@Override
		public View getView(int i, View view, ViewGroup viewGroup) {
			ViewHolder viewHolder;
			// General ListView optimization code.
			if (view == null) {
				view = mInflator.inflate(R.layout.listitem_device, null);
				viewHolder = new ViewHolder();
				viewHolder.deviceAddress = (TextView) view
						.findViewById(R.id.device_address);
				viewHolder.deviceName = (TextView) view
						.findViewById(R.id.device_name);
				System.out.println("mInflator.inflate  getView");
				view.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) view.getTag();
			}

			BluetoothDevice device = mLeDevices.get(i);
			final String deviceName = device.getName();
			if (deviceName != null && deviceName.length() > 0)
				viewHolder.deviceName.setText(deviceName);
			else
				viewHolder.deviceName.setText("Unknow");
			viewHolder.deviceAddress.setText(device.getAddress());

			return view;
		}
	}
	
	
	public BLEClient getBLEClient() {
		return this;
		
	}
	
	
	BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		
//		public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
//			
//			Log.e(TAG, "OnMtuChange, mtu=" + mtu + ", status=" + status) ;
//		};
		
		public String TAG = "GattCallback" ;
		 @Override
	        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
	            Log.e(TAG, "status=" + status + "new State=" + newState ) ;
	            System.out.println("BluetoothGattCallback----onConnectionStateChange"+newState);
	            
	            lastState = newState ;
	            if (newState == BluetoothProfile.STATE_CONNECTED) {
	                Log.i(TAG, "Connected to GATT server 連上了啦~~.");
	                
	                // mtu & reliablewrite 似乎沒啥屁用
	            	//Log.e( TAG, "Request MTU ret=" + mBluetoothGatt.requestMtu(512) ) ;
	                //mBluetoothGatt.beginReliableWrite() ;
	                Common.bleSpp = getBLEClient() ;
	                if(mBluetoothGatt.discoverServices())
	                {
	                    Log.i(TAG, "discoverServices成功");
	                }
	                else{
	                    Log.e(TAG, "discoverServices:not success");
	                }
	                activity.runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							Toast.makeText(activity, "藍芽裝置已連線",Toast.LENGTH_LONG).show();
						}
					}) ;

	            } else if (newState == BluetoothProfile.STATE_DISCONNECTED ||
	            		newState == BluetoothProfile.STATE_DISCONNECTING ) {
	                Log.i(TAG, "斷線了Disconnected from GATT server.");
	                
	                activity.runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							Toast.makeText(activity, "藍芽裝置已斷線",Toast.LENGTH_LONG).show();
						}
					}) ;
	                
	                Common.bleSpp = null ;
	            }
	        }
		 
		 
		 
		 public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			 
			 
        	if(status == BluetoothGatt.GATT_SUCCESS)
        	{
        		Common.sleep(23) ;
        		synchronized (writeLock) {
   				 isWriting = false ;
        		}
        	}
        	else {
        		// write Fail !!
        		Log.e(TAG, "寫入失敗 status=" + status ) ;
        	}
	        	
			 
		 };

	        @Override
	        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
	        	System.out.println("onServicesDiscovered "+status);
	            if (status == BluetoothGatt.GATT_SUCCESS) {
	            	Log.i(TAG, "ACTION_GATT_SERVICES_DISCOVERED");
	            	
	            	 Log.i(TAG, "已經discovery成功" );
	            	getGattServices( mBluetoothGatt.getServices() ) ;
	            	
	            	
	            	

	            } else {
	                Log.w(TAG, "onServicesDiscovered received: " + status);
	            }
	        }
	        
	        
	        @Override
	        public void onCharacteristicRead(BluetoothGatt gatt,
	                                         BluetoothGattCharacteristic characteristic,
	                                         int status) {
	        	
	        	System.out.println("onCharacteristicRead status="+ status);
	            if (status == BluetoothGatt.GATT_SUCCESS) {
	            	// 結果讀取不在這 記錄一下 這裡用別的characteristic才會出現 @@
	            	Log.e(TAG, "讀取到資料/uuid:" +characteristic.getUuid().toString() ) ;
	            	Log.e(TAG, "讀取到資料byte, len=" + characteristic.getValue().length );
	            	//Log.e(TAG, "讀取到資料byte to str:" + new String( characteristic.getValue() ) + ", len=" + characteristic.getValue().length );
	                //broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
	            	
	            	try {
	            		pipeBTRead.write(characteristic.getValue()) ;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	            }
	            else
	            {
	            	Log.e(TAG, "讀取到資料(not gattsuccess) byte, len=" + characteristic.getValue().length );
	            }
	        }
	        @Override
	        public void  onDescriptorWrite(BluetoothGatt gatt, 
	        								BluetoothGattDescriptor characteristic,
	        								int status){
	        	System.out.println("onDescriptorWrite  "+characteristic.getUuid().toString()+" "+status);
	        }
	        @Override
	        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
	        	
	        	//Log.e(TAG, "讀取到資料byte onCharacteristicChanged, len=" + characteristic.getValue().length );
            	try {
            		pipeBTRead.write(characteristic.getValue()) ;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        	//System.out.println("onCharacteristicChanged  "+new String(characteristic.getValue()));
	            //broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
	        }
	} ;
	
	
	@Override
	public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				//System.out.println("mLeScanCallback onLeScan run ");
				mLeDeviceListAdapter.addDevice(device);
				mLeDeviceListAdapter.notifyDataSetChanged();
			}
		});
	}
	
	
	
	/**
	 * 新版 仿造inputstream @@
	 * @param b
	 */
	public void writeGATT( byte b[] ){
		synchronized ( writeLock ) {
			isWriting = true ;
			mSCharacteristic.setValue(b) ;
			
			
			Log.e(TAG, "Data=" + formatByte(b)) ;
			mBluetoothGatt.writeCharacteristic(mSCharacteristic) ;
		}

		Log.i(TAG,  "WriteGatting @@ len=" + b.length) ;
	}
	
	public String formatByte(byte []b) 
	{
		String t = "" ;
		for ( int i = 0 ; i < b.length ; i++ ) {
			t += "," + String.format("%d", (int) b[i]) ;
		}
		return t ;
	}
	
	public void flushGATT() {
		
		while ( isWriting == true )
		{
			if ( lastState == BluetoothGatt.STATE_DISCONNECTED )
				return ;
			if ( lastState == BluetoothGatt.STATE_DISCONNECTING )
				return ;
			
			Common.sleep(10) ;
		}
	}

	@Override
	public void write(byte b) throws IOException {
		synchronized (writeLock) {
			this.bao.write(b) ;
		}
		
	}

	@Override
	public void write(byte[] b) throws IOException {
		synchronized (writeLock) {
			this.bao.write(b);
		}
		
	}
	
	@Override
	public void flush() throws IOException {
		byte [] b ;
		
		synchronized (writeLock) {
			b = this.bao.toByteArray() ;
		}
		
		if ( b.length == 0 ) return ;
		
		//Log.i(TAG, "bao size=" + b.length ) ;
		int len = 0 ;
		for ( int i = 0 ; i < b.length ; i+= MAX_BLE_LENGTH) {
			len = Math.min( b.length, i+MAX_BLE_LENGTH) - i ;
			byte [] data = new byte[len] ;
			for ( int j = 0 ; j < len ; j++ )
			{
				data[j] = b[i+j] ;
			}
			
			// ok / write 
			writeGATT(data) ;

			flushGATT() ;
			writeSkipByte() ;
			
		}
		
		
		this.bao.reset() ;
	}
	
	public void writeSkipByte() {
		byte [] b = new byte[8] ;
		for ( int i = 0 ; i < 8 ; i++ ) {
			b[i] = (byte) 254 ;
		}
		writeGATT(b) ;
		flushGATT() ;
	}
	
	@Override
	public boolean isConnect() {

		if ( this.lastState == BluetoothGatt.STATE_CONNECTED )
		{
			return true ;
		}
		return false;
	}

	@Override
	public int read(byte b[] ) throws IOException {
		
		return this.pipeToApp.read(b) ;
	}

	@Override
	public int read(byte[] b, int len) throws IOException {
		return this.pipeToApp.read(b, 0, len);
	}

	@Override
	public InputStream getInputStream() {
		return this.pipeToApp ;
	}

	@Override
	public OutputStream getOutputStream() {
		
		return this.bao ;
	}



}
