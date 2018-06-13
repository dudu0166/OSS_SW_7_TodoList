import java.io.File;
import java.security.MessageDigest;
import java.sql.ResultSet;

import javax.swing.filechooser.FileSystemView;

public class Controller {
	private DBManager db;
	
	Controller() {
		
		String path = FileSystemView.getFileSystemView().getDefaultDirectory().getPath();
		File document = new File(path);
		if(document.canWrite()) {
			File dir = new File(path+File.separator+"todo-b7");
			if(!dir.exists()){
				dir.mkdir();
				path+=File.separator+"todo-b7";
			}			
		}else {
			path = ".";
		}
		
		db = new DBManager();
		db.connectionDB(path+File.separator+"todoDB");
		String sql = "SELECT count(*) as result FROM sqlite_master WHERE Name = 'userInfo'";
		
		try {
			
			ResultSet rs = db.executeQuery(sql);
			rs.next();
			
			if(rs.getInt("result") == 0) {
				String[] unserInfoTableQuery = {"CREATE TABLE userInfo "
						+ " (id INTEGER PRIMARY KEY AUTOINCREMENT, pass TEXT UNIQUE NOT NULL)",
				};
				db.createTable(unserInfoTableQuery);
				MessageDigest digest = MessageDigest.getInstance("SHA-256");
	            byte[] hash = digest.digest(new String(System.console().readPassword("Please Input init password.")).getBytes("UTF-8"));
	            
	            StringBuffer hexString = new StringBuffer();
	            
	            for (int i = 0; i < hash.length; i++) {
	                String hex = Integer.toHexString(0xff & hash[i]);
	                if(hex.length() == 1) hexString.append('0');
	                hexString.append(hex);
	            }
	 
	    		try {
	    			db.executeUpdate("INSERT INTO userInfo (pass) VALUES (?)",hexString.toString());
	    			db.commit();
	    		} catch (Exception e) {
	    			db.rollback();
	    			e.printStackTrace();
	    		}
			}else {
				MessageDigest digest = MessageDigest.getInstance("SHA-256");
	            byte[] hash = digest.digest(new String(System.console().readPassword("Please Input your password.")).getBytes("UTF-8"));
	            StringBuffer hexString = new StringBuffer();
	            
	            for (int i = 0; i < hash.length; i++) {
	                String hex = Integer.toHexString(0xff & hash[i]);
	                if(hex.length() == 1) hexString.append('0');
	                hexString.append(hex);
	            }
	            rs = db.executeQuery("SELECT count(*) as result FROM userInfo WHERE pass = ?", hexString.toString());
	            rs.next();
	            if(rs.getInt("result") == 0) {
	            	System.out.println("The password is incorrect.");
	            	end();
	            	System.exit(0);
	            }
			}
			rs.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String[] CreatingTableQuery = {"CREATE TABLE IF NOT EXISTS todo "
				+ " (id INTEGER PRIMARY KEY AUTOINCREMENT, what TEXT UNIQUE NOT NULL, due TEXT NOT NULL,"
				+ "finished INTEGER DEFAULT 0, priority INTEGER DEFAULT 0)",
				"CREATE TABLE IF NOT EXISTS tag "
						+ "(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, todo_id INTEGER,"
						+ "		FOREIGN KEY (todo_id) REFERENCES todo(id) ON UPDATE CASCADE) "};
		db.createTable(CreatingTableQuery);
	}
	
	public ResultSet listAll() {
		try {
			return db.executeQuery("SELECT * FROM todo ORDER BY " + db.getOrderCriteria());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public ResultSet listByOptions(String projectName,char... options) {
		try {
			ResultSet rs;
			String sql = "SELECT * FROM " + projectName;
			switch(options[0]){
			case 'a':
				sql = "SELECT * FROM " + projectName;
				break;
			case 'w':
				sql += " WHERE due BETWEEN date('now','weekday 0') AND date('now','weekday 0','+6 days')";
				break;
			case 't':
				sql += " WHERE due LIKE date('now','localtime')||'%'";
				break;
			default:
				if((options.length == 1) && (49 <=options[0] && options[0]<=57)){
					sql += " WHERE due LIKE strftime('%Y','now')||'-0"+options[0]+"'||'%'";
				}else if((options.length == 2) && (options[0] == 49)){
					sql += " WHERE due LIKE strftime('%Y','now')||'-"+options[0]+options[1]+"'||'%'";
				}
				break;
			}
			sql += " ORDER BY " + db.getOrderCriteria(); // insert sorting option
			rs = db.executeQuery(sql);
			return rs;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public ResultSet listById(String id) {
		try {
			return db.executeQuery("SELECT what, due, finished, priority FROM todo WHERE id = ?", id);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public ResultSet listByTimeExcess(boolean isOverDue) {
		String sign = isOverDue ? "<" : ">=";
		try {
			return db.executeQuery("SELECT * FROM todo WHERE due " + sign + " datetime()" 
					+ " ORDER BY " + db.getOrderCriteria());

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public ResultSet listByCompletion(boolean isFinished) {
		try {
			int finished = isFinished ? 1 : 0;
			return db.executeQuery("SELECT * FROM todo WHERE finished = " + finished + " ORDER BY " + db.getOrderCriteria());
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	/* Add a record to the list */
	public void addList(String what, String due, String priority) {
		try {
			db.executeUpdate("INSERT INTO todo (what, due, priority) VALUES (?, ?, ?)",what,due+".000", priority);
			db.commit();
		} catch (Exception e) {
			db.rollback();
			e.printStackTrace();
		}
	}
	
	/*Check the repetition so that a tuple can't have the same tag names.*/
	public boolean hasSameTag(String what, String tagName) {
		try {
			String todoID;
			ResultSet idSearchResult = db.executeQuery("SELECT id FROM todo WHERE what = ?", what), tagSearchResult;
			if(idSearchResult.next()) {
				todoID = idSearchResult.getString(1); 
				tagSearchResult = db.executeQuery("SELECT * FROM tag WHERE name = ? AND todo_id = ?", tagName, todoID);
				if(tagSearchResult.next()) {
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/* Add tags to a record */
	public boolean setTag(String what, String tag) {
		if(hasSameTag(what, tag))
			return false;
		else {
			try {
				ResultSet id_res = db.executeQuery("SELECT id FROM todo WHERE what = ?", what); id_res.next();
				String id = id_res.getString("id");
				db.executeUpdate("INSERT INTO tag (name, todo_id) VALUES (?, ?)", tag, id);
				db.commit();
				id_res.close();
				return true;
			} catch (Exception e) {
				db.rollback();
				e.printStackTrace();
				return false;
			}
		}
	}
	
	public boolean checkValidID(String id) {
		try {
			return db.executeQuery("SELECT id FROM todo WHERE id = ?", id).next();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public ResultSet listByTags(String[] tags) {
		try {
			StringBuilder query = new StringBuilder("todo ");
			int numOfTags = tags.length;
			for(int i = 0; i < numOfTags; i++) {
				query = new StringBuilder("(SELECT * FROM ").append(query.toString())
						.append("WHERE id IN (SELECT todo_id FROM tag WHERE name = ?) )");
			}
			return db.executeQuery(query.substring(1,query.length()-2), tags);
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public ResultSet getAllTags() {
		try {
			return db.executeQuery("SELECT name from tag");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public boolean isRepeat(String what) {
		try {
			ResultSet rs = db.executeQuery("SELECT * FROM todo WHERE what = ?", what);
			return rs.next();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public void updateContent(String id, String todo, String due, String finished, String priority) throws Exception {
		ResultSet rs = db.executeQuery("SELECT * FROM TODO WHERE id = ?", id);
		if(todo.equals("-"))
			todo = rs.getString("what");
		if(due.equals("-"))
			due = rs.getString("due");
		if(finished.equals("-"))
			finished = rs.getString("finished");
		else
			finished = finished.equals("y") ? "1" : "0";
		if(priority.equals("-"))
			priority = rs.getString("priority");
		
		String sql = "UPDATE todo SET what = ?, due = ?, finished = ?, priority = ? WHERE id = ?";
		db.executeUpdate(sql, todo, due, finished, priority, id);
	}
	
	public void removeContent(String id) throws Exception {		
		String sql = "DELETE FROM todo WHERE id =?";
		db.executeUpdate(sql, id);
	}
	
	public String getOrderCriteria() {
		return db.getOrderCriteria();
	}
	
	public void setOrderCriteria(String opt) {
		
		switch (opt) {
		case "n":
			db.setOrderCriteria("id");
			break;
		case "c":
			db.setOrderCriteria("what");
			break;
		case "p":
			db.setOrderCriteria("priority");
			break;
		case "d":
			db.setOrderCriteria("due");
			break;
		}
	}
	
	public void commit() {
		db.commit();
	}
	
	public void rollback() {
		db.rollback();
	}
	
	public ResultSet getTagsById(String id) throws Exception {
		return db.executeQuery("SELECT name FROM tag WHERE todo_id = ?", id);
	}
	
	public void end() {
		db.close();
	}

	
}
