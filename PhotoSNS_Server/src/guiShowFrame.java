import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;

public class guiShowFrame extends JFrame {
	final JTextField textField;

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
				System.exit(0);
			}
		});
		getContentPane().add(btnServerExit, BorderLayout.CENTER);
		textField = new JTextField();
		textField.setText("initializing...");
		textField.setFont(new Font("나눔고딕코딩", Font.PLAIN, 20));
		textField.setSize(300, 50);
		getContentPane().add(textField, BorderLayout.SOUTH);
	}

	public static void main(String[] args) {
		guiShowFrame frame = new guiShowFrame();
		frame.setVisible(true);
		frame.setSize(300, 200);

	}

}
