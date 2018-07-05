package net.nipa0711.javaserver;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

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
				server.start();
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

	public static void main(String[] args) {
		guiShowFrame frame = new guiShowFrame();
		frame.setVisible(true);
		frame.setSize(300, 200);
		changeText("initializing...");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}

}
