package edu.umn.whiteboard.tools;

import edu.umn.whiteboard.Action;
import edu.umn.whiteboard.Overlay;
import edu.umn.whiteboard.R;
import edu.umn.whiteboard.WhiteboardCanvas;
import edu.umn.whiteboard.Action.tools;
import edu.umn.whiteboard.R.id;
import edu.umn.whiteboard.R.layout;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

public class TextTool implements Tool, Overlay{
	
	public static final int TEXT_TOOL_DIALOG = 2;	
	public static int textSize = 30;
	
	@Override
	public void drawOverlay(WhiteboardCanvas wbc, Canvas c, Paint p) {
		c.drawText(text, x, y, p);
	}

	
	@Override
	public void onClick(Activity act) {
		act.showDialog(TEXT_TOOL_DIALOG);
	}
	
	float x;
	float y;
	
	@Override
	public Action onTouchEvent(WhiteboardCanvas wbc, Canvas imageCanvas, MotionEvent event, Paint paint) {
		Action a = null;
		x=event.getX();
		y=event.getY();
		paint.setTextSize(textSize*wbc.getScale());
		switch(event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			wbc.addOverlay(this);
			break;
		case MotionEvent.ACTION_UP:
			wbc.removeOverlay(this);
			drawOverlay(wbc, imageCanvas, paint);
			float[] points = new float[]{x, y, 0, 0};
			imageCanvas.getMatrix().mapPoints(points);
			a = new Action(Action.tools.TEXT, points, wbc.quarter);
			a.color = paint.getColor();
			a.size = textSize;
			a.text = text;
			break;
		}
		wbc.postInvalidate();
		return a;
	}

	@Override
	public String getName() {
		return "Text";
	}
	
	public static String text = "";
	
	public static Dialog getDialog(Activity act) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(act);
		dialog.setTitle("Input Text");
		
		View v = act.getLayoutInflater().inflate(R.layout.texttoollayout, null);
		dialog.setView(v);
		final EditText inputText = (EditText) v.findViewById(R.id.texttoolinput);
		
		dialog.setPositiveButton("OK", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				text = inputText.getText().toString();
				
			}
		});
		dialog.setNegativeButton("Cancel", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				inputText.setText(text);
			}
		});
		return dialog.create();
	}
	
	
}
