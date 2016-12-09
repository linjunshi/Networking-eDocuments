import java.io.*;
import java.net.*;
import java.util.*;

/**
 * @author Edward
 * a class that contains a task for checking update, a timer task is used while waiting user input
 * The socket is used in this class, it is responsible for sending data between client and server
 */
public class TalkToServer extends TimerTask {
	private InetAddress server_ip;
	private Socket client_socket;
	// private ServerSocket listen_socket;
	// private TalkToServer talk_to_server;
	
	private DataOutputStream output;
	private DataInputStream input;
	// private ObjectInputStream input_obj;
	private BufferedReader input_reader;
	private BookPageState curr_book;
	
	private String server_name;
	private String curr_user;
	private int polling_interval;
	private int server_port;
	private static int which = 0;
	private boolean continue_run = true;
	
	
	public TalkToServer(String server_name, String curr_user, int port) {
		this.curr_user = curr_user;
		this.server_name = server_name;
		this.server_port = port;
	}
	
	
	public TalkToServer (BookPageState curr_book, String server_name, String curr_user,
						int server_port_number, int polling_interval) {
		
		this.curr_book = curr_book;
		this.server_name = server_name;
		this.curr_user = curr_user;
		this.server_port = server_port_number;
		this.polling_interval = polling_interval;
		// this.run = true;
//		try {
//			this.output = new DataOutputStream(client_socket.getOutputStream());
//			this.input = new DataInputStream(client_socket.getInputStream());
//			this.input_reader = new BufferedReader(new InputStreamReader(client_socket.getInputStream()));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}
	
	
	public TalkToServer (BookPageState curr_book, String server_name, 
			String curr_user, int server_port_number) {
		
		this.curr_book = curr_book;
		this.server_name = server_name;
		this.server_port = server_port_number;
		this.curr_user = curr_user;
	}
	
	
	
	
	
	


	/**
	 * background process to check update automatically
	 * use: curr_book, input_reader, polling_interval
	 */
	@Override
	public void run() {
		// debug
		print("inside run: "+(which++)+"th");
		while (continue_run) {
			try {
				this.create_new_socket ();
				output.writeBytes("check_update" + '\n' + curr_book.get_name() + '\n' + curr_book.summary() + '\n');
				String summary_from_server = input_reader.readLine();
				if (!curr_book.summary().equals(summary_from_server)) {
					print("There are new posts.");
					break;
				}
				this.close_socket_and_stream();
				Thread.sleep(this.polling_interval);
			} catch (Exception e) {
				//e.printStackTrace();
				return;
			}
		}
	}
	
	
	/**
	 * set the while condition to be false so that we can stop the while loop
	 */
	public void cancel_task () {
		this.continue_run = false;
	}
	
	
	
	/**
	 * create new socket and new streams for this obj, after using this method, 
	 * close_socket_and_stream() should be called.
	 * Every time this method got used, it send the server its name
	 * use: server_name, server_port, curr_user, 
	 */
	private void create_new_socket () {
		/* if the input has a port num, then we need to check if the port num is available, if not find one */
		try {
			String name = "localhost";
			this.server_ip = InetAddress.getByName(name);
			this.client_socket = new Socket(this.server_ip, this.server_port);
			// debug
			print("create a new socket");
			
			this.output = new DataOutputStream(client_socket.getOutputStream());
			this.input = new DataInputStream(client_socket.getInputStream());
			this.input_reader = new BufferedReader(new InputStreamReader(client_socket.getInputStream()));
			// print("tell user our name");
			this.output.writeBytes(this.curr_user+'\n');
		} catch (UnknownHostException e1) { e1.printStackTrace();
		} catch (IOException e) { e.printStackTrace(); }
	}
	
	
	/**
	 * close socket and all streams
	 * before using this method, create_new_socket() should be used
	 */
	private void close_socket_and_stream () {
		try {
			this.client_socket.close();
			this.output.close();
			this.input.close();
			// this.input_obj.close();
			this.input_reader.close();
		} catch (IOException e) { e.printStackTrace(); }
	}
	
	
	
	public String[] return_update_for_display () {
		this.create_new_socket();
		String[] summary_text = null;
		try {
			output.writeBytes("check_update" + '\n' + curr_book.get_name() + '\n' + curr_book.summary() + '\n');
			String summary_from_server = input_reader.readLine();
			summary_text = summary_from_server.split(";");
			
		} catch (IOException e) { e.printStackTrace(); }
		this.close_socket_and_stream();
		return summary_text;
	}
	
	
	
	/**
	 * what this function does is that it you can use it to send command to server,
	 * and it returns back a message from the server
	 * @param s: what you want to send to server
	 * @return: a message, or an Object from the server
	 */
	public Object send_to_server (String s, boolean need_reply) {
		this.create_new_socket();
		Object obj_from_sever = null;
		try {
			output.writeBytes(s);
//			ObjectOutputStream output_obj = new ObjectOutputStream(client_socket.getOutputStream());
//			output_obj.flush();
			if(need_reply) {
				ObjectInputStream input_obj = new ObjectInputStream(client_socket.getInputStream());
				obj_from_sever = input_obj.readObject();
			}
		} catch (IOException | ClassNotFoundException e) { e.printStackTrace(); }
		this.close_socket_and_stream();
		return obj_from_sever;
	}
	
	
	
	
	/**
	 * purely send message to srever, without any extra info
	 * @param s
	 */
	public String send_message_to_server (String s, boolean need_reply) {
		this.create_new_socket();
		String user_server_info = null;
		try {
			output.writeBytes(s);
			if (need_reply)
				user_server_info = this.input_reader.readLine();
		} catch (IOException e) { e.printStackTrace(); }
		this.close_socket_and_stream();
		return user_server_info;
	}
	
	
	
	
	/**
	 * get the original book from server, which means that without any comment, just the book
	 * @param book_name
	 * @return a book object
	 */
	@SuppressWarnings("unchecked")
	public BookPageState receive_original_book (String book_name) {
		BookPageState book = null;
		this.create_new_socket();
		
		try {
			this.output.writeBytes("get_oringinal_book"+'\n'+book_name+'\n');
			int how_many_lines = this.input.readInt();
			//int how_many_lines = Integer.parseInt(this.input_reader.readLine());
			// debug
			// print("num received is = "+how_many_lines);
			book = new BookPageState(book_name, how_many_lines);
			ObjectInputStream input_obj = new ObjectInputStream(this.client_socket.getInputStream());
			book.book_contents = (ArrayList<String>) input_obj.readObject();
			
		} catch (IOException | ClassNotFoundException e) { e.printStackTrace(); }
		
		this.close_socket_and_stream();
		
		return book;
	}
	
	
	public static void print (Object obj) {
		System.out.println(obj);
	}
}