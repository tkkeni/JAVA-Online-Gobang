package Gomoku.Server;

import org.omg.CORBA.LongHolder;

/*
* 计时统一管理
* */
public class Timer {
	private final static LongHolder ID = new LongHolder(0); //类静态变量，是该类的所有实例共同拥有的
	private long id = -1;//本Timer实例的ID
	private long remaining = -1;//剩余时间 单位秒
	private boolean timing = false;//开始计时
	private ITimeOut iTimeOut;//超时执行的操作的接口
	private String username;//本计时器的拥有者
	private int gameID;//本计时器对应的Game
	Timer(ITimeOut iTimeOut, String username, int gameID){
		this.iTimeOut = iTimeOut;
		this.username = username;
		this.gameID = gameID;
		synchronized (Timer.ID){
			this.id = ID.value++;
		}
	}

	public String getUsername() {
		return username;
	}

	void isTiming(boolean timing){
		this.timing = timing;
	}
	long getId() {
		return id;
	}
	void setRemaining(long remaining){ //设置剩余时间 单位秒
		this.remaining = remaining;
	}
	long getRemaining() {
		return remaining;
	} //获取剩余时间

	boolean countDown(){ //剩余时间减一，时间到调用iTimeOut接口
		if(this.remaining <= 0){
			this.iTimeOut.timeOut(username, gameID);
			return false;
		}
		if(this.timing){
			this.remaining -= 1;
		}
		return true;
	}
}
