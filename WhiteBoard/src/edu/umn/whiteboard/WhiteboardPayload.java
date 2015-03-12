package edu.umn.whiteboard;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


public class WhiteboardPayload implements Serializable{
	private static final long serialVersionUID = 645234645L;
	public String imageName;
	public Action[] actions;
	public byte[] image;
	public int version;
	
	public static byte[] serialize(WhiteboardPayload p) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			new ObjectOutputStream(baos).writeObject(p);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return baos.toByteArray();
	}
	
	public static WhiteboardPayload deserialize(byte[] b){
		ByteArrayInputStream bais = new ByteArrayInputStream(b);
		try {
			return (WhiteboardPayload) new ObjectInputStream(bais).readObject();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
