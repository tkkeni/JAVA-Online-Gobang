package Gomoku.Transmission;

import org.omg.CORBA.LongHolder;

import java.util.ArrayList;
import java.util.Collections;

public class Message implements java.io.Serializable{
	public Type type = null;
	private ArrayList<Object> parameter = new ArrayList<>();
	private final static LongHolder ID = new LongHolder(0);
	private long id = -1;
	public Message(){
		synchronized (Message.ID){
			this.id = Message.ID.value++;
		}
	}
	public void addParameter(Object... args){
		Collections.addAll(parameter, args);
	}
	public void addParameterFromList(ArrayList args){
		parameter.addAll(args);
	}
	public void clearParameter(){
		parameter.clear();
	}
	public Object getParameter(int index){
		if(parameter.size() <= index){
			return null;
		}else{
			return parameter.get(index);
		}
	}
	public ArrayList getParameter(){
		return parameter;
	}
	public int getParameterSize(){
		return parameter.size();
	}
	long getId() {
		return id;
	}
	void setID(long id){
		if(this.type != Type.UDP_ACK){
			return;
		}
		this.id = id;
	}
	public void reSetID(){
		synchronized (Message.ID){
			this.id = Message.ID.value++;
		}
	}
	public Message(Type type, ArrayList<Object> parameter){
		this.type = type;
		this.parameter = parameter;
	}
	public enum Type {
		UDP_ACK, HELLO, HELLO_RESULT, REGISTER, REGISTER_RESULT, LOGIN, LOGIN_RESULT, LOGOUT, HEARTBEAT, GAME_CREATE, GAME_CREATE_RESULT, Broadcast_GameList, CLIENT_LEAVE_TABLE,
		CLIENT_LEAVE_TABLE_RESULT, CLIENT_JOIN_TABLE, CLIENT_JOIN_TABLE_RESULT, CLIENT_WATCH_TABLE, CLIENT_WATCH_TABLE_RESULT,
		GAME_CLOSE, CLIENT_GAME_STATUS, CLIENT_GAME_STATUS_RESULT, BROADCAST_GAME_INFO, GAME_CHAT, BROADCAST_CHESS_POLL, CLIENT_CHESS,
		CLIENT_CHESS_RESULT, BROADCAST_CLIENT_CHESS, BROADCAST_CHESS_WIN, GAME_CHESS_DATA, GAME_CHESS_DATA_RESULT, CLIENT_RUN_AWAY,
		GAME_ADMIN_DEFEAT, GAME_SUMMATION, CLIENT_PRIVATE_CHAT, BROADCAST_USER_LIST, CLIENT_CHALLENGE, CLIENT_CHALLENGE_RESULT, CLIENT_CHALLENGE_RESPOND, CLIENT_CHALLENGE_BEGIN
	}
}
