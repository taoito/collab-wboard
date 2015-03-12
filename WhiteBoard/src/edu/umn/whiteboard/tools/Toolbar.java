package edu.umn.whiteboard.tools;

import edu.umn.whiteboard.R;
import edu.umn.whiteboard.R.style;
import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Typeface;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;

public class Toolbar {
	
	Tool[] tools;
	int currentTool;
	private Button[] buttons;
	Activity activity;
	public int color;
	public Paint paint;
	
	public Toolbar(Tool[] tools, Activity act, ViewGroup v) {
		this.tools = tools;
		currentTool = 0;
		activity = act;
		buttons = new Button[tools.length];
		changeColor(0xff000000); //Black
		paint.setTextAlign(Align.CENTER);
		paint.setTypeface(Typeface.createFromAsset(act.getAssets(), "fonts/arial.ttf"));
		//paint.setAntiAlias(true);
		
		for(int i=0; i<tools.length; i++) {
			buttons[i] = new Button(activity);
			buttons[i].setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			buttons[i].setText(tools[i].getName());
			buttons[i].setTextAppearance(activity, R.style.ToolbarButtons);
			
			final int id = i;
			final Tool t = tools[id];
			buttons[i].setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					switchTool(id);
					t.onClick(activity);
				}
			});
			v.addView(buttons[i]);
		}
		buttons[currentTool].setTextAppearance(activity, R.style.ToolbarButtons_selected);
		
	}
	
	public void changeColor(int color) {
		this.color = color;
		if(paint == null)
			paint = new Paint();
		paint.setColor(this.color);
	}
	
	public void switchTool(int toolID){
		buttons[currentTool].setTextAppearance(activity, R.style.ToolbarButtons);
		buttons[toolID].setTextAppearance(activity, R.style.ToolbarButtons_selected);
		currentTool = toolID;
	}
	
	public Tool getCurrentTool() {
		return tools[currentTool];
	}
}
