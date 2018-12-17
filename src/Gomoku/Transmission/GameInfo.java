package Gomoku.Transmission;

import Gomoku.Server.User;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Enumeration;

public class GameInfo implements java.io.Serializable {
	private ArrayList<String> players = new ArrayList<>();
	protected int gameID = -1;

	public void setGameID(int gameID) {
		this.gameID = gameID;
	}

	public int getGameID() {
		return gameID;
	}
	public final boolean addPlayers(String username){ //无修饰符 子类不可见
		return players.size()<2 && players.add(username);
	}
	public ArrayList getPlayers() {
		return players;
	}
}
