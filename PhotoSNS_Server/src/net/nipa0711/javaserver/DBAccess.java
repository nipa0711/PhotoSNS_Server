package net.nipa0711.javaserver;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

/* UserDB 데이터베이스에 있는 User 테이블 액세스 클래스 */
public class DBAccess {
	private DatabaseConnectionPool dbConnectionPool;
	Connection c = null;
	PreparedStatement pstmt = null;
	ResultSet rs = null;

	public DBAccess(String dbName, String driverName, String dbUrl, String dbUser, String dbPwd) throws Exception {
		// 데이터베이스 커넥션 풀 구성
		try {
			dbConnectionPool = new DatabaseConnectionPool(dbName, driverName, dbUrl, dbUser, dbPwd);
		} catch (Exception e) {
			System.out.println("[커넥션풀 생성 오류]" + e.getMessage());
		}
	}

	public void db_connect() {
		try {
			// 데이터베이스 커넥션 가져오기
			c = dbConnectionPool.getConnection();
			if (c == null)
				throw new Exception();
			c.setAutoCommit(true);
		} catch (Exception e) {
			System.out.println("db_connect problem");
			e.printStackTrace();
		}
	}

	public void db_disconnect() {
		if (rs != null) {
			try {
				rs.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (pstmt != null) {
			try {
				pstmt.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// 데이터베이스 커넥션 반납
		dbConnectionPool.freeConnection(c);
	}

	public boolean checkDatabase() {
		boolean dirChk = new File((Variable.db_directory + "/" + Variable.db_SNS)).exists();
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
			String url = "jdbc:sqlite:" + Variable.db_directory + "/" + Variable.db_SNS;

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
					"jdbc:sqlite:" + Variable.db_directory + "/" + Variable.db_SNS, "", "");
			String sql = "CREATE TABLE PhotoSNS (id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, userid TEXT NOT NULL, quote TEXT, thumbnail TEXT, uploadDate TEXT, metadata TEXT, photo TEXT);";
			db.createTable(sql);

			db = new DBAccess("SQLite", "org.sqlite.JDBC",
					"jdbc:sqlite:" + Variable.db_directory + "/" + Variable.db_User, "", "");
			sql = "CREATE TABLE UserInfo (id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, userid TEXT NOT NULL, password TEXT NOT NULL, salt TEXT NOT NULL);";
			db.createTable(sql);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void insert(String sql) {
		db_connect();

		try {
			// 삽입 SQL 문장 실행
			pstmt = c.prepareStatement(sql);
			pstmt.executeUpdate();
			pstmt.close();
			c.setAutoCommit(true);
		} catch (SQLException e) {
			System.out.println("[추가 오류]" + e.getMessage());
		} catch (NullPointerException e) {
			System.out.println("[추가 오류]" + e.getMessage());
		}
		db_disconnect();
	}

	public void createTable(String sql) {
		try {
			insert(sql);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void add_newbie(String userid, String password) throws Exception {
		Security sec = new Security();
		String salt = sec.generateSalt();
		String hash = sec.getHash(password, salt);
		String sql = "INSERT INTO UserInfo (userid, password, salt) VALUES ('" + userid + "','" + hash + "','" + salt
				+ "');";
		insert(sql);
		System.out.println(userid + " is added");
	}

	/* 새로운 레코드를 PhotoSNS 테이블에 저장 */
	public void add_photo(String userid, String quote, String metadata, String photoHex) throws Exception {

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

		insert(sql);
	}

	public String isUserExist(String userid) {
		String result = null;
		try {
			result = searchByUserID(userid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (result.isEmpty()) {
			return "false";
		} else {
			return "true";
		}
	}

	public String login(String userid, String password) {
		Security sec = new Security();
		String sql = "SELECT password, salt FROM UserInfo WHERE (userid ='" + userid + "');";

		try {
			String[] result = getData(sql).split("%|\\#");
			if (result[0].isEmpty()) {
				return "false";
			}
			String savedPW = result[0];
			String salt = result[1];
			String hash = sec.getHash(password, salt);
			if (savedPW.equals(hash) == true) {
				return "true";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "false";
	}

	public String searchByUserID(String userid) throws Exception {
		String sql = "SELECT * FROM UserInfo WHERE ( userid = '" + userid + "');";

		return getData(sql);
	}

	public String getData(String sql) throws Exception {
		db_connect();
		StringBuffer sb = new StringBuffer();
		try {
			// 질의 SQL 문장 실행
			pstmt = c.prepareStatement(sql);
			rs = pstmt.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			while (rs.next()) {
				for (int i = 1; i < rsmd.getColumnCount(); i++) {
					sb.append(rs.getString(i) + "%");
				}
				sb.append(rs.getString(rsmd.getColumnCount()) + "#");
			}

		} catch (Exception e) {
			System.out.println("[반환 오류]" + e.getMessage());
			throw e;
		}
		db_disconnect();
		// 결과 반환
		return sb.toString();
	}

	public String getAll() throws Exception {
		String sql = "SELECT id, userid,quote,thumbnail,uploadDate,metadata FROM PhotoSNS ORDER BY id DESC";

		String[] msg = getData(sql).split("%|\\#");
		StringBuffer sb = new StringBuffer();
		if (!msg[0].isEmpty()) {
			for (int i = 0; i < msg.length; i++) {
				if ((i + 1) % 6 == 4) {
					String Base64Thumbnail = ImageService.imageToBase64(msg[i]);
					msg[i] = Base64Thumbnail;
				}

				if ((i + 1) % 6 == 0) {
					sb.append(msg[i] + "#");
				} else {
					sb.append(msg[i] + "%");
				}
			}
		}
		// 결과 반환
		return sb.toString();
	}

	public String getBigPhoto(String id) throws Exception {
		String sql = "SELECT photo FROM PhotoSNS WHERE id ='" + id + "';";
		String[] msg = getData(sql).split("%|\\#");
		return ImageService.imageToBase64(msg[0]);
	}

	public void delete(String id) {
		String sql = "DELETE FROM PhotoSNS WHERE id ='" + id + "';";
		insert(sql);
	}
}