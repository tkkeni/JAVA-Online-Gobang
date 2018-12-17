package Gomoku.Client;

import Gomoku.Interface.ILoginRegisterToClient;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigInteger;
import java.security.MessageDigest;

import javax.swing.*;

class LoginRegister extends JFrame{
	private ILoginRegisterToClient toClient;
	LoginRegister(ILoginRegisterToClient toClient){
		try{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}catch (Exception e){
			e.printStackTrace();
		}
		this.toClient = toClient;
		this.setTitle("登录");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.getContentPane().add( new LoginPanel() );
		this.pack();
		this.setVisible(true);

	}
	private class LoginPanel extends JPanel{
		JTextField tf_user_id;
		JPasswordField tf_pwd;
		LoginPanel (){
			setPreferredSize( new Dimension(480, 280) );
			setLayout(null);

			JLabel lb_user = new JLabel("用户：");
			lb_user.setBounds(120, 50, 50, 30);
			this.add(lb_user);

			tf_user_id = new JTextField(15);
			tf_user_id.setBounds(175, 50, 170, 30);
			this.add(tf_user_id);

			JLabel lb_pwd = new JLabel("密码：");
			lb_pwd.setBounds(120, 90, 50, 30);
			this.add(lb_pwd);

			tf_pwd= new JPasswordField(15);
			tf_pwd.setBounds(175, 90, 170, 30);
			this.add(tf_pwd);

			JButton btn_login = new JButton("登录");
			btn_login.setFont(new Font("宋体", 1, 18)); // NOI18N
			btn_login.setText("登  录");
			btn_login.setCursor(new Cursor(Cursor.HAND_CURSOR));
			this.add(btn_login);
			btn_login.setBounds(185, 150, 130, 30);
			btn_login.addActionListener(new BtnLoginListener());

			JButton btnRegister = new JButton("注册");
			this.add(btnRegister);
			btnRegister.setBounds(210, 200, 75, 25);
			btnRegister.addActionListener(new RegisterBtnListener());
		}

		@Override
		protected void paintComponent(Graphics g) {
			// TODO Auto-generated method stub
			super.paintComponent(g);

		}
		class BtnLoginListener implements ActionListener{           //登录按钮触发事件
			public void actionPerformed(ActionEvent e) {
				String username = tf_user_id.getText();
				String password = new String(tf_pwd.getPassword());
				if(username.equals("")) {
					JOptionPane.showMessageDialog(null, "用户名不能为空", "【出错啦】", JOptionPane.ERROR_MESSAGE);
				}
				else if(password.equals("")){
					JOptionPane.showMessageDialog(null, "密码不能为空", "【出错啦】", JOptionPane.ERROR_MESSAGE);
				}
				else {
					if(toClient.login(username, getMD5(password))) {
						toClient.toHall();
					}
					else {
						JOptionPane.showMessageDialog(null, "登录失败！账号或密码错误", "【出错啦】", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}
		class RegisterBtnListener implements ActionListener{     //在登录窗口下，点击注册触发事件

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				getContentPane().removeAll();
				getContentPane().add( new RegisterPanel() );
				pack();
				//frame_register.setVisible(true);
				//setVisible(false);
			}
		}
	}
	class RegisterPanel extends JPanel{      //注册按钮触发事件，打开注册新窗口
		JTextField re_user_id;
		JPasswordField re_pwd;
		RegisterPanel(){
			setPreferredSize( new Dimension(480, 280) );
			this.setLayout(null);

			JLabel lb_info = new JLabel("请在下面填写您的基本信息: ");
			lb_info.setBounds(80, 30, 200, 30);
			this.add(lb_info);

			JLabel lb_user_id = new JLabel("用户名：");
			lb_user_id.setBounds(130, 80, 80, 30);
			this.add(lb_user_id);

			re_user_id = new JTextField(20);
			re_user_id.setBounds(190, 80, 160, 30);
			this.add(re_user_id);

			JLabel lb_pwd = new JLabel("密码：");
			lb_pwd.setBounds(130, 120, 80, 30);
			this.add(lb_pwd);

			re_pwd = new JPasswordField(15);
			re_pwd.setBounds(190, 120, 160, 30);
			this.add(re_pwd);

			JButton btn_submit = new JButton("提交");
			btn_submit.addActionListener(new SubmitBtnListener());
			this.add(btn_submit);
			btn_submit.setBounds(200, 170, 100, 30);

			JButton btn_return = new JButton("返回");
			btn_return.addActionListener(new ReturnBtnListener());
			this.add(btn_return);
			btn_return.setBounds(200, 210, 100, 30);
		}
		class ReturnBtnListener implements ActionListener{
			@Override
			public void actionPerformed(ActionEvent e) {
				getContentPane().removeAll();
				getContentPane().add( new LoginPanel() );
				pack();
			}
		}
		class SubmitBtnListener implements ActionListener{     //注册提交事件

			@Override
			public void actionPerformed(ActionEvent e) {
				String username = re_user_id.getText();
				String password = new String(re_pwd.getPassword());
				if(username.equals("")) {
					JOptionPane.showMessageDialog(null, "用户名不能为空", "提示", JOptionPane.ERROR_MESSAGE);
				}
				else if(password.equals("")){
					JOptionPane.showMessageDialog(null, "密码不能为空", "提示", JOptionPane.ERROR_MESSAGE);
				}
				else {
					String md5password = getMD5(password);
					if(toClient.register(username, md5password)) {
						JOptionPane.showMessageDialog(null, "注册成功", "提示", JOptionPane.PLAIN_MESSAGE);
						toClient.toHall();
					}
					else {
						JOptionPane.showMessageDialog(null, "注册失败", "提示", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}
	}
	private String getMD5(String str) {     //MD5加密
		try {
			// 生成一个MD5加密计算摘要
			MessageDigest md = MessageDigest.getInstance("MD5");
			// 计算md5函数
			md.update(str.getBytes());
			// digest()最后确定返回md5 hash值，返回值为8为字符串。因为md5 hash值是16位的hex值，实际上就是8位的字符
			// BigInteger函数则将8位的字符串转换成16位hex值，用字符串来表示；得到字符串形式的hash值
			return new BigInteger(1, md.digest()).toString(16);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
