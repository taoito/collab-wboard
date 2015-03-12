package edu.umn.whiteboard;

import java.io.File;



import yuku.ambilwarna.AmbilWarnaDialog;
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import edu.umn.whiteboard.tools.CircleTool;
import edu.umn.whiteboard.tools.EraserTool;
import edu.umn.whiteboard.tools.LineTool;
import edu.umn.whiteboard.tools.PanTool;
import edu.umn.whiteboard.tools.PencilTool;
import edu.umn.whiteboard.tools.RectangleTool;
import edu.umn.whiteboard.tools.TextTool;
import edu.umn.whiteboard.tools.Tool;
import edu.umn.whiteboard.tools.Toolbar;

//testing purposes
import java.net.*;
import java.io.*;

public class Drawing extends Activity {
	public static final int COLOR_PICKER_DIALOG = 1;
	public static final String IMAGE_FOLDER = "/sdcard/Android/data/edu.umn.whiteboard/";
	public static final String PREFS_FILE = "preferences";
	public Toolbar toolbar;
	WhiteboardCanvas wbc;
	public static TextView status;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		File f = new File(IMAGE_FOLDER);
		if (!f.exists() || !f.isDirectory()) {
			f.mkdirs();
		}
		View main = getLayoutInflater().inflate(R.layout.main, null);
		ViewGroup v = (ViewGroup) main.findViewById(R.id.horizontalscroll).findViewById(R.id.toolbar);
		Tool[] tools = new Tool[] { new PanTool(), new PencilTool(), new EraserTool(), new LineTool(), new RectangleTool(), new CircleTool(), new TextTool() };
		toolbar = new Toolbar(tools, this, v);

		wbc = ((WhiteboardCanvas) main.findViewById(R.id.canvas));
		wbc.setToolbar(toolbar);
		wbc.setDrawing(this);
		status = (TextView) main.findViewById(R.id.status);
		setContentView(main);		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.upload:
			//TODO add code handling for unassigned serverIP & Port 
			wbc.setServerAddr(getServerIP(), getServerPort());
			wbc.showUploadDialog(this);
			break;
		case R.id.download:
			//TODO add code handling for unassigned serverIP & Port
			wbc.setServerAddr(getServerIP(), getServerPort());			
			wbc.showLoadDialog(this);
			break;
		case R.id.settings:
			wbc.showSettingDialog(this);
			break;
		case R.id.newscreen:
			WhiteboardCanvas w = (WhiteboardCanvas) findViewById(R.id.canvas);
			w.newWhiteboard();
			break;
		case R.id.colorpicker:
			runOnUiThread(new Runnable() {
				public void run() {
					showDialog(COLOR_PICKER_DIALOG);
				}
			});
			break;
		case R.id.serversettings:
			Intent i = new Intent(this, ServerSettings.class);
			startActivity(i);
			break;
		case R.id.refresh:
			wbc.setServerAddr(getServerIP(), 19002);
			if (wbc.actionUploader != null) {
				wbc.actionUploader.signal();
			}
		}
		return true;
	}

	@Override
	public Dialog onCreateDialog(int id) {
		Dialog result = null;
		switch (id) {
		case COLOR_PICKER_DIALOG:
			result = new AmbilWarnaDialog(this, toolbar.color, new OnAmbilWarnaListener() {

				@Override
				public void onOk(AmbilWarnaDialog dialog, int color) {
					Log.v("Color", "Color = " + color);
					toolbar.changeColor(color);
				}

				@Override
				public void onCancel(AmbilWarnaDialog dialog) {
					// TODO Auto-generated method stub
				}
			}).getDialog();
			break;
		case TextTool.TEXT_TOOL_DIALOG:
			result = TextTool.getDialog(this);
			break;
		}
		return result;
	}

	public static void setStatus(final String msg) {
		status.post(new Runnable() {
			public void run() {
				status.setText("Status: " + msg);
			}
		});
	}
	
	public String getServerIP(){
		SharedPreferences settings = getSharedPreferences(PREFS_FILE,0);
		String serverIP = settings.getString("serverIP", null);
		return serverIP;
		/*
        SharedPreferences share;
		try {
        	Context mOtherContex = this.createPackageContext("edu.umn.serverhost", Context.CONTEXT_IGNORE_SECURITY);
        	share= mOtherContex.getSharedPreferences("serverIP",Context.MODE_WORLD_READABLE+Context.MODE_WORLD_WRITEABLE);
        	return share.getString("WhiteboardServer.jar", null);
		} catch (NameNotFoundException e) {
			Log.w("getHost",e.toString());
		}
        return null;
        */
	}
	
	public int getServerPort(){
		SharedPreferences settings = getSharedPreferences(PREFS_FILE,0);
		int serverPort =  settings.getInt("serverPort", 19002);
		return serverPort;
	}
}