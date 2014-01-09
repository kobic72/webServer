import java.sql.*;

public class DBManager {
	Connection conn = null;
	Statement stmt = null;
	ResultSet rs = null;
	String sql = null;
	PreparedStatement pstmt = null;
	String result = null;

	//DB연결 설정
	String jdbcUrl = "jdbc:mysql://ihlnext.vps.phps.kr/choi_gyeongwook";
	String userID = "nextuser";
	String userPW = "dbgood";
	
	NewspaperTable nameTable = new NewspaperTable();

	public DBManager(){
		//initialize
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			System.err.println("Driver Error" + e.getMessage());
		}
		System.out.println("JDBC Driver is found. OK.");
	}
	
	@SuppressWarnings("finally")
	public synchronized String getTags(String newspaper) {
		
		try {
			conn = DriverManager.getConnection(jdbcUrl, userID, userPW);
			
			stmt = conn.createStatement();
			sql = "select tag.tag_name, count(selected_tag.tag_id), selected_tag.tag_id from tag JOIN(select * from tag_article JOIN (select article.id from article JOIN newspaper ON article.newspaper_id = newspaper.id where newspaper.name = '" 
					+ nameTable.getNewspaperName(newspaper) +"') AS temp ON tag_article.article_id = temp.id ) AS selected_tag ON selected_tag.tag_id = tag.id GROUP BY selected_tag.tag_id ORDER BY count(selected_tag.tag_id) desc limit 15;";
			rs = stmt.executeQuery(sql);
			
			result = "[";
			boolean previous = false;
			while (rs.next()) {
				if (previous){
					result += ", ";
				}
				result += ("[\"" + rs.getString("tag.tag_name") + "\", " + rs.getString("count(selected_tag.tag_id)") + ", " + rs.getString("selected_tag.tag_id") + "]");
				
				previous = true;
			}
			
			result += "]";
			
		} catch (SQLException e) {
			System.out.printf("FAIL");
			conn.rollback();
		} finally {
			if (pstmt != null) {
				try { pstmt.close(); } 
				catch (final SQLException e) {}
			}

			if (rs != null) {
				try { rs.close(); } 
				catch (final SQLException e) {}
			}

			if (conn != null) {
				try { conn.setAutoCommit(true); }
				catch (final SQLException e) {}
			}

			return result;
		}
	}
	
	@SuppressWarnings("finally")
	public synchronized String getDetail(String newspaper, String tagIdx) {
		
		try {
			conn = DriverManager.getConnection(jdbcUrl, userID, userPW);
			stmt = conn.createStatement();
			sql = "select article.title, article.summary, article.link_uri from article JOIN (select article_id from tag_article where tag_article.tag_id = " 
					+ tagIdx + ") AS selected_tag ON article.id = selected_tag.article_id where article.newspaper_id = (select id from newspaper where name = '"
					+ nameTable.getNewspaperName(newspaper) + "') ORDER BY article.time desc limit 3";
			
			rs = stmt.executeQuery(sql);
			
			result = "[";
			boolean previous = false;
			while (rs.next()) {
				if (previous){
					result += ", ";
				}
				result += ("{\"title\" : \"" + rs.getString("article.title") + "\", \"summary\" : \"" + rs.getString("article.summary") + "\", \"link\" : \"" + rs.getString("article.link_uri") + "\"}");
				
				previous = true;
			}
			
			result += "]";
			
		} catch (SQLException e) {
			System.out.printf("FAIL");
			conn.rollback();
		} finally {
			if (pstmt != null) {
				try { pstmt.close(); } 
				catch (final SQLException e) {}
			}

			if (rs != null) {
				try { rs.close(); } 
				catch (final SQLException e) {}
			}

			if (conn != null) {
				try { conn.setAutoCommit(true); }
				catch (final SQLException e) {}
			}

			return result;
		}
	}
}
