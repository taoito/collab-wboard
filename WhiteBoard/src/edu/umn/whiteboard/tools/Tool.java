package edu.umn.whiteboard.tools;

import edu.umn.whiteboard.Action;
import edu.umn.whiteboard.WhiteboardCanvas;
import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;

public interface Tool {
	public Action onTouchEvent(WhiteboardCanvas wbc, Canvas imageCanvas, MotionEvent event, Paint paint);
	public String getName();
	public void onClick(Activity act);
}
