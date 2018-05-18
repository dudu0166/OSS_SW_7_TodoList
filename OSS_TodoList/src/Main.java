import java.io.Console;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		DBManager db = new DBManager();
		Console console = System.console();
		db.connectionDB("./testDB");
		db.createTable("todo");

		while (true) { 
			switch (console.readLine("\nChoose what to do:\n(a: Add todo, l: List todo, m: Modify todo,"
					+ " t: Tag settings, q: Quit)? ")) {
			case "a":
				addList(console, db);
				break;
			case "l":
				chooseShowOption(console, db);
				break;
			case "m":
				break;
			case "t":
				tagSettings(console, db);
				break;
			case "q":
				db.close();
				System.exit(0);
				break;
			default:
				break;
			}
		}
	}
	
	/* Add a record to the list then return 'what' field */
	private static void addList(Console console, DBManager db) {
		String what = null;

		if(db.addTodo( what = console.readLine("Todo? : "), console.readLine("Due date? (YYYY-MM-DD HH:MM:SS) : ")))  {
			System.out.println("=======Add Success======\n");
			setTags(console, db, what);
		}
		else 
			System.out.println("========Add Fail========\n");
		
		
	}
	
	
	/* Add tags to a record */
	private static void setTags(Console console, DBManager db, String what) {
		String tag;
		if(console.readLine("Would you like to set any tags?(Enter only 'y' to answer yes)").equals("y")) {
			while(true) {
				tag = console.readLine("Tag name(enter 'q' to quit) : ");
				if(tag.equals("q"))	break;
				db.addTag(what, tag);
			}
		}
	}
	
	/* Manage tags */
	private static void tagSettings(Console console, DBManager db) {
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
	
	private static void chooseShowOption(Console console, DBManager db) {
		String ans, info = "Select what you want to view.\n"
				+ "1. All\n"
				+ "2. The list whose due has expired\n"
				+ "3. The list whose due has not expired\n"
				+ "4. Finished list\n"
				+ "5. Unfinished list\n"
				+ "0. Return\n"
				+ ": ";
		
		while(true) {
			ans = console.readLine(info);
			if(ans.equals("0")) break;
			switch(ans) {
			case "1":
				db.listTod("todo");
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
				
			}
		}

	}

}
