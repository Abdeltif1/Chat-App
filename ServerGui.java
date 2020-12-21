package socketChat;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;


import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JScrollPane;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import javax.swing.UIManager;
import java.awt.Color;

public class ServerGui extends JFrame {

	private JPanel contentPane;
	private JTextField port;
	private JTextField message;
	private ServerSocket server = null;
	private Socket socket = null; // the socket that will represent the client
	private DataOutputStream out     = null;
	private DataInputStream in = null;
	private String msg = "";
	private ServerGui servergui = this;
	String username = "";
	String reciever = "All users";
	private String discussion = "";
	private JComboBox users = new JComboBox();
	Map<String,Socket> clients = new HashMap<>();
	private JTextArea screen = new JTextArea();
	private JButton disco = new JButton("Shut Down");
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ServerGui frame = new ServerGui();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	
	
	
	
	
	
	
	/**
	 * Given a string, the server will send it to all the connected users using this method.
	 * @param msg
	 */
	public void serverTousers(String msg) {
		for(Socket s : clients.values()) {
			
				try {
					out = new DataOutputStream(s.getOutputStream());
					out.writeUTF("Server: " + msg );
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		
	}
		
	
	/**
	 * Given a String, this method will update the screen of the Server.
	 * @param msg
	 */
	public void UpdateScreen(String msg) {
		discussion += msg + "\n";
		screen.setText(discussion);
		
	}
		
	
	/**
	 * given a String (name) and a Socket this method will delete the user (name) from the server and all of 
	 * the other connected clients.
	 * @param name
	 * @param socket
	 * @throws IOException
	 */
	public void deleteUser(String name,Socket socket) throws IOException {
			
			for(Socket s : clients.values()) {
				if(s != socket) {
					out = new DataOutputStream(s.getOutputStream());
					out.writeUTF("deleteuser-"+ name );
					out.writeUTF("Server: the user " + name + " disconnected" );
					
				}else {
					
						out = new DataOutputStream(socket.getOutputStream());
						out.writeUTF("deleteall" );
					
				}
				
			}
			clients.remove(name);
			users.removeItem(name);
			UpdateScreen( name + " Disconnected");
			

			
		}
		
		
	
/**
 * 	given a String (name) this method will connect the user (name) from the server and all of 
	* the other connected clients.
 * @param newuser
 * @throws IOException
 */
		public void addUser(String newuser) throws IOException {
			
			for(Socket s : clients.values()) {
				if(s != socket) {
					out = new DataOutputStream(s.getOutputStream());
					out.writeUTF("newuser-"+ newuser );
					out.writeUTF("Server: the user " + newuser + " connected" );
				}else {
					for(String client : clients.keySet()) {
						out = new DataOutputStream(socket.getOutputStream());
						out.writeUTF("newuser-"+ client );
					}
				}
				
			}
			users.addItem(newuser);
			UpdateScreen(newuser + " Connected");
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * Create the frame.
	 */
	public ServerGui() {
		setBackground(UIManager.getColor("info"));
		setAlwaysOnTop(true);
		screen.setEditable(false);
		disco.setEnabled(false); //avoid bugs
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 429, 385);
		contentPane = new JPanel();
		contentPane.setBackground(Color.GRAY);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		port = new JTextField();
		port.setBounds(155, 255, 146, 26);
		contentPane.add(port);
		port.setColumns(10);
		
		JLabel lblNewLabel = new JLabel("Port");
		lblNewLabel.setBounds(312, 260, 61, 16);
		contentPane.add(lblNewLabel);
		
		JButton startserver = new JButton("Start");
		startserver.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				new Thread() { //new thread to allow GUI to operate because we have blocking calls within this ActionListener.
					public void run() {
						try {
							if(port.getText().equals("")) {
								UpdateScreen("Please enter a valid port number");
								return;
							}
							UpdateScreen("Starting Server...");
							server = new ServerSocket(Integer.parseInt(port.getText()));
							UpdateScreen("Waiting for Clients...");
							while(true) {
								socket = server.accept(); //accepting clients
								out = new DataOutputStream(socket.getOutputStream()); // getting the socket output stream
								out.writeUTF("Welcome to the Server");
								in = new DataInputStream(socket.getInputStream()); //getting input stream
								username = in.readUTF();
								clients.put(username, socket);
								addUser(username);
								new ClientReadThread(socket,servergui,username).start(); // Thread that allows us to read constantly.
							}
							
						} catch (IOException ex) {
							// TODO Auto-generated catch block
							ex.printStackTrace();
						}
							
							
						 catch (NumberFormatException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}  //create a server
					}
				}.start();
				
				
				startserver.setEnabled(false); //avoid bugs
				disco.setEnabled(true); 
				port.setEditable(false);
				
			}
		});
		startserver.setBounds(171, 292, 117, 29);
		contentPane.add(startserver);
		users.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				reciever = (String) e.getItem(); //getting the selected reciever by the user
			}
		});
		
		
		users.setModel(new DefaultComboBoxModel(new String[] {"All users"}));
		users.setBounds(26, 256, 117, 27);
		contentPane.add(users);
		
		JLabel lblNewLabel_1 = new JLabel("Users");
		lblNewLabel_1.setBounds(57, 292, 61, 16);
		contentPane.add(lblNewLabel_1);
		
		message = new JTextField();
		message.setBounds(57, 187, 231, 26);
		contentPane.add(message);
		message.setColumns(10);
		
		JButton btnNewButton = new JButton("Send");
		btnNewButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					if(reciever.equals("All users")) { //checking if the server wants to send the message to all the users
						serverTousers(message.getText());
						message.setText("");
						return;
					}
					
					
					out = new DataOutputStream(clients.get(reciever).getOutputStream()); //getting the outputstream of the selected user
					out.writeUTF("Server:" + message.getText());
					UpdateScreen("Server to " + reciever + ": " + message.getText());
					message.setText("");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			}
		});
		btnNewButton.setBounds(300, 187, 99, 29);
		contentPane.add(btnNewButton);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(0, 0, 429, 175);
		contentPane.add(scrollPane);
		scrollPane.setViewportView(screen);
		
		
		disco.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				
				try {
					serverTousers("exit"); //disconnecting all the users once the server shuts down
					server.close();
					System.exit(0);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				
				
			}
		});
		disco.setBounds(171, 324, 117, 29);
		contentPane.add(disco);
	}

}
