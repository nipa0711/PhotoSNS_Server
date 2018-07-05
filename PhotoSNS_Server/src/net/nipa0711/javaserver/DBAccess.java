package net.nipa0711.javaserver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

/* UserDB 데이터베이스에 있는 User 테이블 액세스 클래스 */
public class DBAccess {
	private DatabaseConnectionPool dbConnectionPool;

	public DBAccess(String dbName, String driverName, String dbUrl, String dbUser, String dbPwd) throws Exception {

		// 데이터베이스 커넥션 풀 구성
		try {
			dbConnectionPool = new DatabaseConnectionPool(dbName, driverName, dbUrl, dbUser, dbPwd);
		} catch (Exception e) {
			System.out.println("[커넥션풀 생성 오류]" + e.getMessage());

			throw e;
		}
	}

	/* 새로운 레코드를 PhotoSNS 테이블에 저장 */
	public void insert(String uploader, String quote, String thumbnail, String metadata, String photo)
			throws Exception {
		// 데이터베이스 커넥션 가져오기
		Connection c = dbConnectionPool.getConnection();
		if (c == null)
			throw new Exception();

		long time = System.currentTimeMillis();
		SimpleDateFormat takenTime = new SimpleDateFormat("yyyy년MM월dd일 HH시mm분ss초");
		String photoUploadTime = takenTime.format(new Date(time));

		// 삽입 SQL 문장 작성
		String sql = "INSERT INTO PhotoSNS (quote, uploader, thumbnail,uploadDate,metadata, photo)" + " VALUES ( '"
				+ quote + "', '" + uploader + "', '" + thumbnail + "','" + photoUploadTime + "','" + metadata + "','"
				+ photo + "');";
		try {
			// 삽입 SQL 문장 실행
			PreparedStatement pstmt = c.prepareStatement(sql);

			pstmt.executeUpdate();
			pstmt.close();
			c.setAutoCommit(true);
		} catch (SQLException e) {
			System.out.println("[추가 오류]" + e.getMessage());
			throw e;
		} catch (NullPointerException e) {
			System.out.println("[추가 오류]" + e.getMessage());
			throw e;
		}

		// 데이터베이스 커넥션 반납
		dbConnectionPool.freeConnection(c);
	}

	public String getAll() throws Exception {
		// 데이터베이스 커넥션 가져오기
		Connection c = dbConnectionPool.getConnection();
		if (c == null)
			throw new Exception();

		// 반환 SQL 문장 작성
		String sql = "SELECT id,quote,uploader,thumbnail,uploadDate,metadata FROM PhotoSNS ORDER BY id DESC";
		StringBuffer sb = new StringBuffer();
		try {

			// 질의 SQL 문장 실행
			PreparedStatement pstmt = c.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {

				sb.append(rs.getString(1) + "%" + rs.getString(2) + "%" + rs.getString(3) + "%" + rs.getString(4) + "%"
						+ rs.getString(5) + "%" + rs.getString(6) + "#");
			}

			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
				}
			}

		} catch (Exception e) {
			System.out.println("[반환 오류]" + e.getMessage());
			throw e;
		}

		// 데이터베이스 커넥션 반납
		dbConnectionPool.freeConnection(c);
		// 결과 반환
		return sb.toString();
	}

	public String getBigPhoto(String id) throws Exception {
		// 데이터베이스 커넥션 가져오기
		Connection c = dbConnectionPool.getConnection();
		if (c == null)
			throw new Exception();
		String sql = "SELECT photo FROM PhotoSNS WHERE id ='" + id + "';";
		StringBuffer sb = new StringBuffer();

		try {
			// 삽입 SQL 문장 실행
			PreparedStatement pstmt = c.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			rs.next();
			sb.append(rs.getString(1));

			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
				}
			}
		} catch (SQLException e) {
			System.out.println("[추가 오류]" + e.getMessage());
			throw e;
		} catch (NullPointerException e) {
			System.out.println("[추가 오류]" + e.getMessage());
			throw e;
		}

		// 데이터베이스 커넥션 반납
		dbConnectionPool.freeConnection(c);
		return sb.toString();
	}

	public void delete(String id) throws Exception {
		// 데이터베이스 커넥션 가져오기
		Connection c = dbConnectionPool.getConnection();
		if (c == null)
			throw new Exception();

		// 삽입 SQL 문장 작성
		String sql = "DELETE FROM PhotoSNS WHERE id ='" + id + "';";
		try {
			// 삽입 SQL 문장 실행
			PreparedStatement pstmt = c.prepareStatement(sql);

			pstmt.executeUpdate();
			pstmt.close();
			c.setAutoCommit(true);
		} catch (SQLException e) {
			System.out.println("[추가 오류]" + e.getMessage());
			throw e;
		} catch (NullPointerException e) {
			System.out.println("[추가 오류]" + e.getMessage());
			throw e;
		}

		// 데이터베이스 커넥션 반납
		dbConnectionPool.freeConnection(c);
	}

	/*
	 * public static void main(String[] args) {
	 * 
	 * try { // SQLite DB 사용
	 * 
	 * DBAccess db = new DBAccess("SQLite", "org.sqlite.JDBC",
	 * "jdbc:sqlite:/d:\\PhotoSNS.db", "", "");
	 * 
	 * 
	 * // User 테이블에 새로운 레코드 추가 // db.insert(101, "101", "김상진", "컴퓨터공학과",
	 * "010-2935-2275");
	 * 
	 * // User 테이블의 기존 레코드 갱신 (비밀번호 변경) // db.update(101, "1245");
	 * 
	 * // User 테이블의 특정 레코드 삭제 // db.delete(102);
	 * 
	 * // User 테이블의 학생 레코드 검색 // String str = db.search(101); //
	 * System.out.println(str); } catch (Exception e) {
	 * System.out.println("데이터베이스 액세스를 실패하였습니다."); } }
	 */
}