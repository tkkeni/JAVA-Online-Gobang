package Gomoku.Server;

import Gomoku.Transmission.GameInfo;
import org.omg.CORBA.BooleanHolder;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.concurrent.ConcurrentHashMap;


public class Game extends GameInfo {
	private static int GameCount = 0;
	private static TimerManagement timerManagement = new TimerManagement();
	private ConcurrentHashMap <String, User> watchers = new ConcurrentHashMap<>();
	private Vector<User> players = new Vector<>();
	private ConcurrentHashMap <String, Timer> countDown = new ConcurrentHashMap<>();
	GameStatus gameStatus = GameStatus.WAIT;
	String currentChess = "";
	String blackSize = "";
	private ITimeOut iTimeOut;
	private int[][] chessData;
	Game(ITimeOut iTimeOut){
		this.iTimeOut = iTimeOut;
		this.gameID = GameCount++;
		chessData = new int[15 + 1][15 + 1];
	}

	String clientChessSave(String username, int x, int y){
		synchronized (this){
			chessData[x][y] = username.equals(blackSize) ? 1 : 2;
			if (Judgement.judge(chessData) == 1)
				return "黑方胜";
			else if (Judgement.judge(chessData) == 2)
				return "白方胜";
			else
				return "";
		}
	}

	int[][] getChessData() {
		synchronized (this){
			return chessData;
		}
	}
	UserRole getPlayerRole(String username){
		for(User player : players){
			if(player.getUsername().equals(username)){
				return UserRole.PLAYER;
			}
		}
		if(watchers.containsKey(username)){
			return UserRole.WATCHER;
		}
		return UserRole.NOT_EXIST;
	}
	public boolean isCanJoin(){
		return players.size() == 2;
	}
	int getPlayerCount(){
		return players.size();
	}
	void forEachUser(Consumer<User> action){
		forEachPlayer(action);
		forEachWatcher(action);
	}
	void forEachPlayer(Consumer<User> action){
		for(User user : players){
			action.accept(user);
		}
	}
	void forEachWatcher(Consumer<User> action){
		watchers.forEachValue(1, action);
	}
	boolean join(User user){
		if(this.players.size() == 2){
			return false;
		}
		this.players.add(user);
		return true;
	}
	void leave(String user){
		for(User player : players){
			if(player.getUsername().equals(user)){
				System.out.println(user + ": player leave table " + this.gameID);
				this.players.remove(player);
				return;
			}
		}
		if(this.watchers.containsKey(user)){
			System.out.println(user + ": player leave table " + this.gameID);
			this.watchers.remove(user);
		}else{
			System.out.println(user + ": not in table " + this.gameID);
		}
	}
	boolean watch(User user){
		this.players.remove(user);
		this.watchers.remove(user.getUsername());
		this.watchers.put(user.getUsername(), user);
		return true;
	}
	void playerStatus(User user, Boolean isReady){
		if(players.contains(user)){
			user.setClientStatus(isReady ? User.Status.CHESS_READY : User.Status.CHESS_WAIT);
		}else{
			return;
		}
		if(players.size() == 2){
			final BooleanHolder allReady = new BooleanHolder(true);
			forEachPlayer(play -> {
				allReady.value = allReady.value && play.getClientStatus() == User.Status.CHESS_READY;
			});
			if(allReady.value){
				countDown.forEachValue(1, timer -> {
					timerManagement.remove(timer.getId());
				});
				countDown.clear();
				forEachPlayer(play -> {
					/*
					* new Timer() 传入ITimeOut
					* */
					Timer timer = new Timer(this.iTimeOut, play.getUsername(), gameID);
					timer.setRemaining(900);
					countDown.put(play.getUsername(), timer);
					play.setClientStatus(User.Status.CHESS_PLAYING_WHITE);
				});
				int random = (int)(Math.random() * 2);
				players.get(random).setClientStatus(User.Status.CHESS_PLAYING_BLACK);
				this.gameStatus = GameStatus.PLAYING;
				blackSize = players.get(random).getUsername();
				System.out.println(gameID + " is playing, black -> " + blackSize);
				currentChess = blackSize;
			}
		}
	}
	void reset(){
		this.gameStatus = GameStatus.WAIT;
		forEachPlayer(player->{
			player.setClientStatus(User.Status.CHESS_WAIT);
		});
		chessData = new int[15 + 1][15 + 1];
	}
	boolean isAllowChess(String username, int x, int y){
		return currentChess.equals(username);
	}
	long getPlayerTime(String username){
		Timer timer = countDown.get(username);
		if(timer == null){
			return 0;
		}
		return timer.getRemaining();
	}
	void gameSwitch(){
		if(this.gameStatus == GameStatus.PLAYING){
			for(User user : players){
				if(!user.getUsername().equals(this.currentChess)){
					this.currentChess = user.getUsername();
					return;
				}
			}
			switchCountDown();
		}
	}
	void switchCountDown(){
		countDown.forEach(1, (user, timer) -> {
			if(user.equals(currentChess)){
				//timer.timing(true);
			}else{
				//timer.timing(false);
			}
		});
	}
	enum GameStatus{
		WAIT, PLAYING
	}
	enum UserRole{
		PLAYER, WATCHER, NOT_EXIST
	}
}