package edu.umn.whiteboard.tools;

import edu.umn.whiteboard.Action;
import edu.umn.whiteboard.WhiteboardCanvas;
import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

class GestureListener extends GestureDetector.SimpleOnGestureListener {
	 @Override
	    public boolean onDown(MotionEvent e) {
	        return false;
	    }
	    // event when double tap occurs
	    @Override
	    public boolean onDoubleTap(MotionEvent e) {

	        return true;
	    }

}

public class PanTool implements Tool {

	float lastx, lasty;
	float originalDistance;
	float centerx, centery;

	boolean zooming = false;
	GestureDetector mgest = new GestureDetector(new GestureListener());

	@Override
	public Action onTouchEvent(WhiteboardCanvas wbc, Canvas imageCanvas, MotionEvent event, Paint paint) {
		Action a = null;
		
		int qtr;
		if (mgest.onTouchEvent(event) ){
			if(zooming) {
				Log.d("Double Tap with zoom=true", "Setting back preview");
				wbc.updatePreview();
				wbc.setQWhiteBoard(0);
				zooming = false;
			}
			else {
				//int xdim = 480;
		    	//int ydim = 620;
		        float x = event.getX();
		        float y = event.getY();
		        Log.d("Double Tap", "Tapped at: (" + x + "," + y + ")");
		        
		        qtr = wbc.detectQtr(x,y);
		        wbc.setQWhiteBoard(qtr);
		        
		        zooming = true;
			}
		}
		
		
		Log.v("Pan", "Action = "+maskAction(event)+", Index = "+maskIndex(event));
		float x = event.getX();
		float y = event.getY();
		/*if (zooming) {
			zoom(wbc, imageCanvas, event, paint);
			
		} else {
			switch (maskAction(event)) {
			case MotionEvent.ACTION_POINTER_DOWN:
				zooming = true;
				Log.v("Zoom", "Starting Zoom");
				float x2 = event.getX(1);
				float y2 = event.getY(1);
				centerx = (x2+x)/2;
				centery = (y2+y)/2;
				float xdis = x-x2;
				float ydis = y-y2;
				originalDistance = (float)Math.sqrt(xdis*xdis+ydis*ydis);
				break;
			case MotionEvent.ACTION_MOVE:
				//wbc.shiftCanvas(lastx - x, lasty - y);
				//wbc.postInvalidate();
				break;
			}
			lastx = x;
			lasty = y;
		}*/
		
		return a;
	}

	public void zoom(WhiteboardCanvas wbc, Canvas imageCanvas, MotionEvent event, Paint paint) {
		if (maskAction(event) == MotionEvent.ACTION_POINTER_UP) {
			zooming = false;
			Log.v("Zoom", "Stopping Zoom");
			if(maskIndex(event)==0) { //Change lastx and lasty if the primary finger was let up
				lastx=event.getX(1);		//Stops pan from jumping when zoom fingers released in the
				lasty=event.getY(1);		//wrong order
			}
			return;
		}

		float x = event.getX();
		float y = event.getY();
		float x2 = event.getX(1);
		float y2 = event.getY(1);
		float xdis = x-x2;
		float ydis = y-y2;
		float dis = (float)Math.sqrt(xdis*xdis+ydis*ydis);
		float scale = dis/originalDistance;
		wbc.scaleCanvas(scale, centerx, centery);
		originalDistance = dis;
		lastx = x;
		lasty = y;
	}

	public int maskAction(MotionEvent e) {
		return e.getAction()&MotionEvent.ACTION_MASK;
	}
	public int maskIndex(MotionEvent e) {
		return (e.getAction()&MotionEvent.ACTION_POINTER_ID_MASK)>>MotionEvent.ACTION_POINTER_ID_SHIFT;
	}
	
	@Override
	public String getName() {
		return "Zoom";
	}

	@Override
	public void onClick(Activity act) {

	}

}
