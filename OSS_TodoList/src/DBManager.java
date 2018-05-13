
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DBManager {
	private Connection connection;
	private Statement st;

	DBManager() {
		try {
			Class.forName("org.sqlite.JDBC");// JDBC 드라이버 로딩.
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
			if (st.executeQuery("SELECT count(*) FROM SQLITE_MASTER WHERE name = '" + projectName + "'")
					.getBoolean("count(*)")) {// 테이블이 존재하는 지 확인.

				// String com = System.console().readLine("Table already exists.
				// Would you cover the table? (y/n)").toLowerCase();
				//
				// if(com.equals("n"))
				// return 0;
				// else if(!com.equals("y"))
				// return -1;
				// else
				// return -1;
			}

			// st.executeUpdate("CREATE TABLE "+projectName+" (id integer
			// primary key autoincrement, what text not null, due text not null,
			// finished integer default 0)");
			st.executeUpdate("CREATE TABLE IF NOT EXISTS " + projectName
					+ " (id integer primary key autoincrement, what text not null, due text not null, finished integer default 0)");
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
			st.executeUpdate("insert into todo (what, due) values ('" + what + "', '" + due + "')");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean listTod(String projectName) {
		try {
			ResultSet rs = st.executeQuery("SELECT * FROM " + projectName);
			while(rs.next()) {
				System.out.println(rs.getString("what") + " , " + rs.getString("due"));
			}
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
			while (rs.next()) {
				System.out.println(rs.getString("what") + " , " + rs.getString("due"));
			}
			rs.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
