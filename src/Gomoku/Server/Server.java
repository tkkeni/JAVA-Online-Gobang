package Gomoku.Server;

import Gomoku.Transmission.*;
import org.omg.CORBA.StringHolder;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public class Server {
	private ServerUDP serverUDP;
	private ConcurrentHashMap<Integer, Game> games;
	private ConcurrentHashMap<String, ChallengeInfo> challenge;
	private UserData db_users = new UserData();
	private UserList online_users = new UserList();
	private final long HEARTBEAT_TIMEOUT = 10000; //10秒
	private Server(){
		try{
			serverUDP = new ServerUDP(6000);
            games = new ConcurrentHashMap<>();
            challenge = new ConcurrentHashMap<>();
			new UDPProcessing().start();
			new BroadcastGameList().start();
			new BroadcastUserList().start();
			new HeartBeat().start();
		}catch (IOException e){
			e.printStackTrace();
		}
	}
	private class UDPProcessing extends Thread {
		@Override
		public void run() {
			System.out.println("Server udp begin receive");
            ConcurrentHashMap<InetSocketAddress, ConcurrentHashMap<Long, ReceiveResult>> udpPackage = serverUDP.getUDPPackage();

			while (true){
				for (Map.Entry<InetSocketAddress, ConcurrentHashMap<Long, ReceiveResult>> entry : udpPackage.entrySet()) {
                    ConcurrentHashMap<Long, ReceiveResult>packageList = entry.getValue();
                    for(Map.Entry<Long, ReceiveResult> item : packageList.entrySet()){
                        ReceiveResult receiveResult = new ReceiveResult();
                        receiveResult.message = item.getValue().message;
                        receiveResult.socketAddress = item.getValue().socketAddress;
                        packageList.remove(item.getKey());
                        try{
                            switch (receiveResult.message.type) {
                                case HELLO:{
                                    receiveResult.message.type = Message.Type.HELLO_RESULT;
                                    receiveResult.message.clearParameter();
                                    receiveResult.message.addParameter("Hello");
                                    serverUDP.send(receiveResult.message, receiveResult.socketAddress);
                                    System.out.println(receiveResult.socketAddress + " Hello");
                                    break;
                                }
                                case HEARTBEAT:{
                                    if(receiveResult.message.getParameterSize() != 1){
                                        System.out.println(receiveResult.socketAddress + " HEARTBEAT Parameter error");
                                    }else{
                                        String username = (String)receiveResult.message.getParameter(0);
                                        
                                        User user = online_users.getUser(username);
                                        if(user == null){
                                            System.out.println(receiveResult.socketAddress + " HEARTBEAT User not fount");
                                        }else{
                                            user.setHeartbeat(System.currentTimeMillis());
                                        }
                                    }
                                    break;
                                }
                                case REGISTER:{
                                    receiveResult.message.type = Message.Type.REGISTER_RESULT;
                                    if(receiveResult.message.getParameterSize() != 2){
                                        receiveResult.message.clearParameter();
                                        receiveResult.message.addParameter(false, "Parameter error");
                                    }else{
                                        String username = (String)receiveResult.message.getParameter(0);
                                        String password = (String)receiveResult.message.getParameter(1);
                                        receiveResult.message.clearParameter();
                                        if(db_users.registered(username, password)){
                                            User user = new User();
                                            user.setUsername(username);
                                            user.setUDPInfo(receiveResult.socketAddress);
                                            online_users.login(user);
                                            System.out.println(username + " 注册并登录");
                                            receiveResult.message.addParameter(true);
                                        }else{
                                            receiveResult.message.addParameter(false, "用户名可能已经存在");
                                        }
                                    }
                                    serverUDP.send(receiveResult.message, receiveResult.socketAddress);
                                    break;
                                }
                                case LOGIN:{
                                    receiveResult.message.type = Message.Type.LOGIN_RESULT;
                                    if(receiveResult.message.getParameterSize() != 2){
                                        System.out.println(receiveResult.socketAddress + " udp login failed! Parameter error");
                                        receiveResult.message.clearParameter();
                                        receiveResult.message.addParameter(false, "Parameter error");
                                    }else{
                                        String username = (String)receiveResult.message.getParameter(0);
                                        String password = (String)receiveResult.message.getParameter(1);
                                        receiveResult.message.clearParameter();
                                        if(db_users.verify(username, password)){
                                            if(online_users.isLogin(username)){
                                                receiveResult.message.addParameter(false, "用户名已经被登录");
                                            }else{
                                                User user = new User();
                                                user.setUsername(username);
                                                user.setUDPInfo(receiveResult.socketAddress);
                                                online_users.login(user);
                                                System.out.println(username + " login success");
                                                receiveResult.message.addParameter(true);
                                            }
                                        }else{
                                            receiveResult.message.addParameter(false, "用户名或密码错误");
                                        }
                                    }
                                    serverUDP.send(receiveResult.message, receiveResult.socketAddress);
                                    break;
                                }
                                case GAME_CREATE:{
                                    receiveResult.message.type = Message.Type.GAME_CREATE_RESULT;
                                    if(receiveResult.message.getParameterSize() != 1){
                                        System.out.println(receiveResult.socketAddress + " createGame failed! Parameter error");
                                        receiveResult.message.clearParameter();
                                        receiveResult.message.addParameter(false, "Parameter error");
                                    }else{
                                        String username = (String)receiveResult.message.getParameter(0);
                                        receiveResult.message.clearParameter();
                                        if(online_users.isLogin(username)){
                                            User user = online_users.getUser(username);
                                            Game game = new Game(new TimeOut());
                                            if(! game.join(user)){
                                                receiveResult.message.addParameter(false, "join game unknown error");
                                            }else{
                                                games.put(game.getGameID(), game);
                                                user.setClientStatus(User.Status.CHESS_WAIT);
                                                receiveResult.message.addParameter(true, game.getGameID());
                                                serverUDP.send(receiveResult.message, receiveResult.socketAddress);
                                                try{
                                                    Thread.sleep(100);
                                                }catch (InterruptedException e){
                                                    e.printStackTrace();
                                                }
                                                broadcastGameInfo(game.getGameID());
                                            }
                                        }else{
                                            receiveResult.message.addParameter(false, "尚未登录");
                                        }
                       
                                    }
                                    serverUDP.send(receiveResult.message, receiveResult.socketAddress);
                                    break;
                                }
                                case LOGOUT:{
                                    if(receiveResult.message.getParameterSize() != 1){
                                        System.out.println("LOGOUT 参数缺失");
                                        break;
                                    }
                                    String username = (String)receiveResult.message.getParameter(0);
                                    online_users.logout(username);
                                    System.out.println(username + " leave");
                                    break;
                                }
                                case CLIENT_LEAVE_TABLE:{
                                    receiveResult.message.type = Message.Type.CLIENT_LEAVE_TABLE_RESULT;
                                    if(receiveResult.message.getParameterSize() != 2){
                                        System.out.println("CLIENT_LEAVE_TABLE 参数缺失");
                                        receiveResult.message.clearParameter();
                                        receiveResult.message.addParameter(false, "CLIENT_LEAVE_TABLE 参数缺失");
                                        serverUDP.send(receiveResult.message, receiveResult.socketAddress);
                                        break;
                                    }
                                    int id = (Integer) receiveResult.message.getParameter(0);
                                    String username = (String) receiveResult.message.getParameter(1);
                                    receiveResult.message.clearParameter();
                                    if(! games.containsKey(id)){
                                        System.out.println("CLIENT_LEAVE_TABLE ID 缺失");
                                        receiveResult.message.clearParameter();
                                        receiveResult.message.addParameter(false, "CLIENT_LEAVE_TABLE ID 缺失");
                                        serverUDP.send(receiveResult.message, receiveResult.socketAddress);
                                        break;
                                    }
                                    
                                    if(! online_users.isLogin(username)){
                                        System.out.println("CLIENT_LEAVE_TABLE 尚未登录");
                                        receiveResult.message.clearParameter();
                                        receiveResult.message.addParameter(false, "CLIENT_LEAVE_TABLE 游戏名  缺失");
                                        serverUDP.send(receiveResult.message, receiveResult.socketAddress);
                                        break;
                                    }
                                    Game game = games.get(id);
                                    game.leave(username);
                                    System.out.println(username + " leave table: " + id);
                                    if(online_users.isLogin(username)){
                                        online_users.getUser(username).setClientStatus(User.Status.IN_HALL);
                                        receiveResult.message.addParameter(true);
                                    }else{
                                        receiveResult.message.addParameter(false, "尚未登录");
                                    }
                                    serverUDP.send(receiveResult.message, receiveResult.socketAddress);
                                    if(game.getPlayerCount() == 0){
                                        broadcastGameClose(game);
                                        games.remove(id);
                                        System.out.println("table: " + id + " remove because user num is 0");
                                    }else{
                                        broadcastGameInfo(game.getGameID());
                                    }
                                    break;
                                }
                                case CLIENT_JOIN_TABLE:{
                                    receiveResult.message.type = Message.Type.CLIENT_JOIN_TABLE_RESULT;
                                    if(receiveResult.message.getParameterSize() != 2){
                                        System.out.println("CLIENT_JOIN_TABLE 参数缺失");
                                        receiveResult.message.clearParameter();
                                        receiveResult.message.addParameter(false, "CLIENT_JOIN_TABLE 参数缺失");
                                        serverUDP.send(receiveResult.message, receiveResult.socketAddress);
                                        break;
                                    }
                                    int id = (Integer) receiveResult.message.getParameter(0);
                                    String username = (String) receiveResult.message.getParameter(1);
                                    receiveResult.message.clearParameter();
                                    if(! games.containsKey(id)){
                                        System.out.println("CLIENT_JOIN_TABLE ID 缺失");
                                        receiveResult.message.addParameter(false, "CLIENT_JOIN_TABLE ID 缺失");
                                        serverUDP.send(receiveResult.message, receiveResult.socketAddress);
                                        break;
                                    }
                                    if(online_users.isLogin(username)){
                                        Game game = games.get(id);
                                        User user = online_users.getUser(username);
                                        if(game.join(user)){
                                            receiveResult.message.addParameter(true, id);
                                            user.setClientStatus(User.Status.CHESS_WAIT);
                                            broadcastGameInfo(id);
                                        }else{
                                            receiveResult.message.addParameter(false, "棋局人数已满");
                                        }
                                    }else{
                                        receiveResult.message.addParameter(false, "尚未登录");
                                    }
                                    serverUDP.send(receiveResult.message, receiveResult.socketAddress);
                                    break;
                                }
                                case CLIENT_WATCH_TABLE:{
                                    receiveResult.message.type = Message.Type.CLIENT_WATCH_TABLE_RESULT;
                                    if(receiveResult.message.getParameterSize() != 2){
                                        System.out.println("CLIENT_WATCH_TABLE 参数缺失");
                                        receiveResult.message.clearParameter();
                                        receiveResult.message.addParameter(false, "参数缺失");
                                    }else{
                                        int id = (Integer) receiveResult.message.getParameter(0);
                                        String username = (String) receiveResult.message.getParameter(1);
                                        receiveResult.message.clearParameter();
                                        if(! games.containsKey(id)){
                                            System.out.println("CLIENT_WATCH_TABLE ID 缺失");
                                            receiveResult.message.addParameter(false, "ID缺失");
                                        }else if(! online_users.isLogin(username)){
                                            System.out.println("CLIENT_WATCH_TABLE 尚未登录");
                                            receiveResult.message.addParameter(false, "游戏名缺失");
                                        }else{
                                            Game game = games.get(id);
                                            User user = online_users.getUser(username);
                                            boolean re = game.watch(user);
                                            receiveResult.message.addParameter(re, re ? id : "观战失败");
                                            if(re){
                                                user.setClientStatus(User.Status.IN_WATCH);
                                                broadcastGameInfo(id);
                                            }
                                        }
                                    }
                                    serverUDP.send(receiveResult.message, receiveResult.socketAddress);
                                    break;
                                }
                                case CLIENT_GAME_STATUS:{
                                    receiveResult.message.type = Message.Type.CLIENT_GAME_STATUS_RESULT;
                                    if(receiveResult.message.getParameterSize() != 3){
                                        System.out.println("CLIENT_GAME_READY 参数缺失");
                                        receiveResult.message.clearParameter();
                                        receiveResult.message.addParameter(false, "参数缺失");
                                    }else{
                                        int id = (Integer) receiveResult.message.getParameter(0);
                                        String username = (String) receiveResult.message.getParameter(1);
                                        Boolean status = (Boolean) receiveResult.message.getParameter(2);
                                        receiveResult.message.clearParameter();
                                        if(! games.containsKey(id)){
                                            System.out.println("CLIENT_GAME_READY ID 缺失");
                                            receiveResult.message.addParameter(false, "ID缺失");
                                        }else if(! online_users.isLogin(username)){
                                            System.out.println("CLIENT_GAME_READY 游戏名 缺失");
                                            receiveResult.message.addParameter(false, "ID缺失");
                                        }else{
                                            Game game = games.get(id);
                                            User user = online_users.getUser(username);
                                            if(game.gameStatus == Game.GameStatus.WAIT){
                                                game.playerStatus(user, status);
                                                receiveResult.message.addParameter(true);
                                                broadcastGameInfo(id);
                                                if(game.gameStatus == Game.GameStatus.PLAYING){
                                                    game.switchCountDown();
                                                    broadcastChessPoll(game, game.blackSize);
                                                }
                                            }
                                        }
                                    }
                                    serverUDP.send(receiveResult.message, receiveResult.socketAddress);
                                    break;
                                }
                                case GAME_CHAT:{
                                    if(receiveResult.message.getParameterSize() != 3){
                                        System.out.println("GAME_CHAT 缺失参数");
                                        break;
                                    }
                                    int gameID = (int)receiveResult.message.getParameter(0);
                                    if(! games.containsKey(gameID)) {
                                        System.out.println("GAME_CHAT ID 缺失");
                                        break;
                                    }
                                    Game game = games.get(gameID);
                                    broadcastGameChatMessage(game, (String)receiveResult.message.getParameter(1), (String)receiveResult.message.getParameter(2));
                                }
                                case CLIENT_CHESS:{
                                    receiveResult.message.type = Message.Type.CLIENT_CHESS_RESULT;
                                    if(receiveResult.message.getParameterSize() != 4){
                                        receiveResult.message.clearParameter();
                                        receiveResult.message.addParameter(false, "CLIENT_CHESS 参数缺失");
                                    }else {
                                        int id = (Integer) receiveResult.message.getParameter(0);
                                        String username = (String) receiveResult.message.getParameter(1);
                                        int x = (int) receiveResult.message.getParameter(2);
                                        int y = (int) receiveResult.message.getParameter(3);
                                        receiveResult.message.clearParameter();
                                        if(! games.containsKey(id)){
                                            System.out.println("CLIENT_CHESS gameID 不存在");
                                            receiveResult.message.addParameter(false, "gameID不存在");
                                        }else if(! online_users.isLogin(username)){
                                            System.out.println("CLIENT_CHESS 尚未登录");
                                            receiveResult.message.addParameter(false, "尚未登录");
                                        }else{
                                            Game game = games.get(id);
                                            if(game.isAllowChess(username, x, y)){
                                                broadGameChess(game, username, x, y);
                                                game.gameSwitch();
                                                receiveResult.message.addParameter(true);
                                                serverUDP.send(receiveResult.message, receiveResult.socketAddress);
                                                String win = game.clientChessSave(username, x, y);
                                                if(! win.equals("")){
                                                    broadGameWin(game, win);
                                                    game.reset();
                                                }else{
                                                    broadcastChessPoll(game, game.currentChess);
                                                }
                                                break;
                                            }else{
                                                receiveResult.message.addParameter(false, "当前不允许下棋");
                                            }
                                        }
                                    }
                                    serverUDP.send(receiveResult.message, receiveResult.socketAddress);
                                    break;
                                }
                                case GAME_CHESS_DATA:{
                                    receiveResult.message.type = Message.Type.GAME_CHESS_DATA_RESULT;
                                    if(receiveResult.message.getParameterSize() != 1){
                                        receiveResult.message.clearParameter();
                                        receiveResult.message.addParameter(false, "GAME_CHESS_DATA 参数缺失");
                                    }else{
                                        int gameID = (int)receiveResult.message.getParameter(0);
                                        Game game = games.get(gameID);
                                        receiveResult.message.clearParameter();
                                        int[][] chessData = game.getChessData();
                                        ArrayList<Integer> data = new ArrayList<>();
                                        for(int i = 0;i<16;i++){
                                            for(int i2 = 0;i2<16;i2++){
                                                data.add(chessData[i][i2]);
                                            }
                                        }
                                        receiveResult.message.addParameter(data.toString().replace("[","").replace("]","").replace(", ", ""));
                                    }
                                    serverUDP.send(receiveResult.message, receiveResult.socketAddress);
                                    break;
                                }
                                case CLIENT_RUN_AWAY:{
                                    if(receiveResult.message.getParameterSize() != 2){
                                        //receiveResult.message.addParameter("CLIENT_RUN_AWAY 参数缺失");
                                        System.out.println("CLIENT_RUN_AWAY 参数缺失");
                                        break;
                                    }
                                    int gameID = (int)receiveResult.message.getParameter(0);
                                    String username = (String)receiveResult.message.getParameter(1);
                                    if(!games.containsKey(gameID)){
                                        //receiveResult.message.addParameter("CLIENT_RUN_AWAY gameID不存在");
                                        System.out.println("CLIENT_RUN_AWAY gameID不存在");
                                        break;
                                    }
                                    if(! online_users.isLogin(username)) {
                                        System.out.println("CLIENT_RUN_AWAY 尚未登录");
                                        break;
                                    }
                                    Game game = games.get(gameID);
                                    game.reset();
                                    game.leave(username);
                                    broadGameWin(game, username + " 逃跑");
                                    broadcastGameInfo(game.getGameID());
                                    break;
                                }
                                case GAME_ADMIN_DEFEAT: {
                                    if(receiveResult.message.getParameterSize() != 2){
                                        System.out.println("GAME_ADMIN_DEFEAT 参数缺失");
                                        break;
                                    }
                                    int gameID = (int)receiveResult.message.getParameter(0);
                                    String username = (String)receiveResult.message.getParameter(1);
                                    if(! games.containsKey(gameID)){
                                        System.out.println("GAME_ADMIN_DEFEAT gameID不存在");
                                        break;
                                    }
                                    Game game = games.get(gameID);
                                    if(game.getPlayerRole(username) != Game.UserRole.PLAYER){
                                        System.out.println("GAME_ADMIN_DEFEAT 不是玩家");
                                        break;
                                    }
                                    broadGameWin(game, username + "认输");
                                    game.reset();
                                    break;
                                }
                                case GAME_SUMMATION: {

                                }
                                case CLIENT_PRIVATE_CHAT: {
                                    if(receiveResult.message.getParameterSize() != 3){
                                        System.out.println("CLIENT_PRIVATE_CHAT 参数缺失");
                                        break;
                                    }
                                    String username = (String)receiveResult.message.getParameter(0);
                                    int gameID = (int) receiveResult.message.getParameter(1);
                                    String msg = (String)receiveResult.message.getParameter(2);
                                    if(! online_users.isLogin(username)){
                                        System.out.println("CLIENT_PRIVATE_CHAT 尚未登录");
                                        break;
                                    }
                                    if(! games.containsKey(gameID)){
                                        System.out.println("CLIENT_PRIVATE_CHAT gameID不存在");
                                        break;
                                    }
                                    Game game = games.get(gameID);
                                    if(game.getPlayerRole(username) != Game.UserRole.PLAYER){
                                        System.out.println("CLIENT_PRIVATE_CHAT 非玩家");
                                        break;
                                    }
                                    game.forEachPlayer((user)->{
                                        if(! user.getUsername().equals(username)){
                                            Message message = new Message();
                                            message.type = Message.Type.CLIENT_PRIVATE_CHAT;
                                            message.addParameter(username, gameID, msg);
                                            serverUDP.send(message, user.getClientAddress());
                                        }
                                    });
                                    break;
                                }
                                case CLIENT_CHALLENGE: {
                                    if(receiveResult.message.getParameterSize() != 2){
                                        System.out.println("CLIENT_CHALLENGE 参数缺失");
                                        break;
                                    }
                                    String request = (String)receiveResult.message.getParameter(0);
                                    String toWho = (String)receiveResult.message.getParameter(1);
                                    if(! online_users.isLogin(request)){
                                        System.out.println("CLIENT_CHALLENGE 用户未登录");
                                        break;
                                    }
                                    if(! online_users.isLogin(toWho)){
                                        System.out.println("CLIENT_CHALLENGE 被挑战用户未登录");
                                        break;
                                    }

                                    if(challenge.containsKey(request)){
                                        if(System.currentTimeMillis() - challenge.get(request).time < 15000){
                                            receiveResult.message.type = Message.Type.CLIENT_CHALLENGE_RESULT;
                                            receiveResult.message.clearParameter();
                                            receiveResult.message.addParameter(false, "您已挑战过其他玩家, 若玩家未回复，请15秒再次尝试");
                                            serverUDP.send(receiveResult.message, receiveResult.socketAddress);
                                            break;
                                        }else{
                                            challenge.remove(request);
                                        }
                                    }
                                    challenge.put(request, new ChallengeInfo(request, toWho));
                                    User user = online_users.getUser(toWho);
                                    Message message = new Message();
                                    message.type = Message.Type.CLIENT_CHALLENGE;
                                    message.addParameter(request, toWho);
                                    serverUDP.send(message, user.getClientAddress());
                                    System.out.println("CLIENT_CHALLENGE 发出挑战");

                                    receiveResult.message.type = Message.Type.CLIENT_CHALLENGE_RESULT;
                                    receiveResult.message.clearParameter();
                                    receiveResult.message.addParameter(true);
                                    serverUDP.send(receiveResult.message, receiveResult.socketAddress);
                                    break;
                                }
                                case CLIENT_CHALLENGE_RESPOND:{
                                    if(receiveResult.message.getParameterSize() != 3){
                                        System.out.println("CLIENT_CHALLENGE_RESPOND 参数缺失");
                                        break;
                                    }
                                    String request = (String)receiveResult.message.getParameter(0);
                                    String toWho = (String)receiveResult.message.getParameter(1);
                                    Boolean result = (Boolean)receiveResult.message.getParameter(2);
                                    ChallengeInfo challengeInfo = challenge.get(request);
                                    if(challengeInfo == null){
                                        System.out.println("CLIENT_CHALLENGE_RESPOND 挑战不存在");
                                        break;
                                    }
                                    if(System.currentTimeMillis() - challengeInfo.time > 15000){
                                        System.out.println("CLIENT_CHALLENGE_RESPOND 挑战超时");
                                        break;
                                    }
                                    System.out.println("CLIENT_CHALLENGE_RESPOND 挑战已被 " + result);
                                    if(result){
                                        Game game = new Game(new TimeOut());
                                        User user1 = online_users.getUser(request);
                                        User user2 = online_users.getUser(toWho);
                                        game.join(user1);
                                        game.join(user2);
                                        games.put(game.getGameID(), game);
                                        Message message = new Message();
                                        message.addParameter(game.getGameID());
                                        message.type = Message.Type.CLIENT_CHALLENGE_BEGIN;
                                        serverUDP.send(message, user1.getClientAddress());
                                        serverUDP.send(message, user2.getClientAddress());
                                        broadcastGameInfo(game.getGameID());
                                    }else{
                                        Message message = new Message();
                                        message.addParameter(-1);
                                        message.type = Message.Type.CLIENT_CHALLENGE_BEGIN;
                                        serverUDP.send(message, online_users.getUser(request).getClientAddress());
                                    }
                                    challenge.remove(request);
                                    break;
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
    private class BroadcastUserList extends Thread {
        @Override
        public void run() {
            while (true){
                Message message = new Message();
                message.type = Message.Type.BROADCAST_USER_LIST;
                ArrayList<String> result = new ArrayList<>();
                for(String user : online_users.getUserList()){
                    result.add(user);
                    result.add("空闲");
                }
                games.forEachValue(1, (game)-> game.forEachPlayer((user)->{
                    int index = result.indexOf(user.getUsername());
                    if(index != -1){
                        result.set(index + 1, "游戏中");
                    }
                }));
                message.addParameterFromList(result);
                online_users.forEachUser((user) -> {
                    serverUDP.send(message, user.getClientAddress(), UDP.TransmissionType.NOT_WAIT);
                    message.reSetID();
                });
                try{
                    Thread.sleep(2500);
                }catch (InterruptedException e){
                    e.printStackTrace();
                    break;
                }

            }
        }
    }
	private class HeartBeat extends Thread{
        @Override
        public void run() {
            ArrayList<String> timeOutUser = new ArrayList<>();
            while(true){
                timeOutUser.clear();
                long currentTime = System.currentTimeMillis();
                online_users.forEachUser(user -> {
                    if(currentTime - user.getHeartbeat() > HEARTBEAT_TIMEOUT){
                        String username = user.getUsername();
                        timeOutUser.add(username);
                        System.out.println(username+ ": timeOut");
                        online_users.logout(username);
                    }
                });
                games.forEachValue(1, game -> {
                    Iterator<String> iterator = timeOutUser.iterator();
                    while(iterator.hasNext()){
                        String username = iterator.next();
                        Game.UserRole userRole = game.getPlayerRole(username);
                        switch (userRole){
                            case PLAYER:{
                                if(game.gameStatus == Game.GameStatus.PLAYING){
                                    broadGameWin(game, username + " 逃跑");
                                    game.reset();
                                }
                                game.leave(username);
                                if(game.getPlayerCount() == 0){
                                    games.remove(game.getGameID());
                                    System.out.println("table: " + game.getGameID() + " remove because user num is 0");
                                }
                                iterator.remove();
                                break;
                            }
                            case WATCHER:{
                                game.leave(username);
                                iterator.remove();
                                break;
                            }
                        }
                    }
                });
                try{
                    Thread.sleep(5000);
                }catch (InterruptedException e){
                    e.printStackTrace();
                    break;
                }
            }
        }
    }
	private void broadcastGameChatMessage(Game game, String username, String msg){
	    /*
	    * 因为有点懒，所以判断username在不在Game里就不写了
	    * */
        Message message = new Message();
        message.type = Message.Type.GAME_CHAT;
        message.addParameter(username, msg);
        game.forEachUser(user -> serverUDP.send(message, user.getClientAddress()));
    }
	private void broadcastGameClose(Game game){
        Message message = new Message();
        message.type = Message.Type.GAME_CLOSE;
        message.addParameter(game.getGameID());
        game.forEachWatcher(watcher -> serverUDP.send(message, watcher.getClientAddress()));
    }
	private Message gameInfoToMessage(int gameID){
		Game game = games.get(gameID);
        Message message = new Message();
        message.type = Message.Type.BROADCAST_GAME_INFO;
        message.addParameter(game.gameStatus != Game.GameStatus.WAIT);
        game.forEachPlayer(player->{
            message.addParameter(player.getUsername());
            message.addParameter(player.getClientStatus() == User.Status.CHESS_READY || player.getClientStatus() == User.Status.CHESS_PLAYING_BLACK);
            message.addParameter(game.getPlayerTime(player.getUsername()));
        });
		if(message.getParameterSize() == 4){
            message.addParameter(null, null, null);
		}
		game.forEachWatcher(watcher -> message.addParameter(watcher.getUsername()));
        return message;
	}
	private void broadcastGameInfo(int gameID){
        Game game = games.get(gameID);
        game.forEachUser(user -> serverUDP.send(gameInfoToMessage(gameID), user.getClientAddress()));
    }
    private void broadcastChessPoll(Game game, String username){
        Message message = new Message();
        message.type = Message.Type.BROADCAST_CHESS_POLL;
        message.addParameter(username);
        game.forEachUser(user -> serverUDP.send(message, user.getClientAddress()));
    }
    private void broadGameChess(Game game, String username, int x, int y){
	    Message message = new Message();
	    message.type = Message.Type.BROADCAST_CLIENT_CHESS;
        message.addParameter(game.getGameID(), x, y);
        game.forEachUser(user -> {
            if(!user.getUsername().equals(username))
                serverUDP.send(message, user.getClientAddress());
        });
    }
    private void broadGameWin(Game game, String who){
        Message message = new Message();
        message.type = Message.Type.BROADCAST_CHESS_WIN;
        message.addParameter(game.getGameID(), who);
        game.forEachUser(user -> serverUDP.send(message, user.getClientAddress()));
    }
	private class BroadcastGameList extends Thread {
		@Override
		public void run() {
			while (true){
                Message message = new Message();
                message.type = Message.Type.Broadcast_GameList;
				games.forEach((gameID, game) -> {
                    GameInfo gameInfo = new GameInfo();
                    game.forEachPlayer(play -> gameInfo.addPlayers(play.getUsername()));
                    gameInfo.setGameID(game.getGameID());
                    message.addParameter(gameInfo);
                });
				online_users.forEachUser((user) -> {
                    serverUDP.send(message, user.getClientAddress(), UDP.TransmissionType.NOT_WAIT);
                    message.reSetID();
                });
				try{
					Thread.sleep(2500);
				}catch (InterruptedException e){
					e.printStackTrace();
					break;
				}

			}
		}
	}
    private class TimeOut implements ITimeOut{
        @Override
        public void timeOut(String username, int gameID) {
            Game game = games.get(gameID);
            if(game == null){
                System.out.println("timeOut gameID 不存在");
                return;
            }
            StringHolder second = new StringHolder();
            if(game.getPlayerRole(username) == Game.UserRole.PLAYER){
                game.forEachPlayer(player -> {
                    if(!player.getUsername().equals(username)){
                        second.value = player.getUsername();
                    }
                });
                broadGameWin(game, second.value);
            }else{
                System.out.println("timeOut gameID 不是玩家");
            }
        }
    }
    private class ChallengeInfo{
        ChallengeInfo(String challenger, String toWho){
            this.challenger = challenger;
            this.toWho = toWho;
            this.time = System.currentTimeMillis();
        }
        long time;
	    String challenger;
	    String toWho;
    }
	public static void main(String[] args){
		new Server();
	}
}
