package chatproject;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import socketChat.Client;

import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.JComboBox;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.DefaultComboBoxModel;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.JInternalFrame;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.Window.Type;

public class ChatClient extends JFrame {

	private JPanel contentPane;
	private JTextField message;
	private JTextField state;
	private JTextField port;
	private JTextField address;
	private JTextField username;
	private Socket socket = null;
	private DataInputStream clientin = null;
	private DataOutputStream clientout = null;
	private String msg = "";
	private String user = "";
	private JTextArea screen = new JTextArea();
	private JButton disconnect;
	private ChatClient chat = null;
	private Client client = null;
	private ClientReadThread cl = null;
	private JComboBox users;
	private String reciever = user;
	private String discussion = "";
	private JLabel lblNewLabel_2;
	private JLabel lblNewLabel_3;
	private JLabel lblNewLabel_4;
	JButton send = new JButton("Send");
	private List<Integer> keys = new ArrayList<Integer>();
	
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ChatClient frame = new ChatClient();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	
	
	/**
	 * Given a String, this method will update the client's screen with the given message.
	 * @param msg
	 */
	public void UpdateScreen(String msg) {
		discussion += msg + "\n";
		screen.setText(discussion);
		
	}
	
	
	
	/**
	 * given a String, this method will handle that message and decode it to see which action to perform.
	 * @param msg
	 * @throws IOException
	 * @throws UnsupportedAudioFileException 
	 * @throws LineUnavailableException 
	 */
	public void Handle(String msg) throws IOException, LineUnavailableException, UnsupportedAudioFileException {
		String decode[] = null;
		if(msg.contains("newuser")) {
			decode = msg.split("-");
			users.addItem(decode[1]);
		}else if(msg.contains("deleteuser")) {
			decode = msg.split("-");
			users.removeItem(decode[1]);
		}else if(msg.contains("deleteall")){
			users.removeAllItems();
		}else if(msg.contains("exit")){
			socket.close();
			System.exit(0);
		}else if(msg.contains("ClientTyping")) {
			decode = msg.split("-");
			screen.setText(discussion + decode[1] + " is typing...");
			wait(150);
			
		}
		
		
		else {
			UpdateScreen(msg);
		}
		
	}
	
	public void wait( int time) {

		new Thread() {
			public void run() {
				try {
					TimeUnit.MILLISECONDS.sleep(time);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.run();
		screen.setText(discussion);
	}
	
	
	
	
	
	static void playSound(String soundFile) throws LineUnavailableException, MalformedURLException, UnsupportedAudioFileException, IOException {
	    File f = new File(soundFile);
	    AudioInputStream audioIn = AudioSystem.getAudioInputStream(f.toURI().toURL());  
	    Clip clip = AudioSystem.getClip();
	    clip.open(audioIn);
	    clip.start();
	}
	
	
	public static void makeSound(String str){
	    File sound = new File(str);
	    

	    try{
	        Clip clip = AudioSystem.getClip();
	        clip.open(AudioSystem.getAudioInputStream(sound));
	        clip.start();
	    } catch (Exception e){
	        e.printStackTrace();
	    }
	}
	
	

	/**
	 * Create the frame.
	 */
	public ChatClient() {
		try {
		    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if ("Nimbus".equals(info.getName())) {
		            UIManager.setLookAndFeel(info.getClassName());
		            break;
		        }
		    }
		} catch (UnsupportedLookAndFeelException e) {
		    // handle exception
		} catch (ClassNotFoundException e) {
		    // handle exception
		} catch (InstantiationException e) {
		    // handle exception
		} catch (IllegalAccessException e) {
		    // handle exception
		}

		
		
		setResizable(false);
		screen.setEditable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 612, 434);
		contentPane = new JPanel();
		contentPane.setBackground(UIManager.getColor("scrollbar"));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JButton connect = new JButton("Connect");
		connect.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					
					if(!connect.isEnabled()) {
						return;
					}
					if(username.getText().equals("") || address.getText().equals("") || port.getText().equals("")) { //avoiding bug by checking if the given input if valid (fail fast)
						screen.setText("Please enter a valid username,\n address and port number");
						return;
					}
					socket = new Socket(address.getText(), Integer.parseInt(port.getText())); // creating the socket
					clientout    = new DataOutputStream(socket.getOutputStream()); // getting the output stream for the socket
					new Thread() { //new thread to constantly keep reading input messages without interrupting the GUI or other functions
						public void run() {
							try {
								clientin = new DataInputStream(socket.getInputStream()); 
								while(true) {
									String msg = clientin.readUTF();
									Handle(msg); //decoding the recieved message
								}
							} catch (IOException e) {
								
							} catch (LineUnavailableException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (UnsupportedAudioFileException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
						}
					}.start();
					user = username.getText(); //sending the server the given username.
					clientout.writeUTF(user);
					state.setText("Connected");
					//avoiding bug:
					state.setEditable(false);
					disconnect.setEnabled(true);
					port.setEditable(false);
					address.setEditable(false);
					username.setEditable(false);
					connect.setEnabled(false);
					
				} catch (NumberFormatException | IOException  e1) {
					// TODO Auto-generated catch block
					if(e1.getMessage().equals("Connection refused")) {
						screen.setText("Connection refused because the server \n is not On yet");
						return;
						
					}
				}
				
				
				
				
			}
		});
		connect.setBounds(396, 17, 117, 29);
		contentPane.add(connect);
		
		JLabel lblNewLabel = new JLabel("Port");
		lblNewLabel.setBounds(532, 99, 61, 16);
		contentPane.add(lblNewLabel);
		
		JLabel lblAddress = new JLabel("Address");
		lblAddress.setBounds(532, 159, 61, 16);
		contentPane.add(lblAddress);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(0, 0, 384, 280);
		contentPane.add(scrollPane);
		scrollPane.setViewportView(screen);
		
		message = new JTextField();
		message.addKeyListener(new KeyAdapter() {
			
			//sending message by simple pressing Enter
			@Override
			public void keyPressed(KeyEvent e) {
				if(!send.isEnabled()) {
					return;
				}
				
				
				if(e.getKeyCode() != KeyEvent.VK_ENTER) {
					try {
						clientout.writeUTF("ClientTyping-" +reciever+ "-" +username.getText());
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					};
				}
				
				
				
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					try {
						if(message.getText().equals("")) { //not allowing empty messages
							return;
						}
						
						
						clientout.writeUTF(reciever + "=" + message.getText()); //formatting the message
						UpdateScreen("You to " + reciever + ": " + message.getText()); //updating the screen
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} 
					message.setText("");
				
					
				}
			}
		});
		message.setBounds(116, 306, 188, 26);
		contentPane.add(message);
		message.setColumns(10);
		
		
		send.addMouseListener(new MouseAdapter() {
			//sending a message by simply clicking on the Send button
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					if(!send.isEnabled()) {
						return;
					}
					if(message.getText().equals("")) { //not allowing empty messages
						return;
					}
					
					
					clientout.writeUTF(reciever + "=" + message.getText());//formatting the message
					UpdateScreen("You to " + reciever + ": " + message.getText());
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} 
				message.setText("");
			}
		});
		send.setBounds(316, 306, 117, 29);
		contentPane.add(send);
		
		state = new JTextField();
		state.setForeground(UIManager.getColor("desktop"));
		state.setBounds(476, 306, 130, 26);
		contentPane.add(state);
		state.setColumns(10);
		
		JLabel lblNewLabel_1 = new JLabel("Status");
		lblNewLabel_1.setBounds(507, 287, 61, 16);
		contentPane.add(lblNewLabel_1);
		
		port = new JTextField();
		port.setBounds(396, 94, 130, 26);
		contentPane.add(port);
		port.setColumns(10);
		
		address = new JTextField();
		address.setBounds(396, 154, 130, 26);
		contentPane.add(address);
		address.setColumns(10);
		
		username = new JTextField();
		username.setBounds(396, 220, 130, 26);
		contentPane.add(username);
		username.setColumns(10);
		
		disconnect = new JButton("Disconnect");
		disconnect.setEnabled(false);
		disconnect.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					if(!disconnect.isEnabled()) {
						return;
					}
					clientout.writeUTF("disconnect-" + user); //sending the server a message to disconnect the client
					socket.close(); //closing the socket
					System.exit(0); //closing the GUI.
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			}
		});
		disconnect.setBounds(396, 53, 117, 29);
		contentPane.add(disconnect);
		
		users = new JComboBox();
		users.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				reciever = (String) e.getItem(); //getting the selected receiver.
			}
		});
		
		users.setBounds(0, 307, 101, 27);
		contentPane.add(users);
		
		lblNewLabel_2 = new JLabel("UserName");
		lblNewLabel_2.setBounds(532, 225, 74, 16);
		contentPane.add(lblNewLabel_2);
		
		lblNewLabel_3 = new JLabel("Online Users");
		lblNewLabel_3.setBounds(10, 346, 80, 16);
		contentPane.add(lblNewLabel_3);
		
		lblNewLabel_4 = new JLabel("message");
		lblNewLabel_4.setBounds(185, 346, 61, 16);
		contentPane.add(lblNewLabel_4);
	}
}
