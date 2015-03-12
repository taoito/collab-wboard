package edu.umn.whiteboard;

import java.io.IOException;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import com.umn.offloader.Command;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
/**
 * Activity to change the server settings such as address and port number
 * @author Mark Zingler <zing0021@umn.edu>
 *
 */
public class ServerSettings extends Activity implements OnClickListener {
	private EditText addressField;
	private EditText portField;
	private String serverAddress = null; 
	private int serverPort = 19002;
	
	private static String proxyAddress="146.57.249.98";
	private static int proxyPort   = 19002;
	
	public static Socket socket2server = null;
	public static ObjectInputStream ois=null;
	public static ObjectOutputStream oos=null;

	public static final String classPath = "WhiteboardServer.jar edu.umn.whiteboard.WhiteboardServer";
	//public static final String msg2proxy = "WhiteboardServer.jar";
	public static final String msg2proxy = "WhiteboardServer.jar";
	private static final boolean USE_PROXY = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.serversettings);
		addressField = (EditText) findViewById(R.id.address);
		addressField.setText(proxyAddress);

		portField = (EditText) findViewById(R.id.port);
		portField.setText("" + proxyPort);

		Button ok = (Button) findViewById(R.id.settingsOk);
		ok.setOnClickListener(this);

		Button cancel = (Button) findViewById(R.id.settingsCancel);
		cancel.setOnClickListener(this);

		/*test = (Button) findViewById(R.id.testConnection);
		test.setOnClickListener(this);*/

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.settingsOk:
			//serverAddress = addressField.getText().toString();
			//serverPort = Integer.parseInt(portField.getText().toString());
			serverAddress = "146.57.249.98";
			serverPort = 19002;
			
			try {								
				socket2server = new Socket(serverAddress,serverPort);

				// Send a message to server
				oos = new ObjectOutputStream(socket2server.getOutputStream());
				Command c = new Command("testconnection", ServerSettings.classPath, null, null, 0, 0,null);
				oos.writeObject(c);
				oos.flush();
								
				// Get a reply from server
				ois = new ObjectInputStream(socket2server.getInputStream());
				Object payload = ois.readObject();

				if(payload == null){
					Log.w("ServerSetting", "failed to get server address");
					return;
				}
				else{
					SharedPreferences settings = getSharedPreferences(Drawing.PREFS_FILE,0);
					SharedPreferences.Editor editor = settings.edit();
					editor.putString("serverIP", serverAddress);
					editor.putInt("serverPort", serverPort);
					editor.commit();
				}
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			finally{
				try {
					oos.close();
					ois.close();
					socket2server.close();
				}catch(IOException ex){}
			}
			
			this.finish();
			break;
		case R.id.settingsCancel:
			this.finish();
			break;
		}
	}
	
	
	/*
	public SharedPreferences getpre(){
        SharedPreferences share=null;
		try {
        	Context mOtherContex = this.createPackageContext("edu.umn.serverhost", Context.CONTEXT_IGNORE_SECURITY);
        	share= mOtherContex.getSharedPreferences("serverIP",Context.MODE_WORLD_READABLE+Context.MODE_WORLD_WRITEABLE);
        	return share;
		} catch (NameNotFoundException e) {
			Log.w("getHost",e.toString());
		}
		return null;
	}	
	*/

	
	public static Socket getServerSocket() throws Exception {
			if(USE_PROXY)	{
				Socket s = new Socket(proxyAddress, proxyPort);
				ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
				oos.writeObject("umn.edu.whiteboard.WhiteboardServer");
			
				ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
				String result = (String)ois.readObject();
				String[] split = result.split(":");
				Socket serverSocket = new Socket(split[0], Integer.parseInt(split[1]));
				return serverSocket;
			} 
			else{
				if(socket2server == null){
					System.err.println("ServerSettings.getServerSocket: connection with the server was not established");
					//TODO add instructions alert
				}
				return socket2server;
			}
	}
	
	/* potential code clean up improvements, so far caused problems with socket read/write
	public int sendToServer (Command c, String action, String fileName){
		try{
			socket2server = new Socket(serverAddress,serverPort);
			oos = new ObjectOutputStream(socket2server.getOutputStream());
			oos.writeObject(c);
			oos.flush();
			return 0;
		}catch(IOException ex){
			Log.v(action, action+" of "+fileName+" failed");
			Drawing.setStatus(action+" of "+fileName+" failed");
			ex.printStackTrace();
			return -1;
		}
	}
	
	public static WhiteboardPayload getFromServer(String action, String fileName){
		try{
			return (WhiteboardPayload) ois.readObject();
		}catch(IOException ex){
			Log.v(action, action+" of "+fileName+" failed");
			Drawing.setStatus(action+" of "+fileName+" failed");
			ex.printStackTrace();
			return null;
		}catch(ClassNotFoundException ex){
			Log.v(action, action+" of "+fileName+" failed");
			Drawing.setStatus(action+" of "+fileName+" failed");
			ex.printStackTrace();
			return null;
		}	
	}*/
}