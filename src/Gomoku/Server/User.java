package Gomoku.Server;

import java.net.*;

public class User {
	private String username = "";
	private InetSocketAddress clientAddress;
	private Status clientStatus = Status.WAIT_UDP;
	private long heartbeat = 0;

	long getHeartbeat() {
		return heartbeat;
	}

	void setHeartbeat(long heartbeat) {
		this.heartbeat = heartbeat;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	void setUDPInfo(InetSocketAddress address){
		this.clientAddress = address;
	}

	InetSocketAddress getClientAddress() {
		return clientAddress;
	}

	Status getClientStatus() {
		return clientStatus;
	}

	void setClientStatus(Status clientStatus) {
		this.clientStatus = clientStatus;
	}

	public String getUsername() {
		return username;
	}

	enum Status {
		WAIT_UDP, IN_HALL, CHESS_WAIT, CHESS_READY, CHESS_PLAYING_BLACK, CHESS_PLAYING_WHITE, IN_WATCH
	}
}
