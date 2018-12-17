package Gomoku.Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import Gomoku.Interface.*;
class ChessFrame extends JFrame {
	private ChessBoard chessBoard;
	private UserPanel user_1, user_2;
	private JTextArea chat, watch;
	private JTextField chatInput;
	private IChessFrameToClient chessFrameToClient;
	private JButton button_ready, button_adminDefeat, button_summation, button_privateChat;
	private boolean isNeedReset = false;
	private boolean isPlayer = false;
	private boolean isPlaying = false;
	ChessFrame(IChessFrameToClient chessFrameToClient) {
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.chessFrameToClient = chessFrameToClient;
		try{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}catch (Exception e){
			e.printStackTrace();
		}
		setPreferredSize(new Dimension(1120, 710));
		Font font = new Font("微软雅黑", Font.PLAIN, 16);
		setLayout(null);
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(null);
		mainPanel.setBounds(0, 0, 1120, 710);

		chessBoard = new ChessBoard();
		mainPanel.add(chessBoard);
		chessBoard.setBounds(0,0,660,660);

		user_1 = new UserPanel();
		mainPanel.add(user_1);
		user_1.setBounds(660,30,200,100);


		user_2 = new UserPanel();
		mainPanel.add(user_2);
		user_2.setBounds(880,30,200,100);

		button_privateChat = new JButton("私聊");
		button_privateChat.setFont(font);
		mainPanel.add(button_privateChat);
		button_privateChat.setBounds(740,135,120,32);//740,960
		button_privateChat.addActionListener(new PrivateChatListener());
		button_privateChat.setVisible(false);

		JLabel label_watch = new JLabel("观战者:");
		label_watch.setFont(font);
		mainPanel.add(label_watch);
		label_watch.setBounds(660,140,420,20);

		watch = new JTextArea();
		watch.setFont(font);
		watch.setEditable(false);
		mainPanel.add(watch);
		watch.setBounds(660,170,420,40);

		JLabel label = new JLabel("聊天:");
		label.setFont(font);
		mainPanel.add(label);
		label.setBounds(660,220,420,20);

		chat = new JTextArea();
		chat.setFont(font);
		chat.setEditable(false);
		mainPanel.add(chat);
		chat.setBounds(660,250,420,300);

		chatInput = new JTextField();
		chatInput.setFont(font);
		mainPanel.add(chatInput);
		chatInput.setBounds(660,560,330,32);

		JButton chatSend = new JButton("Send");
		chatSend.setFont(font);
		mainPanel.add(chatSend);
		chatSend.setBounds(990,560,90,32);
		chatSend.addActionListener(new SendListener());

		button_ready = new JButton("准备");
		button_ready.setFont(font);
		button_ready.addActionListener(new ReadyListener());
		mainPanel.add(button_ready);
		button_ready.setBounds(660,600,120,32);

		button_adminDefeat = new JButton("认输");
		button_adminDefeat.setFont(font);
		mainPanel.add(button_adminDefeat);
		button_adminDefeat.setBounds(800,600,120,32);
		button_adminDefeat.setEnabled(false);
		button_adminDefeat.addActionListener(new AdminDefeat());

		button_summation = new JButton("求和");
		button_summation.setFont(font);
		mainPanel.add(button_summation);
		button_summation.setBounds(940,600,120,32);
		button_summation.setEnabled(false);

		getContentPane().add(mainPanel);
		pack();
		setVisible(false);
		addWindowListener(new WindowAdapter() {//窗口关闭事件
			public void windowClosing(WindowEvent e) {
			if(isPlayer && isPlaying){
				if(JOptionPane.showConfirmDialog(null, "当前操作将被视为逃跑, 确认要退出吗？", "五子棋",JOptionPane.YES_NO_OPTION) != 0){
					return;
				}
				chessFrameToClient.clientRunAway();
			}else{
				chessFrameToClient.clientLeaveTable();
			}
			setVisible(false);
			chessBoard.clear();
			}
		});
	}

	void chessPoll(String username, boolean isHere){
		if(user_1.getUsername().equals(username)){
			user_1.setChessVisible(true);
			user_2.setChessVisible(false);
			user_1.countDown(true);
			user_2.countDown(false);
		}else{
			user_2.setChessVisible(true);
			user_1.setChessVisible(false);
			user_2.countDown(true);
			user_1.countDown(false);
		}
		chessBoard.isMinePoll = isHere;
		if(isNeedReset){
			chessBoard.color = 1;
			chessBoard.clear();
			isNeedReset = false;
			chessBoard.updateUI();
		}
		isPlaying = true;
	}
	void receiveClientChess(int x, int y){
		chessBoard.chess(x, y);
	}
	void reset(){
		chessBoard.isMinePoll = false;
		isNeedReset = true;
		if(isPlayer){
			button_ready.setText("准备");
			button_ready.setEnabled(true);
			button_adminDefeat.setEnabled(false);
			button_summation.setEnabled(false);
		}
		user_1.setChessVisible(false);
		user_2.setChessVisible(false);
		isPlaying = false;
		user_1.countDown(false);
		user_1.countDown(false);
		user_1.setTime("未准备");
		user_2.setTime("未准备");
	}
	void systemMsg(String msg){
		chat.append("系统消息 -> " + msg + "\n");
	}
	void receiveChatMessage(String username, String msg){
		chat.append(username + ": " + msg + "\n");
	}
	void updateGameInfo(ArrayList all) {
		if(all.size() < 7){
			System.out.println("updatePlayerWatcher 错误！参数不完整");
			System.out.println(all.toString());
			return;
		}
		user_1.setUsername((String)all.get(1));
		System.out.println(all.toString());
		if((Boolean)all.get(0)){
			//游戏开始
			user_1.setColor((Boolean)all.get(2) ? Color.BLACK : Color.WHITE);
			user_2.setUsername((String)all.get(4));
			user_2.setColor((Boolean)all.get(5) ? Color.BLACK : Color.WHITE);
			user_1.setTime((Long) all.get(3));
			user_2.setTime((Long) all.get(6));
			if(isPlayer){
				button_ready.setEnabled(false);
				button_adminDefeat.setEnabled(true);
				button_summation.setEnabled(true);
			}
		}else{
			user_1.setTime((Boolean)all.get(2) ? "准备" : "未准备");
			if(all.get(4) == null){
				user_2.waiting();
				user_2.setTime("");
				button_privateChat.setVisible(false);
			}else{
				user_2.setUsername((String)all.get(4));
				user_2.setTime((Boolean)all.get(5) ? "准备" : "未准备");
				if(isPlayer){
					String user1 = user_1.getUsername();
					String user2 = user_2.getUsername();
					button_privateChat.setVisible(true);
					if(chessFrameToClient.getCurrentUsername().equals(user1)){
						button_privateChat.setBounds(960,135,120,32);
						chessFrameToClient.privateChatSetName(user2);
					}else{
						button_privateChat.setBounds(740,135,120,32);
						chessFrameToClient.privateChatSetName(user1);
					}
				}
			}
		}
		watch.setText("");
		for(int i = 7;i<all.size();i++){
			watch.append(all.get(i) + " ");
		}

	}
	void clearChat(){
		chat.setText("");
	}
	void setToPlayer(){
		chessBoard.setEnabled(false);
		button_ready.setEnabled(true);
		button_adminDefeat.setEnabled(false);
		button_summation.setEnabled(false);
		button_ready.setText("准备");
		isPlayer = true;
		if(! user_1.getUsername().equals("") && ! user_2.getUsername().equals("")){
			button_privateChat.setVisible(true);
		}else{
			button_privateChat.setVisible(false);
		}
	}
	void setToWatcher(){
		chessBoard.setEnabled(false);
		button_ready.setEnabled(false);
		button_adminDefeat.setEnabled(false);
		button_summation.setEnabled(false);
		isPlayer = false;
		button_privateChat.setVisible(false);
	}
	void serChessData(int[][] data){
		chessBoard.chessData = data;
		chessBoard.updateUI();
	}
	private class SendListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			String msg = chatInput.getText();
			if(! msg.equals("")){
				chessFrameToClient.chatMessage(msg);
			}
			chatInput.setText("");
		}
	}
	private class ReadyListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			JButton thisButton = ((JButton)e.getSource());
			if(thisButton.getText().equals("准备")){
				if(chessFrameToClient.playerGameStatus(true)){
					thisButton.setText("取消准备");
				}
			}else{
				if(chessFrameToClient.playerGameStatus(false)){
					thisButton.setText("准备");
				}
			}

		}
	}
	private class AdminDefeat implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			if(JOptionPane.showConfirmDialog(null, "确定要认输吗?", "五子棋",JOptionPane.YES_NO_OPTION) == 0){
				chessFrameToClient.adminDefeat();
				reset();
			}
		}
	}
	private class PrivateChatListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			if(! user_1.getUsername().equals("等待玩家...") && ! user_2.getUsername().equals("等待玩家...")){
				chessFrameToClient.privateChat();
			}
		}
	}
	class ChessBoard extends JPanel {
		int[][] chessData;
		int N = 15; //N*N的棋盘
		private int k = 40; //方格宽（高）度
		private int x0 = 30, y0 = 30;
		private int r = 15; //棋子（圆）半径
		private int color = 1;
		boolean isMinePoll = false;
		private int[] confirm = new int[2];
		//棋盘上每个交叉点的状态：0-无子（默认），1-黑子，2-白字
		ChessBoard() {
			confirm[0] = -1;
			confirm[1] = -1;
			chessData = new int[N + 1][N + 1];
			setPreferredSize(new Dimension(660, 660));
			addMouseListener(new ChessMouseListener());
		}
		void clear(){
			chessData = new int[N + 1][N + 1];
			confirm[0] = -1;
			confirm[1] = -1;
		}
		@Override
		public void paint(Graphics g) {
			super.paint(g);
			setBackground(new Color(221,198,87,255));
			for (int i = 0; i <= N; i++) {
				g.drawLine(x0, y0 + i * k, x0 + N * k, y0 + i * k);
			}
			for (int i = 0; i <= N; i++) {
				g.drawLine(x0 + i * k, y0, x0 + i * k, y0 + N * k);
			}
			for (int i = 0; i <= N; i++) {
				for (int j = 0; j <= N; j++) {
					drawChess(i, j, chessData[i][j], g);
				}
			}
			if(confirm[0] != -1 && confirm[1] != -1){
				drawChess(confirm[0], confirm[1], 3, g);
			}
		}
		private void drawChess(int x, int y, int color, Graphics g) {
			if (color == 1) {
				g.setColor(Color.black);
				g.fillOval(x0 + x * k - r, y0 + y * k - r, 2 * r, 2 * r);
			} else if (color == 2) {
				g.setColor(Color.white);
				g.fillOval(x0 + x * k - r, y0 + y * k - r, 2 * r, 2 * r);
			} else if(color == 3){
				System.out.println("drawChess 3: " + x + "," + y);
				g.setColor(Color.gray);
				g.fillOval(x0 + x * k - r, y0 + y * k - r, 2 * r, 2 * r);
			}
		}
		void chess(int x, int y){
			chessData[x][y] = color;
			color = color == 1 ? 2 : 1;
			updateUI();
		}
		private class ChessMouseListener implements MouseListener {
			public void mouseClicked(MouseEvent e) {
				if(!isMinePoll){
					return;
				}
				int x = Math.round((e.getX() - x0) / (k + 0.0f));
				int y = Math.round((e.getY() - y0) / (k + 0.0f));
				if(e.getClickCount() == 1){
					if(confirm[0] == x && confirm[1] == y){
						confirmChess(x, y);
					}else{
						confirm[0] = x;
						confirm[1] = y;
						updateUI();
					}
					System.out.println("chess wait confirm");
				}
				if(e.getClickCount() == 2){
					confirmChess(x, y);
				}

			}

			public void mousePressed(MouseEvent e) {
			}

			public void mouseReleased(MouseEvent e) {
			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
			}
		}
		private void confirmChess(int x, int y){
			confirm[0] = -1;
			confirm[1] = -1;
			if(chessData[x][y] != 0){
				return;
			}
			if(! chessFrameToClient.clientSubmitChess(x, y)){
				return;
			}
			isMinePoll = false;
			chessData[x][y] = color;
			color = color == 1 ? 2 : 1; //黑白颠倒
			repaint(); //重绘
		}
	}
}
