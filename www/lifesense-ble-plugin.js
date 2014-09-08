var lifesenseBLEPlugin = {
    startScanning: function(successCallback, errorCallback) {
        cordova.exec(
            successCallback, // success callback function
            errorCallback, // error callback function
            'LifesenseBLEPlugin', // mapped to our native Java class called "LifesenseBLEPlugin"
            'startScanning', // with this action name
            [{}]
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
     getCurrentDevices:function(successCallback, errorCallback) {
        cordova.exec(
            successCallback, 
            errorCallback, 
            'LifesenseBLEPlugin', 
            'getCurrentDevices',
            [{}]
        ); 
     },
     pairDevice:function(successCallback, errorCallback, device) {
        cordova.exec(
            successCallback,
            errorCallback, 
            'LifesenseBLEPlugin',
            'pairDevice', 
            [{device:device}]
        ); 
        console.log(JSON.stringify(device));
     },
     getPairedDevice:function(successCallback, errorCallback) {
        cordova.exec(
            successCallback, 
            errorCallback, 
            'LifesenseBLEPlugin', 
            'getPairedDevice',
            [{}]
        ); 
     },
     askForData:function(successCallback, errorCallback) {
        cordova.exec(
            successCallback, 
            errorCallback, 
            'LifesenseBLEPlugin', 
            'askForData',
            [{}]
        ); 
     },
     askForDataByDeviceMacAddress:function(successCallback, errorCallback, address) {
        cordova.exec(
            successCallback, 
            errorCallback, 
            'LifesenseBLEPlugin', 
            'askForDataByDeviceMacAddress',
            [{address:address}]
        ); 
     },
     getData:function(successCallback, errorCallback) {
        cordova.exec(
            successCallback, 
            errorCallback, 
            'LifesenseBLEPlugin', 
            'getData',
            [{}]
        ); 
     },
     clearDeviceList:function(successCallback, errorCallback) {
        cordova.exec(
            successCallback, 
            errorCallback, 
            'LifesenseBLEPlugin', 
            'clearDeviceList',
            [{}]
        ); 
     }
}
module.exports = lifesenseBLEPlugin;
