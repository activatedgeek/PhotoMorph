package com.example.myinstagram;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

public class SelectImage extends ActionBarActivity {
	static final int IMG_CAPTURE=1,IMG_CHOOSE=2;
	static String remoteConfig = "Remote Configuration";
	SharedPreferences config;
	
	Bitmap imageBmp=null;
	String imageBmpPath=null;
	Button reset,capture,gallery,clear;
	Button process,dummy;
	LinearLayout options;
	
	Spinner effects;
	String[] list;
	String effect="None";
	
	DrawView display;
	RelativeLayout main;
	RelativeLayout.LayoutParams params;
	
	private void setup(){
		options = (LinearLayout) findViewById(R.id.options);
		process = (Button)findViewById(R.id.process);
		config = getSharedPreferences(remoteConfig, 0);	
		dummy = (Button)findViewById(R.id.dummy);
				
		reset = (Button)findViewById(R.id.reset);
		capture = (Button)findViewById(R.id.choose);
		gallery = (Button)findViewById(R.id.gallery);
		clear = (Button)findViewById(R.id.clear);
		
		effects = (Spinner)findViewById(R.id.effect);
		list = getResources().getStringArray(R.array.effects);
		SpinnerAdapter<CharSequence> adapter = new SpinnerAdapter<CharSequence>(getApplicationContext(), list);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		effects.setAdapter(adapter);
		
		display = (DrawView) findViewById(R.id.editor);
		params = (RelativeLayout.LayoutParams)display.getLayoutParams();
		View[] v = {effects,options};
		display.setRelatedViews(v);
	}
	
	protected void setupListeners(){
		reset.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showToast("Reset selected image");
				imageBmp=null;
				display.reset();
				display.setImageResource(R.drawable.placeholder);
			}
		});
		
		capture.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showToast("Capture Image from Camera");
				captureImage();
			}
		});
		
		gallery.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showToast("Select Image from Gallery");
				chooseImage();
			}
		});
		
		clear.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showToast("Cleared selection");
				try{
					display.reset();
				}catch(Exception e){
					showToast("Error in Loading Image");
					display.setImageResource(R.drawable.placeholder);
				}
			}
		});
		
		process.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(imageBmp==null){
					showToast("Please load an image first from camera/gallery");
				}
				else if(effect.equalsIgnoreCase("None")){
					showToast("Please select an effect!");
					effects.performClick();
				}
				else if(!verifyIP(config.getString("remoteIp", "192.168.8.1"))){
					showToast("Check your network settings!");
					Intent settings = new Intent(getApplicationContext(), Settings.class);
					startActivity(settings);
				}
				else{
					String path,ePath;
					try {
						path = display.saveImage();
						if(path!="-1"){
							ePath = saveFile("effect.data");
							SendImage si = new SendImage(path, ePath,getApplicationContext(),display,SelectImage.this);
							si.execute();
						}else{
							showToast("Invalid operation/selection");
						}
					} catch (IOException e) {
						showToast("Error: "+e.getMessage());
					}
				}
			}
		});
		
		effects.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,int row, long arg3){
				effect = list[row];
				showToast("Effect to be applied: "+list[row]);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
		});
		
		//Dummy button for settings popup
		dummy.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent settings = new Intent(getApplicationContext(), Settings.class);
				startActivity(settings);
			}
		});
		dummy.performClick();
	}
	
	@SuppressLint("NewApi") @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		
		setContentView(R.layout.activity_select_image);
		//getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setup();
		display.setImageResource(R.drawable.placeholder);
		setupListeners();
	}
	
	private void showToast(String text){
		Toast t = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
		t.show();
	}
	
	private boolean verifyIP(String ip){
		final String IPADDRESS_PATTERN = 
				"^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
				"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
				"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
				"([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
		return ip.matches(IPADDRESS_PATTERN);
	}
	
	private void captureImage(){
		Intent capture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		if(capture.resolveActivity(getPackageManager())!= null){
			File root = new File(Environment.getExternalStorageDirectory()+ File.separator + "myInstagram" + File.separator);
			root.mkdirs();
			File save = new File(root,"sel.jpg");
			Uri saveImageHere = Uri.fromFile(save);
			capture.putExtra(MediaStore.EXTRA_OUTPUT, saveImageHere);
			imageBmpPath = save.getAbsolutePath();
			startActivityForResult(capture, IMG_CAPTURE);
		}
	}
	
	private void chooseImage(){
		Intent choose = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		if(choose.resolveActivity(getPackageManager())!= null){
			startActivityForResult(choose, IMG_CHOOSE);
		}
	}
	
	private String saveFile(String name){
		File root = new File(Environment.getExternalStorageDirectory()+ File.separator + "myInstagram" + File.separator);
		root.mkdirs();
		File save = new File(root,name);
		try {
			PrintWriter pw = new PrintWriter(new FileOutputStream(save,false));
			pw.write(effect);
			pw.close();
			showToast("Save Successful!");
		} catch (FileNotFoundException e) {
			Log.i("imageServer","Error saving file: "+e.getMessage());
		}
		return save.getAbsolutePath();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK && requestCode == IMG_CAPTURE){
			display.reset();
			imageBmp = (Bitmap) BitmapFactory.decodeFile(imageBmpPath);
			display.setImageBitmap(imageBmp);
			
			ContentValues content = new ContentValues();
			content.put(Images.Media.DATE_TAKEN, System.currentTimeMillis());
			content.put(Images.Media.MIME_TYPE, "image/jpeg");
			content.put(MediaStore.MediaColumns.DATA, imageBmpPath);
			getContentResolver().insert(Images.Media.EXTERNAL_CONTENT_URI, content);
		}
		else if(resultCode == RESULT_OK && requestCode == IMG_CHOOSE && data!=null){
			display.reset();
			Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
            cursor.moveToFirst();
            String picturePath = cursor.getString(cursor.getColumnIndex(filePathColumn[0]));
            cursor.close();
            imageBmpPath = picturePath;
            imageBmp = (Bitmap)BitmapFactory.decodeFile(imageBmpPath);
            try{
				display.setImageBitmap(imageBmp);
			}catch(Exception e){
				showToast("Error in Loading Image");
				display.setImageResource(R.drawable.placeholder);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.select_image, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if(id == R.id.toggle){
			if(options.getVisibility() == View.INVISIBLE){
				display.changeView(View.VISIBLE);
			}else{
				display.changeView(View.INVISIBLE);
			}
		}
		if(id == R.id.settings){
			Intent settings = new Intent(this, Settings.class);
			startActivity(settings);
		}
		else if(id == R.id.exit){
			this.finish();
		}
		return super.onOptionsItemSelected(item);
	}

	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_select_image,
					container, false);
			return rootView;
		}
	}

}
