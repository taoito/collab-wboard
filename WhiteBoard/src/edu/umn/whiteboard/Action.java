package edu.umn.whiteboard;

import java.io.Serializable;

public class Action implements Serializable{
	private static final long serialVersionUID = 1L;
	public enum tools {
		PENCIL, LINE, ERASER, RECTANGLE, CIRCLE, TEXT
	}

	public tools tool;
	public int quarter;
	public float[] coordinates;
	public int color;
	public float size;
	public String text;
	public long time;
	public String name; //Xiaofei
	public int version; //Xiaofei
	public Action(tools t) {
		tool = t;
		time = System.currentTimeMillis();
	}
	public Action(tools t, float[] points, int qtr) {
		tool = t;
		time = System.currentTimeMillis();
		coordinates = points;
		quarter = qtr;
	}
	@Override
	public String toString() {
		String s = tool+" Color = "+color;
		for(int i=0; i<coordinates.length && i<4; i++) {
			s+=" "+coordinates[i];
		}
		if(coordinates.length>4) {
			s+=" ...";
		}
		if(tool==tools.PENCIL || tool==tools.ERASER || tool==tools.LINE || tool==tools.TEXT) {
			s+=" size = "+size;
		}
		if(tool==tools.TEXT) {
			s+=" text = "+text;
		}
		return s;
		
	}
}
