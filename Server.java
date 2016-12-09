import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
	private HashMap<String, BookPageState> books_pages;
	private HashMap<String, BookPageState> original_books_pages;
	private HashMap<String, String> client_servers;
	private ArrayList<String> push_list;
	
	private String curr_user;
	private int port;
	// ServerSocket welcome_socket;
	
	public Server () {
		books_pages = new HashMap<String, BookPageState>();
		original_books_pages = new HashMap<String, BookPageState>();
		client_servers = new HashMap<String, String>();
		push_list = new ArrayList();
		File[] files = new File(BookPageState.dir).listFiles();
		for (File f : files) {
			if (f.isFile()) {
				String file_name = f.getName();
				if (file_name.matches(".*_page[0-9]")) {
					books_pages.put(file_name, new BookPageState(file_name));
					original_books_pages.put(file_name, new BookPageState(file_name));
				}
			}
		}
		print("The server is listening on port number " + this.port + '\n' +
				"The database for discussion posts has been intialised");
	}
	
	
	
	public static void main(String[] args) {
		Server server = new Server();
		server.port = Integer.parseInt(args[0]);
		ServerSocket welcome_socket;
		while (true) {
			/* if the input has a port num, then we need to check if the port num is available, if not find one */
			try {
				welcome_socket = new ServerSocket(server.port);
				break;
			} catch (IOException e) { 
				e.printStackTrace();
				server.port++;
			}
		}
		while(true){
			Socket client;
			try {
				client = welcome_socket.accept();
				Scanner sc = new Scanner(client.getInputStream());
				server.curr_user = sc.nextLine();
				if(!server.push_list.contains(server.curr_user))
					print("Query from: "+server.curr_user);
				server.distinguish_input(sc, client);
				if (sc != null) sc.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	
	/**
	 * 1. 	if the request from the client is "check_update", then it will send back a summary
	 * 		of the state of the current book and page
	 * 2.   
	 * @param sc
	 * @param client
	 * @throws Exception
	 */
	private void distinguish_input (Scanner sc, Socket client) throws Exception {
		DataOutputStream output_stream = new DataOutputStream(client.getOutputStream());
		String client_message = sc.nextLine();
		
		// print(client.getInetAddress().getHostName());
		
		// debug
		// print("message from client: " + client_message);
		
		if (client_message.equals("check_update")) {
			String book = sc.nextLine();
			String summary = sc.nextLine();
			output_stream.writeBytes(books_pages.get(book).summary()+'\n');
			
			
			// debug
			// print("write to client");

		} else if (client_message.equals("post_to_forum")) {
			String book_name = sc.nextLine();
			String line_num = sc.nextLine();
			String content = sc.nextLine();
			// String user = sc.nextLine();
			int which_line = Integer.parseInt(line_num);
			BookPageState bookpage = this.books_pages.get(book_name);
			bookpage.add_post(content, this.curr_user, which_line);
			print("New post received from " + this.curr_user);
			print("Post added to the database and given serial number ("+
					bookpage.get_book_name()+", "+bookpage.get_page_num()+", "+which_line+", "+bookpage.all_posts[which_line-1].size()+").");
			if(push_list.size() == 0)
				print("Push list empty. No action required.");
			else
				print("New posts has been forworded to the users.");
			
		} else if (client_message.equals("get_post_of_line")) {
			String book_name = sc.nextLine();
			int line_num = Integer.parseInt(sc.nextLine());
			ObjectOutputStream output_obj = new ObjectOutputStream(client.getOutputStream());
			BookPageState book = this.books_pages.get(book_name);
			output_obj.writeObject(book.all_posts[line_num-1]);
			output_obj.flush();
			
		} else if (client_message.equals("get_oringinal_book")) {
			String book_name = sc.nextLine();
			// debug
			print("user request to send original book: "+book_name);
			BookPageState book = this.original_books_pages.get(book_name);
			/** reply back how many obj client have to accept */
			output_stream.writeInt(book.all_posts.length);
			
			ObjectOutputStream output_obj = new ObjectOutputStream(client.getOutputStream());
			output_obj.writeObject(book.book_contents);
			
			// debug
			//print("obj sent");
			
			
		} else if (client_message.equals("request_push_mode")) {
			push_list.add(this.curr_user);
			
		} else if (client_message.equals("reader_server")) {
			String server_info = sc.nextLine();
			this.client_servers.put(this.curr_user, server_info);
			// debug
			print("client server recorded");
			
		} else if (client_message.equals("chat_request")) {
			String target_user = sc.nextLine();
			output_stream.writeBytes(this.client_servers.get(target_user)+'\n');
		}
		
	}
	
	
	
	
	
	
	
	
	
	public static void print (Object obj) {
		System.out.println(obj);
	}
}