
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBManager {
	private Connection connection;
	private String orderCriteria = "LOWER(what)";

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
			connection.createStatement();
			return true;
		} catch (Exception e) {
			System.out.println("========Connection Error=======");
			e.printStackTrace();
			return false;
		}
	}
	
	
	public void createTable(String[] table) {
		try {
			Statement st = connection.createStatement();
			for(String sql : table) {
				st.executeUpdate(sql);
			}
			
			commit();
		} catch (SQLException e) {
			rollback();
			e.printStackTrace();
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
	
	
	//SQL Injection 대비 SQL 업데이트 메소드
	public void executeUpdate(String sql, String... items) throws Exception{
		PreparedStatement stmt = connection.prepareStatement(sql);
		int parameterIndex = 1;
		
		for(String item : items)
			stmt.setString(parameterIndex++, item);
		
		if(stmt.executeUpdate() == 0)
			throw new Exception();
	}
	//SQL Injection 대비 SQL 업데이트 메소드
	public ResultSet executeQuery(String sql, String... items) throws Exception{
		PreparedStatement stmt = connection.prepareStatement(sql);
		int parameterIndex = 1;
		
		for(String item : items){
			
//			System.out.println(parameterIndex+","+item); //임시 주석처리 
			stmt.setString(parameterIndex++, item);
		}
		
		return stmt.executeQuery();
	}
	
	public String getOrderCriteria() {
		return orderCriteria;
	}
	
	public void setOrderCriteria(String crit) {
		orderCriteria = crit;
	}
	
	
}
