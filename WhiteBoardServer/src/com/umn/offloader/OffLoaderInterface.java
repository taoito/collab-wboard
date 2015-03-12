package com.umn.offloader;

import edu.umn.whiteboard.*;

//Implemented by the class that wants to offload something (ie, on the Android device)...?
public interface OffLoaderInterface {
	
	//One of these two descriptions seems wrong
	public WhiteboardPayload send(Command e);	// this method is how the class sends data to the outside world
	public Object receive();		// this method is how the outside world gets data from the class
	
	public int getClassID();		// this is to identify the class so that data can be sent to it
	public String getClassName();	// this is so we can retrieve the classname for the class.
	
}
