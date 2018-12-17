package Gomoku.Transmission;

import sun.dc.pr.PRError;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Arrays;

public class ClientUDP extends UDP {
	private static final char[] HEX_CHAR = {'0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
	private ClientSecurity clientSecurity;
	private Status status = Status.HANDSHAKE;
	public ClientUDP() throws SocketException {
		super(0);
		try{
			clientSecurity = new ClientSecurity();
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	private String bytesToHex(byte[] bytes) {
		char[] buf = new char[bytes.length * 2];
		int index = 0;
		for(byte b : bytes) { // 利用位运算进行转换，可以看作方法一的变种
			buf[index++] = HEX_CHAR[b >>> 4 & 0xf];
			buf[index++] = HEX_CHAR[b & 0xf];
		}

		return new String(buf);
	}
	public boolean handshake(InetSocketAddress socketAddress){
		Message message = new Message();
		message.type = Message.Type.HELLO;
		byte[] key = clientSecurity.generateKey();
		message.addParameter(bytesToHex(key));
		ReceiveResult receiveResult = sendWaitResponse(message, socketAddress, 3000);
		if(receiveResult != null){
			if(receiveResult.message.getParameterSize() == 1 && receiveResult.message.getParameter(0).equals("Hello")){
				status = Status.SECURITY;
				System.out.println("握手成功");
				return true;
			}
			System.out.println("握手失败#0");
			return false;
		}
		System.out.println("握手失败#1");
		return false;
	}
	protected byte[] beforeSend(byte[] msg, InetSocketAddress socketAddress){

		if(status == Status.HANDSHAKE){
			return msg;
		}else{
			return clientSecurity.encrypt(msg);
		}
	}
	protected Message afterReceive(byte[] msg, int length, InetSocketAddress socketAddress){
		try{
			ByteArrayInputStream byteIn = new ByteArrayInputStream(clientSecurity.decrypt(msg, length));
			ObjectInputStream objectIn = new ObjectInputStream(byteIn);
			return (Message)objectIn.readObject();
		}catch (IOException | ClassNotFoundException e){
			System.out.println("反序列化失败，可能是解密出现问题");
			return null;
		}
	}
	enum Status{
		HANDSHAKE, SECURITY
	}
}
