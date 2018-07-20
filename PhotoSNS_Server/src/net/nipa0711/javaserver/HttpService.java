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
		Map<?, ?> parameters = (Map<?, ?>) exchange.getAttribute("parameters");

		// HTTP 요청방식에 따른 처리절차 수행
		String request = exchange.getRequestMethod();
		if (request.equalsIgnoreCase("GET") || request.equalsIgnoreCase("POST")) {
			// 요청 파라메터 가져오기
			int command = Integer.parseInt((String) parameters.get("command"));
			// String id = (String) parameters.get("id");
			String params = (String) parameters.get("params");
			System.out.println("command:" + command);
			// System.out.println("id:" + id);
			// System.out.println("params:" + params);
			String results = "If you see this message, it means you are wrong!";

			DBAccess db = null;
			try {
				db = new DBAccess("SQLite", "org.sqlite.JDBC",
						"jdbc:sqlite:" + Variable.db_directory + "/" + Variable.db_SNS, "", "");
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			DBAccess userInfo = null;
			try {
				userInfo = new DBAccess("SQLite", "org.sqlite.JDBC",
						"jdbc:sqlite:" + Variable.db_directory + "/" + Variable.db_User, "", "");
			} catch (Exception e) {
				e.printStackTrace();
			}

			switch (command) {
			case 0:
				try {
					// SQLite DB 사용
					String androidMsg[] = params.split("%|\\#");
					String uploader = androidMsg[0];
					String quote = androidMsg[1];
					String metadata = androidMsg[2];
					String photoHex = androidMsg[3];

					db.add_photo(uploader, quote, metadata, photoHex);
					System.out.println("add complete");
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case 2:
				try {
					results = db.getAll();
					System.out.println("release complete");
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case 4:
				try {
					db.delete(params);
					// db.notifyAll();
					System.out.println("delete complete");

				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case 6:
				try {
					results = db.getBigPhoto(params);
					System.out.println("release complete");

				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case 9: // sign-up
				try {
					String[] msg = params.split("%|\\#");
					String userid = msg[0];
					String password = msg[1];
					userInfo.add_newbie(userid, password);
					results = "true";
				} catch (Exception e) {
					e.printStackTrace();
					results = "false";
				}
				break;
			case 10: // user is exist?
				results = userInfo.isUserExist(params);
				break;
			case 11: // log in complete
				try {
					String[] msg = params.split("%|\\#");
					String userid = msg[0];
					String password = msg[1];
					results = userInfo.login(userid, password);
					System.out.println(userid + " log in " + results);
				} catch (Exception e) {
					e.printStackTrace();
					results = "false";
				}
				break;
			}

			// GET 또는 POST 요청 처리 부분
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