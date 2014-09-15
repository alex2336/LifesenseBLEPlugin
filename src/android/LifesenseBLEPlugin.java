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
    /**
     * Device manager callback instance
     */
	private DeviceManagerCallback mDelegate;

    /**
     * Device being paired.
     */
	private LSDeviceInfo pairedDevice;

    /**
     * List of devices discovered. Populated on scan.
     */
	private ArrayList<BleDevice> scannedDeviceList = new ArrayList<BleDevice>();

    /**
     * List of data retrieved from the Bluetooth device. Populated on askForDataByDeviceMacAddress
     */
	private ArrayList<PedometerData> dataList = new ArrayList<PedometerData>();

    /**
     * Bluetooth device manager instance
     */
	private BleDeviceManager bleDeviceManager;

    /**
     * Flag indicating whether task should be interrupted or not. Set to false once a device has been successfully paired.
     */
    private boolean pairInterruptFlag = true;

    /**
     * Lifesense constant values
     */
    private TypeConversion typeConversion = new TypeConversion();

    /**
     * Gson helper class for converting objects to JSON
     */
    private Gson gson = new Gson();

	private String tag = "plugin";

    /**
     * Used to store paired device information.
     */
    private SharedPreferences sharedPref;

    /**
     * Used for execution timeout
     */
    private Handler mHandler = new Handler();

    /**
     * Error message for other tasks running
     */
	private String RUNNING_TASKS_ERROR = "Operation failed! Probably other tasks are running.";

    /**
     * Cordova plugin action names
     */
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
		Log.d(tag, action);

        if (action.equals(ACTION_START_SCANNING)) {
			startScanning(args.getString(0),callbackContext);
			return true;
		}else if(action.equals(ACTION_STOP_SCANNING)){
			stopScanning(callbackContext);
			return true;
		}else if(action.equals(GET_CURRENT_DEVICES)){
			getScannedDevices(callbackContext);
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

    /**
     * Clears the scanned device list
     * @param callbackContext
     */
	private void clearDeviceList(CallbackContext callbackContext){
		scannedDeviceList.clear();
		callbackContext.success("Scanned device list cleared.");
	}

    /**
     * Initiates the retrieval of pedometer data
     * @param callbackContext
     */
	private void askForData(CallbackContext callbackContext){
		final CallbackContext callbackContextCopy = callbackContext;
		mDelegate = new DeviceManagerCallback(){
			@Override
			public void onReceivePedometerMeasurementData(final PedometerData pData) {		
				dataList.add(pData);
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
			callbackContext.error(RUNNING_TASKS_ERROR);
			Log.d(tag,"OTHER TASKS ARE RUNNING!!");
		}
	}

    /**
     * Initiates the retrieval of pedometer data based on the provided device address
     * @param jsonString
     * @param callbackContext
     */
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
					dataList.add(pData);
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
				callbackContext.error(RUNNING_TASKS_ERROR);
				Log.d(tag,"OTHER TASKS ARE RUNNING!!");
			}
		}
	}

    /**
     * Retrieves the items in the <code>dataList</code>, transforms to JSON format, and sends as a plugin result
     * @param callbackContext
     */
	private void getData(CallbackContext callbackContext) {
		JSONObject json = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		try{
			if(pairedDevice!=null){
				for(int i=0;i< dataList.size();i++){
					jsonArray.put(new JSONObject(gson.toJson(dataList.get(i))));
				}
			}
			json.put("data",jsonArray);
		}catch(Exception e){
			e.printStackTrace();
		}
		dataList.clear();
		bleDeviceManager.interruptCurrentTask();
		Log.d(tag,"Interrupt Current Task!");
		PluginResult result = new PluginResult(PluginResult.Status.OK, json);
		callbackContext.sendPluginResult(result);
		

	}

    /**
     * Initiates the scanning of devices in range
     * @param jsonString
     * @param callbackContext
     */
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
					Log.d(tag, lsDevice.getDeviceName() + " " + scannedDeviceList.size());
					BleDevice bleDevice=new BleDevice(
						lsDevice.getDeviceName(),
						lsDevice.getDeviceAddress(),
						typeConversion.enumToInteger(lsDevice.getDeviceType()),
						lsDevice.getModelNumber());
					scannedDeviceList.add(bleDevice);
				}
			}
		};
		bleDeviceManager.setCallback(mDelegate);
		if(!bleDeviceManager.startScanning()){
			callbackContext.error(RUNNING_TASKS_ERROR);
		}else{
			Runnable myTask = new Runnable() {
				@Override
				public void run() {
					bleDeviceManager.stopScanning();
					getScannedDevices(callbackContextCopy);
				}
			};
			mHandler.postDelayed(myTask, timeout);
		}
	}

    /**
     * Stops the scanning operation
     * @param callbackContext
     */
	private void stopScanning(CallbackContext callbackContext){
		bleDeviceManager.setCallback(mDelegate);
		if(bleDeviceManager.stopScanning()){
			callbackContext.success("Scanning stopped.");
		}else{
			callbackContext.error(RUNNING_TASKS_ERROR);
		}
	}

    /**
     * Initates device pairing based on the provided device JSON object
     * @param jsonString
     * @param callbackContext
     */
	private void pairDevice(String jsonString, CallbackContext callbackContext){
		try{
			final CallbackContext callbackContextCopy = callbackContext;
			JSONObject json = new JSONObject(jsonString);
			BleDevice device = gson.fromJson(json.get("device").toString(), BleDevice.class);
			int timeout = (Integer)json.get("timeout");
			Log.d(tag,"param:"+jsonString);
			Log.d(tag,"Pair device:"+device.toString());
            LSDeviceInfo mDevice = new LSDeviceInfo();
			mDevice.setDeviceName(device.getName());
			mDevice.setDeviceType(typeConversion.integerToEnum(device.getSensorType()));
			mDevice.setDeviceAddress(device.getAddress());
			mDevice.setModelNumber(device.getModelNumber());
			bleDeviceManager.stopScanning();
			pairInterruptFlag = true;
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
						pairInterruptFlag = false;
						getPairedDevice(callbackContextCopy);
					}
				}
			};
			bleDeviceManager.setCallback(mDelegate);
			if(!bleDeviceManager.toPairDevice(mDevice)){
				callbackContext.error(RUNNING_TASKS_ERROR);
			}else{
				Runnable myTask = new Runnable() {
					@Override
					public void run() {
						if(pairInterruptFlag){
							Log.d(tag,"InterruptResult:"+bleDeviceManager.interruptCurrentTask());
						}
						callbackContextCopy.error("Pair Time Out.");
					}
				};
				mHandler.postDelayed(myTask, timeout);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

    /**
     * Retrieves the items in the <code>scannedDeviceList</code>, transforms to JSON format, and sends as a plugin result
     * @param callbackContext
     */
	private void getScannedDevices(CallbackContext callbackContext) {
		JSONObject response = new JSONObject();
		JSONArray json = new JSONArray();
		try{
			for(int i=0;i< scannedDeviceList.size();i++){
				//json.put(scannedDeviceList.get(i).getJson());
				// JSONObject obj = new JSONObject();
				// obj.put("address", scannedDeviceList.get(i).getAddress());
				// obj.put("name", scannedDeviceList.get(i).getName());
				// obj.put("modelNumber", scannedDeviceList.get(i).getModelNumber());
				JSONObject obj = new JSONObject(gson.toJson(scannedDeviceList.get(i)));
				Log.d(tag, "obj");
				// json.put(gson.toJson(scannedDeviceList.get(i)));
				json.put(obj);
			}
			response.put("devices", json);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		if(json.length()==0){
			callbackContext.error("No device found.");
		}
		else{
			scannedDeviceList.clear();
			PluginResult result = new PluginResult(PluginResult.Status.OK, response);
			callbackContext.sendPluginResult(result);
		}
	}

    /**
     * Retrieves the paired device information in <code>pairedDevice</code>, transforms to JSON format, and sends as a plugin result
     * @param callbackContext
     */
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
    	super.initialize(cordova, webView);

		mDelegate = new DeviceManagerCallback(){
            //TODO: Check if initialization will work without the overriden implementation
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
					scannedDeviceList.add(bleDevice);
				}
			}
		};

        // Initialize shared preferences
		sharedPref = this.cordova.getActivity().getSharedPreferences("com.leotech.plugin.LifesenseBLEPlugin", Context.MODE_PRIVATE);

        // Instantiate BleDeviceManager
		bleDeviceManager = BleDeviceManager.getInstance();
		bleDeviceManager.initialize(this.cordova.getActivity().getApplicationContext(),mDelegate);
	}

    /**
     * Helper method to check whether <code>scannedDeviceList</code> contains the provided device address
     * @param address
     * @return boolean
     */
	private boolean deviceExists(String address) {
		boolean found=false;
		for (int i = 0; i < scannedDeviceList.size(); i++)
		{
			if (scannedDeviceList.get(i).getAddress().equals(address))
			{
				return found=true;
			}
		}
		return found;
	}

}
