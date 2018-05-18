
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
					+ " (id integer primary key autoincrement, what text not null, due text not null, finished integer default 0, category text default 'none')");
			return 1;

		} catch (Exception e) {
			System.out.println("=======Create Table Error=======");
			e.printStackTrace();
			return -1;
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
		try {
			Pattern p = Pattern.compile("(^\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}$)");
			
			while(!p.matcher(due).find()){
				due = System.console().readLine("Due date? (YYYY-MM-DD HH:MM:SS) : ");
			}
			
			executeUpdate("INSERT INTO todo (what, due) VALUES (?, ?)",what,due+".000");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean listTod(String projectName) {
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

	public boolean overdueList(String projectName) {
		try {
			ResultSet rs = st.executeQuery("SELECT * FROM " + projectName + " WHERE due < datetime()");
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
	
	public boolean showListByCategory(String category) {
		try {
			ResultSet rs = executeQuery("SELECT * FROM todo WHERE category = ?",category);
			printCurrentRecords(rs);
			rs.close();
			return true;
		} catch(Exception e) {
			e.printStackTrace();
			return false;
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
			System.out.println(parameterIndex+","+item);
			stmt.setString(parameterIndex++, item);
		}
		
		return stmt.executeQuery();
	}
	
	public void printCurrentRecords(ResultSet rs) throws SQLException {
		//YYYY-MM-DD HH:MM:SS
		ColoredPrinter cp = new ColoredPrinter.Builder(1, false).build();
		String leftAlignFormat = "| %-15s | %-23s |%n";
		System.out.format("+-----------------+-------------------------+%n");
		System.out.format("| What            | Due                     |%n");
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
				cp.println("|");
			}else{
				System.out.print(String.format(leftAlignFormat , what , due ));
			}
			
		}
		System.out.format("+-----------------+-------------------------+%n");
		
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
