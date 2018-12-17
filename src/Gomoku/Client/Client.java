package Gomoku.Client;
import Gomoku.Transmission.ClientUDP;
import Gomoku.Transmission.Message;
import Gomoku.Transmission.ReceiveResult;

import javax.swing.*;
import java.net.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import Gomoku.Interface.*;

public class Client {
	private InetSocketAddress serverSocketAddress;
	private Status clientStatus = Status.INIT;
	private ClientUDP clientUDP;
	private String clientUsername = "";
	private int currentGame = -1;
	private LoginRegister loginRegister;
	private GomokuHall gomokuHall;
	private ChessFrame chessFrame;
	private PrivateChat privateChat;
	private Client(){
		serverSocketAddress = new InetSocketAddress("localhost", 6000);
		try{
			clientUDP = new ClientUDP();
			clientUDP.handshake(serverSocketAddress);
			new Receive().start();
		}catch (SocketException e){
			e.printStackTrace();
		}
		loginRegister = new LoginRegister(new LoginRegisterToHere());
		gomokuHall = new GomokuHall(new HallToClient());
		gomokuHall.setVisible(false);
		chessFrame = new ChessFrame(new ChessFrameToHere());
		chessFrame.setVisible(false);
		privateChat = new PrivateChat(new PrivateChatToClient());
		privateChat.setVisible(false);
	}


	private class HeardBeat extends Thread{
		@Override
		public void run() {
			Message message = new Message();
			message.type = Message.Type.HEARTBEAT;
			message.addParameter(clientUsername);
			while(true){
				clientUDP.send(message, serverSocketAddress);
				try{
					Thread.sleep(3000);
				}catch (InterruptedException e){
					e.printStackTrace();
					break;
				}
				message.reSetID();
			}
		}
	}

	private class Receive extends Thread{
		@Override
		@SuppressWarnings("unchecked")
		public void run() {
			while(true){
				ConcurrentHashMap<InetSocketAddress, ConcurrentHashMap<Long, ReceiveResult>> udpPackage = clientUDP.getUDPPackage();
				for (Map.Entry<InetSocketAddress, ConcurrentHashMap<Long, ReceiveResult>> entry : udpPackage.entrySet()) {
					ConcurrentHashMap<Long, ReceiveResult>packageList = entry.getValue();
					for(Map.Entry<Long, ReceiveResult> item : packageList.entrySet()) {
						ReceiveResult receiveResult = new ReceiveResult();
						receiveResult.message = item.getValue().message;
						receiveResult.socketAddress = item.getValue().socketAddress;
						//ArrayList parameter = receiveResult.message.parameter;
						try{
							switch (receiveResult.message.type) {
								case Broadcast_GameList: {
									if(clientStatus == Status.IN_HALL) {
										gomokuHall.gameTableManagement.updateGames(receiveResult.message.getParameter());
									}
									packageList.remove(item.getKey());
									break;
								}
								case BROADCAST_GAME_INFO: {
									chessFrame.updateGameInfo(receiveResult.message.getParameter());
									packageList.remove(item.getKey());
									break;
								}
								case GAME_CLOSE: {
									packageList.remove(item.getKey());
									if(receiveResult.message.getParameterSize() != 1){
										System.out.println("GAME_CLOSE 参数缺失");
										break;
									}
									int gameID = (int)receiveResult.message.getParameter(0);
									if(currentGame == gameID){
										clientStatus = Status.IN_HALL;
										chessFrame.setVisible(false);
										JOptionPane.showMessageDialog(gomokuHall,"该棋局已被关闭", "五子棋", JOptionPane.ERROR_MESSAGE);
									}else{
										System.out.println("GAME_CLOSE 参数与客户不符");
										break;
									}
								}
								case GAME_CHAT:{
									packageList.remove(item.getKey());
									if(receiveResult.message.getParameterSize() != 2){
										System.out.println("GAME_CHAT 参数缺失");
										break;
									}
									String username = (String)receiveResult.message.getParameter(0);
									String msg = (String)receiveResult.message.getParameter(1);
									chessFrame.receiveChatMessage(username, msg);
									break;
								}
								case BROADCAST_CHESS_POLL:{
									packageList.remove(item.getKey());
									if(receiveResult.message.getParameterSize() != 1){
										System.out.println("BROADCAST_CHESS_POLL 参数缺失");
										break;
									}
									String username = (String)receiveResult.message.getParameter(0);
									chessFrame.chessPoll(username, username.equals(clientUsername));
									break;
								}
								case BROADCAST_CLIENT_CHESS:{
									packageList.remove(item.getKey());
									if(receiveResult.message.getParameterSize() != 3){
										System.out.println("BROADCAST_CLIENT_CHESS 参数缺失");
										break;
									}
									int gameID = (int) receiveResult.message.getParameter(0);
									int x = (int) receiveResult.message.getParameter(1);
									int y = (int) receiveResult.message.getParameter(2);
									if(currentGame == gameID)
										chessFrame.receiveClientChess(x, y);
									break;
								}
								case BROADCAST_CHESS_WIN:{
									packageList.remove(item.getKey());
									if(receiveResult.message.getParameterSize() != 2){
										System.out.println("BROADCAST_CHESS_WIN 参数缺失");
										break;
									}
									int gameID = (int) receiveResult.message.getParameter(0);
									String win = (String) receiveResult.message.getParameter(1);
									if(currentGame == gameID){
										chessFrame.reset();
										JOptionPane.showMessageDialog(chessFrame, win, "五子棋", JOptionPane.INFORMATION_MESSAGE);
									}
								}
								case CLIENT_PRIVATE_CHAT:{
									packageList.remove(item.getKey());
									if(receiveResult.message.getParameterSize() != 3){
										System.out.println("CLIENT_PRIVATE_CHAT 参数缺失");
										break;
									}
									if(currentGame != (int) receiveResult.message.getParameter(1)){
										System.out.println("CLIENT_PRIVATE_CHAT 不在一个游戏内");
										break;
									}
									privateChat.receive((String)receiveResult.message.getParameter(2));
									if(! privateChat.isVisible()){
										chessFrame.systemMsg("收到一条私聊消息");
									}
									break;
								}
								case BROADCAST_USER_LIST: {
									packageList.remove(item.getKey());
									gomokuHall.updateUserList(receiveResult.message.getParameter(), clientUsername);
									break;
								}
								case CLIENT_CHALLENGE:{
									packageList.remove(item.getKey());
									if(receiveResult.message.getParameterSize() != 2){
										System.out.println("CLIENT_CHALLENGE 参数缺失");
										break;
									}
									Boolean result = JOptionPane.showConfirmDialog(gomokuHall,
											receiveResult.message.getParameter(0) + " 想挑战您, 是否接受?", "五子棋",
											JOptionPane.YES_NO_OPTION) == 0;
									Message message = new Message();
									message.type = Message.Type.CLIENT_CHALLENGE_RESPOND;
									message.addParameter(receiveResult.message.getParameter(0),
											receiveResult.message.getParameter(1), result);
									clientUDP.send(message, serverSocketAddress);
									break;
								}
								case CLIENT_CHALLENGE_BEGIN:{
									packageList.remove(item.getKey());
									if(receiveResult.message.getParameterSize() != 1){
										System.out.println("CLIENT_CHALLENGE_RESPOND 参数缺失");
										break;
									}
									int id = (int)receiveResult.message.getParameter(0);
									System.out.println("挑战: " + id);
									if(id == -1){
										JOptionPane.showMessageDialog(gomokuHall, "您的挑战请求已被拒绝", "五子棋", JOptionPane.ERROR_MESSAGE);
									}else{
										currentGame = id;
										clientStatus = Status.ON_CHESS;
										chessFrame.setToPlayer();
										chessFrame.setVisible(true);
										gomokuHall.setVisible(false);
									}
								}
							}
						}catch (ClassCastException e){
							e.printStackTrace();
						}
					}
				}
				try{
					Thread.sleep(10);
				}catch (InterruptedException e){
					e.printStackTrace();
					break;
				}
			}
		}
	}

	class ChessFrameToHere implements IChessFrameToClient{
		@Override
		public void clientLeaveTable() {
			gameOperation(currentGame, Message.Type.CLIENT_LEAVE_TABLE);
			privateChat.endChat();
		}
		@Override
		public void chatMessage(String msg) {
			if(clientStatus == Status.ON_CHESS || clientStatus == Status.ON_WATCH){
				Message message = new Message();
				message.type = Message.Type.GAME_CHAT;
				message.addParameter(currentGame, clientUsername, msg);
				clientUDP.send(message, serverSocketAddress);
			}
		}
		public boolean playerGameStatus(boolean readyOrNot) {
			if(clientStatus != Status.ON_CHESS){
				return false;
			}
			Message message = new Message();
			message.type = Message.Type.CLIENT_GAME_STATUS;
			message.addParameter(currentGame, clientUsername, readyOrNot);
			ReceiveResult receiveResult = clientUDP.sendWaitResponse(message, serverSocketAddress, 3000);
			if(receiveResult == null){
				JOptionPane.showMessageDialog(chessFrame, "请求失败","准备游戏",  JOptionPane.ERROR_MESSAGE);
				return false;
			}
			if(receiveResult.message.getParameterSize() == 0){
				JOptionPane.showMessageDialog(chessFrame, "服务器回复消息的参数有误","准备游戏",  JOptionPane.ERROR_MESSAGE);
				return false;
			}
			if(!(Boolean)receiveResult.message.getParameter(0)){
				JOptionPane.showMessageDialog(chessFrame, receiveResult.message.getParameter(1),"准备游戏",  JOptionPane.ERROR_MESSAGE);
				return false;
			}
			return true;
		}
		public boolean clientSubmitChess(int x, int y) {
			Message message = new Message();
			message.type = Message.Type.CLIENT_CHESS;
			message.addParameter(currentGame, clientUsername, x, y);
			ReceiveResult receiveResult = clientUDP.sendWaitResponse(message, serverSocketAddress, 3000);
			if(receiveResult == null){
				JOptionPane.showMessageDialog(chessFrame, "请求失败","下棋",  JOptionPane.ERROR_MESSAGE);
				return false;
			}
			if(receiveResult.message.getParameterSize() == 0){
				JOptionPane.showMessageDialog(chessFrame, "服务器返回结果参数错误","下棋",  JOptionPane.ERROR_MESSAGE);
				return false;
			}else if((Boolean) receiveResult.message.getParameter(0)){
				return true;
			}else{
				JOptionPane.showMessageDialog(chessFrame, receiveResult.message.getParameter(1),"下棋",  JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		public void clientRunAway(){
			Message message = new Message();
			message.type = Message.Type.CLIENT_RUN_AWAY;
			message.addParameter(currentGame, clientUsername);
			clientUDP.send(message, serverSocketAddress);
			privateChat.endChat();
		}
		public String getCurrentUsername(){
			return clientUsername;
		}

		public void privateChat() {
			privateChat.beginChat();
		}
		public void privateChatSetName(String hisName){
			privateChat.setHisName(hisName);
		}

		@Override
		public void adminDefeat() {
			Message message = new Message();
			message.type = Message.Type.GAME_ADMIN_DEFEAT;
			message.addParameter(currentGame, clientUsername);
			clientUDP.send(message, serverSocketAddress);
		}
	}

	class LoginRegisterToHere implements ILoginRegisterToClient{
		@Override
		public boolean login(String username, String password) {
			return operating(true, username, password);
		}

		@Override
		public boolean register(String username, String password) {
			return operating(false, username, password);
		}
		@Override
		public void toHall() {
			loginRegister.dispose();
			loginRegister = null;
			gomokuHall.setVisible(true);
		}
		private boolean operating(boolean isLogin, String username, String password){
			System.out.println(username + ", " + password);
			Message message = new Message();
			message.type = isLogin ? Message.Type.LOGIN : Message.Type.REGISTER;
			message.addParameter(username, password);
			ReceiveResult receiveResult =  clientUDP.sendWaitResponse(message, serverSocketAddress, 3000);
			if(receiveResult == null || receiveResult.message.getParameterSize() == 0){
				return false;
			}
			try{
				Boolean result = (Boolean) receiveResult.message.getParameter(0);
				if(! result){
					System.out.println(receiveResult.message.getParameter(1));
					return false;
				}
				clientUsername = username;
				gomokuHall.setTitle(username);
				new HeardBeat().start();
				clientStatus = Status.IN_HALL;
				return true;
			}catch (ClassCastException err){
				err.printStackTrace();
				return false;
			}
		}
	}

	class HallToClient implements IHallToClient{
		@Override
		public void createGame() {
			gameOperation(-1, Message.Type.GAME_CREATE);
		}

		@Override
		public void quit() {
			Message message = new Message();
			message.type = Message.Type.LOGOUT;
			message.addParameter(clientUsername);
			clientUDP.send(message, serverSocketAddress);
		}
		@Override
		public boolean joinGame(int gameID) {
			chessFrame.clearChat();
			return gameOperation(gameID, Message.Type.CLIENT_JOIN_TABLE);
		}

		@Override
		public boolean watchGame(int gameID) {

			Message message = new Message();
			message.type = Message.Type.GAME_CHESS_DATA;
			message.addParameter(gameID);
			ReceiveResult receiveResult = clientUDP.sendWaitResponse(message, serverSocketAddress, 5000);
			if(receiveResult == null){
				JOptionPane.showMessageDialog(gomokuHall,"获取棋盘失败", "观战", JOptionPane.ERROR_MESSAGE);
				return false;
			}else if(receiveResult.message.getParameterSize() != 1){
				JOptionPane.showMessageDialog(gomokuHall,"获取棋盘失败, 回应参数有误", "观战", JOptionPane.ERROR_MESSAGE);
				return false;
			}else{
				int[][] chessData = new int[16][16];
				String data = (String) receiveResult.message.getParameter(0);
				int index = 0;
				for(int i = 0;i<16;i++){
					for(int i2 = 0;i2<16;i2++){
						chessData[i][i2] = data.charAt(index++) - '0';
					}
				}
				chessFrame.serChessData(chessData);
			}
			chessFrame.clearChat();
			chessFrame.setToWatcher();
			return gameOperation(gameID, Message.Type.CLIENT_WATCH_TABLE);
		}

		@Override
		public boolean challenge(String username) {
			Message message = new Message();
			message.type = Message.Type.CLIENT_CHALLENGE;
			message.addParameter(clientUsername, username);
			ReceiveResult receiveResult = clientUDP.sendWaitResponse(message, serverSocketAddress, 3000);
			if(receiveResult == null){
				return false;
			}
			return receiveResult.message.getParameterSize() != 0 && (Boolean) receiveResult.message.getParameter(0);
		}
	}

	class PrivateChatToClient implements IPrivateChatToClient{
		@Override
		public void send(String msg) {
			Message message = new Message();
			message.type = Message.Type.CLIENT_PRIVATE_CHAT;
			message.addParameter(clientUsername, currentGame, msg);
			clientUDP.send(message, serverSocketAddress);
		}
	}

	private boolean gameOperation(int gameID, Message.Type type){
		Message message = new Message();
		message.type = type;
		if(gameID != -1){
			message.addParameter(gameID);
		}
		message.addParameter(this.clientUsername);
		ReceiveResult receiveResult = clientUDP.sendWaitResponse(message, serverSocketAddress, 2000);
		if(receiveResult == null){
			return false;
		}
		if(!(Boolean)receiveResult.message.getParameter(0)){
			JOptionPane.showMessageDialog(gomokuHall,receiveResult.message.getParameter(1), "错误" , JOptionPane.ERROR_MESSAGE);
			return false;
		}
		switch (type){
			case GAME_CREATE: case CLIENT_JOIN_TABLE:
				clientStatus = Status.ON_CHESS;
				currentGame = (int)receiveResult.message.getParameter(1);
				chessFrame.setToPlayer();
				chessFrame.setVisible(true);
				gomokuHall.setVisible(false);
				break;
			case CLIENT_WATCH_TABLE:
				clientStatus = Status.ON_WATCH;
				currentGame = (int)receiveResult.message.getParameter(1);
				//clientToUI.showChessFrame(currentGame);
				chessFrame.setToWatcher();
				chessFrame.setVisible(true);
				gomokuHall.setVisible(false);
				break;
			case CLIENT_LEAVE_TABLE:
				clientStatus = Status.IN_HALL;
				chessFrame.setVisible(false);
				gomokuHall.setVisible(true);
				break;
		}

		return true;
	}

	enum Status {
		INIT, IN_HALL, ON_CHESS, ON_WATCH
	}

	public static void main(String[] args){
		new Client();
		new Client();
		new Client();
	}
}
