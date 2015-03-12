package edu.umn.whiteboard.tools;

import edu.umn.whiteboard.Action;
import edu.umn.whiteboard.Overlay;
import edu.umn.whiteboard.WhiteboardCanvas;
import edu.umn.whiteboard.Action.tools;
import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.MotionEvent;

public class LineTool implements Tool, Overlay{
	
	float startx, starty, endx, endy;
	float width = 2;
	
	@Override
	public Action onTouchEvent(WhiteboardCanvas wbc, Canvas imageCanvas, MotionEvent event, Paint paint) {
		Action a = null;
		switch(event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			startx = endx = event.getX();
			starty  = endy = event.getY();
			
			wbc.addOverlay(this);
			break;
		case MotionEvent.ACTION_MOVE:
			endx = event.getX();
			endy = event.getY();
			wbc.postInvalidate();
			break;
		case MotionEvent.ACTION_UP:
			endx = event.getX();
			endy = event.getY();
			wbc.removeOverlay(this);
			drawOverlay(wbc, imageCanvas, paint);
			float[] points = new float[]{startx, starty, endx, endy};
			imageCanvas.getMatrix().mapPoints(points);
			a = new Action(Action.tools.PENCIL, points, wbc.quarter);
			a.color = paint.getColor();
			a.size = width;
			break;
		}
		return a;
	}

	@Override
	public String getName() {
		return "Line";
	}

	@Override
	public void drawOverlay(WhiteboardCanvas wbc, Canvas c, Paint p) {
		float scaledWidth = width*wbc.getScale();
		c.drawOval(new RectF(startx - scaledWidth, starty - scaledWidth, startx + scaledWidth, starty + scaledWidth), p);
		c.drawOval(new RectF(endx - scaledWidth, endy - scaledWidth, endx + scaledWidth, endy + scaledWidth), p);
		float xdis = endx-startx;
		float ydis = endy-starty;
		float dis = (float)Math.sqrt(xdis*xdis+ydis*ydis);
		float xvector = -ydis/dis;
		float yvector = xdis/dis;
		Path path = new Path();
		path.moveTo(startx+xvector*scaledWidth, starty+yvector*scaledWidth);
		path.rLineTo(xdis, ydis);
		path.rLineTo(-xvector*2*scaledWidth, -yvector*2*scaledWidth);
		path.rLineTo(-xdis, -ydis);
		path.close();
		c.drawPath(path, p);
	}

	@Override
	public void onClick(Activity act) {
		// TODO Auto-generated method stub
		
	}

}
