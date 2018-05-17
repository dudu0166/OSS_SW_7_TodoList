import java.io.Console;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		DBManager db = new DBManager();
		Console console = System.console();
		db.connectionDB("./testDB");
		db.createTable("todo");

		while (true) {
			switch (console.readLine("\nChoose what to do:\n(a: Add todo, l: List todo, m: Modify todo, o: Overdue todo,"
					+ "f: view finished list only, u: view unfinished list only q: Quit)?")) {
			case "a":
				if(db.addTodo(console.readLine("Todo? : "), console.readLine("Due date? (YYYY-MM-DD HH:MM:SS) : ")))
					System.out.println("=======Add Success=======");
				else
					System.out.println("=======Add Fail=======");
				break;
			case "l":
				db.listTod("todo");
				break;
			case "m":
				break;
			case "o":
				db.overdueList("todo");
				break;
			case "f":
				db.showListByCompletion(true);
			case "u":
				db.showListByCompletion(false);
			case "q":
				System.exit(0);
				break;
			default:
				break;
			}
		}
	}

}
