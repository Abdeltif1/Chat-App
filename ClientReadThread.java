package chatproject;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ClientReadThread extends Thread {
	
	private DataInputStream in     = null;
	private Socket          socket   = null;
	private ServerGui server = null;
	private String user = "";
	Map<String,Socket> clients = new HashMap<>();
	ClientReadThread(Socket socket1,ServerGui server1, String username) throws IOException{
		socket = socket1;
		server = server1;
		user = username;
		in = new DataInputStream(socket.getInputStream());
	}
	

	
	
	public void run() {
		try {
			while(true) {
				String msg = in.readUTF();
				DataOutputStream outToClient = null;
				String decode[] = null;
				if(msg.contains("disconnect")) {
					decode = msg.split("-");
					server.deleteUser(decode[1],socket);
				}if(msg.contains("=")) {
					decode = msg.split("=");
					outToClient = new DataOutputStream(server.clients.get(decode[0]).getOutputStream());
					outToClient.writeUTF(user +":"+decode[1]);
				}if(msg.contains("ClientTyping")) {
					decode = msg.split("-");
					outToClient = new DataOutputStream(server.clients.get(decode[1]).getOutputStream());
					outToClient.writeUTF("ClientTyping-" +decode[2]);
				}
				
			}
		} catch (IOException e) {
			
		}
		
	}
	
	
	
	
	
	

}
