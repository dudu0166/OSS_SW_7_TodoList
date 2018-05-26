import java.io.Console;

public class Controller {
	private DBManager db;
	private Console console;
	private boolean running = false;
	
	Controller() {
		db = new DBManager();
		console = System.console();
		db.connectionDB("./testDB");
		db.createTable("todo");
		running = true;
	}
	
	
	public String inputMainCommand() {
		return console.readLine("\nChoose what to do:\n(a: Add todo, l: List todo, o: Order option"
				+ " t: Tag settings, q: Quit)? ");
	}
	
	/***** todo: rename this method as a proper one *****/
	public void others(String command) {
		if(command.matches("^l\\s-..?$")){
			if(command.length() == 4)
				db.listTodo("todo", command.charAt(3));
			else
				db.listTodo("todo", command.charAt(3),command.charAt(4));
		}
	}
	
	
	
	/* Add a record to the list */
	public void addList() {
		String what = null;

		if(db.addTodo( what = console.readLine("Todo? : "), console.readLine("Due date? (YYYY-MM-DD HH:MM:SS) : "),
				console.readLine("Priority? ")))  {
			System.out.println("=======Add Success======\n");
			setTags(what);
			
		}
		else 
			System.out.println("========Add Fail========\n");
		
		
	}
	
	/* Add tags to a record */
	private void setTags(String what) {
		String tag;
		if(console.readLine("Would you like to set any tags?(Enter only 'y' to answer yes)").equals("y")) {
			while(true) {
				tag = console.readLine("Tag name(enter 'q' to quit) : ");
				if(tag.equals("q"))	break;
				if(db.hasTag(what, tag))	
					System.out.println("This tag already exists on this entry!");
				else 
					db.addTag(what, tag);
			}
		}
	}
	
	/* Manage tags */
	public void tagSettings() {
		String ans, info = "Select what you want.\n"
				+ "1. Show all tags\n"
				+ "2. Remove a tag from current list\n"
				+ "3. Remove all tags\n"
				+ "0. Return\n"
				+ ": ";
		
		while(true) {
			ans = console.readLine(info);
			if(ans.equals("0")) break;
			switch(ans) {
			case "1":
				db.printCurrentTags();
				break;
			case "2":
				break;
			case "3":
				break;
			case "4":
				break;
			case "5":
				break;
				
			}
		}
	}
	
	public void chooseShowOption() {
		String ans, info = "Select what you want to view.\n"
				+ "1. All\n"
				+ "2. The list whose due has expired\n"
				+ "3. The list whose due has not expired\n"
				+ "4. Finished list\n"
				+ "5. Unfinished list\n"
				+ "6. Show list by tags\n"
				+ "0. Return\n"
				+ ": ";
		
		while(true) {
			ans = console.readLine(info);
			if(ans.equals("0")) break;
			switch(ans) {
			case "1":
				db.listTodo("todo");
				break;
			case "2":
				db.overdueList("todo", true);
				break;
			case "3":
				db.overdueList("todo", false);
				break;
			case "4":
				db.showListByCompletion(true);
				break;
			case "5":
				db.showListByCompletion(false);
				break;
			case "6":
				inputTag();
			}
		}

	}
	
	private void inputTag() {
		String inp, tags[];
		inp = console.readLine("Enumerate tags what you want to view.(Use space as a delimiter)\n");
		tags = inp.split(" ");
		db.showListByTags(tags);
	}
	
	public void setOrderOption() {
		System.out.println("Enumerate order option as which you want to view list.\n"
				+ "Current option: " + db.getOrderCriteria() + "\n"
		+ "(i: id, c: content(lexicographic), d: due)\n");
		String inp = console.readLine("Or type 'b' if you don't want to touch anything\n:");
		if(inp.equals("b"))
			return;
		db.setOrderCriteria(inp);
	}
	
	public boolean isRunning() {
		return running;
	}
	
	public void end() {
		running = false;
		db.close();
	}

	
}
