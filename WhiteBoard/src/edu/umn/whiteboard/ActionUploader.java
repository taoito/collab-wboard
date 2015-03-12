package edu.umn.whiteboard;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.graphics.Matrix;
import android.util.Log;

import com.umn.offloader.Command;

public class ActionUploader extends Thread {
	ConcurrentLinkedQueue<Action> queue;
	Object lock;
	boolean stillRunning;
	WhiteboardCanvas wbc;
	
	public ActionUploader(ConcurrentLinkedQueue<Action> queue, WhiteboardCanvas wbc) {
		this.queue = queue;
		this.wbc = wbc;
		lock = new Object();
		stillRunning = false;
	}

	public void stopUploading() {
		stillRunning = false;
		this.interrupt();
	}

	public void signal() {
		synchronized (lock) {
			lock.notify();
		}
	}

	public void run() {
		stillRunning = true;
		while (stillRunning) {
			try {
				synchronized (lock) {
					lock.wait();
				}

				Action[] actions = new Action[queue.size()];
				if(queue.size()!=0) {
					queue.toArray(actions);
				}
				Log.v("upload", "Uploading " + queue.size() + " Actions");
				Drawing.setStatus("Uploading " + queue.size() + " Actions");
				
//				Socket sock = ServerSettings.getServerSocket();
				String servAddr = wbc.draw.getServerIP();
				Socket sock = new Socket();
				sock.connect(new InetSocketAddress(servAddr,19002),1000);
				
				long start = System.currentTimeMillis();
				
				WhiteboardPayload payload = new WhiteboardPayload();
/*xiaofei*/		for(int i =0 ; i< actions.length; i++){
					actions[i].name = wbc.imageName;
					actions[i].version = wbc.version;
				}
				payload.actions = actions;
				payload.imageName = wbc.imageName;
				payload.version = wbc.version;

				Command c = new Command("action", ServerSettings.classPath, null, WhiteboardPayload.serialize(payload), 0, 0,null);

				ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
				
				
//				oos = ServerSettings.oos;
//				ois = ServerSettings.ois;
				oos.writeObject(c);
				oos.flush();
				ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
				WhiteboardPayload result = (WhiteboardPayload) ois.readObject();
				
				long end = System.currentTimeMillis();
				Log.v("upload","processing time:"+(end-start));
				Log.v("upload", "Uploading Actions Complete");
				Drawing.setStatus("Uploading Actions Complete");
				Drawing.setStatus("web "+ wbc.version + " result " + result.version);

				if (wbc.version < result.version) {
					// Save and restore the display and drawing matrices
		/*			Matrix mat = wbc.bitmapMatrix;
					Matrix canvasMat = wbc.imageCanvas.getMatrix();
					wbc.downloadImage(wbc.imageName);
					wbc.bitmapMatrix = mat;
					wbc.imageCanvas.setMatrix(canvasMat);*/
/*Xiaofei*/					wbc.downloadAction(wbc.imageName, wbc.version);

				}
				Log.w("save","save the image "+wbc.imageName);
		//		wbc.saveImage(wbc.imageName);

				for (int i = 0; i < actions.length; i++) {
					queue.remove();
				}

//				sock.close();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.v("Upload", "Uploading Actions unsucessful");
				Drawing.setStatus("Uploading Actions unsucessful");
				e.printStackTrace();
			}
		}

	}
}
