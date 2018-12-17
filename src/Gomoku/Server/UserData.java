package Gomoku.Server;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.ConcurrentHashMap;

class UserData {
	private ConcurrentHashMap<String, String> users = null;
	@SuppressWarnings("unchecked")
	UserData(){
		try{
			//读取用户文件数据
			FileInputStream fileIn = new FileInputStream("userData.ser");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			Object data = in.readObject();
			in.close();
			fileIn.close();
			if(data instanceof ConcurrentHashMap){
				users = (ConcurrentHashMap<String, String>)data;
			}else{
				users = new ConcurrentHashMap<>();
			}
		}catch(IOException | ClassCastException | ClassNotFoundException i){
			System.out.println("new UserData");
			users = new ConcurrentHashMap<>();
		}
	}
	boolean verify(String username, String password){ //验证用户名密码是否正确，用户名不存在返回false
		System.out.println(username + ", " + password);
		if(users.containsKey(username)){
			return users.get(username).equals(password);
		}
		return false;
	}
	boolean registered(String username, String password){ //用户注册，先判断用户名是否存在，然后保存到文件
		System.out.println(username + ", " + password);
		if(users.containsKey(username)) {  //判断用户是否存在
			//JOptionPane.showMessageDialog(null, "该用户名已存在", "提示", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		else {
			users.put(username, password);
			try{
				//若用户不存在，将用户密码添加到文件
				FileOutputStream fileOut = new FileOutputStream("userData.ser");
				ObjectOutputStream out = new ObjectOutputStream(fileOut);
				out.writeObject(users);
				out.close();
				fileOut.close();
				return true;
			}
			catch(IOException i)
			{
				i.printStackTrace();
				return false;
			}
		}
	}
}

