package edu.umn.whiteboard;

import java.io.ByteArrayOutputStream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;

import com.umn.offloader.Command;

import edu.umn.whiteboard.tools.Toolbar;

public class WhiteboardCanvas extends SurfaceView {

	int xdim = 480;
	int ydim = 620;

	Bitmap image;

	Canvas imageCanvas;
	Toolbar toolbar;
	ConcurrentLinkedQueue<Action> queue;
	String imageName;
	String defaultImageName;
	String workingImageName;
	boolean isShared;
	int version;
	ActionUploader actionUploader;
	Drawing draw;
	ArrayList<Overlay> overlays;
	Matrix bitmapMatrix;
	
	String servAddr;
	int    servPort;
	
	public int quarter;
	Bitmap q1,q2,q3,q4,prev;
	public String user;

	
	public WhiteboardCanvas(Context context, AttributeSet attrs) {
		super(context, attrs);
		image = null;
		user = "DefaultUser";
		System.out.println("Time: " + System.currentTimeMillis());
	}
	
	public void setDrawing(Drawing o){
		draw = o;
	}
	
	public void setServerAddr(String serverip, int port){
		servAddr = serverip;
		servPort = port;
	}

	public void newWhiteboard() {
		//Bitmap b = Bitmap.createBitmap(xdim, ydim, Bitmap.Config.ARGB_8888);
		//b.eraseColor(0xffffffff); // fill with white
		
		prev = Bitmap.createBitmap(xdim, ydim, Bitmap.Config.ARGB_8888);
		//Also create 4 bitmaps for different quarters
		q1 = Bitmap.createBitmap(xdim, ydim, Bitmap.Config.ARGB_8888);
		q2 = Bitmap.createBitmap(xdim, ydim, Bitmap.Config.ARGB_8888);
		q3 = Bitmap.createBitmap(xdim, ydim, Bitmap.Config.ARGB_8888);
		q4 = Bitmap.createBitmap(xdim, ydim, Bitmap.Config.ARGB_8888);
		prev.eraseColor(0xffffffff); // fill with white
		q1.eraseColor(0xffffffff);
		q2.eraseColor(0xffffffff);
		q3.eraseColor(0xffffffff);
		q4.eraseColor(0xffffffff);

		quarter = 0;
		
		newWhiteboard(prev, quarter);
		isShared = false;
	}
	
	public void newWhiteboard(Bitmap b, int q) {
		image = b;
		if(imageCanvas!=null) {
			imageCanvas.setBitmap(image);
		}else {
			imageCanvas = new Canvas(image);
		}
		if(overlays == null)
			overlays = new ArrayList<Overlay>();
		if(bitmapMatrix == null)
			bitmapMatrix = new Matrix();
		if(queue == null)
			queue = new ConcurrentLinkedQueue<Action>();
		version = 1;
		if(actionUploader!=null) {
			actionUploader.stopUploading();
		}
		if (q != 0)
			imageName = defaultImageName + q;
		
		if (q == 0) {
			for(int i=0;i<620;i++){
				prev.setPixel(240, i, 0x00000000);
			}
			for(int i=0;i<480;i++){
				prev.setPixel(i, 310, 0x00000000);
			}
		}
		Drawing.setStatus("Currently in quarter " + q);
		actionUploader = new ActionUploader(queue, this);
		postInvalidate();
	}

	
	public void setQWhiteBoard(int qtr) {
		if (qtr == 1) {
			newWhiteboard(q1, qtr);
			workingImageName = imageName;
			//if (quarter != qtr) 
			quarter = 1; downloadImage(imageName);
			//downloadAction(imageName,version);
			
		}else if(qtr == 2) {
			newWhiteboard(q2, qtr);
			//if (quarter != qtr) 
				
			quarter = 2; downloadImage(imageName);
			//downloadAction(imageName,version);
		}else if(qtr == 3) {
			newWhiteboard(q3, qtr);
			//if (quarter != qtr) 
				
			quarter = 3; downloadImage(imageName);
			//downloadAction(imageName,version);
		}else if(qtr == 4) {
			newWhiteboard(q4, qtr);
			//if (quarter != qtr) 
				
			quarter = 4; downloadImage(imageName);
			//downloadAction(imageName,version);
		}else {
			Log.d("Prev","Called newWhiteboard(prev)");
			quarter = 0;
			newWhiteboard(prev, quarter);
			downloadImage(defaultImageName);
		}
	}

	public void setToolbar(Toolbar t) {
		toolbar = t;
	}
	
	public void updatePreview() {
		
		
	}
	public void getLocalQtr() {
		
		
	}
	
	public int detectQtr(float x,float y) {
		
		if (x < (xdim/2) && y < (ydim/2)){
        	Log.d("Double Tap Q1", "Quarter 1");
        	return 1;
        }else if(x >= (xdim/2) && y < (ydim/2)){
        	Log.d("Double Tap Q2", "Quarter 2");
        	return 2;
        }else if (x < (xdim/2) && y >= (ydim/2)) {
        	Log.d("Double Tap Q3", "Quarter 3");
        	return 3;
        }else {
        	Log.d("Double Tap Q4", "Quarter 4");
        	return 4;
        }
	}

	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (image == null) {
			newWhiteboard();
		}
		Log.v("Size", getWidth() + " x " + getHeight());
	}

	@Override
	public void onDraw(Canvas canvas) {
		Log.v("Draw", "Drawing");
		canvas.drawBitmap(image, bitmapMatrix, null);
		for (Overlay o : overlays) {
			o.drawOverlay(this, canvas, toolbar.paint);
		}
	}

	public void shiftCanvas(float dx, float dy) {
		imageCanvas.translate(dx, dy);
		bitmapMatrix.postTranslate(-dx, -dy);
	}

	public void scaleCanvas(float scale, float x, float y) {
		imageCanvas.scale(1 / scale, 1 / scale, x, y);
		bitmapMatrix.postScale(scale, scale, x, y);
	}

	public float getScale() {
		return bitmapMatrix.mapRadius(1);
	}

	public void addOverlay(Overlay o) {
		overlays.add(o);
	}

	public void removeOverlay(Overlay o) {
		overlays.remove(o);
	}
	
	public void showSettingDialog(Activity act){
		AlertDialog.Builder dialog2 = new AlertDialog.Builder(act);
		dialog2.setTitle("Set user name");
		View v2 = act.getLayoutInflater().inflate(R.layout.userdialog, null);
		dialog2.setView(v2);
		final EditText inputUser = (EditText) v2.findViewById(R.id.username);
		
		
		dialog2.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				user = inputUser.getText().toString();
				Drawing.setStatus("User: " + user);	
			}
		});
		dialog2.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
			}
		});
		dialog2.create().show();
	}

	public void showUploadDialog(Activity act) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(act);
		dialog.setTitle("New Project Name");

		View v = act.getLayoutInflater().inflate(R.layout.filedialog, null);
		dialog.setView(v);
		final EditText inputText = (EditText) v.findViewById(R.id.filename);

		dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				try {
					uploadImage(inputText.getText().toString());
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		});
		dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
			}
		});
		dialog.create().show();
	}

	public void showLoadDialog(Activity act) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(act);
		dialog.setTitle("Load Project Name");
		View v = act.getLayoutInflater().inflate(R.layout.filedialog, null);
		dialog.setView(v);
		final EditText inputText = (EditText) v.findViewById(R.id.filename);
		dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				try {
					quarter = 0;
					downloadImage(inputText.getText().toString());
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});
		dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
			}
		});
		dialog.create().show();
	}

	public void uploadImage(String fileName) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		q1.compress(CompressFormat.JPEG, 50, baos);
		WhiteboardPayload payload = new WhiteboardPayload();
		//fileName = fileName + quarter;
		payload.imageName = fileName;
		payload.version = version;
		payload.image = baos.toByteArray();
		byte[] bytes = WhiteboardPayload.serialize(payload);
		Command c = new Command("create", ServerSettings.classPath, null, bytes, 0, 0,null);

		try {
			Log.v("upload", "Uploading image = "+fileName);
			Drawing.setStatus("Uploading image = "+fileName);
//			Socket s = ServerSettings.getServerSocket();
			ObjectOutputStream oos = null;//new ObjectOutputStream(s.getOutputStream());
			ObjectInputStream ois = null;//new ObjectInputStream(s.getInputStream());
			
			//set up the socket
			Socket sock = new Socket();
			//Socket sock = ServerSettings.getServerSocket();
			sock.connect(new InetSocketAddress(servAddr,servPort), 1000);			
			oos = new ObjectOutputStream(sock.getOutputStream());
			oos.writeObject(c);
			oos.flush();

			ois = new ObjectInputStream(sock.getInputStream());			
			WhiteboardPayload result = null;
			result = (WhiteboardPayload) ois.readObject();

			isShared = true;
			imageName = fileName;
			defaultImageName = fileName;
			if(!actionUploader.stillRunning) {
				actionUploader.start();
			}
			
			Log.v("upload", "Upload of "+fileName+" Complete");
			Drawing.setStatus("Upload of "+fileName+" Complete");
//			s.close();
			
		} catch (Exception e1) {
			Log.v("upload", "Upload of "+fileName+" Failed");
			Drawing.setStatus("Upload of "+fileName+" Failed");
			e1.printStackTrace();
		}
	}

	
	public void downloadImage(String fileName) {
		WhiteboardPayload payload = new WhiteboardPayload();
		payload.imageName = fileName;
		byte[] bytes = WhiteboardPayload.serialize(payload);
		Command c = new Command("getimage", ServerSettings.classPath, null, bytes, 0, 0,null);
		
		try {
			Log.v("download", "Downloading image = "+fileName);
			Drawing.setStatus("Downloading image = "+fileName);
			Socket s = new Socket();
			s.connect(new InetSocketAddress(servAddr,servPort), 1000);
			ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
			oos.writeObject(c);
			oos.flush();
			
			ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
		


			WhiteboardPayload result = null;
			result = (WhiteboardPayload) ois.readObject();
			Bitmap b = BitmapFactory.decodeByteArray(result.image, 0, result.image.length);
			Bitmap b2 = b.copy(Config.ARGB_8888, true);
			if (quarter == 0) {
				prev = b2;
				newWhiteboard(prev, quarter);
			}
			else if (quarter == 1) {
				q1 = b2;
				newWhiteboard(q1, quarter);
			}
			else if (quarter == 2) {
				q2 = b2;
				newWhiteboard(q2, quarter);
			}
			else if (quarter == 3) {
				q3 = b2;
				newWhiteboard(q3, quarter);
			}
			else if (quarter == 4) {
				q4 = b2;
				newWhiteboard(q4, quarter);
			}
			//setQWhiteBoard(quarter);
			isShared = true;
			imageName = fileName;
			if (quarter == 0) 
				defaultImageName = fileName;
			version = result.version;
			if(!actionUploader.stillRunning) {
				actionUploader.start();
			}
			Log.v("download", "Download of "+fileName+" Complete");
			Drawing.setStatus("Download of "+fileName+" Complete");
			
//			s.close();
			
		} catch (Exception e1) {
			Log.v("download", "Download of "+fileName+" Failed");
			Drawing.setStatus("Download of "+fileName+" Failed");
			e1.printStackTrace();
		}
	}
/*Xiaofei downloadAction*/	
	public void downloadAction(String fileName, int versionNumber) {
		WhiteboardPayload payload = new WhiteboardPayload();
		payload.imageName = fileName;
		payload.version = versionNumber;
		byte[] bytes = WhiteboardPayload.serialize(payload);
		Command c = new Command("getaction", ServerSettings.classPath, null, bytes, 0, 0,null);
		double debugindex = 0;
		try {
			Log.v("download", "Downloading actions = "+fileName);
			Drawing.setStatus("Downloading actions = "+fileName);
			Socket s = new Socket();
			s.connect(new InetSocketAddress(servAddr,servPort), 1000);
			ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
			oos.writeObject(c);
			oos.flush();
			
			ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
		
			/*
			oos = ServerSettings.oos;
			ois = ServerSettings.ois;
			*/

			WhiteboardPayload result = null;
			result = (WhiteboardPayload) ois.readObject();
			
			Log.v("download", "before for loop");
			Log.v("download", "actions length =" + result.actions.length);
			
			for(Action a : result.actions){
				Log.v("download", "before switch");
				switch (a.tool) {				
				case PENCIL:
				case ERASER:
				case LINE:
					drawPencil(this, imageCanvas, a, toolbar.paint);
					break;
				case RECTANGLE:
					drawRect(this, imageCanvas, a, toolbar.paint);
					break;
				case CIRCLE:
					drawCircle(this, imageCanvas, a, toolbar.paint);
					break;
				case TEXT:
					drawText(this, imageCanvas, a, toolbar.paint);
					break;
				}
			}
			if(!actionUploader.stillRunning) {
				actionUploader.start();
			}
			isShared = true;
			version = result.actions[result.actions.length-1].version;
			Log.v("download", "Download of "+ fileName+" Complete");
			Drawing.setStatus("Download of "+ fileName+" Complete");
			
//			s.close();
			
		} catch (Exception e1) {
			Log.v("download", "Download of "+fileName+" Failed");
			Drawing.setStatus("Download of "+fileName+" Failed");
			e1.printStackTrace();
		}
	}
		
		public void drawPencil(WhiteboardCanvas wbc, Canvas c, Action a,Paint p){
			float width = a.size;
			float scaledWidth = width*wbc.getScale();		
			p.setColor(a.color);
			
			float x=a.coordinates[0],y=a.coordinates[1];
			float lastx=a.coordinates[0],lasty=a.coordinates[1];
			
			c.drawOval(new RectF(x - scaledWidth, y - scaledWidth, x + scaledWidth, y + scaledWidth), p);

					
			for (int i = 2; i < a.coordinates.length; i += 2) {
				x = a.coordinates[i];
				y = a.coordinates[i+1];
				
				float xdis = x-lastx;
				float ydis = y-lasty;
				float dis = (float)Math.sqrt(xdis*xdis+ydis*ydis);
				float xvector = -ydis/dis;
				float yvector = xdis/dis;
				Path path = new Path();
				path.moveTo(lastx+xvector*scaledWidth, lasty+yvector*scaledWidth);
				path.rLineTo(xdis, ydis);
				path.rLineTo(-xvector*2*scaledWidth, -yvector*2*scaledWidth);
				path.rLineTo(-xdis, -ydis);
				path.close();
				c.drawPath(path, p);
				c.drawOval(new RectF(x - scaledWidth, y - scaledWidth, x + scaledWidth, y + scaledWidth), p);
				lastx = x;
				lasty = y;
			}
			
			x=a.coordinates[a.coordinates.length-2];
			y=a.coordinates[a.coordinates.length-1];
			c.drawOval(new RectF(x - scaledWidth, y - scaledWidth, x + scaledWidth, y + scaledWidth), p);

		}
	
		public void drawRect(WhiteboardCanvas wbc, Canvas imageCanvas, Action a,Paint paint){
			paint.setColor(a.color);
			float left = a.coordinates[0];
			float top = a.coordinates[1];
			float right = a.coordinates[2];
			float bottom = a.coordinates[3];
			
			float t;
			if(left>right){
				t=left;
				left=right;
				right=t;
			}
			if(top>bottom) {
				t=top;
				top=bottom;
				bottom=t;
			}
			imageCanvas.drawRect(new RectF(left, top, right, bottom), paint);
		}
		public void drawCircle(WhiteboardCanvas wbc, Canvas imageCanvas, Action a,Paint paint){
			paint.setColor(a.color);
			float centerx = a.coordinates[0];
			float centery = a.coordinates[1];
			float radiusx = a.coordinates[2];
			float radiusy = a.coordinates[3];
			
			float xdis = centerx-radiusx;
			float ydis = centery-radiusy;
			float distance = (float)Math.sqrt(xdis*xdis+ydis*ydis);
			imageCanvas.drawOval(new RectF(centerx-distance, centery-distance, centerx+distance, centery+distance), paint);
		}
		public void drawText(WhiteboardCanvas wbc, Canvas imageCanvas, Action a,Paint paint){
			paint.setColor(a.color);
			paint.setTextSize((int) a.size);	
			String text = a.text;
			
			float x = a.coordinates[0];
			float y = a.coordinates[1];
			
			imageCanvas.drawText(text, x, y, paint);	
		}	
/*Xiaofei end*/		
	
	float lastx;
	float lasty;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();
		Log.v("Touch", "Touch event at " + x + " " + y + " : id=" + event.getAction());

		Action a = toolbar.getCurrentTool().onTouchEvent(this, imageCanvas, event, toolbar.paint);
		if (a != null) {
			queue.add(a);
			version+=1;
			actionUploader.signal();
			Log.v("Action", a.toString());
		}
		invalidate();
		return true;
	}
	public boolean saveImage(String filename){
		File f= new File("sdcard/"+filename+".jpg");
		try {
			OutputStream fout = new FileOutputStream(f);
			image.compress(Bitmap.CompressFormat.JPEG, 80, fout);
			fout.flush();
			fout.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
}
