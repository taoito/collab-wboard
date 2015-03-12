package edu.umn.whiteboard;

import java.awt.Color;


import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;

import java.net.*;
import javax.imageio.ImageIO;

import com.umn.offloader.Command;
import com.umn.offloader.OffLoaderInterface;

public class WhiteboardServer implements OffLoaderInterface {

	//!CHANGED
	//public final String DATA_FOLDER = "data/edu.umn.whiteboard/";
	public final String DATA_FOLDER = "data/whiteboard/";
	//public Object result;
	public WhiteboardPayload result;
	public final String VersionLocation = DATA_FOLDER+"VersionNumbers.obj";
	public HashMap<String, Integer> versionNumbers;
	public int latestVersionNumber; 
	public ConcurrentLinkedQueue<Action> queue = new ConcurrentLinkedQueue<Action>();
	public String defaultImageName;
	
	private ServerSocket serverSocket;
	private int port = 19002;
	
	public WhiteboardServer(){
        try {
        	serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("Could not listen on port: "+port);
            System.exit(1);
        }				
	}

	private void handleConnection(WhiteboardServer server){
		// The server do a loop here to accept all connection initiated by the
		// client application.
		while (true) {
			try {
				Socket socket = serverSocket.accept();
				new ConnectionHandler(server, socket);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}		
	}

	/* setVersion: assign version value to specified whiteboard canvas
	 * Input:
	 * 	- name: name of whiteboard canvas
	 * 	- version: new version number to be set
	 * */	
	private void setVersion(String name, int version){
		
		if (versionNumbers == null)
			versionNumbers = new HashMap<String, Integer>();
		
		versionNumbers.put(name, version);
		updateVersions(versionNumbers);				
	}
	
	
	/* getVersion: returns the latest version of the specified whiteboard canvas
	 * Input: name of whiteboard canvas
	 * Return: version number
	 * */
	private int getVersion(String name){

		ObjectInputStream versionStream;
		int version = 1;
		
		try {
			/* read version file */
			versionStream = new ObjectInputStream(new FileInputStream(new File(VersionLocation)));
			versionNumbers = (HashMap<String, Integer>) versionStream.readObject();
			versionStream.close();
			
			/* get latest version */
			if (!versionNumbers.containsKey(name)){ 
			}
			else 
				version = versionNumbers.get(name);
		
			/* create new file if one doesn't exist */
		} catch (FileNotFoundException err) {
			versionNumbers = new HashMap<String, Integer>();
			versionNumbers.put(name, 1);
			updateVersions(versionNumbers);
			
		} catch (Exception err) {
			err.printStackTrace();

			versionNumbers = new HashMap<String, Integer>();
			versionNumbers.put(name, 1);
			updateVersions(versionNumbers);			
		}		
		return version;
	}
	
	/* updateVersions: write out new version list to disk */
	private void updateVersions(HashMap<String,Integer> versions){
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(new File(VersionLocation)));
			oos.writeObject(versionNumbers);
			oos.close();
		} catch (Exception e1) {
			System.err.println("Error saving properties file");
			e1.printStackTrace();
		}		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public WhiteboardPayload send(Command e) {
		//WhiteboardPayload result = null;
		WhiteboardPayload result = new WhiteboardPayload();

		String cmd = e.getCommandToDo();
		if(cmd.equals("testconnection")){
			return result;
		}
		
		WhiteboardPayload payload = WhiteboardPayload.deserialize((byte[]) e.getPayload());
		
		try{
			
			if (cmd.equals("create")) {
				newImage(payload);
				result = payload;
			} 		
			else if (cmd.equals("action")) {
				 latestVersionNumber = getVersion(payload.imageName);
				if (payload.actions.length != 0) {
/*xiaofei*/			for(Action a : payload.actions){
						a.version = latestVersionNumber+1;
						queue.add(a);
					}				
					performActions(payload);
				}
				//WhiteboardPayload p = new WhiteboardPayload();
				result.imageName = payload.imageName;
				result.version = getVersion(payload.imageName);
				return result;
			}
			else if (cmd.equals("getimage")) {
				File f = new File(DATA_FOLDER + payload.imageName + ".jpg");
				FileInputStream fis = new FileInputStream(f);
				byte[] b = new byte[(int) f.length()];
				fis.read(b);
				fis.close();
				//WhiteboardPayload p = new WhiteboardPayload();
				result.imageName = payload.imageName;
				result.image = b;
				
				result.version = getVersion(payload.imageName);
				System.out.println("Sent to user " + payload.imageName + ".jpg");
				return result;
			}
/*Xiaofei*/	else if (cmd.equals("getaction")){
				ConcurrentLinkedQueue<Action> resultqueue = new ConcurrentLinkedQueue<Action>();
				Action[] actions = new Action[queue.size()];
				if(queue.size() !=0){
					System.out.println(queue.size());
					queue.toArray(actions);
				}
				for(Action a : actions){
					System.out.println("Name " + a.name + " Version " + a.version + "payload.name " + payload.imageName + "Payload.version" + payload.version);
					if(a.name.equals(payload.imageName) && a.version >= payload.version){
						resultqueue.add(a);
					}
				}
				System.out.println("resultqueue.size = " + resultqueue.size());
				Action[] resultactions = new Action[resultqueue.size()];
				if(resultqueue.size() !=0){
					
					resultqueue.toArray(resultactions);
				}
				for(int i=0; i<resultqueue.size(); i++){
					resultqueue.remove();
				}
				result.actions = resultactions;
				result.imageName = payload.imageName;
				result.version = getVersion(payload.imageName);
			}	
		} catch (IOException err) {
			err.printStackTrace();
		}
		
		return result;
	}

	@Override
	public Object receive() {
		// TODO Auto-generated method stub
		return result;
	}

	public void newImage(WhiteboardPayload p) throws IOException {
		checkDataFolder();
		File f = new File(DATA_FOLDER + p.imageName + ".jpg");
		defaultImageName = p.imageName;
		FileOutputStream fos = new FileOutputStream(f);
		fos.write(p.image);
		fos.close();
		File f1 = new File(DATA_FOLDER + p.imageName + "1.jpg");
		FileOutputStream fos1 = new FileOutputStream(f1);
		fos1.write(p.image);
		fos1.close();
		File f2 = new File(DATA_FOLDER + p.imageName + "2.jpg");
		FileOutputStream fos2 = new FileOutputStream(f2);
		fos2.write(p.image);
		fos2.close();
		File f3 = new File(DATA_FOLDER + p.imageName + "3.jpg");
		FileOutputStream fos3 = new FileOutputStream(f3);
		fos3.write(p.image);
		fos3.close();
		File f4 = new File(DATA_FOLDER + p.imageName + "4.jpg");
		FileOutputStream fos4 = new FileOutputStream(f4);
		fos4.write(p.image);
		fos4.close();
		System.out.println("Received new project, saved " + p.imageName + ".jpg");
		setVersion(p.imageName, p.version);
	}

	public void checkDataFolder() {
		File f = new File(DATA_FOLDER);
		if (!f.exists() || !f.isDirectory()) {
			f.mkdirs();
		}
	}

	public void performActions(WhiteboardPayload p) throws IOException {
		if (p == null) {
			return;
		}
		Image i = loadImage(p);
		Graphics g = i.getGraphics();
		for (Action a : p.actions) {
			switch (a.tool) {
			case PENCIL:
			case ERASER:
			case LINE:
				drawLine(g, a);
				break;
			case RECTANGLE:
				drawRect(g, a);
				break;
			case CIRCLE:
				drawCircle(g, a);
				break;
			case TEXT:
				drawText(g, a);
				break;
			}
		}
		int version = getVersion(p.imageName) + p.actions.length;
		//versionNumbers.put(p.imageName, version);
		setVersion(p.imageName, version);
		saveImage(i, p.imageName);
		generatePreview();
		//BufferedImage image = ImageIO.read(new File(DATA_FOLDER + "cloud.jpg"));
		//System.out.println("Read!");

	}
	
	public void generatePreview() throws IOException{
		int rows = 2;   
        int cols = 2;
        int chunks = rows * cols;

        int chunkWidth, chunkHeight;
        int type;
        //fetching image files
        File[] imgFiles = new File[chunks];
        for (int i = 0; i < chunks; i++) {
        	int j = i + 1;
        	System.out.println(defaultImageName);
            imgFiles[i] = new File(DATA_FOLDER + defaultImageName + j + ".jpg");
        }

       //creating a buffered image array from image files
        BufferedImage[] buffImages = new BufferedImage[chunks];
        for (int i = 0; i < chunks; i++) {
        	buffImages[i] = ImageIO.read(imgFiles[i]);
        }
        type = buffImages[0].getType();
        chunkWidth = buffImages[0].getWidth();
        chunkHeight = buffImages[0].getHeight();

        //Initializing the final image
        BufferedImage finalImg = new BufferedImage(chunkWidth*cols, chunkHeight*rows, type);

        int num = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                finalImg.createGraphics().drawImage(buffImages[num], chunkWidth * j, chunkHeight * i, null);
                num++;
            }
        }
        System.out.println("Image concatenated.....");
        
        BufferedImage scaledImg = resize(finalImg, chunkWidth, chunkHeight);
        
        ImageIO.write(scaledImg, "jpeg", new File(DATA_FOLDER + defaultImageName + ".jpg"));
	}
	
	public BufferedImage resize(BufferedImage img, int newW, int newH) {
		int w = img.getWidth();
		int h = img.getHeight();
		BufferedImage dimg = new BufferedImage(newW, newH, img.getType());
		Graphics2D g = dimg.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(img, 0, 0, newW, newH, 0, 0, w, h, null);
		g.dispose();
		return dimg;
	}

	public Image loadImage(WhiteboardPayload p) throws IOException {
		File f = new File(DATA_FOLDER + p.imageName + ".jpg");
		BufferedImage i = ImageIO.read(f);
		return i;
	}
	
	public void saveImage(Image i, String name) throws IOException{
		File f = new File(DATA_FOLDER + name + ".jpg");
		ImageIO.write((RenderedImage) i, "jpg", f);
		
	}

	public void drawCircle(Graphics g, Action a) {
		g.setColor(new Color(a.color));
		float xdis = a.coordinates[0] - a.coordinates[2];
		float ydis = a.coordinates[1] - a.coordinates[3];
		float dis = (float) Math.sqrt(xdis * xdis + ydis * ydis);
		g.fillOval((int) (a.coordinates[0] - dis), (int) (a.coordinates[1] - dis), (int) (2 * dis), (int) (2 * dis));
	}

	public void drawRect(Graphics g, Action a) {
		g.setColor(new Color(a.color));
		int left, top, right, bottom;
		if (a.coordinates[0] < a.coordinates[2]) {
			left = (int) a.coordinates[0];
			right = (int) a.coordinates[2];
		} else {
			left = (int) a.coordinates[2];
			right = (int) a.coordinates[0];
		}
		if (a.coordinates[1] < a.coordinates[3]) {
			top = (int) a.coordinates[1];
			bottom = (int) a.coordinates[3];
		} else {
			top = (int) a.coordinates[3];
			bottom = (int) a.coordinates[1];
		}
		int width = right - left;
		int height = bottom - top;
		g.fillRect(left, top, width, height);
	}

	public void drawText(Graphics g, Action a) {
		g.setFont(new Font("arial", 0, (int)a.size));
		int length = getStringLength(g, a.text);
		g.setColor(new Color(a.color));
		g.drawString(a.text, (int) (a.coordinates[0] - length / 2), (int) (a.coordinates[1]));
	}

	public int getStringLength(Graphics g, String s) {
		return (int) g.getFontMetrics().getStringBounds(s, g).getMaxX();
	}

	public void drawLine(Graphics g, Action a) {
		g.setColor(new Color(a.color));
		
		for (int i = 0; i < a.coordinates.length - 2; i += 2) {
			g.fillOval((int) (a.coordinates[i] - a.size), (int) (a.coordinates[i + 1] - a.size), 2 * (int) a.size, 2 * (int) a.size);
			float xdis = a.coordinates[i + 2] - a.coordinates[i];
			float ydis = a.coordinates[i + 3] - a.coordinates[i + 1];
			float dis = (float) Math.sqrt(xdis * xdis + ydis * ydis);
			float xvector = -ydis / dis;
			float yvector = xdis / dis;

			int[] xPoints = new int[] { (int) (a.coordinates[i] + xvector * a.size), (int) (a.coordinates[i + 2] + xvector * a.size),
					(int) (a.coordinates[i + 2] - xvector * a.size), (int) (a.coordinates[i] - xvector * a.size) };

			int[] yPoints = new int[] { (int) (a.coordinates[i + 1] + yvector * a.size), (int) (a.coordinates[i + 3] + yvector * a.size),
					(int) (a.coordinates[i + 3] - yvector * a.size), (int) (a.coordinates[i + 1] - yvector * a.size) };
			g.fillPolygon(xPoints, yPoints, 4);
		}
		g.fillOval((int) (a.coordinates[a.coordinates.length - 2] - a.size), (int) (a.coordinates[a.coordinates.length - 1] - a.size), 2 * (int) a.size,
				2 * (int) a.size);
	}

	@Override
	public int getClassID() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getClassName() {
		return "WhiteboardServer";
	}

	//@author anna
	public static void main(String[] args) throws IOException {
        System.out.println("WhiteboardServer is started...");
		WhiteboardServer server = new WhiteboardServer();		
		server.handleConnection(server);     
	}
}
