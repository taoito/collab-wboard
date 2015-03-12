package edu.umn.whiteboard.tools;

import edu.umn.whiteboard.Action;
import edu.umn.whiteboard.Overlay;
import edu.umn.whiteboard.WhiteboardCanvas;
import edu.umn.whiteboard.Action.tools;
import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;

public class CircleTool implements Tool, Overlay{
	
	Canvas overlay;
	float centerx, centery, radiusx, radiusy;
	
	@Override
	public Action onTouchEvent(WhiteboardCanvas wbc, Canvas imageCanvas, MotionEvent event, Paint paint) {
		Action a = null;
		switch(event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			centerx = radiusx = event.getX();
			centery  = radiusy = event.getY();
			
			wbc.addOverlay(this);
			break;
		case MotionEvent.ACTION_MOVE:
			radiusx = event.getX();
			radiusy = event.getY();
			wbc.postInvalidate();
			break;
		case MotionEvent.ACTION_UP:
			radiusx = event.getX();
			radiusy = event.getY();
			wbc.removeOverlay(this);
			drawOverlay(wbc, imageCanvas, paint);
			float[] points = new float[]{centerx, centery, radiusx, radiusy};
			imageCanvas.getMatrix().mapPoints(points);
			a = new Action(Action.tools.CIRCLE, points, wbc.quarter);
			a.color = paint.getColor();
			break;
		}
		return a;
	}

	@Override
	public String getName() {
		return "Circle";
	}

	@Override
	public void drawOverlay(WhiteboardCanvas wbc, Canvas c, Paint p) {
		float xdis = centerx-radiusx;
		float ydis = centery-radiusy;
		float distance = (float)Math.sqrt(xdis*xdis+ydis*ydis);
		c.drawOval(new RectF(centerx-distance, centery-distance, centerx+distance, centery+distance), p);
	}

	@Override
	public void onClick(Activity act) {
		// TODO Auto-generated method stub
		
	}


}
