package net.nipa0711.javaserver;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.filechooser.FileSystemView;

public class PhotoSNS_Main {
	public static void how_to_use(String db_path) {
		System.out.println("==================");
		System.out.println("Usage");
		System.out.println("==================");
		System.out.println("Set port number using '-port' option");
		System.out.println("ex) -port 1234\n");
		System.out.println("Set database location where you want using '-path' option");
		System.out.println("ex) -path C:/\n");
		System.out.println("If you want to use default option, type '-defalut'");
		System.out.println("port : 1234\npath : your desktop directory '" + db_path + "' is default path");
		System.out.println("default option is priority than others\n");
	}

	public static void main(String[] args) {
		File home = FileSystemView.getFileSystemView().getHomeDirectory();
		String db_path = home.getAbsolutePath();
		if (args.length < 1) {
			how_to_use(db_path);
		} else if (args.length > 5) {
			System.out.println("please read usage.\nonly '-path', '-port', '-default' can allowed\n\n");
			how_to_use(db_path);
		} else {
			Map<String, String> argsMap = new HashMap<String, String>();
			boolean isDefaultExist = false;
			try {
				for (int i = 0; i < args.length; i++) {
					if (!(args[i].equals("-path") || args[i].equals("-port") || args[i].equals("-default"))) {
						throw new IllegalArgumentException("Not a valid command: " + args[i]);
					}
					switch (args[i].charAt(0)) {
					case '-':
						if (args[i].length() < 2) {
							throw new IllegalArgumentException("Not a valid argument: " + args[i]);
						}
						if (args[i].charAt(1) == '-') {
							if (args[i].length() < 3) {
								throw new IllegalArgumentException("Not a valid argument: " + args[i]);
							}
						} else {
							if (args[i].equals("-default")) {
								isDefaultExist = true;
							}
							if ((args.length - 1 == i) && (isDefaultExist == false)) {
								throw new IllegalArgumentException("Expected arg after: " + args[i]);
							}
							if (isDefaultExist == false) {
								argsMap.put(args[i], args[i + 1]);
								i++;
							}
						}
						break; // end of case
					}
				}
			} catch (Exception e) {
				System.out.println(e.getMessage());
				return;
			}

			Variable var = new Variable();
			if (isDefaultExist == true) {
				var.db_directory = db_path;
				var.port = 1234;
			} else {
				if (argsMap.containsKey("-path")) {
					var.db_directory = argsMap.get("-path");
				}
				if (argsMap.containsKey("-port")) {
					var.port = Integer.parseInt(argsMap.get("-port"));
				}
			}

			System.out.println("database directory : " + var.db_directory);
			System.out.println("port : " + var.port);

			try {
				DBAccess db = new DBAccess("SQLite", "org.sqlite.JDBC",
						"jdbc:sqlite:" + var.db_directory + "/" + var.db_Name, "", "");
				db.createNewDatabase();
			} catch (Exception e) {
				e.printStackTrace();
			}
			HostServer server = new HostServer();
			server.Start();
		}
	}

}
