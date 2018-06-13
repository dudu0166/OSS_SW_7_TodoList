import java.io.Console;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.regex.Pattern;
import com.diogonunes.jcdp.color.ColoredPrinter;
import com.diogonunes.jcdp.color.api.Ansi.Attribute;
import com.diogonunes.jcdp.color.api.Ansi.BColor;
import com.diogonunes.jcdp.color.api.Ansi.FColor;

public class View {
	private Controller controller;
	private Console console;
	private ResultSet rs;
	
	View() {
		controller = new Controller();
		console = System.console();
	}
	
	// Initial message or screen which user will meet first. 
	public void initialScreen() {
		System.out.println("Welcome!");
	}
	
	// Input 3 contents for adding, then pass the valid values to controller
	public void inputForAdd() {
		int done = 0; // Number of finished input
		String what = "", due = "", priority = "";
		System.out.println("<<Type 'c' for cancel or 'u' for undo>>");
		while(done < 3) {
			if(done == 0) {
				what = inputCommand("What to do: ");
				if(what.equals("c"))
					break;
				if(what.equals("u")) {
					done = 0;
					continue;
				}
				if(controller.isRepeat(what)) {
					System.out.println("It already exists!");
					continue;
				}
				++done;
			}
			if(done == 1) {
				due = inputCommand("Due date(YYYY-MM-DD HH:MM:SS): ");
				if(due.equals("c"))
					break;
				if(due.equals("u")) {
					done = 0;
					continue;
				}
				if(!isValidDate(due)) {
					System.out.println("Invalid date format.");
					continue;
				}
				++done;
			}
			if(done == 2) {
				priority = inputCommand("Priority(Please input only positive integer):");
				if(priority.equals("c"))
					break;
				if(priority.equals("u")) {
					done = 1;
					continue;
				}
				if(isPositiveInteger(priority)) {
					++done;
				}
				else {
					System.out.println("Invalid priority format.");
				}
			}
			
		}
		
		if(done == 3) {
			if(inputCommand("To do: " + what + "\nDue: " + due + "\npriority: " + priority + "\n"
					+ "Is it correct? Enter 'rewrite' to rewrite, or any other key to confirm addition: ").equals("rewrite"))
				done = 0;
			controller.addList(what, due, priority);
			inputTags(what);
		} else {
			System.out.println("Canceled.");
		}
	}
	
	
	private void inputTags(String what) {
		String tag;
		if(inputCommand("Would you like to set any tags?(Enter only 'y' to answer yes) ").equals("y")) {
			while(true) {
				tag = inputCommand("Tag name(enter 'q' to quit) : ");
				if(tag.equals("q"))	break;
				
				if(controller.setTag(what, tag))	
					System.out.println("Successfully added");
				else
					System.out.println("This tag already exists on this entry!");
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
			ans = inputCommand(info);
			if(ans.equals("0")) break;
			switch(ans) {
			case "1":
				rs = controller.listAll();
				break;
			case "2":
				rs = controller.listByTimeExcess(true);
				break;
			case "3":
				rs = controller.listByTimeExcess(false);
				break;
			case "4":
				rs = controller.listByCompletion(true);
				break;
			case "5":
				rs = controller.listByCompletion(false);
				break;
			case "6":
				inputTagsInList();
			}
			try {
				printCurrentRecords();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	// input id -> rewrite a content -> call modify method in controller(Checking validity)
	public void inputToModify() {
		boolean choosing = true;
		
		while(choosing) {
			String info = "Please input content No. that you want to modify. (Show list: l Back: b)";
			String id = inputCommand(info);
			if(id.equals("l")) {
				chooseShowOption();
				continue;
			}
			if(id.equals("b")) {
				return;
			}
			//check the validity of id
			if(isPositiveInteger(id) && controller.checkValidID(id)) {
				try {
					ResultSet rs = controller.listById(id); rs.next();
					String isFinished = rs.getString("finished").equals("1") ? "Finished" : "Unfinished";
					System.out.println("\n<<Current state>>\n" 
							+ "TODO : " + rs.getString("what") + ", "
							+ "DUE : " + rs.getString("due") + ", " + isFinished + ", "
							+ "Priority : " + rs.getString("priority") + "\n");
					//input new info of selected content
					System.out.println("Input new information for this content or input '-' for no change.\n");
					String newTODO, newDue, completeness, newPriority;

					newTODO = inputCommand("New TODO: ");
					while(!isValidDate(newDue = inputCommand("(YYYY-MM-DD HH:MM:SS)New Due: "))) {
						if(newDue.equals("-"))
							break;
						System.out.println("Invalid due form.");
					}
					
					while(!(completeness = inputCommand("(y or n)Is it finished? ")).equals("y") && !completeness.equals("n")) {
						if(completeness.equals("-"))
							break;
						System.out.println("Please enter y or n.");
					}
					
					while(!isPositiveInteger(newPriority = inputCommand("New priority: "))) {
						if(newPriority.equals("-"))
							break;
						System.out.println("Please input only positive number.");
					}
					controller.updateContent(id, newTODO, newDue, completeness, newPriority);
					inputTags(newTODO);
					controller.commit();
					choosing = false;
				} catch(Exception e) {
					controller.rollback();
					e.printStackTrace();
				}
			} else {
				System.out.println("Invalid id.");
			}
		}
		System.out.println("");
	}
	public void inputToRemove() {
		boolean choosing = true;
		
		while(choosing) {
			String info = "Please input content No. that you want to remove. (Show list: l Back: b)";
			String id = inputCommand(info);
			if(id.equals("l")) {
				chooseShowOption();
				continue;
			}
			if(id.equals("b")) {
				return;
			}
			//check the validity of id
			if(isPositiveInteger(id) && controller.checkValidID(id)) {
				try {
					ResultSet rs = controller.listById(id); rs.next();
					String isFinished = rs.getString("finished").equals("1") ? "Finished" : "Unfinished";
					System.out.println("\n<<Current state>>\n" 
							+ "TODO : " + rs.getString("what") + ", "
							+ "DUE : " + rs.getString("due") + ", " + isFinished + ", "
							+ "Priority : " + rs.getString("priority") + "\n");
					String com;
					
					while(!(com = inputCommand("Are you sure you want to remove this todo?(y or n)"+System.lineSeparator())).equals("y") && !com.equals("n")) {
						System.out.println("Please enter y or n.");
					}
					
					if(com.equals("y")) {
						controller.removeContent(id);
						System.out.println("Remove OK");
					}	
					controller.commit();
					choosing = false;
				} catch(Exception e) {
					controller.rollback();
					e.printStackTrace();
				}
			} else {
				System.out.println("Invalid id.");
			}
		}
		System.out.println("");
	}
	
	
	
	/***** todo: rename this method as a proper one *****/
	public void optionalList(String command) {
		if(command.matches("^l\\s-..?$")){//List todo에 들어 
			if(command.length() == 4)
				rs =controller.listByOptions("todo", command.charAt(3));
			else
				rs = controller.listByOptions("todo", command.charAt(3),command.charAt(4));
			try {
				printCurrentRecords();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void inputTagsInList() {
		String inp, tags[];
		inp = inputCommand("Enumerate tags what you want to view.(Use space as a delimiter)\n");
		tags = inp.split(" ");
		rs = controller.listByTags(tags);
	}
	
	public void printCurrentRecords() throws SQLException {
		//YYYY-MM-DD HH:MM:SS
		ColoredPrinter cp = new ColoredPrinter.Builder(1, false).build();
		String leftAlignFormat = "| %-3s | %-15s | %-23s |";
		System.out.format("+-----+-----------------+-------------------------+%n");
		System.out.format("| No. |       What      |          Due            |%n");
		System.out.format("+-----+-----------------+-------------------------+%n");
		while (rs.next()) {
			String what = rs.getString("what");
			String due = rs.getString("due");
			String id = rs.getString("id");
			
			if(dDay(Integer.parseInt(due.substring(0, 4)),Integer.parseInt(due.substring(5, 7)),Integer.parseInt(due.substring(8, 10))) >= -7){
				cp.print("|");
				cp.print(String.format(" %-3s " , id),Attribute.NONE, FColor.WHITE, BColor.RED);
				cp.clear();
				cp.print("|");
				cp.print(String.format(" %-15s " , what),Attribute.NONE, FColor.WHITE, BColor.RED);
				cp.clear();
				cp.print("|");
				cp.print(String.format(" %-23s " , due),Attribute.NONE, FColor.WHITE, BColor.RED);
				cp.clear();
				cp.print("|");
			}else{
				System.out.print(String.format(leftAlignFormat , id,  what , due ));
			}
			printTagsOf(id);
		}
		System.out.format("+-----+-----------------+-------------------------+%n");
		rs.close();
	}
	
	public void setOrderOption() {
		System.out.println("Enumerate order option as which you want to view list.\n"
				+ "Current option: " + controller.getOrderCriteria() + "\n"
		+ "(n: content number, c: content(lexicographic), d: due)\n");
		String inp = console.readLine("Or type 'b' if you don't want to touch anything\n:");
		if(inp.equals("b"))
			return;
		controller.setOrderCriteria(inp);
	}
	
	private void printTagsOf(String id) {
		try {
			ResultSet rs = controller.getTagsById(id);
			if(rs.next()) {
				System.out.print(" Tags : ");
				System.out.print(rs.getString(1));
				while(rs.next()) {
					System.out.print(", " + rs.getString(1) );
				}
			}
			System.out.println();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private int dDay(int y, int m, int d){
		TimeZone tz = TimeZone.getTimeZone("Asia/Seoul");
		Calendar today = Calendar.getInstance(tz);
		Calendar dday = Calendar.getInstance(tz); 
		
		dday.set(y,m-1,d);
		
		long day = dday.getTimeInMillis()/86400000; 
		long tday = today.getTimeInMillis()/86400000; 
		long count = tday - day;
		
		return (int)(count+1); // 날짜는 하루 + 시켜줘야합니다.

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
				printCurrentTags();
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
	
	/* Print all tags that this DB has now */
	public void printCurrentTags() {
		try {
			ResultSet rs = controller.getAllTags();
			System.out.println("------------Current Tags list------------");
			while (rs.next()) {
				System.out.println(rs.getString(1)); // Print its name
			}
			System.out.println("-----------------------------------------");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	// for checking if priority is valid
	private boolean isPositiveInteger(String str) {
		int len = str.length();
		char c;
		for(int i = 0; i < len; i++) {
			c = str.charAt(i);
			if(c < '0' || c > '9')
				return false;
		}
		return true;
	}
	
	private boolean isValidDate(String date) {
		Pattern p = Pattern.compile("(^\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}$)");
		return p.matcher(date).find();
	}
	
	public String inputCommand(String message) {
		return console.readLine(message);
	}
	
	public void exit() {
		controller.end();
	}
	
}
