package Gomoku.Client;

import org.omg.CORBA.BooleanHolder;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
public class UserPanel extends JPanel {
	private JLabel p_username, p_time;
	private String username = "";
	private Chess chess;
	private Long countDown;
	private final BooleanHolder timing = new BooleanHolder(false);
	UserPanel(){
		setPreferredSize(new Dimension(200, 80));
		setBorder(new LineBorder(Color.black, 1));
		setLayout(new BorderLayout());

		JPanel left = new JPanel();
		left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

		p_username = new JLabel("Username");
		p_username.setFont(new Font("微软雅黑", Font.PLAIN, 18));
		p_username.setBorder(new EmptyBorder(10, 10, 10, 0));
		left.add(p_username);

		p_time = new JLabel("");
		p_time.setFont(new Font("微软雅黑", Font.BOLD, 23));
		p_time.setBorder(new EmptyBorder(10, 10, 10, 0));
		left.add(p_time);

		add(left, BorderLayout.CENTER);

		chess = new Chess();
		chess.setVisible(false);
		add(chess, BorderLayout.EAST);
		new ThreadCountDown().start();
	}
	private class Chess extends JPanel {
		private Color chessColor = Color.black;
		private int x, y, r = 15;
		Chess(){
			setPreferredSize(new Dimension(40, 80));
			x = (30 - r) / 2 - 5;
			y = (80 - r) / 2;
		}
		@Override
		public void paint(Graphics g) {
			super.paint(g);
			g.setColor(chessColor);
			g.fillOval(x, y, 2 * r, 2 * r);

		}
	}
	void waiting(){
		p_username.setText("等待玩家...");
		this.username = "";
	}
	public void setUsername(String username){
		p_username.setText(username);
		this.username = username;
	}
	void setTime(Long time){
		countDown = time;
		setTime(countDown.toString());
	}
	void countDown(boolean run){
		synchronized (timing){
			this.timing.value = run;
		}

	}
	void countDownSwitch(){
		synchronized (timing){
			this.timing.value = !this.timing.value;
		}
	}
	String getUsername(){
		return this.username;
	}
	private class ThreadCountDown extends Thread{
		@Override
		public void run() {
			while(true){
				synchronized (timing){
					if(timing.value){
						if(countDown != 0){
							countDown -= 1;
							//setTime(countDown.toString());
						}
					}
				}
				try{
					Thread.sleep(1000);
				}catch (InterruptedException e){
					e.printStackTrace();
					break;
				}
			}
		}
	}
	void setChessVisible(boolean enable){
		chess.setVisible(enable);
	}
	void setTime(String text){
		p_time.setText(text);
	}
	void setColor(Color color){
		chess.chessColor = color;
	}
}
