
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.regex.Pattern;

import com.diogonunes.jcdp.color.ColoredPrinter;
import com.diogonunes.jcdp.color.api.Ansi.Attribute;
import com.diogonunes.jcdp.color.api.Ansi.BColor;
import com.diogonunes.jcdp.color.api.Ansi.FColor;

public class DBManager {
	private Connection connection;
	private Statement st;

	DBManager() {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (Exception e) {
			System.out.println("========Init Error=======");
			e.printStackTrace();
		}
	}

	public boolean connectionDB(String filePath) {
		try {
			connection = DriverManager.getConnection("jdbc:sqlite:" + filePath);
			connection.setAutoCommit(false);
			st = connection.createStatement();
			return true;
		} catch (Exception e) {
			System.out.println("========Connection Error=======");
			e.printStackTrace();
			return false;
		}
	}

	public int createTable(String projectName) {
		try {
			st.executeUpdate("CREATE TABLE IF NOT EXISTS " + projectName
					+ " (id INTEGER PRIMARY KEY AUTOINCREMENT, what TEXT UNIQUE NOT NULL, due TEXT NOT NULL,"
					+ "finished INTEGER DEFAULT 0, priority INTEGER DEFAULT 0, category TEXT DEFAULT 'none')");
			st.executeUpdate("CREATE TABLE IF NOT EXISTS tag "
					+ "(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, todo_id INTEGER,"
					+ "		FOREIGN KEY (todo_id) REFERENCES todo(id) ON UPDATE CASCADE) ");
			commit();
			return 1;
		} catch (Exception e) {
			rollback();
			System.out.println("=======Create Table Error=======");
			e.printStackTrace();
			return -1;
		}
	}
	
	public void commit() { 
		try {
			connection.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		} 
	}
	public void rollback()  { 
		try {
			connection.rollback();
		} catch (SQLException e) {
			e.printStackTrace();
		} 
	}

	public void close() {
		try {
			if (connection == null)
				connection.close();
		} catch (Exception e) {
			System.out.println("=======Close Error=======");
			e.printStackTrace();
		}
	}

	public boolean addTodo(String what, String due) {
		Pattern p = Pattern.compile("(^\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}$)");
		
		while(!p.matcher(due).find()){
			due = System.console().readLine("Due date? (YYYY-MM-DD HH:MM:SS) : ");
		}
		try {
			executeUpdate("INSERT INTO todo (what, due) VALUES (?, ?)",what,due+".000");
			commit();
		} catch (Exception e) {
			e.printStackTrace();
			rollback();
		}
		return true;
	}

	public boolean listTodo(String projectName) {
		try {
			ResultSet rs = st.executeQuery("SELECT * FROM " + projectName);
			printCurrentRecords(rs);
			rs.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean listTodo(String projectName,char... options) {
		try {
			ResultSet rs;
			switch(options[0]){
			case 'a':
				rs = st.executeQuery("SELECT * FROM " + projectName);
				break;
			case 'w':
				rs = st.executeQuery("SELECT * FROM "+projectName+" WHERE due BETWEEN date('now','weekday 0') AND date('now','weekday 0','+6 days')");
				break;
			case 't':
				rs = st.executeQuery("SELECT * FROM "+projectName+" WHERE due LIKE date('now','localtime')||'%'");
				break;
			default:
				if((options.length == 1) && (49 <=options[0] && options[0]<=57)){
					rs = st.executeQuery("SELECT * FROM "+projectName+" WHERE due LIKE strftime('%Y','now')||'-0"+options[0]+"'||'%'");
				}else if((options.length == 2) && (options[0] == 49)){
					rs = st.executeQuery("SELECT * FROM "+projectName+" WHERE due LIKE strftime('%Y','now')||'-"+options[0]+options[1]+"'||'%'");
				}else{
					rs = st.executeQuery("SELECT * FROM " + projectName);
				}
				break;
			}

			printCurrentRecords(rs);
			rs.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean overdueList(String projectName, boolean isOverDue) {
		String sign = isOverDue ? "<" : ">=";
		try {
			ResultSet rs = st.executeQuery("SELECT * FROM " + projectName + " WHERE due " + sign + " datetime()");
			printCurrentRecords(rs);
			rs.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean showListByCompletion(boolean isFinished) {
		try {
			int finished = isFinished ? 1 : 0;
			ResultSet rs = st.executeQuery("SELECT * FROM todo WHERE finished = " + finished);
			printCurrentRecords(rs);
			rs.close();
			return true;
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/*Check the repetition so that a tuple can't have the same tag names.*/
	public boolean hasTag(String what, String tagName) {
		try {
			String todoID;
			ResultSet idSearchResult = executeQuery("SELECT id FROM todo WHERE what = ?", what), tagSearchResult;
			if(idSearchResult.next()) {
				todoID = idSearchResult.getString(1); 
				tagSearchResult = executeQuery("SELECT * FROM tag WHERE name = ? AND todo_id = ?", tagName, todoID);
				if(tagSearchResult.next()) {
					System.out.println(tagSearchResult.getString("name") + " " + tagSearchResult.getString("todo_id"));
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean addTag(String what, String tagName) {
		try {
			ResultSet id_res = executeQuery("SELECT id FROM todo WHERE what = ?", what); id_res.next();
			String id = id_res.getString("id");
			executeUpdate("INSERT INTO tag (name, todo_id) VALUES (?, ?)", tagName, id);
			commit();
			id_res.close();
			System.out.println("successfully added");
			return true;
		} catch (Exception e) {
			rollback();
			e.printStackTrace();
			return false;
		}
	}
	
	public void showListByTags(String[] tags) {
		try {
			ResultSet rs;
			StringBuilder query = new StringBuilder("todo ");
			int numOfTags = tags.length;
			for(int i = 0; i < numOfTags; i++) {
				query = new StringBuilder("(SELECT * FROM ").append(query.toString())
						.append("WHERE id IN (SELECT todo_id FROM tag WHERE name = ?) )");
			}
			rs = executeQuery(query.substring(1,query.length()-2), tags);
			printCurrentRecords(rs);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	//SQL Injection 대비 SQL 업데이트 메소드
	private void executeUpdate(String sql, String... items) throws Exception{
		
		PreparedStatement stmt = connection.prepareStatement(sql);
		int parameterIndex = 1;
		
		for(String item : items)
			stmt.setString(parameterIndex++, item);
		
		if(stmt.executeUpdate() == 0)
			throw new Exception();
	}
	//SQL Injection 대비 SQL 업데이트 메소드
	private ResultSet executeQuery(String sql, String... items) throws Exception{
		
		PreparedStatement stmt = connection.prepareStatement(sql);
		int parameterIndex = 1;
		
		for(String item : items){
			
//			System.out.println(parameterIndex+","+item); //임시 주석처리 
			stmt.setString(parameterIndex++, item);
		}
		
		return stmt.executeQuery();
	}
	
	public void printCurrentRecords(ResultSet rs) throws SQLException {
		//YYYY-MM-DD HH:MM:SS
		ColoredPrinter cp = new ColoredPrinter.Builder(1, false).build();
		String leftAlignFormat = "| %-15s | %-23s |";
		System.out.format("+-----------------+-------------------------+%n");
		System.out.format("|      What       |          Due            |%n");
		System.out.format("+-----------------+-------------------------+%n");
		while (rs.next()) {
			String what = rs.getString("what");
			String due = rs.getString("due");
			
			if(dDay(Integer.parseInt(due.substring(0, 4)),Integer.parseInt(due.substring(5, 7)),Integer.parseInt(due.substring(8, 10))) >= -7){
				cp.print("|");
				cp.print(String.format(" %-15s " , what),Attribute.NONE, FColor.WHITE, BColor.RED);
				cp.clear();
				cp.print("|");
				cp.print(String.format(" %-23s " , due),Attribute.NONE, FColor.WHITE, BColor.RED);
				cp.clear();
				cp.print("|");
			}else{
				System.out.print(String.format(leftAlignFormat , what , due ));
			}

			printTagsOf(rs.getString("id"));
		}
		System.out.format("+-----------------+-------------------------+%n");
		
	}
	
	private void printTagsOf(String id) {
		try {
			ResultSet rs = executeQuery("SELECT name FROM tag WHERE todo_id = ?", id);
			System.out.print(" Tags : ");
			while(rs.next()) {
				System.out.print(rs.getString(1) + " ");
			}
			System.out.println();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/* Print all tags that this DB has now */
	public void printCurrentTags() {
		try {
			ResultSet rs = executeQuery("SELECT name from tag");
			System.out.println("------------Current Tags list------------");
			while (rs.next()) {
				System.out.println(rs.getString(1)); // Print its name
			}
			System.out.println("-----------------------------------------");
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
	
	
}
