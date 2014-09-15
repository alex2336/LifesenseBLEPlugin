#import <Cordova/CDVPlugin.h>
#import <CoreBluetooth/CoreBluetooth.h>
#import <LSHardwareFramework/LSHardwareFramework.h>
@interface LifesenseBLEPlugin : CDVPlugin


//checks if device has step counter support
- (void) startScanning:(CDVInvokedUrlCommand*)command;

//start live update
- (void) stopScanning:(CDVInvokedUrlCommand *)command;

//stop live update
- (void) pairDevice:(CDVInvokedUrlCommand *)command;

//get data based on from and to arguments (in milliseconds)
- (void) askForDataByDeviceMacAddress:(CDVInvokedUrlCommand *)command;

@end
