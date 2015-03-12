package edu.umn.whiteboard;

import com.umn.offloader.*;
import java.net.*;
import java.io.*;


public class ConnectionHandler implements Runnable{
	private Socket socket;
	private WhiteboardServer server;
	
	public ConnectionHandler(WhiteboardServer server, Socket socket) {
		this.server = server;
		this.socket = socket;
		Thread t = new Thread(this);
		t.start();
	}

	public void run() {
		try{
			WhiteboardPayload payload;
			// Read a message sent by client application
			ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
			Object message = ois.readObject();
			assert(message instanceof Command); 
						
			// Send a response information to the client application
			payload = server.send((Command)message);
			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
			oos.writeObject(payload);
			ois.close();
			oos.close();
			socket.close();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
