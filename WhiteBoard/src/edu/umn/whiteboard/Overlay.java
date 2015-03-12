package edu.umn.whiteboard;

import android.graphics.Canvas;
import android.graphics.Paint;

public interface Overlay {
	public void drawOverlay(WhiteboardCanvas wbc, Canvas c, Paint p);
}
