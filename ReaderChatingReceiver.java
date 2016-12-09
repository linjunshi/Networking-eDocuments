import java.io.*;
import java.net.*;

/**
 * @author Edward
 * Basically it's a server running to receive messages
 */
public class ReaderChatingReceiver extends Thread {
	private boolean run = true;
	private boolean read_input = false;
	private String server_name;
	private String curr_user;
	
	private int server_port;
	private int port = 30000;
	
	
	public ReaderChatingReceiver (String server_name, String curr_user, int server_port) {
		TalkToServer talk = new TalkToServer(server_name, curr_user, server_port);
		this.server_name = server_name;
		this.curr_user = curr_user;
		this.server_port = server_port;
		/** sent the head server the ip and name */
		talk.send_message_to_server("reader_server", false);
		InetAddress ip;
		Socket socket;
		try {
			ip = InetAddress.getLocalHost();
			socket = new Socket(ip, port);
			ObjectOutputStream output_obj = new ObjectOutputStream(socket.getOutputStream());
			
		} catch (IOException e) { e.printStackTrace(); }
		
	}
	
	public void run () {
		DatagramSocket server_socket = null;
		/** find available port */
		while (true) {
			try {
				server_socket = new DatagramSocket(this.port);
				break;
			} catch (SocketException e) {e.printStackTrace();port++;}
		}
		
		while(this.run){
			 DatagramPacket received_packet = new DatagramPacket(new byte[1024], 1024);
			 try {
				server_socket.receive(received_packet);
				this.print(packet_to_string(received_packet));
				
			} catch (Exception e) { e.printStackTrace();}
			
		}
		if (server_socket != null) server_socket.close();
	}
	
	

	
	
	public void request_chat(String[] strings) {
		TalkToServer talk_to_server = new TalkToServer(this.server_name, this.curr_user, this.server_port);
		String user_server_info = talk_to_server.send_message_to_server(strings[0]+'\n'+strings[1]+'\n', true);
		String[] info = user_server_info.split(" ");
		String host_name = info[0];
		int port = Integer.parseInt(info[1]);
	}
	
	
	
	
	
	
	
	private String packet_to_string (DatagramPacket received_packet) {
		byte[] buf = received_packet.getData();
		ByteArrayInputStream bais = new ByteArrayInputStream(buf);
		InputStreamReader isr = new InputStreamReader(bais);
		BufferedReader br = new BufferedReader(isr);
		String line = null;
		try {
			line = br.readLine();
		} catch (IOException e) { e.printStackTrace(); }
		return line;
	}
	
	public boolean is_reading() {
		return this.read_input;
	}
	
	
	public void disconnect_chating () {
		this.run = false;
	}
	
	public boolean is_running() {
		return this.run;
	}
	
	public void print (Object obj) {
		System.out.println(obj);
	}
}
