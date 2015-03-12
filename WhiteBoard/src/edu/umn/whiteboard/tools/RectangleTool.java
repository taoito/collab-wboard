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

public class RectangleTool implements Tool, Overlay{
	
	float left;
	float right;
	float top;
	float bottom;
	
	@Override
	public Action onTouchEvent(WhiteboardCanvas wbc, Canvas imageCanvas, MotionEvent event, Paint paint) {
		Action a = null;
		switch(event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			left = right = event.getX();
			top  = bottom = event.getY();
			
			wbc.addOverlay(this);
			break;
		case MotionEvent.ACTION_MOVE:
			right = event.getX();
			bottom = event.getY();
			wbc.postInvalidate();
			break;
		case MotionEvent.ACTION_UP:
			right = event.getX();
			bottom = event.getY();
			wbc.removeOverlay(this);
			drawRect(imageCanvas, paint);
			float[] points = new float[]{left, top, right, bottom};
			imageCanvas.getMatrix().mapPoints(points);
			a = new Action(Action.tools.RECTANGLE, points, wbc.quarter);
			a.color = paint.getColor();
			break;
		}
		return a;
	}

	@Override
	public String getName() {
		return "Rectangle";
	}

	@Override
	public void drawOverlay(WhiteboardCanvas wbc, Canvas c, Paint p) {
		c.drawRect(new RectF(left, top, right, bottom), p);
	}
	
	public void drawRect(Canvas c, Paint p) {
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
		c.drawRect(new RectF(left, top, right, bottom), p);
	}

	@Override
	public void onClick(Activity act) {
		// TODO Auto-generated method stub
		
	}

}
