package com.example.myinstagram;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class SendImage extends AsyncTask<Void, Void, Void>{
	static String remoteConfig = "Remote Configuration";
	SharedPreferences config;
	Context context;
	DrawView display;
	String remoteIp,path,ePath;
	int remotePort;
	Socket socket=null;
	boolean result;
	String message;
	ProgressDialog dialog;
	
	public SendImage(String path, String ePath,Context context,DrawView display,Activity activity){
		this.context = context;
		this.path = path;
		this.ePath = ePath;
		this.display = display;
		config = context.getSharedPreferences(remoteConfig, 0);
		remoteIp = config.getString("remoteIp", "192.168.8.1");
		remotePort = config.getInt("remotePort", 80);
		dialog = new ProgressDialog(activity);
		dialog.setMessage("Please wait, image is being processed..");
		dialog.setIndeterminate(true);
        dialog.setCancelable(false);
	}
	
	@Override
	protected void onPreExecute(){
        dialog.show();
	}
	
	@Override
	protected Void doInBackground(Void... arg0) {
		//progress = ProgressDialog.show(context, "Processing", "Please wait while the Image is being processed",true);
		try {
			result = TransactImage(path);
			if(result){
				message = "Image processed successfully";
			}
		} catch (UnknownHostException e) {
			message = "Error: "+e.getMessage();
		} catch (IOException e) {
			message = "Error: "+e.getMessage();
		}
		return null;
	}
	
	@Override
	protected void onProgressUpdate(Void... param){
		//Toast.makeText(context, "Sending Image...", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	protected void onPostExecute(Void param){
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
		display.subSetImage((Bitmap) BitmapFactory.decodeFile(path));
		File t = new File(path);
		t.delete();
		dialog.dismiss();
	}
	
	protected boolean TransactImage(String path) throws IOException,UnknownHostException{
		boolean send=false,receive=false;
		try{
			socket = new Socket(remoteIp,remotePort);
			String[] paths = {ePath,path};
			send = sendFiles(paths,socket);
			receive = receiveImage(socket);
		}
		catch(IOException e){
			Log.i("servercatch","Connection to "+remoteIp+":"+remotePort+" : "+e.getMessage());
		}finally{
			if(socket!=null)
				socket.close();
		}
		return send&receive;
	}
	
	protected boolean receiveImage(Socket socket) throws IOException{
		File root = new File(Environment.getExternalStorageDirectory()+ File.separator + "PhotoMorph" + File.separator);
		root.mkdirs();		
		File save = new File(root,"temp.jpg");
		FileOutputStream out = new FileOutputStream(save);
		
		Bitmap bm = BitmapFactory.decodeStream(socket.getInputStream());
		try{
			bm.compress(Bitmap.CompressFormat.JPEG, 100, out);
			out.flush();
			out.close();
		}catch(Exception e){
			message = "Error: "+e.getMessage();
		}
		return true;
	}
	
	protected boolean sendFiles(String[] path,Socket socket){
		BufferedOutputStream bos;
		DataOutputStream dos;
		File[] files;
		try{
			//initialize file list
			files = new File[path.length];
			for(int i=0;i<path.length;++i){
				files[i] = new File(path[i]);
			}
			
			bos = new BufferedOutputStream(socket.getOutputStream());
			dos = new DataOutputStream(bos);
			
			dos.writeInt(files.length);
			for(File file: files){
				long length = file.length();
				dos.writeLong(length);
				String name = file.getName();
				dos.writeUTF(name);
				
				FileInputStream fis = new FileInputStream(file);
				BufferedInputStream bis = new BufferedInputStream(fis);
				int theByte=0;
				while((theByte=bis.read())!=-1){
					bos.write(theByte);
				}
				bos.flush();
				bis.close();
				file.delete();
			}
		}catch(Exception e){
			message = "Error: "+e.getMessage();
			Log.i("imageServer","Error: "+e.getMessage());
			return false;
		}
		return true;
	}
}