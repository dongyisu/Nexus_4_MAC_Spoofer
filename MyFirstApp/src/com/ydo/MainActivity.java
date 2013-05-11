package com.ydo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	 public final static String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
	 public final static String PATCH_FILE_PATH = "/data/misc/wifi/";
	 //public final static String PATCH_FILE_PATH = "/sdcard/backups/test/";
	 
	 private Thread thread;   
	 
	 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView currentMacValueText=(TextView)findViewById(R.id.textCurrentMacValue);
        String currentMac = getMacAddress(this);
        currentMacValueText.setText(currentMac);
       

        if( getIntent().getBooleanExtra("Exit me", false)){
            finish();
            return; // add this to prevent from doing unnecessary stuffs
        }
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    /** Called when the user clicks the Send button */
    public void sendMessage(View view) {
        // Do something in response to button
//    	Intent intent = new Intent(this, DisplayMessageActivity.class);
//    	EditText editText = (EditText) findViewById(R.id.edit_message);
//    	String message = editText.getText().toString();
//    	intent.putExtra(EXTRA_MESSAGE, message);
//    	startActivity(intent);
    }
    
    
    /**
     * Click on the spoof button
     * @param view
     */
    public void spoofMacAddress(View view) {
    	EditText userInputMacText = (EditText) findViewById(R.id.EditFakeMac);
    	String spoofMacAddress = userInputMacText.getText().toString();
    	
    	
    	Context context = getApplicationContext();
    	int duration = Toast.LENGTH_LONG;
    	CharSequence toastText;
    	//validate the input format
    	if (validateInputMacAddress(spoofMacAddress)){
    		
    		toastText=getText(R.string.msg_mac_valid);
    		Toast toast = Toast.makeText(context, toastText, duration);
        	toast.show();
        	if (validateCurrentDeviceModel()){
	        	if (isDeviceRooted()){
	        		toastText=getText(R.string.device_rooted);
	        		toast = Toast.makeText(context, toastText, duration);
	            	toast.show();
		        	toastText=changeNexus4MacAddressStep1(spoofMacAddress);
		        	toast = Toast.makeText(context, toastText, duration);
		        	toast.show();
		        	toastText=changeNexus4MacAddressStep2(getMacAddress(this),spoofMacAddress);
		        	toast = Toast.makeText(context, toastText, duration);
		        	toast.show();
		        	toastText=getText(R.string.restart_wifi);
		        	toast = Toast.makeText(context, toastText, duration);
		        	toast.show();
		        	//restart WIFI service to take account
		        	restartWifi();
		        	//refresh the current MAC adress whenever it's spoofed or not
		        	// FIXEME there is a delay when the MAC is reloaded and returned by WIfimanager

		        	
		        	TextView currentMacValueText=(TextView)findViewById(R.id.textCurrentMacValue);
		            String currentMac = getMacAddress(this);
		            System.out.println("new mac address =" + currentMac);
		            currentMacValueText.setText(currentMac);
		            
	        	}else{
	        		toastText=getText(R.string.device_not_rooted);
	        		toast = Toast.makeText(context, toastText, duration);
	            	toast.show();
	        	}
        	}else{
        		toastText=getText(R.string.device_not_nexus4);
        		toast = Toast.makeText(context, toastText, duration);
            	toast.show();
        	}
    		
    	}else{
    		toastText=getText(R.string.msg_mac_invalid);
    		Toast toast = Toast.makeText(context, toastText, duration);
        	toast.show();
    	}
    	
    	
    }
    
    /**
     * Check the current device is a Nexus4
     * @return
     */
    public boolean validateCurrentDeviceModel(){
    	String deviceName=getDeviceName();
    	// my nexus4 has a device name "LGE Nexus 4"
    	return deviceName.contains("Nexus 4");
    }
    
    private String getDeviceName() {
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		if (model.startsWith(manufacturer)) {
			return capitalize(model);
		} else {
			return capitalize(manufacturer) + " " + model;
		}
	}


    private String capitalize(String s) {
		if (s == null || s.length() == 0) {
			return "";
		}
		char first = s.charAt(0);
		if (Character.isUpperCase(first)) {
			return s;
		} else {
			return Character.toUpperCase(first) + s.substring(1);
		}
	} 
    
    /**
     * Validate Input MAC address's format
     * @param inputMac
     * @return true if the value is valid MAC
     */
    public boolean validateInputMacAddress(String inputMac){
    	return Pattern.matches("^([0-9A-F]{2}){6}$", inputMac);
    }
    
    /**
     * Check if the device is rooted
     * @return
     */
    public boolean isDeviceRooted(){
    	return ShellInterface.isSuAvailable();
    }
    
    /**
     * Change WCNSS_qcom_cfg.ini file
     * @param targetMac
     * @return
     */
    public String changeNexus4MacAddressStep1(String targetMac){

    	
    	
    	try {
    		
    		String macAddressTwoPointRemoved= targetMac.replaceAll(":", "");
    		
    		//String cmd = "su -c sed -i  's#^Intf0MacAddress=.*#Intf0MacAddress=848506D51776#g' /sdcard/backups/test/WCNSS_qcom_cfg.ini";
    		String cmd = "su -c sed  's#^Intf0MacAddress=.*#Intf0MacAddress="+macAddressTwoPointRemoved+"#g' "+PATCH_FILE_PATH+"WCNSS_qcom_cfg.ini >"+PATCH_FILE_PATH+"WCNSS_qcom_cfg.ini.tmp"
    				+ "&& mv "+PATCH_FILE_PATH+"WCNSS_qcom_cfg.ini "+PATCH_FILE_PATH+"WCNSS_qcom_cfg.ini.bak "
    				+ "&& mv "+PATCH_FILE_PATH+"WCNSS_qcom_cfg.ini.tmp "+PATCH_FILE_PATH+"WCNSS_qcom_cfg.ini";		
    		
    		
    		
    		// Executes the command.
    		Process process = Runtime.getRuntime().exec(cmd);

    		//Process process = Runtime.getRuntime().exec("su -c ls -l /sdcard/backups/test/");

    		// Reads stdout.
    		// NOTE: You can write to stdin of the command using
    		// process.getOutputStream().
    		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    		int read;
    		char[] buffer = new char[4096];
    		StringBuffer output = new StringBuffer();
    		while ((read = reader.read(buffer)) > 0) {
    		    output.append(buffer, 0, read);
    		}
    		reader.close();

    		// Waits for the command to finish.
    		process.waitFor();

    		return output.toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
    	

    	
    	
    }

    private String formatHexString (String inputMac){
    	StringBuffer sb= new StringBuffer();
    	int index=0;
    	while (index<inputMac.length()) {
			sb.append("\\x");
			sb.append(inputMac.substring(index, index+2));
			index = index+2;
		}
    	return sb.toString();
    }
    
    /**
     * Patch the WCNSS_qcom_wlan_nv.bin using the tr command and create a backup
     * @param originalMac
     * @param targetMac
     * @return console output
     */
    
    public String changeNexus4MacAddressStep2(String originalMac, String targetMac){
    	
    	try {
    		
    		String cleanOriginalMac=formatHexString(originalMac.replaceAll(":", ""));
    		
    		String cleanTargetMac=formatHexString(targetMac);
    		//String cleanTargetMac="\\x84\\x85\\x06\\xD5\\x17\\x76";
    		
    		
    		//String cmd = "su -c tr '[\\x10\\x68\\x3f\\x8d\\xda\\x8e]' '[\\x84\\x85\\x06\\xD5\\x17\\x76]' </sdcard/backups/test/WCNSS_qcom_wlan_nv.bin >/sdcard/backups/test/WCNSS_qcom_wlan_nv.bin.tmp && mv /sdcard/backups/test/WCNSS_qcom_wlan_nv.bin /sdcard/backups/test/WCNSS_qcom_wlan_nv.bin.bak && mv /sdcard/backups/test/WCNSS_qcom_wlan_nv.bin.tmp /sdcard/backups/test/WCNSS_qcom_wlan_nv.bin";
    		
    				String cmd = "su -c tr '["+cleanOriginalMac+"]' '["+cleanTargetMac+"]' <"+PATCH_FILE_PATH+"WCNSS_qcom_wlan_nv.bin >"+PATCH_FILE_PATH+"WCNSS_qcom_wlan_nv.bin.tmp "
    				+ "&& mv "+PATCH_FILE_PATH+"WCNSS_qcom_wlan_nv.bin "+PATCH_FILE_PATH+"WCNSS_qcom_wlan_nv.bin.bak "
    				+ "&& mv "+PATCH_FILE_PATH+"WCNSS_qcom_wlan_nv.bin.tmp "+PATCH_FILE_PATH+"WCNSS_qcom_wlan_nv.bin";
//    		
    		// Executes the command.
    		Process process = Runtime.getRuntime().exec(cmd);
    		

    		// Reads stdout.
    		// NOTE: You can write to stdin of the command using
    		// process.getOutputStream().
    		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    		int read;
    		char[] buffer = new char[4096];
    		StringBuffer output = new StringBuffer();
    		while ((read = reader.read(buffer)) > 0) {
    		    output.append(buffer, 0, read);
    		}
    		reader.close();

    		// Waits for the command to finish.
    		process.waitFor();

    		return output.toString();
    		} catch (IOException e) {
    		throw new RuntimeException(e);
    		} catch (InterruptedException e) {
    		throw new RuntimeException(e);
    		}
    	

    }
    
    /**
     * Get MAC address of the device
     * @param context
     * @return
     */
    public String getMacAddress(Context context) {
        WifiManager wimanager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        String macAddress = wimanager.getConnectionInfo().getMacAddress();
        if (macAddress == null) {
            macAddress = "Device don't have mac address or wi-fi is disabled";
        }
        return macAddress;
    }
    
    /**
     * 
     * @param context
     */
    public void restartWifi() {
        
    	WifiManager wifiManager  = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
	    if(wifiManager.isWifiEnabled()){
	      wifiManager.setWifiEnabled(false);
	      wifiManager.setWifiEnabled(true);
	    }else{
	      wifiManager.setWifiEnabled(true);
	    }
    }
    
    /**
     * send intent to exit apps
     */
    private void exitApps(){
    	Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("Exit me", true);
        startActivity(intent);
        finish();
    }
    
    /**
     * menu management
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
//            case R.id.menu_about:
//                //showAbout();
//                return true;
            case R.id.menu_exit:
                exitApps();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
