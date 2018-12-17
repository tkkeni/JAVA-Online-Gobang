package Gomoku.Client;

import Gomoku.Interface.IPrivateChatToClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class PrivateChat extends JFrame { //对战私聊, 单独一个窗口, 自己设计窗口
	private IPrivateChatToClient toClient;
	private JTextField tf;
	private JTextArea chatClient;
	private PrivateChat parent;
	private static JFrame frame;
	private String hisName = "";
	PrivateChat(IPrivateChatToClient toClient){
		this.toClient = toClient;
		ClientPanel mypanel = new ClientPanel();
		getContentPane().add(mypanel);
		setPreferredSize(new Dimension(600, 300));
		pack();
		setVisible(true);
	}
	void setHisName(String username){
		this.hisName = username;
		setTitle("To: " + hisName);
	}
	class ClientPanel extends JPanel{
		ClientPanel(){
			this.setLayout(new BorderLayout());
			JPanel pinfo = new JPanel();
			pinfo.setLayout(new BorderLayout());
			pinfo.setBorder(BorderFactory.createTitledBorder(null, "Information Border",
					javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
					javax.swing.border.TitledBorder.DEFAULT_POSITION,
					new Font("宋体", 1, 14))); // NOI18N

			JScrollPane sp1 = new  JScrollPane();
			sp1.setPreferredSize(new Dimension(8, 250));
			chatClient = new JTextArea(5,10);
			chatClient.setEditable(false);
			sp1.setViewportView(chatClient);
			pinfo.add(sp1,BorderLayout.CENTER);
			this.add(pinfo, BorderLayout.CENTER);

			JPanel pcom = new JPanel();

			JLabel lb3 = new JLabel();
			lb3.setFont(new Font("宋体", 1, 16)); // NOI18N
			lb3.setText("Message:");
			pcom.add(lb3);
			tf = new JTextField(25);
			pcom.add(tf);
			JButton btn = new JButton("Send");
			btn.addActionListener(new SendListener());
			pcom.add(btn);

			pcom.setBorder(BorderFactory.createTitledBorder(null, "Communication Border",
					javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
					javax.swing.border.TitledBorder.DEFAULT_POSITION,
					new Font("宋体", 1, 14)));

			this.add(pcom, BorderLayout.SOUTH);
		}

	}
	class SendListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			String msg = tf.getText();
			if(! msg.equals("")){
				toClient.send(msg);
				tf.setText("");
				chatClient.append("You: " + msg + "\n");
			}
		}
	}
	void beginChat(){
		setVisible(true);
	}
	void endChat(){
		chatClient.setText("");
		tf.setText("");
		setVisible(false);
	}
	void receive(String msg){ //收到消息
		chatClient.append(hisName + ": " + msg + "\n");
	}
}