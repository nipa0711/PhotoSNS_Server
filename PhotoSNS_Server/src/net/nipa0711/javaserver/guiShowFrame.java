package net.nipa0711.javaserver;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

public class guiShowFrame extends JFrame {
	static JTextField textField;

	public static void changeText(String text) {
		textField.setText(text);
	}

	public guiShowFrame() {

		JButton btnServerStart = new JButton("SERVER START");
		btnServerStart.setSize(300, 75);
		btnServerStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				HostServer server = new HostServer();
				server.Start();
			}
		});
		getContentPane().add(btnServerStart, BorderLayout.NORTH);

		JButton btnServerExit = new JButton("SERVER EXIT");
		btnServerExit.setSize(300, 75);
		btnServerExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.out.println("Server end");
				System.exit(0);
			}
		});
		getContentPane().add(btnServerExit, BorderLayout.CENTER);
		textField = new JTextField();
		textField.setFont(new Font("D2Coding", Font.PLAIN, 20));
		textField.setSize(300, 50);
		getContentPane().add(textField, BorderLayout.SOUTH);
	}

	public static void createNewDatabase() {
		Variable var = new Variable();

		boolean dirChk = new File(var.db_directory + "/" + var.db_Name).exists();

		if (dirChk == false) {
			new File(var.db_directory).mkdirs();
			String url = "jdbc:sqlite:" + var.db_directory + var.db_Name;

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

	public static void makeTable() {
		try {
			Variable var = new Variable();
			DBAccess db = new DBAccess("SQLite", "org.sqlite.JDBC",
					"jdbc:sqlite:" + var.db_directory + "\\" + var.db_Name, "", "");
			db.createTable();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		guiShowFrame frame = new guiShowFrame();
		frame.setVisible(true);
		frame.setSize(300, 200);
		createNewDatabase();
		changeText("initializing...");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}

}
