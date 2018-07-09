package net.nipa0711.javaserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

public class HostServer {
	public void Start() {
		// HTTP 서버 Port 지정
		int port = 1234;

		// HTTP 서버 구동
		HostServer server = new HostServer();
		server.HttpService(port);
		System.out.println("현재 서버 접속 포트 번호 : " + port);
		guiShowFrame.changeText("서버 접속 포트 번호 : " + port);
	}

	private void HttpService(int port) {
		try {
			// HttpServer 객체 생성
			InetSocketAddress addr = new InetSocketAddress(port);
			HttpServer server = HttpServer.create(addr, 0);

			// HttpServer가 인식하는 URL 지정
			HttpContext context1 = server.createContext("/hostserver/photoSNS", new HttpService());

			// HTTP 파라메터 통합
			context1.getFilters().add(new ParameterFilter());

			// HttpServer 구동시작
			server.setExecutor(Executors.newCachedThreadPool());
			server.start();
			System.out.println("SERVER START!");
		} catch (Exception e) {
			System.out.println("[HTTP 서비스 오류] " + e.getMessage());
		}
	}

	/* GET 파라메터와 POST 파라메터를 parameters 파라메터로 통합 */
	public class ParameterFilter extends Filter {
		public String description() {
			return "Parses the requested URI for parameters";
		}

		@Override
		public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
			parseGetParameters(exchange);
			parsePostParameters(exchange);
			chain.doFilter(exchange);
		}

		private void parseGetParameters(HttpExchange exchange) throws UnsupportedEncodingException {
			Map<String, Object> parameters = new HashMap<String, Object>();
			URI requestedUri = exchange.getRequestURI();
			String query = requestedUri.getRawQuery();
			parseQuery(query, parameters);
			exchange.setAttribute("parameters", parameters);
		}

		private void parsePostParameters(HttpExchange exchange) throws IOException {
			if ("post".equalsIgnoreCase(exchange.getRequestMethod())) {
				@SuppressWarnings("unchecked")
				Map<String, Object> parameters = (Map<String, Object>) exchange.getAttribute("parameters");
				InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
				BufferedReader br = new BufferedReader(isr);
				String query = br.readLine();
				parseQuery(query, parameters);
				exchange.setAttribute("parameters", parameters);
			}
		}

		@SuppressWarnings("unchecked")
		private void parseQuery(String query, Map<String, Object> parameters) throws UnsupportedEncodingException {
			if (query != null) {
				String pairs[] = query.split("[&]");

				for (String pair : pairs) {
					String param[] = pair.split("[=]");
					String key = null;
					String value = null;
					if (param.length > 0) {
						key = URLDecoder.decode(param[0], System.getProperty("file.encoding"));
					}

					if (param.length > 1) {
						value = URLDecoder.decode(param[1], System.getProperty("file.encoding"));
					}

					if (parameters.containsKey(key)) {
						Object obj = parameters.get(key);
						if (obj instanceof List<?>) {
							List<String> values = (List<String>) obj;
							values.add(value);
						} else if (obj instanceof String) {
							List<String> values = new ArrayList<String>();
							values.add((String) obj);
							values.add(value);
							parameters.put(key, values);
						}
					} else {
						parameters.put(key, value);
					}
				}
			}
		}
	}
}
