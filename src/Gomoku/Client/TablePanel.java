package Gomoku.Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import Gomoku.Interface.*;
class TablePanel extends JPanel{
	private JPanel left, right;
	private JTextArea user1, user2;
	private int tableID = -1;
	private IHallToClient toClient;
	TablePanel(int tableID, IHallToClient toClient){
		this.toClient = toClient;
		this.tableID = tableID;
		this.setLayout(new BorderLayout());
		this.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(new JLabel(new ImageIcon("img/table.png")), BorderLayout.CENTER);

		left = new JPanel();
		left.setPreferredSize(new Dimension(50, 72));
		left.setLayout(new FlowLayout());
		JLabel left_icon = new JLabel(new ImageIcon("img/user.png"));
		left_icon.setPreferredSize(new Dimension(32, 32));
		left.add(left_icon);
		user1 = new JTextArea();
		user1.setPreferredSize(new Dimension(50, 40));
		user1.setEditable(false);
		user1.setLineWrap(true);
		user1.setWrapStyleWord(true);
		left.add(user1);
		left.setVisible(false);

		right = new JPanel();
		right.setPreferredSize(new Dimension(50, 72));
		right.setLayout(new FlowLayout());
		JLabel right_icon = new JLabel(new ImageIcon("img/user.png"));
		right_icon.setPreferredSize(new Dimension(32, 32));
		right.add(right_icon);
		user2 = new JTextArea();
		user2.setPreferredSize(new Dimension(50, 40));
		user2.setEditable(false);
		user2.setLineWrap(true);
		user2.setWrapStyleWord(true);
		right.add(user2);
		right.setVisible(false);

		mainPanel.add(left, BorderLayout.WEST);
		mainPanel.add(right, BorderLayout.EAST);
		this.add(mainPanel, BorderLayout.CENTER);

		JPanel panel_button = new JPanel();
		JButton join = new JButton("加入");
		join.addActionListener(new JoinListener());
		panel_button.add(join);
		JButton watch = new JButton("观战");
		watch.addActionListener(new WatchListener());
		panel_button.add(watch);
		this.add(panel_button, BorderLayout.SOUTH);

	}
	private class JoinListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			toClient.joinGame(tableID);
		}
	}
	private class WatchListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			toClient.watchGame(tableID);
		}
	}
	void updateUsers(String user1, String user2){
		if(user1 != null){
			this.user1.setText(user1);
			left.setVisible(true);
		}else{
			left.setVisible(false);
		}
		if(user2 != null){
			this.user2.setText(user2);
			right.setVisible(true);
		}else{
			right.setVisible(false);
		}
	}

}
