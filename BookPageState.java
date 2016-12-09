import java.io.*;
import java.net.*;
import java.util.*;

public class BookPageState {
	public ArrayList<String>[] all_posts;  // an array of array, posts for every line
	public ArrayList<String> book_contents;
	public static final String dir = "./eBook-pages";
	private String whole_name; 			// includes page
	private String book_name;			// not include page
	private int page_num;
	
	
	
	/**
	 * first constructor for server to open the book and then reads in all the content of the book
	 * because the server has files available for it
	 * @param name
	 */
	public BookPageState (String name) {
		this.whole_name = name;
		String[] s = this.whole_name.split("_page");
		/** devide the file name into book_name and page_number */
		this.book_name = s[0];
		this.page_num = Integer.parseInt(s[1]);
		
		book_contents = new ArrayList<String>();
		// don't know if the missing of this one will be bad
		//all_posts = new ArrayList[book_contents.size()];
		
		Scanner file = null;
		try{
			file = new Scanner(new FileReader(BookPageState.dir + "/" +name));
			while (file.hasNextLine())
				book_contents.add(file.nextLine());
		} catch (FileNotFoundException e) { e.printStackTrace(); } finally { if (file != null) file.close(); }
		
		all_posts = new ArrayList[book_contents.size()];
		for (int i = 0; i < book_contents.size(); i++) {
			all_posts[i] = new ArrayList<String>();
		}
	}
	
	
	/**
	 * the second constructor is for readers, readers don't have files, so they need to
	 * get the content somewhere else, so in this constructor we don't open files and read
	 * all the contents
	 * @param name: name of the book
	 * @param how_many_lines: how many lines does this book have
	 */
	public BookPageState (String name, int how_many_lines) {
		this.whole_name = name;
		String[] s = this.whole_name.split("_page");
		this.book_name = s[0];
		this.page_num = Integer.parseInt(s[1]);
		book_contents = new ArrayList<String>();
		all_posts = new ArrayList[how_many_lines];
		for (int i = 0; i < how_many_lines; i++) {
			all_posts[i] = new ArrayList<String>();
		}
	}
	
	/**
	 * @return a string summary of this state -> "LINE_NUM  NUM_OF_POST  "
	 */
	public String summary () {
		StringBuffer summary_message = new StringBuffer();
		for (int line = 0; line < book_contents.size(); line++) {
			summary_message.append(line + " " + all_posts[line].size());
			if(line != book_contents.size()-1) summary_message.append(";");
		}
		// debug
		//print(summary_message.toString());
		return summary_message.toString();
	}
	
	
	/**
	 * @param line_num: input the line number specified and return the num of post of that line
	 * @return
	 */
	public int get_num_of_post (int line_num) {
		return this.all_posts[line_num].size();
	}
	
	public String get_name () {
		return this.whole_name;
	}
	
	public String get_book_name () {
		return this.book_name;
	}
	
	public int get_page_num () {
		return this.page_num;
	}
	
	public void add_post (String post, String user_name, int line_num) {
		this.all_posts[line_num-1].add(user_name+": "+post);
	}
	
	public void update_post_of_line (ArrayList<String> post, int line_num) {
		this.all_posts[line_num-1] = post;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public static void print (Object obj) {
		System.out.println(obj);
	}
}