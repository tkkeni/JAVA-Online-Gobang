package Gomoku.Transmission;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketException;


public class ServerUDP extends UDP {
	private ServerSecurity serverSecurity;
	public ServerUDP(int port) throws SocketException {
		/*
		 * 端口为0代表客户端
		 * */
		super(port);
		try{
			serverSecurity = new ServerSecurity();
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	private byte[] hexToBytes(String str) {
		if(str == null || str.trim().equals("")) {
			return new byte[0];
		}
		byte[] bytes = new byte[str.length() / 2];
		for(int i = 0; i < str.length() / 2; i++) {
			String subStr = str.substring(i * 2, i * 2 + 2);
			bytes[i] = (byte) Integer.parseInt(subStr, 16);
		}
		return bytes;
	}
	protected byte[] beforeSend(byte[] msg, InetSocketAddress socketAddress){
		byte[] t_msg = serverSecurity.encrypt(socketAddress, msg);
		return t_msg;
	}
	protected Message afterReceive(byte[] msg, int length, InetSocketAddress socketAddress){
		try{
			ByteArrayInputStream byteIn = new ByteArrayInputStream(msg);
			ObjectInputStream objectIn = new ObjectInputStream(byteIn);
			Message message = (Message)objectIn.readObject();
			if(message.type == Message.Type.HELLO){
				if(message.getParameterSize() != 1){
					System.out.println("HELLO 参数缺失");
					return null;
				}
				byte[] key = serverSecurity.pkey_decrypt(hexToBytes((String)message.getParameter(0)));
				System.out.println(socketAddress + " key length: " + key.length);
				serverSecurity.saveKey(socketAddress, key);
				return message;
			}else{
				System.out.println("未加密数据包: " + message.type.toString());
				return null;
			}
		}catch (StreamCorruptedException | ClassNotFoundException e){
			//密文无法直接转换为对象
			byte[] t = serverSecurity.decrypt(socketAddress, msg, length);
			if(t == null){
				System.out.println("未找到密钥");
				return null;
			}else{
				ByteArrayInputStream byteIn = new ByteArrayInputStream(t);
				try{
					ObjectInputStream objectIn = new ObjectInputStream(byteIn);
					return (Message)objectIn.readObject();
				}catch (IOException | ClassNotFoundException err){
					err.printStackTrace();
					return null;
				}
			}
		}catch (ClassCastException e){
			System.out.println("HELLO 参数错误");
			return null;
		}catch (IOException e){
			e.printStackTrace();
			return null;
		}
	}
}
