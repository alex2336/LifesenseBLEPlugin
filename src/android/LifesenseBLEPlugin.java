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
	private String errorMessage = "Operation failed!Probably other tasks are running.";
	String ACTION_START_SCANNING = "startScanning";
	String ACTION_STOP_SCANNING = "stopScanning";
	String GET_CURRENT_DEVICES = "getCurrentDevices";
	String PAIR_DEVICE = "pairDevice";
	String GET_PARIED_DEVICE = "getPairedDevice";
	String ASK_FOR_DATA = "askForData";
	String ASK_FOR_DATA_BY_DEVICE_ID = "askForDataByDeviceId";
	String GET_DATA = "getData";
	String CLEAR_DEVICE_LIST = "clearDeviceList";
	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		Log.d(tag,action);
		if (action.equals(ACTION_START_SCANNING)) {
			Log.d("CordovaLog:","start scanning!");
			startScanning(callbackContext);
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
		}else if(action.equals(ASK_FOR_DATA_BY_DEVICE_ID)){
			askForDataByDeviceId(args.getString(0),callbackContext);
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
		mDelegate=new DeviceManagerCallback(){
			@Override
			public void onReceivePedometerMeasurementData(final PedometerData pData) {		
				data.add(pData);
				lastUpdateTime = System.currentTimeMillis();
				Log.d(tag,"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!DATA RECEIVED");
			}
		};
		bleDeviceManager.setCallback(mDelegate);
		if(bleDeviceManager.getDeviceMeasurementData(pairedDevice)){
			callbackContext.success("Data requesting started.");
		}else{
			callbackContext.error(errorMessage);
			Log.d(tag,"OTHER TASKS ARE RUNNING!!");
		}
	}
	private void askForDataByDeviceId(String jsonString, CallbackContext callbackContext){
		String name;
		try{
			JSONObject deviceNameJson = new JSONObject(jsonString);
			name = (String)deviceNameJson.get("name");
		}catch(Exception e){
			e.printStackTrace();
			callbackContext.error("invalid parameter.");
			return;
		}
		String defaultValue = "not found!";
		String savedDeviceJson = sharedPref.getString(name,defaultValue);
		if(savedDeviceJson==defaultValue){
			callbackContext.error("This device haven't been paired before");
		}else{
			LSDeviceInfo device = gson.fromJson(savedDeviceJson,LSDeviceInfo.class);
			pairedDevice = device;
			askForData(callbackContext);
		}
	}
	private void getData(CallbackContext callbackContext) {
		JSONArray json = new JSONArray();
		if(System.currentTimeMillis()-lastUpdateTime>=2000){
			Log.d(tag,(System.currentTimeMillis()-lastUpdateTime)+"");
			Log.d(tag,"size:"+data.size()+"");
			try{
				if(pairedDevice!=null){
					for(int i=0;i<data.size();i++){
						json.put(gson.toJson(data.get(i)));
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			data.clear();
		}
		if(json.length()==0){
			callbackContext.error("No data received.");
		}else{
			PluginResult result = new PluginResult(PluginResult.Status.OK, json);
			callbackContext.sendPluginResult(result);
		}

	}
	private void startScanning(CallbackContext callbackContext){
		mDelegate=new DeviceManagerCallback(){
			@Override
			public void onDiscoverDevice(final LSDeviceInfo lsDevice) 
			{
				if(lsDevice!=null&&!deviceExists(lsDevice.getDeviceName()))
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
		bleDeviceManager.setCallback(mDelegate);
		if(bleDeviceManager.startScanning()){
			callbackContext.success("Scanning started.");
		}else{
			callbackContext.error(errorMessage);
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
			JSONObject deviceJson = new JSONObject(jsonString);
			BleDevice device = gson.fromJson(deviceJson.get("device").toString(), BleDevice.class);
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
						editor.putString(device.getDeviceName(), gson.toJson(device));
						editor.commit();
						Log.d(tag,"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!Device paired:"+device.getDeviceName());
					}
				}
			};
			bleDeviceManager.setCallback(mDelegate);
			if(bleDeviceManager.toPairDevice(mDevice)){
				callbackContext.success("Pairing started");
			}else{
				callbackContext.error(errorMessage);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void listKnownDevices(CallbackContext callbackContext) {
		JSONArray json = new JSONArray();
		try{
			for(int i=0;i<tempList.size();i++){
					//json.put(tempList.get(i).getJson());
				json.put(gson.toJson(tempList.get(i)));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		if(json.length()==0){
			callbackContext.error("No device found.");
		}else{
			PluginResult result = new PluginResult(PluginResult.Status.OK, json);
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
		super.initialize(cordova, webView);
		typeConversion=new TypeConversion();
		gson = new Gson();
		mDelegate=new DeviceManagerCallback(){
			@Override
			public void onDiscoverDevice(final LSDeviceInfo lsDevice) 
			{
				if(lsDevice!=null&&!deviceExists(lsDevice.getDeviceName()))
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
	private boolean deviceExists(String name) 
	{
		boolean found=false;
		for (int i = 0; i < tempList.size(); i++) 
		{
			if (tempList.get(i).getName().equals(name)) 
			{
				return found=true;
			}
		}
		return found;
	}

}