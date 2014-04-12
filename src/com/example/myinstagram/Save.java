package com.example.myinstagram;

import java.io.File;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Save extends Activity {
	String filename="default";
	Button saver;
	EditText name;
	
	static String remoteConfig = "Remote Configuration";
	SharedPreferences config;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_save);
		
		config = getSharedPreferences(remoteConfig, 0);
		saver = (Button)findViewById(R.id.saveNow);
		name = (EditText) findViewById(R.id.filename);
		saver.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				filename = name.getText().toString()+".jpg";
				if(checkValidFilename()){
					SharedPreferences.Editor editor = config.edit();
					editor.putString("filename", filename);
					editor.commit();
					
					setResult(Activity.RESULT_OK);
					finish();
				}else{
					showToast("Please enter a valid filename");
				}
			}
		});
	}
	
	protected boolean checkValidFilename(){
		try{
			File root = new File(Environment.getExternalStorageDirectory()+ File.separator + "PhotoMorph" + File.separator);
			File f = new File(root,filename);
			f.getCanonicalPath();
		}catch(Exception e){
			return false;
		}
		return true;
	}
	
	private void showToast(String text){
		Toast t = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
		t.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.save, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_save, container,
					false);
			return rootView;
		}
	}

}
