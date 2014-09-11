var lifesenseBLEPlugin = {
	startScanning: function(successCallback, errorCallback, timeout) {
		cordova.exec(
			successCallback, // success callback function
			errorCallback, // error callback function
			'LifesenseBLEPlugin', // mapped to our native Java class called "LifesenseBLEPlugin"
			'startScanning', // with this action name
			[{timeout:timeout}]
		); 
	 },
	 stopScanning: function(successCallback, errorCallback) {
		cordova.exec(
			successCallback, 
			errorCallback, 
			'LifesenseBLEPlugin',
			'stopScanning',
			[{}]
		); 
	 },
	 pairDevice:function(successCallback, errorCallback, device, timeout) {
		cordova.exec(
			successCallback,
			errorCallback, 
			'LifesenseBLEPlugin',
			'pairDevice', 
			[{device:device,timeout:timeout}]
		); 
		console.log(JSON.stringify(device));
	 },
	 askForDataByDeviceMacAddress:function(successCallback, errorCallback, address, timeout) {
		cordova.exec(
			successCallback, 
			errorCallback, 
			'LifesenseBLEPlugin', 
			'askForDataByDeviceMacAddress',
			[{address:address,timeout:timeout}]
		); 
	 }
	 // ,
	 //  getCurrentDevices:function(successCallback, errorCallback) {
	 //    cordova.exec(
	 //        successCallback, 
	 //        errorCallback, 
	 //        'LifesenseBLEPlugin', 
	 //        'getCurrentDevices',
	 //        [{}]
	 //    ); 
	 // },
	 // getPairedDevice:function(successCallback, errorCallback) {
	 //    cordova.exec(
	 //        successCallback, 
	 //        errorCallback, 
	 //        'LifesenseBLEPlugin', 
	 //        'getPairedDevice',
	 //        [{}]
	 //    ); 
	 // },
	 // askForData:function(successCallback, errorCallback) {
	 //    cordova.exec(
	 //        successCallback, 
	 //        errorCallback, 
	 //        'LifesenseBLEPlugin', 
	 //        'askForData',
	 //        [{}]
	 //    ); 
	 // },
	 // getData:function(successCallback, errorCallback) {
	 //    cordova.exec(
	 //        successCallback, 
	 //        errorCallback, 
	 //        'LifesenseBLEPlugin', 
	 //        'getData',
	 //        [{}]
	 //    ); 
	 // },
	 // clearDeviceList:function(successCallback, errorCallback) {
	 //    cordova.exec(
	 //        successCallback, 
	 //        errorCallback, 
	 //        'LifesenseBLEPlugin', 
	 //        'clearDeviceList',
	 //        [{}]
	 //    ); 
	 // }
}
module.exports = lifesenseBLEPlugin;
