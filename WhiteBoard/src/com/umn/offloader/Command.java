package com.umn.offloader;

import java.io.Serializable;


public class Command implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8324860847127752935L;
	
	String commandToDo;
	String classPath;
	String component;
	int ClientID;
	Object Payload;
	int workmode;
	Object extraData;
	
	public Command(String commandName, String ClassPath, String component, Object payload, int clientID,int mode, Object extra)
	{
		commandToDo = commandName;
		classPath = ClassPath;
		this.component=component;
		Payload = payload;
		workmode=mode;
		extraData = extra;
		ClientID = clientID;
	}
	
	public String getCommandToDo(){
		return commandToDo;
	}

	public String getClassPath(){
		return classPath;
	}
	public String getComponent(){
		return component;
	}
	public int getClientID(){
		return ClientID;
	}
	
	public Object getPayload(){
		return Payload;
	}
	
	public int getWorkmode(){
		return workmode;
	}
	public Object getExtradata(){
		return extraData;
	}
}
