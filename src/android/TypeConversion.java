package com.leotech.plugin;

import java.util.HashMap;

import lifesense.ble.commom.DeviceType;

public class TypeConversion 
{
	private  int LF_SENSOR_TYPE_UNKNOW=0x00; 
	private  int LF_SENSOR_TYPE_WEIGHT_SCALE=0x01;
	private  int LF_SENSOR_TYPE_PEDOMETER=0x04; 
	private  int LF_SENSOR_TYPE_BLOODPRESSURE=0x05; 
	private  int LF_SENSOR_TYPE_KITCHEN_SCALE=0x06;
	private  int LF_SENSOR_TYPE_HEIGHT=0x07; 
	private  int LF_SENSOR_TYPE_GENERAL_WEIGHT_SCALE=0x08;
	private HashMap<String, String> modelHashMap;
	
	public DeviceType integerToEnum(int type)
	{
		DeviceType mtype = null;
		switch (type) 
		{

		case 0: 
		{
			mtype=DeviceType.UNKNOWN;
		}
			break;
		case 1: 
		{
			mtype=DeviceType.WEIGHT_SCALE;
		}
			break;
		case 4: 
		{
			mtype=DeviceType.PEDOMETER;
			
		}
			break;
		case 5: 
		{
			mtype=DeviceType.BLOOD_PRESSURE;
		}
			break;
		case 6: 
		{
			mtype=DeviceType.KITCHEN_SCALE;
		}
			break;
		case 7: 
		{
			mtype=DeviceType.HEIGHT_SCALE;
		}
			break;
		case 8:
		{
			mtype=DeviceType.BODY_FAT_SCALE;
		}break;
		}			
		return mtype;
	}
	
	public int enumToInteger(DeviceType deviceType) 
	{
		int mtype=0;
		switch (deviceType) 
		{

		case PEDOMETER: 
		{
			mtype=LF_SENSOR_TYPE_PEDOMETER;
		}
			break;
		case WEIGHT_SCALE: 
		{
			mtype=LF_SENSOR_TYPE_WEIGHT_SCALE;
		}
			break;
		case BLOOD_PRESSURE: 
		{
			mtype=LF_SENSOR_TYPE_BLOODPRESSURE;
			
		}
			break;
		case KITCHEN_SCALE: 
		{
			mtype=LF_SENSOR_TYPE_KITCHEN_SCALE;
		}
			break;
		case HEIGHT_SCALE: 
		{
			mtype=LF_SENSOR_TYPE_HEIGHT;
		}
			break;
		case BODY_FAT_SCALE: 
		{
			mtype=LF_SENSOR_TYPE_GENERAL_WEIGHT_SCALE;
		}
			break;
		default:
		{
			mtype=LF_SENSOR_TYPE_UNKNOW;
		}break;
		}			
		return mtype;
	}

		public String byte2hex(byte[] data) {
			 String   hs="";
			 String   stmp="";
			 for (int n=0;n<data.length;n++) 
			 {
				 stmp=(Integer.toHexString(data[n]&0XFF));
				 if(stmp.length()==1) 
				 {
					 hs=hs+"0"+stmp;
				 }
				 else  
				 {
					 hs=hs+stmp;
				 }
					 
			 }
			 return   hs.toUpperCase();	 
		}
		
		 private  byte charToByte(char c)
		 { 
			 return (byte) "0123456789ABCDEF".indexOf(c);  
			 
		 }  
		 
	public  byte[] hexStringToBytes(String hexString) 
	{
		if (hexString == null || hexString.equals("")) 
		{   return null;  
		}  
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;  
		char[] hexChars = hexString.toCharArray();  
		byte[] data = new byte[length];  
		for (int i = 0; i < length; i++) 
		{  
			int pos = i * 2;  
			data[i] = (byte) (charToByte(hexChars[pos]) << 4 
					| charToByte(hexChars[pos + 1]));
			}  
		return data;  
		} 
	public HashMap<String, String> getModelHashMap(int type)
	{
		modelHashMap=new HashMap<String,String>();
		switch(type)
		{
			case 4:
			{
			modelHashMap.put("406A0","LS406-B");
			modelHashMap.put("402A0","LS402-B");
			modelHashMap.put("402A1","LS402-B");
			modelHashMap.put("401A0","LS401-B");
			modelHashMap.put("405A0","LS405-B");
			};break;
			case 5:
			{
				modelHashMap.put("1014B","TMB-1014-BT");
				modelHashMap.put("810A0","LS810-B/TENSIO");
				modelHashMap.put("802A0","LS802-B");
				modelHashMap.put("805A0","LS805-B");
				modelHashMap.put("1018B","TMB-1018-BT");
				modelHashMap.put("808A0","LS808-B");
				modelHashMap.put("13950","BU 550 connect");
				modelHashMap.put("13930","BU 575 connect");
				modelHashMap.put("13960","BW 300 connect");
				modelHashMap.put("10140","vs-4300-w");
				modelHashMap.put("810A1","LS810-B");
				modelHashMap.put("802A1","vs-4400");
				modelHashMap.put("805A1","vs-4000");
				modelHashMap.put("10180","TMB-1018-BT");
				modelHashMap.put("10141","vs-4300-b");
				modelHashMap.put("802A2","LS802-B");
				modelHashMap.put("10181","TMB-1018-BT");
				modelHashMap.put("10142","TMB-1014-BT");
				modelHashMap.put("802A3","LS802-B");
				modelHashMap.put("10182","TMB-1018-BT");
				modelHashMap.put("10143","RWBPM01");
				modelHashMap.put("802A4","TENSIO SCREEN");
				modelHashMap.put("10144","TMB-1014-BT");
				modelHashMap.put("10145","TMB-1014-BT");
				modelHashMap.put("10146","BPW-9154");
				modelHashMap.put("10147","TMB-1014-BT");
				modelHashMap.put("10148","TMB-1014-BT");
				modelHashMap.put("10149","TMB-1014-BT");
				modelHashMap.put("1014A","TMB-1014-BT");
				modelHashMap.put("1014C","TMB-1014-BT");
				modelHashMap.put("995A0","TMB995(BT4.0)");
				};break;
			case 1:
			{
				modelHashMap.put("102B ","LS102-B");
				modelHashMap.put("103B ","LS103-B");
				modelHashMap.put("106A0","BS-705-BT");
				modelHashMap.put("12660","WEB COACH One");
				modelHashMap.put("102B1","LS102-B");
				modelHashMap.put("12661","WEB COACH One");
				modelHashMap.put("12662","WEB COACH One");
				modelHashMap.put("101A0","LS101-B");
				modelHashMap.put("12300","A2");
				modelHashMap.put("12690","B1");
				modelHashMap.put("922A0","S1-B");
				modelHashMap.put("202B ","LS202-B");
				modelHashMap.put("203B ","LS203-B");
				modelHashMap.put("102B0","LS102-B");
				modelHashMap.put("12301","A2");
				modelHashMap.put("106A1","BS-705-B");
				modelHashMap.put("922A1","S1-B");
				};break;
			case 6:
			{
				modelHashMap.put("1136B", "GKS-1136-BT");
				};break;
			case 7:
			{
				modelHashMap.put("305A0", "GKS-1136-BT");
				};break;
			case 8:
			{
				modelHashMap.put("1251B","GBF-1251-B");
				modelHashMap.put("1255B","BF-1255-B");
				modelHashMap.put("1256B","BF-1256-B");
				modelHashMap.put("1257B","GBF-1257-B");
				modelHashMap.put("1144B","GBF-1144-B");
				modelHashMap.put("12670","WEB COACH");
				modelHashMap.put("13190","7222F");
				modelHashMap.put("202B ","LS202-B");
				modelHashMap.put("203B ","LS203-B");
				modelHashMap.put("202B5","202");
				modelHashMap.put("203B0","vs-3200-w");
				modelHashMap.put("12510","GBF-1251-B");
				modelHashMap.put("12550","vs-3100");
				modelHashMap.put("12560","BF-1256-B");
				modelHashMap.put("12570","GBF-1257-B");
				modelHashMap.put("12671","WEB COACH");
				modelHashMap.put("13191","7224FBOW");
				modelHashMap.put("202B6","BS 440 connect");
				modelHashMap.put("203B1","vs-3200-b");
				modelHashMap.put("13192","SC-902");
				modelHashMap.put("203B2","9154 BK3R");
				modelHashMap.put("203B3","9154 WH3R");
				modelHashMap.put("203B4","LS203-B");
				modelHashMap.put("202B0","LS202-B");
				};break;
				
			default:break;
			
		}
		return modelHashMap;	
	}
}
