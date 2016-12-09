import java.io.*;
import java.net.*;
import java.util.*;

/**
 * @author Edward
 * the reader class uses BookPageState class and TalkToServer class, it's a client class
 */
public class Reader {
	private HashMap<String, BookPageState> books_pages;
	private BookPageState curr_book = null;
	private TalkToServer check_update;
	private ReaderChatingReceiver user_receiver;
	// private TalkToServer talk_to_server;
	private Timer t = null;
	private String user_name;
	private String server_name;
	private String mode;
	private int server_port;
	private int polling_interval;
	
	
	
	public Reader (String[] args) {
		books_pages = new HashMap<String, BookPageState>();
//		this.talk_to_server = new TalkToServer(this.curr_book, this.server_name, this.user_name,
//				this.server_port, this.polling_interval);
		this.mode = args[0];
		this.polling_interval = Integer.parseInt(args[1])*1000;
		this.user_name = args[2];
		this.server_name = args[3];
		this.server_port = Integer.parseInt(args[4]);
		/** you have to inform the ReaderChatingReceiver about the server port number, 
		 * other wise it doesn't know where to contact the server */
		this.user_receiver = new ReaderChatingReceiver(this.server_name, this.user_name, this.server_port);
		user_receiver.run();
	}
	
	
	
	/**
	 * thee main function deals with finding available ports, build socket, and call the scan function
	 * and the timer task : "check update" 
	 * @throws Exception
	 */
	public static void main (String[] args) {
		// java Reader mode polling_interval user_name server_name server_port_number
		Reader r = new Reader(args);
		
		if(r.mode.equals("push")) {
			r.polling_interval = 1000;
//			TalkToServer send = new TalkToServer(r.curr_book, r.server_name, r.user_name,
//								r.server_port, r.polling_interval);
			TalkToServer talk_to_server = new TalkToServer(r.curr_book, r.server_name, r.user_name,
															r.server_port, r.polling_interval);
			talk_to_server.send_to_server("request_push_mode"+'\n', false);
			talk_to_server.send_to_server(r.user_name+'\n', false);
			
			
		}
		
		/**
		 * this while loop waits for user input and at the same time checks for update
		 */
		while(true){
			/** if the receiver is not running then turn it on */
			if (r.user_receiver.is_running() != true)
				r.user_receiver.run();
			BufferedReader user_input = null;
			String s = null;
			try {
				if (r.user_receiver.is_reading()) continue;
				user_input = new BufferedReader(new InputStreamReader(System.in));
				s = user_input.readLine();
			} catch (IOException e) { e.printStackTrace(); }
			r.scan_user_input(s, user_input);
		}
		
	}
	
	

	
	
	
	/**
	 * scan through the input and look at the commands, "display, post ..."
	 */
	private void scan_user_input (String s, BufferedReader user_input) {
		if (s != null && s.length() != 0) {
			String strings[] = s.split(" ");
			if (strings[0].equals("display")){
				dispaly(strings);
				this.t = new Timer();
				this.check_update = new TalkToServer(this.curr_book, this.server_name, this.user_name,
													this.server_port, this.polling_interval);
				t.schedule(this.check_update, this.polling_interval);
				
			} else if (strings[0].equals("post_to_forum")) {
				if (curr_book == null) {
					print("You're not reading any book currently!");
					return;
				}
				post_discussion(strings);
				
			} else if (strings[0].equals("read_post")) {
				if (curr_book == null) print("You're not reading any book currently!");
				read_post(strings);
				
			} else if (strings[0].equals("chat_request")) {
				this.user_receiver.request_chat(strings);
				
			}
		} else {
			// debug
			print("No input");
		}
	}
	
	
	
	
	
	
	private void request_chat(String[] strings) {
		TalkToServer talk_to_server = new TalkToServer(this.curr_book, this.server_name, this.user_name,
				this.server_port, this.polling_interval);
		talk_to_server.send_message_to_server(strings[0]+'\n'+strings[1]+'\n', false);
	}



	@SuppressWarnings("unchecked")
	private void read_post (String[] strings) {
		int line_num = Integer.parseInt(strings[1]);
		TalkToServer talk_to_server = new TalkToServer(this.curr_book, this.server_name, this.user_name,
													this.server_port, this.polling_interval);
		ArrayList<String> post = (ArrayList<String>)talk_to_server.send_to_server(
																"get_post_of_line" + '\n'+
																this.curr_book.get_name() + '\n'+
																line_num + '\n', true);
		print("    Book by "+this.curr_book.get_name()+", Page "+
				this.curr_book.get_page_num()+", Line number "+line_num+":");
		for (int i = 0; i < post.size(); i++) {
			if (!this.curr_book.all_posts[line_num-1].contains(post.get(i)))
				print("    "+(i+1)+" "+post.get(i));
		}
//		for (String s : post) {
//			print("    "+s);
//		}
		this.curr_book.all_posts[line_num-1] = post;
	}
	
	
	
	
	/**
	 * talk to server into adding a post in the server
	 * @param strings: the string that user type in (post_to_forum ... ...)
	 */
	private void post_discussion(String[] strings) {
		String line_num = strings[1];
		String content = strings[2];
		for (int i = 3; i < strings.length; i++)
			content = content + " " + strings[i];
		
		// debug
		//print("post content : " + content);
		
		TalkToServer talk_to_server = new TalkToServer(this.curr_book, this.server_name, this.user_name,
														this.server_port, this.polling_interval);
		talk_to_server.send_to_server("post_to_forum"+'\n'+
										this.curr_book.get_name()+'\n'+
										line_num+'\n'+
										content+'\n', false);
		this.curr_book.add_post(content, this.user_name, Integer.parseInt(line_num));
	}



	/**
	 * function when the user type in display
	 * @param strings
	 */
	private void dispaly (String strings[]) {
		Scanner file = null;
		try{
			/** 
			 * because we are in the display mode, so if wen have previous task, then we need to cancel
			 * them to avoid creating multiple tasks.
			 */
			if (this.t != null) {
				this.check_update.cancel_task();
				this.t.cancel();
				// this.t = null;
			}
			
			
			/** get the original book, set up curr_book */
			String file_name = strings[1] + "_page" + strings[2];
			file = new Scanner(new FileReader(BookPageState.dir + "/" + file_name));
			if (!books_pages.containsKey(file_name)) {
				TalkToServer talk_to_server = new TalkToServer(this.curr_book, this.server_name, this.user_name,
																this.server_port, this.polling_interval);
				BookPageState origin_book = talk_to_server.receive_original_book(file_name);
				curr_book = origin_book;
				// debug
				// print("get the origin book, curr is not null? -> " + (curr_book!=null));
				books_pages.put(file_name, curr_book);
			} else {
				curr_book = books_pages.get(curr_book.get_name());
			}
			
			
			TalkToServer update = new TalkToServer(this.curr_book, this.server_name, this.user_name,
													this.server_port, this.polling_interval);
			String[] posts_in_server = update.return_update_for_display();
			String[] posts_in_client = this.curr_book.summary().split(";");
			for (int line = 0; file.hasNextLine(); line++) {
				/** if it's equal, check if there are posts in the line, put 'n' and 'm'
				 * at the beginning of the line*/
				String is_new = " ";
				if (posts_in_client[line].equals(posts_in_server[line])) {
					if(curr_book.get_num_of_post(line) != 0)
						is_new = "m";
				} else {
					is_new = "n";
				}
				print(is_new + file.nextLine());
			}
		} catch (FileNotFoundException e) { e.printStackTrace(); }finally { if (file != null) file.close();}
		
	}
	
	
	
	
	
	public static void print (Object obj) {
		System.out.println(obj);
	}

}


/**
*1.BookPageState: modified the summary message format into every line and num of post at that line. || 2. Reader: add some if statement to the scan to ensure user read books first and then post; About to change the display function to enable the 'n' and 'm'. || 3. CheckUpdate currently works for sending and receiving data to and from server. || 4. Server: work fine in "check_update"
*/