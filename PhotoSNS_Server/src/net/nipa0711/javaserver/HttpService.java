package net.nipa0711.javaserver;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class HttpService implements HttpHandler {

	public void handle(HttpExchange exchange) throws IOException {
		// parameters 파라메터를 Map 자료구조에 저장
		Map<String, Object> parameters = (Map<String, Object>) exchange.getAttribute("parameters");

		// HTTP 요청방식에 따른 처리절차 수행
		String request = exchange.getRequestMethod();
		if (request.equalsIgnoreCase("GET") || request.equalsIgnoreCase("POST")) {
			// 요청 파라메터 가져오기
			int command = Integer.parseInt((String) parameters.get("command"));
			String id = (String) parameters.get("id");
			String params = (String) parameters.get("params");
			System.out.println("command:" + command);
			System.out.println("id:" + id);
			System.out.println("params:" + params);
			String results = "If you see this message, it means you are wrong!";

			//String raspiPath = "/home/pi/Desktop/PhotoSNS.db";
			String windowPath = "//e:\\sqlite\\db\\PhotoSNS.db";
			String dbPath;
			dbPath = windowPath;

			switch (command) {
			case 0:
				try {
					// SQLite DB 사용
					DBAccess db = new DBAccess("SQLite", "org.sqlite.JDBC", "jdbc:sqlite:" + dbPath, "", "");

					String androidMsg[] = params.split("[%]");

					db.insert(androidMsg[0], androidMsg[1], androidMsg[2], androidMsg[3], androidMsg[4]);

					System.out.println("add complete");
					guiShowFrame.changeText("add complete");

				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case 2:
				try {
					// SQLite DB 사용
					DBAccess db = new DBAccess("SQLite", "org.sqlite.JDBC", "jdbc:sqlite:" + dbPath, "", "");
					results = db.getAll();
					System.out.println("release complete");
					guiShowFrame.changeText("release complete");

				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case 4:
				try {
					// SQLite DB 사용
					DBAccess db = new DBAccess("SQLite", "org.sqlite.JDBC", "jdbc:sqlite:" + dbPath, "", "");
					db.delete(params);
					System.out.println("delete complete");
					guiShowFrame.changeText("delete complete");

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case 6:
				try {
					// SQLite DB 사용
					DBAccess db = new DBAccess("SQLite", "org.sqlite.JDBC", "jdbc:sqlite:" + dbPath, "", ""); // d:\\PhotoSNS.db
					results = db.getBigPhoto(params);
					System.out.println("release complete");
					guiShowFrame.changeText("release complete");

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;

			}

			// GET 또는 POST 요청 처리 부분 

			// 응답 메시지 작성 (예시)
			// String results = "SERVER SEND TO YOU - HELLO";

			// results 문자열을 응답
			Headers responseHeaders = exchange.getResponseHeaders();
			responseHeaders.set("Content-Type", "text/plain");
			exchange.sendResponseHeaders(200, 0);
			OutputStream os = exchange.getResponseBody();
			os.write(results.getBytes());
			os.close();
		}
	}
}