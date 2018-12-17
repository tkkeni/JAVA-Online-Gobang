package Gomoku.Server;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

class UserList { //在线用户
	private ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>(); //线程安全的哈希表，用户名为键，保存User对象
	UserList(){
		
	}
	void login(User user){ //客户登录
		users.put(user.getUsername(), user);
	}
	void logout(String user){
		users.remove(user);
	}
	boolean isLogin(String user){ //用户是否存在
		return users.containsKey(user);
	}
	User getUser(String user){
		return users.get(user);
	}
	ArrayList<String> getUserList(){ //返回所有在线的玩家
		ArrayList<String> userList = new ArrayList<>();
		users.forEachKey(1, userList::add);
		return userList;
	}
	void forEachUser(Consumer<User> action){
		for(Map.Entry<String, User> entry : users.entrySet()){
			action.accept(entry.getValue());
		}
	}
}
