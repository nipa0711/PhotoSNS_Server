package net.nipa0711.javaserver;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Vector;

/* 데이터베이스 커넥션 풀 클래스 */
public class DatabaseConnectionPool {
	private static final String SQLITE = "SQLite";
	private static final String MYSQL = "MySQL";

	private String dbName;
	private String driverName;
	private String password;
	private String url;
	private String user;
	private Driver driver;
	private Vector freeConnections;
	private int maxConn;
	private int count;

	public DatabaseConnectionPool(String dbName, String drivername, String conUrl, String conUser, String conPassword)
			throws SQLException {
		freeConnections = new Vector();
		this.dbName = dbName;
		this.driverName = drivername;
		this.url = conUrl;
		this.user = conUser;
		this.password = conPassword;

		try {
			if (dbName.equals(MYSQL)) {
				driver = (Driver) Class.forName(driverName).newInstance();
				DriverManager.registerDriver(driver);
			} else if (dbName.equals(SQLITE)) {
				Class.forName(driverName);
			}
		} catch (Exception ex) {
			new SQLException();
		}
		count = 0;
		maxConn = 5;
	}

	public void destroy() {
		closeAll();
		try {
			// DriverManager.deregisterDriver(driver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized void freeConnection(Connection connection) {
		freeConnections.addElement(connection);
		count--;
		notifyAll();
	}

	public synchronized Connection getConnection() {
		Connection connection = null;
		if (freeConnections.size() > 0) {
			connection = (Connection) freeConnections.elementAt(0);
			freeConnections.removeElementAt(0);
			try {
				if (connection.isClosed()) {
					connection = getConnection();
				}
			} catch (Exception e) {
				print(e.getMessage());
				connection = getConnection();
			}
			return connection;
		}

		if (count < maxConn) {
			connection = newConnection();
			print("새로운 커넥션이 생성되었습니다!");
		}
		if (connection != null) {
			count++;
		}
		if (connection == null) {
			connection = newConnection();
		}
		return connection;
	}

	private synchronized void closeAll() {
		for (Enumeration enumeration = freeConnections.elements(); enumeration.hasMoreElements();) {
			Connection connection = (Connection) enumeration.nextElement();
			try {
				connection.close();
			} catch (Exception e) {
				print(e.getMessage());
			}
		}
		freeConnections.removeAllElements();
	}

	private Connection newConnection() {
		Connection connection = null;
		try {
			if (dbName.equals(MYSQL)) {
				connection = DriverManager.getConnection(url, user, password);
			} else if (dbName.equals(SQLITE)) {
				connection = DriverManager.getConnection(url);
			}
		} catch (Exception e) {
			print("새로운 커넥션 생성에 실패했습니다!");
			print("[오류 메시지] " + e.getMessage());
			return null;
		}
		return connection;
	}

	private void print(String print) {
		System.out.println(print);
	}
}