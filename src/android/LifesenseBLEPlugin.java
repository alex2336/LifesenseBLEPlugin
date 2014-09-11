package com.leotech.plugin;
import android.content.Context;
import java.lang.Object;
import lifesense.ble.bean.LSDeviceInfo;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import lifesense.ble.commom.DeviceManagerCallback;
import org.apache.cordova.PluginResult;
import lifesense.ble.commom.BleDeviceManager;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import lifesense.ble.commom.DeviceType;
import org.json.JSONObject;
import java.util.ArrayList;
import lifesense.ble.bean.PedometerData;
import android.util.Log;
import com.google.gson.Gson;
import java.lang.System;
import android.content.SharedPreferences;
import android.os.Handler;
import java.lang.Integer;
public class LifesenseBLEPlugin extends CordovaPlugin {
	private DeviceManagerCallback mDelegate;    
	private LSDeviceInfo mDevice;
	private LSDeviceInfo pairedDevice;
	private ArrayList<BleDevice> tempList=new ArrayList<BleDevice>();
	private ArrayList<BleDevice> deviceList=new ArrayList<BleDevice>();
	private BleDeviceManager bleDeviceManager;
	private TypeConversion typeConversion;
	private Gson gson;
	private ArrayList<PedometerData> data;
	private long lastUpdateTime;
	private String tag = "plugin";
	private SharedPreferences sharedPref;
	private Handler mHandler;
	private String errorMessage = "Operation failed!Probably other tasks are running.";
	String ACTION_START_SCANNING = "startScanning";
	String ACTION_STOP_SCANNING = "stopScanning";
	String GET_CURRENT_DEVICES = "getCurrentDevices";
	String PAIR_DEVICE = "pairDevice";
	String GET_PARIED_DEVICE = "getPairedDevice";
	String ASK_FOR_DATA = "askForData";
	String ASK_FOR_DATA_BY_DEVICE_MAC_ADDRESS = "askForDataByDeviceMacAddress";
	String GET_DATA = "getData";
	String CLEAR_DEVICE_LIST = "clearDeviceList";
	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		Log.d(tag,action);
		if (action.equals(ACTION_START_SCANNING)) {
			startScanning(args.getString(0),callbackContext);
			return true;
		}else if(action.equals(ACTION_STOP_SCANNING)){
			stopScanning(callbackContext);
			return true;
		}else if(action.equals(GET_CURRENT_DEVICES)){
			listKnownDevices(callbackContext);
			return true;
		}else if(action.equals(PAIR_DEVICE)){
			pairDevice(args.getString(0),callbackContext);
			return true;
		}else if(action.equals(GET_PARIED_DEVICE)){
			getPairedDevice(callbackContext);
			return true;
		}else if(action.equals(ASK_FOR_DATA)){
			askForData(callbackContext);
			return true;
		}else if(action.equals(ASK_FOR_DATA_BY_DEVICE_MAC_ADDRESS)){
			askForDataByDeviceMacAddress(args.getString(0),callbackContext);
			return true;
		}else if(action.equals(GET_DATA)){
			getData(callbackContext);
			return true;
		}else if(action.equals(CLEAR_DEVICE_LIST)){
			clearDeviceList(callbackContext);
			return true;
		}
		return false;
	}
	private void clearDeviceList(CallbackContext callbackContext){
		tempList.clear();
		callbackContext.success("device list cleared.");
	}
	private void askForData(CallbackContext callbackContext){
		final CallbackContext callbackContextCopy = callbackContext;
		mDelegate=new DeviceManagerCallback(){
			@Override
			public void onReceivePedometerMeasurementData(final PedometerData pData) {		
				data.add(pData);
				lastUpdateTime = System.currentTimeMillis();
				Log.d(tag,"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!DATA RECEIVED");
				try{
					String json = gson.toJson(pData);
					PluginResult result = new PluginResult(PluginResult.Status.OK, new JSONObject(json));
					callbackContextCopy.sendPluginResult(result);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		};
		bleDeviceManager.setCallback(mDelegate);
		bleDeviceManager.interruptCurrentTask();
		Log.d(tag,"Interrupt Current Task!");
		if(bleDeviceManager.getDeviceMeasurementData(pairedDevice)){
			callbackContext.success("Data requesting started.");
		}else{
			callbackContext.error(errorMessage);
			Log.d(tag,"OTHER TASKS ARE RUNNING!!");
		}
	}
	private void askForDataByDeviceMacAddress(String jsonString, CallbackContext callbackContext){
		String address;
		int timeout;
		Log.d(tag,jsonString);
		try{
			JSONObject json = new JSONObject(jsonString);
			address = (String)json.get("address");
			timeout = (Integer)json.get("timeout");
		}catch(Exception e){
			e.printStackTrace();
			callbackContext.error("invalid parameter.");
			return;
		}
		String defaultValue = "not found!";
		String savedDeviceJson = sharedPref.getString(address,defaultValue);
		final CallbackContext callbackContextCopy = callbackContext;
		if(savedDeviceJson==defaultValue){
			callbackContext.error("This device haven't been paired before");
		}else{
			LSDeviceInfo device = gson.fromJson(savedDeviceJson,LSDeviceInfo.class);
			pairedDevice = device;
			mDelegate=new DeviceManagerCallback(){
				@Override
				public void onReceivePedometerMeasurementData(final PedometerData pData) {		
					data.add(pData);
					lastUpdateTime = System.currentTimeMillis();
					Log.d(tag,"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!DATA RECEIVED");
				}
			};
			bleDeviceManager.setCallback(mDelegate);
			Log.d(tag,gson.toJson(pairedDevice));
			if(bleDeviceManager.getDeviceMeasurementData(pairedDevice)){
				Runnable myTask = new Runnable() {
					@Override
					public void run() {
						bleDeviceManager.interruptCurrentTask();
						getData(callbackContextCopy);
					}
				};
				mHandler.postDelayed(myTask, timeout);
			}else{
				callbackContext.error(errorMessage);
				Log.d(tag,"OTHER TASKS ARE RUNNING!!");
			}
		}
	}
	private void getData(CallbackContext callbackContext) {
		JSONObject json = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		try{
			if(pairedDevice!=null){
				for(int i=0;i<data.size();i++){
					jsonArray.put(new JSONObject(gson.toJson(data.get(i))));
				}
			}
			json.put("data",jsonArray);
		}catch(Exception e){
			e.printStackTrace();
		}
		data.clear();
		if(jsonArray.length()==0){
			callbackContext.error("No data received.");
		}else{
			bleDeviceManager.interruptCurrentTask();
			Log.d(tag,"Interrupt Current Task!");
			PluginResult result = new PluginResult(PluginResult.Status.OK, json);
			callbackContext.sendPluginResult(result);
		}

	}
	private void startScanning(String jsonString, CallbackContext callbackContext){
		int timeout;
		Log.d(tag,jsonString);
		try{
			JSONObject json = new JSONObject(jsonString);
			timeout = (Integer)json.get("timeout");
		}catch(Exception e){
			e.printStackTrace();
			callbackContext.error("invalid parameter.");
			return;
		}
		final CallbackContext callbackContextCopy = callbackContext;
		mDelegate=new DeviceManagerCallback(){
			@Override
			public void onDiscoverDevice(final LSDeviceInfo lsDevice) 
			{
				if(lsDevice!=null&&!deviceExists(lsDevice.getDeviceAddress()))
				{
					Log.d(tag,lsDevice.getDeviceName()+" "+tempList.size());
					BleDevice bleDevice=new BleDevice(
						lsDevice.getDeviceName(),
						lsDevice.getDeviceAddress(),
						typeConversion.enumToInteger(lsDevice.getDeviceType()),
						lsDevice.getModelNumber());
					tempList.add(bleDevice);
				}
			}
		};
		bleDeviceManager.setCallback(mDelegate);
		if(!bleDeviceManager.startScanning()){
			callbackContext.error(errorMessage);
		}else{
			Runnable myTask = new Runnable() {
				@Override
				public void run() {
					bleDeviceManager.stopScanning();
					listKnownDevices(callbackContextCopy);
				}
			};
			mHandler.postDelayed(myTask, timeout);
		}
	}
	private void stopScanning(CallbackContext callbackContext){
		bleDeviceManager.setCallback(mDelegate);
		if(bleDeviceManager.stopScanning()){
			callbackContext.success("Scanning stopped.");
		}else{
			callbackContext.error(errorMessage);
		}
	}
	private void pairDevice(String jsonString, CallbackContext callbackContext){
		try{
			final CallbackContext callbackContextCopy = callbackContext;
			JSONObject json = new JSONObject(jsonString);
			BleDevice device = gson.fromJson(json.get("device").toString(), BleDevice.class);
			int timeout = (Integer)json.get("timeout");
			Log.d(tag,"param:"+jsonString);
			Log.d(tag,"Pair device:"+device.toString());
			mDevice=new LSDeviceInfo();
			mDevice.setDeviceName(device.getName());
			mDevice.setDeviceType(typeConversion.integerToEnum(device.getSensorType()));
			mDevice.setDeviceAddress(device.getAddress());
			mDevice.setModelNumber(device.getModelNumber());
			bleDeviceManager.stopScanning();
			mDelegate=new DeviceManagerCallback(){
				@Override
				public void onPairedResults(final LSDeviceInfo device, final int state) {
					if(device!=null&&state==0)
					{
						pairedDevice = device;
						SharedPreferences.Editor editor = sharedPref.edit();
						editor.putString(device.getDeviceAddress(), gson.toJson(device));
						editor.commit();
						Log.d(tag,"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!Device paired:"+device.getDeviceName());
						getPairedDevice(callbackContextCopy);
					}
				}
			};
			bleDeviceManager.setCallback(mDelegate);
			if(!bleDeviceManager.toPairDevice(mDevice)){
				callbackContext.error(errorMessage);
			}else{
				Runnable myTask = new Runnable() {
					@Override
					public void run() {
						bleDeviceManager.interruptCurrentTask();
						callbackContextCopy.error("Pair Time Out.");
					}
				};
				mHandler.postDelayed(myTask, timeout);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void listKnownDevices(CallbackContext callbackContext) {
		JSONObject response = new JSONObject();
		JSONArray json = new JSONArray();
		try{
			for(int i=0;i<tempList.size();i++){
					//json.put(tempList.get(i).getJson());
				// JSONObject obj = new JSONObject();
				// obj.put("address", tempList.get(i).getAddress());
				// obj.put("name", tempList.get(i).getName());
				// obj.put("modelNumber", tempList.get(i).getModelNumber());
				JSONObject obj = new JSONObject(gson.toJson(tempList.get(i)));
				Log.d(tag, "obj");
				// json.put(gson.toJson(tempList.get(i)));
				json.put(obj);
			}
			response.put("devices", json);
		}catch(Exception e){
			e.printStackTrace();
		}
		if(json.length()==0){
			callbackContext.error("No device found.");
		}else{
			PluginResult result = new PluginResult(PluginResult.Status.OK, response);
			callbackContext.sendPluginResult(result);
		}
	}
	private void getPairedDevice(CallbackContext callbackContext) {

		JSONObject json = new JSONObject();
		try{
			if(pairedDevice!=null){
				json = new JSONObject(gson.toJson(pairedDevice));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		if(json.length()==0){
			callbackContext.error("No paired device.");
		}else{
			PluginResult result = new PluginResult(PluginResult.Status.OK, json);
			callbackContext.sendPluginResult(result);
		}
	}

	@Override
	public void initialize(CordovaInterface cordova, CordovaWebView webView){
		data = new ArrayList<PedometerData>();
		mHandler = new Handler();
		super.initialize(cordova, webView);
		typeConversion=new TypeConversion();
		gson = new Gson();
		mDelegate=new DeviceManagerCallback(){
			@Override
			public void onDiscoverDevice(final LSDeviceInfo lsDevice) 
			{
				if(lsDevice!=null&&!deviceExists(lsDevice.getDeviceAddress()))
				{
					Log.d(tag,lsDevice.getModelNumber());
					BleDevice bleDevice=new BleDevice(
						lsDevice.getDeviceName(),
						lsDevice.getDeviceAddress(),
						typeConversion.enumToInteger(lsDevice.getDeviceType()),
						lsDevice.getModelNumber());
					tempList.add(bleDevice);    
				}
			}
		};
		sharedPref = this.cordova.getActivity().getSharedPreferences("com.leotech.plugin.LifesenseBLEPlugin", Context.MODE_PRIVATE);
		bleDeviceManager=BleDeviceManager.getInstance(); 
		bleDeviceManager.initialize(this.cordova.getActivity().getApplicationContext(),mDelegate);
	}
	private boolean deviceExists(String address) 
	{
		boolean found=false;
		for (int i = 0; i < tempList.size(); i++) 
		{
			if (tempList.get(i).getAddress().equals(address)) 
			{
				return found=true;
			}
		}
		return found;
	}

}
