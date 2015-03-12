package edu.umn.whiteboard.tools;

import java.nio.FloatBuffer;
import java.util.ArrayList;

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

public class EraserTool implements Tool, Overlay{

	float lastx;
	float lasty;
	int width = 20;
	ArrayList<Float> buffer;
	
	@Override
	public Action onTouchEvent(WhiteboardCanvas wbc, Canvas imageCanvas, MotionEvent event, Paint paint) {
		Action a = null;
		float x = event.getX();
		float y = event.getY();
		paint = new Paint();
		paint.setColor(0xffffffff);
		
		switch (event.getAction()) {
		case MotionEvent.ACTION_MOVE:
			float xdis = x-lastx;
			float ydis = y-lasty;
			float dis = (float)Math.sqrt(xdis*xdis+ydis*ydis);
			float xvector = -ydis/dis;
			float yvector = xdis/dis;
			Path path = new Path();
			path.moveTo(lastx+xvector*width, lasty+yvector*width);
			path.rLineTo(xdis, ydis);
			path.rLineTo(-xvector*2*width, -yvector*2*width);
			path.rLineTo(-xdis, -ydis);
			path.close();
			imageCanvas.drawPath(path, paint);
			drawCircle(imageCanvas, paint, x, y);
			buffer.add(x);
			buffer.add(y);
			break;	
		case MotionEvent.ACTION_DOWN:
			buffer = new ArrayList<Float>();
			buffer.add(x);
			buffer.add(y);
			drawCircle(imageCanvas, paint, x, y);
			wbc.addOverlay(this);
			break;
		case MotionEvent.ACTION_UP:
			drawCircle(imageCanvas, paint, x, y);
			wbc.removeOverlay(this);
			
			Float[] Floats = new Float[buffer.size()];
			buffer.toArray(Floats);
			float[] floats = new float[Floats.length];
			for(int i = 0; i<Floats.length; i++){
				floats[i] = Floats[i];
			}
			imageCanvas.getMatrix().mapPoints(floats);
			a = new Action(Action.tools.ERASER, floats, wbc.quarter);
			a.color = paint.getColor();
			//Eraser size scales inversely with the image zoom
			//Eraser is always the same size with respect to the actual screen pixels
			a.size = width/wbc.getScale(); 
			break;
		}
		lastx=x;
		lasty=y;
		
		return a;
	}

	private void drawCircle(Canvas imageCanvas, Paint paint, float x, float y) {
		imageCanvas.drawOval(new RectF(x - width, y - width, x + width, y + width), paint);
	}

	@Override
	public String getName() {
		return "Eraser";
	}

	@Override
	public void onClick(Activity act) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawOverlay(WhiteboardCanvas wbc, Canvas c, Paint p) {
		int color = p.getColor();
		p.setColor(0xffeeeeee);
		c.drawCircle(lastx, lasty, width, p);
		p.setColor(color);
		
	}

}
