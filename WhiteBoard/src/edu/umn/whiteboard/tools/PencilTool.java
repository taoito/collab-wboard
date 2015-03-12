package edu.umn.whiteboard.tools;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.apache.http.util.ByteArrayBuffer;

import edu.umn.whiteboard.Action;
import edu.umn.whiteboard.WhiteboardCanvas;
import edu.umn.whiteboard.Action.tools;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;

public class PencilTool implements Tool{
	float lastx;
	float lasty;
	int width = 3;
	ArrayList<Float> buffer;
	
	public Action onTouchEvent(WhiteboardCanvas wbc, Canvas imageCanvas, MotionEvent event, Paint paint) {
		Action a = null;
		float x = event.getX();
		float y = event.getY();
		float scaledWidth = width*wbc.getScale();
		
		switch (event.getAction()) {
		case MotionEvent.ACTION_MOVE:
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
			imageCanvas.drawPath(path, paint);
			drawCircle(imageCanvas, x, y, scaledWidth, paint);
			buffer.add(x);
			buffer.add(y);
			break;
		case MotionEvent.ACTION_DOWN:
			buffer = new ArrayList<Float>();
			buffer.add(x);
			buffer.add(y);
			drawCircle(imageCanvas, x, y, scaledWidth, paint);
			break;
		case MotionEvent.ACTION_UP:
			drawCircle(imageCanvas, x, y, scaledWidth, paint);
			
			Float[] Floats = new Float[buffer.size()];
			buffer.toArray(Floats);
			float[] floats = new float[Floats.length];
			for(int i = 0; i<Floats.length; i++){
				floats[i] = Floats[i];
			}
			imageCanvas.getMatrix().mapPoints(floats);
			a = new Action(Action.tools.PENCIL, floats, wbc.quarter);
			a.color = paint.getColor();
			a.size = width;
			break;
		}
		lastx=x;
		lasty=y;
		
		return a;
	}
	
	public void drawCircle(Canvas imageCanvas, float x, float y, float scaledWidth, Paint paint) {
		imageCanvas.drawOval(new RectF(x - scaledWidth, y - scaledWidth, x + scaledWidth, y + scaledWidth), paint);
	}
	
	public String getName(){
		return "Pencil";
	}

	@Override
	public void onClick(Activity act) {
		// TODO Auto-generated method stub
		
	}
}
