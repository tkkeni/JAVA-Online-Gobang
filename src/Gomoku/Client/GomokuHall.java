package Gomoku.Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Vector;

import Gomoku.Interface.*;


class GomokuHall extends JFrame {
	private IHallToClient hallToClient;
	private JList<String> userList;
	GameTableManagement gameTableManagement;
	GomokuHall(IHallToClient hallToClient){
		GomokuHall gomokuHall = this;
		this.hallToClient = hallToClient;
		try{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}catch (Exception e){
			e.printStackTrace();
		}
		this.setLayout(new BorderLayout());
		Font font = new Font("微软雅黑", Font.PLAIN, 16);

		JPanel panel_hall = new JPanel();
		panel_hall.setLayout(new BorderLayout());

		JButton button_createTable = new JButton(" 开始新的对局 ");
		button_createTable.setFont(font);
		button_createTable.setPreferredSize(new Dimension(150, 40));
		button_createTable.addActionListener(new CreateGameListener());
		panel_hall.add(button_createTable, BorderLayout.NORTH);
		JPanel panel_tables = new JPanel();
		panel_tables.setLayout(new FlowLayout());
		panel_tables.setPreferredSize(new Dimension(800, 500));
		panel_hall.add(panel_tables, BorderLayout.CENTER);
		this.add(panel_hall, BorderLayout.CENTER);

		JPanel panel_userList = new JPanel();
		panel_userList.setLayout(new BorderLayout());
		JLabel online = new JLabel("在线玩家", JLabel.CENTER);
		online.setFont(font);
		online.setBackground(Color.WHITE);
		online.setPreferredSize(new Dimension(120, 30));

		panel_userList.add(online, BorderLayout.NORTH);
		panel_userList.setBackground(Color.WHITE);

		userList = new JList<>();
		userList.setFont(font);
		userList.setBackground(Color.WHITE);
		panel_userList.add(userList, BorderLayout.CENTER);
		userList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2){
					System.out.println("mouseClicked");
					String username = userList.getSelectedValue();
					if(username != null){
						if(username.lastIndexOf("(空闲)") > username.lastIndexOf("(游戏中)")){
							username = username.substring(0, username.lastIndexOf("(空闲)") - 1);
							if(JOptionPane.showConfirmDialog(gomokuHall, "确定要挑战 " + username + " 吗?", "五子棋",JOptionPane.YES_NO_OPTION) == 0){
								if(hallToClient.challenge(username)){
									JOptionPane.showMessageDialog(gomokuHall, "发送成功, 等待回应", "五子棋", JOptionPane.INFORMATION_MESSAGE);
								}else{
									JOptionPane.showMessageDialog(gomokuHall, "发送失败, 请稍后重试", "五子棋", JOptionPane.ERROR_MESSAGE);
								}
							}
						}else{
							JOptionPane.showMessageDialog(gomokuHall, "该玩家正在游戏中!", "五子棋", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			}
		});
		userList.setPreferredSize(new Dimension(120, 500));
		panel_userList.setPreferredSize(new Dimension(120, 500));
		this.add(panel_userList, BorderLayout.EAST);

		pack();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		gameTableManagement = new GameTableManagement(panel_tables, hallToClient);

		addWindowListener(new WindowAdapter() {//窗口关闭事件
			public void windowClosing(WindowEvent e) {
				hallToClient.quit();
				super.windowClosing(e);
			}
		});

	}

	void updateUserList(ArrayList<String> list, String currentUsername){
		Vector<String> t_userList = new Vector<>();
		for(int i = 0;i<list.size();i+=2){
			if(list.get(i).equals(currentUsername)){
				continue;
			}
			t_userList.add(list.get(i) + " (" + list.get(i + 1) + ")");
		}
		userList.setListData(t_userList);
	}
	class CreateGameListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			hallToClient.createGame();
		}
	}
}

