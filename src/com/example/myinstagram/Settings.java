package com.example.myinstagram;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Settings extends Activity {
	static String remoteConfig = "Remote Configuration";
	String ipAddress;
	int port;
	Button save;
	EditText remoteIP,remotePort;
	SharedPreferences config;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		setup();
		defaultPref();
		
		save.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ipAddress =  remoteIP.getText().toString();
				port = Integer.parseInt(remotePort.getText().toString());
				if(verifyIP(ipAddress)){
					SharedPreferences.Editor editor = config.edit();
					editor.putString("remoteIp", ipAddress);
					editor.putInt("remotePort", port);
					editor.commit();
					showToast("Configuration saved!");
					finish();
				}else{
					showToast("Please enter a valid IP address!");
				}
			}
		});
	}
	
	private boolean verifyIP(String ip){
		final String IPADDRESS_PATTERN = 
				"^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
				"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
				"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
				"([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
		return ip.matches(IPADDRESS_PATTERN);
	}
	
	private void showToast(String text){
		Toast t = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
		t.show();
	}
	
	protected void setup(){
		save = (Button) findViewById(R.id.save);
		remoteIP = (EditText) findViewById(R.id.remoteip);
		remotePort = (EditText) findViewById(R.id.remoteport);
		config = getSharedPreferences(remoteConfig, 0);
	}
	
	protected void defaultPref(){
		String remoteIp = config.getString("remoteIp", "192.168.8.1");
		int port = config.getInt("remotePort", 80);
		remoteIP.setText(remoteIp);
		remotePort.setText(Integer.toString(port));
	}
}
