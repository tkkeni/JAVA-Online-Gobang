package Gomoku.Server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TimerManagement { //管理所有的计时器对象
	private ConcurrentHashMap<Long, Timer> timers = new ConcurrentHashMap<>(); //线程安全的HashMap
	public TimerManagement(){
		new CountDown().start();//新开线程处理所有的计时器操作
	}
	public void add(Timer timer){
		timers.put(timer.getId(), timer);
		//新的计时器加入
	}
	void remove(long id){
		timers.remove(id);
		//根据id移除制定的计时器
	}
	public long getRemaining(long id){
		if(timers.containsKey(id)){
			return timers.get(id).getRemaining();
		}else{
			return -1;
		}
		//获取指定计时器的剩余时间
	}
	private class CountDown extends Thread{
		@Override
		public void run() {
			Timer timer;
			while(true){
				for (Map.Entry<Long, Timer> entry : timers.entrySet()) { //遍历所有计时器对象
					timer = entry.getValue();
					if(! timer.countDown()){
						System.out.println(entry.getKey() + " : timer timeOut");
						timers.remove(entry.getKey());
					}
				}
				try{
					Thread.sleep(999);
				}catch (InterruptedException e){
					e.printStackTrace();
				}
			}
		}
	}
}
