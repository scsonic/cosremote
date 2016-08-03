package com.ygk.cosremote;

import java.util.ArrayList;
import java.util.Set;

import com.ygk.cosremote.R;
import com.ygk.cosremote.SelectFragment.SelectPair;
import com.ygk.cosremote.SppClient.State;
import com.ygk.cosremote.effects.ConfigFragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * ����App��Fragment ~~
 * @author 1310081
 *
 */
public class ConnectFragment extends Fragment implements Runnable{

	static public String TAG = "ConnectFragment" ;

	boolean running = true ;
	Spinner spBTDevice ;
	Button btnConnectBT ;
	Button btnConnectBLE ;
	Button btnConnectWifi ;
	
	BLEClient bleClient = null ;
	LinearLayout llConnected ;
	TextView tvConnectStatus ;
	TextView tvTitle ;
	Set<BluetoothDevice> pairedDevices ;
	BluetoothAdapter bluetoothAdapter ;
	ArrayList<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>() ;
	Thread thread = null ;
	
    public ConnectFragment()
    {
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
    	View rootView = inflater.inflate(R.layout.fragment_connect, container, false);
    	
    	spBTDevice = (Spinner) rootView.findViewById(R.id.spBTDevice );
    	btnConnectBT = (Button) rootView.findViewById(R.id.btnConnectBT );
    	llConnected = (LinearLayout) rootView.findViewById(R.id.llConnected );
    	tvConnectStatus = (TextView) rootView.findViewById(R.id.tvConnectStatus );
    	btnConnectBLE = (Button) rootView.findViewById(R.id.btnConnectBLE) ;
    	btnConnectWifi = (Button) rootView.findViewById(R.id.btnConnectWifi) ;
    	tvTitle = (TextView) rootView.findViewById(R.id.tvTitle) ;
    	
    	setSpinner( getPartedDevice() ) ;
    	
    	thread = new Thread(this) ;
    	thread.start() ;
    	
    	btnConnectBT.setOnClickListener(onConnectListener) ;
    	
    	btnConnectBLE.setOnClickListener(new OnClickListener() {
			
			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				
				if ( Common.bleSpp == null ) {
					bleClient = new BLEClient(getActivity()) ;
					if ( bleClient.initiate() ) {
						Log.i(TAG, "��BLE�i��") ;
						bleClient.onCreateProcess() ;
						bleClient.mScanDeviceDialog.show();
					}
					else 
					{
						Common.showAlert("�L�k�ϥ��Ū�", "�z������S���Ū�4.0�Ω|���Ұ��ŪޡC") ;
						Log.i(TAG, "�¤���S��BLE") ;
					}
				}
				else {
					
					try {
						Common.bleSpp.mBluetoothGatt.disconnect() ;
					}
					catch (Exception ex) {
						Log.e(TAG, "Disconnect fail" + ex.getMessage()) ;
					}
					Common.bleSpp = null ;
					
				}


			}
		}) ;
    	
    	if ( ! Common.isSDK43UP() ) {
    		btnConnectBLE.setEnabled(false) ;
    	}
    	
    	btnConnectWifi.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new CountDownTimer(3000, 1000) {
					
					@Override
					public void onTick(long millisUntilFinished) {

					}
					
					@Override
					public void onFinish() {
						Common.showAlert("�s�uWifi", "Wifi�������䤣��˸m") ;
					}
				}.start() ;
			}
		});
    	
    	
    	tvTitle.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				ConfigFragment config = new ConfigFragment() ;
				MainActivity activity = (MainActivity) getActivity() ;
				activity.llTab.setVisibility(View.GONE) ;
				android.app.FragmentTransaction trans = getFragmentManager().beginTransaction();
				trans.addToBackStack(null);
				trans.replace(R.id.container, config ).commit();
				return false;
			}
		}) ;
        return rootView;        
    }
    
    public void loadSpinnerSelected()
    {
    	String address = Common.getLastBTDevice() ;
    	for ( int i = 0 ;i < deviceList.size() ; i++ ){ 
    		BluetoothDevice d = deviceList.get(i) ;
    		if ( d.getAddress().equals(address))
    		{
    			spBTDevice.setSelection(i) ;
    		}
    	}
    }
    
    public void setSpinner(ArrayList<String> list)
    {
    	String arr[] = new String[ list.size() ] ;
    	list.toArray(arr) ;
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item, arr);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.spBTDevice.setAdapter(adapter);
        
        spBTDevice.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				Common.saveLastBTDevice(deviceList.get(position).getAddress());
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
        });
        
        loadSpinnerSelected() ;
    }

	public ArrayList<String> getPartedDevice() {
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		pairedDevices = bluetoothAdapter.getBondedDevices();
		
		ArrayList<String> list = new ArrayList<String>() ;
		
		if (pairedDevices.size() > 0)
		{
			for (BluetoothDevice device : pairedDevices) {
				String deviceBTName = device.getName();
				deviceList.add(device) ;
				list.add(deviceBTName + " @ " + device.getAddress() + "");
				Log.e(TAG, deviceBTName + " @ " + device.getAddress() + "");
			}
		}
		
		return list;
	}
	
	OnClickListener onConnectListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			new Thread(){
				public void run() {
					
					try {
						
						if ( Common.spp == null )
						{
							int selected = spBTDevice.getSelectedItemPosition() ;
							Common.spp = new SppClient() ;
							Common.spp.connect(deviceList.get(selected).getAddress()) ;
							
							if ( Common.spp.running == true )
							{
								ShowConnectResult("�s�u���\") ;
							}
							else
								ShowConnectResult("�s�u����") ;
						}
						else
						{
							Common.spp.disconnect() ;
							Common.spp = null ;
						}

					}
					catch (Exception ex)
					{
						ShowConnectResult("�s�u����") ;
					}
				};
			}.start();
			
		}
	};


	
	public String tempConnectResult ;
	public void ShowConnectResult(String text)
	{
		tempConnectResult = text ;
		Activity activity = getActivity() ;
		if ( activity != null )
		{
			activity.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					if ( tvConnectStatus != null )
						tvConnectStatus.setText(tempConnectResult) ;
				}
			});
		}
	}

	@Override
	public void run() {
		while ( running )
		{
			Common.sleep(333) ;
			
			Activity activity = getActivity() ;
			if ( activity == null) continue ;
			
			activity.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					
					if ( tvConnectStatus == null) return ; 
					
					if ( Common.spp == null )
					{
						tvConnectStatus.setText("�|���s�u") ;
						btnConnectBT.setText("�s�u") ;
					}
					else 
					{
						if ( Common.spp.state == State.Init )
						{
							tvConnectStatus.setText("�|���s�u") ;
							btnConnectBT.setText("�s�u") ;
						}
						else if (Common.spp.state == State.Connected )
						{
							tvConnectStatus.setText("�w�s�u") ;
							btnConnectBT.setText("�_�u") ;
						}
						else if ( Common.spp.state == State.Connecting )
						{
							tvConnectStatus.setText("�s�u��") ;
							btnConnectBT.setText("�_�u") ;
						}
						else if (Common.spp.state == State.NoBluetooth )
						{
							tvConnectStatus.setText("�Ū��������A�жi�]�w���}") ;
							btnConnectBT.setText("�s�u") ;
						}
						else if ( Common.spp.state == State.Disconnect 
								|| Common.spp.state == State.Fail 
								|| ! Common.spp.isConnected())
						{
							tvConnectStatus.setText("�w���h�s�u") ;
							btnConnectBT.setText("�s�u") ;
						}
						else
						{
							tvConnectStatus.setText("�w���h�s�u @@") ;
							btnConnectBT.setText("�s�u") ;
						}
					}
					
					
					if ( Common.isSDK43UP() ) {
						// ble
						if ( Common.bleSpp == null ) 
						{
							btnConnectBLE.setText(getActivity().getString(R.string.connect_ble)) ;
						}
						else {
							if ( Common.bleSpp.lastState == BluetoothGatt.STATE_CONNECTING ) {
								btnConnectBLE.setText("�s�u���K") ;
							}
							if ( Common.bleSpp.lastState == BluetoothGatt.STATE_CONNECTED ) {
								btnConnectBLE.setText("�w�s�u�A�qBLE�_�u") ;
							}
							
						}
					}
				}
			}) ;

		}
	}
	

	@Override
	public void onDetach() {
		running = false ;
		super.onDetach();
	}
}
