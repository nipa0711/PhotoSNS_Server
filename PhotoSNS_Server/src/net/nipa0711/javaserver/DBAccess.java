package net.nipa0711.javaserver;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
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

	public boolean checkDatabase() {
		boolean dirChk = new File((Variable.db_directory + "/" + Variable.db_Name)).exists();
		if (dirChk == true) {
			System.out.println("database is already exist");
			return true;
		}
		return false;
	}

	public void createNewDatabase() {
		if (checkDatabase() == false) {
			new File(Variable.db_directory).mkdirs();
			System.out.println("created database here : " + Variable.db_directory);
			new File(Variable.photo_directory).mkdirs();
			System.out.println("created photo directory here : " + Variable.photo_directory);
			new File(Variable.thumbnail_directory).mkdirs();
			System.out.println("created thumbnail directory here : " + Variable.thumbnail_directory);
			String url = "jdbc:sqlite:" + Variable.db_directory + "/" + Variable.db_Name;

			try (Connection conn = DriverManager.getConnection(url)) {
				if (conn != null) {
					DatabaseMetaData meta = conn.getMetaData();
					System.out.println("The driver name is " + meta.getDriverName());
					System.out.println("A new database has been created.");
					makeTable();
				}

			} catch (SQLException e) {
				System.out.println(e.getMessage());
			}
		}
	}

	public void makeTable() {
		try {
			DBAccess db = new DBAccess("SQLite", "org.sqlite.JDBC",
					"jdbc:sqlite:" + Variable.db_directory + "/" + Variable.db_Name, "", "");
			db.createTable();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void createTable() throws Exception {
		String sql = "CREATE TABLE PhotoSNS (id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, userid VARCHAR2(255), quote VARCHAR2(255), thumbnail BLOB, uploadDate VARCHAR2(255),metadata string, photo BLOB);";

		// 데이터베이스 커넥션 가져오기
		Connection c = dbConnectionPool.getConnection();
		if (c == null)
			throw new Exception();

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

	/* 새로운 레코드를 PhotoSNS 테이블에 저장 */
	public void insert(String userid, String quote, String metadata, String photoHex) throws Exception {
		// 데이터베이스 커넥션 가져오기
		Connection c = dbConnectionPool.getConnection();
		if (c == null)
			throw new Exception();

		long time = System.currentTimeMillis();
		SimpleDateFormat takenTime = new SimpleDateFormat("yyyy년MM월dd일 HH시mm분ss초");
		String photoUploadTime = takenTime.format(new Date(time));
		String photoName = userid + "_" + photoUploadTime + ".jpg";
		String photoPath = Variable.photo_directory + "/" + photoName;
		ImageService.saveToFile(photoHex, photoPath);
		String thumbnail = Variable.thumbnail_directory + "/" + "thumb_" + photoName;
		ImageService.makeThumbnail(photoPath, thumbnail);

		// 삽입 SQL 문장 작성
		String sql = "INSERT INTO PhotoSNS (userid,quote,  thumbnail,uploadDate,metadata, photo)" + " VALUES ( '"
				+ userid + "', '" + quote + "', '" + thumbnail + "','" + photoUploadTime + "','" + metadata + "','"
				+ photoPath + "');";
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
		String sql = "SELECT id, userid,quote,thumbnail,uploadDate,metadata FROM PhotoSNS ORDER BY id DESC";
		StringBuffer sb = new StringBuffer();
		try {

			// 질의 SQL 문장 실행
			PreparedStatement pstmt = c.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				String thumbnailPath = rs.getString(4);
				String Base64Thumbnail = ImageService.imageToBase64(thumbnailPath);
				sb.append(rs.getString(1) + "%" + rs.getString(2) + "%" + rs.getString(3) + "%" + Base64Thumbnail + "%"
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
			String bigPhoto = rs.getString(1);
			String Base64BigPhoto = ImageService.imageToBase64(bigPhoto);
			sb.append(Base64BigPhoto);

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
}