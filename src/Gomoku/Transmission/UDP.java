package Gomoku.Transmission;

import java.io.*;
import java.net.SocketException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.List;
import java.util.Collections;
public class UDP {
	private DatagramSocket udp;
	private ExecutorService threadPool;
	private ConcurrentHashMap<InetSocketAddress, ConcurrentHashMap<Long, ReceiveResult>> UDPPackage = new ConcurrentHashMap<>();
	private ConcurrentHashMap<InetSocketAddress, List<Long>> UDPPackageWaitACK = new ConcurrentHashMap<>();
	private ConcurrentHashMap<InetSocketAddress, List<Long>> UDPPackageReceive = new ConcurrentHashMap<>();
	private final int UDP_timeOut = 1500;
	private final int UDP_retryCount = 3;
	public UDP(int port) throws SocketException{
		/*
		* 端口为0代表客户端
		* */
		udp = new DatagramSocket(port);
		threadPool = Executors.newCachedThreadPool();
		threadPool.execute(new ThreadUDPReceive());
	}

	public ConcurrentHashMap<InetSocketAddress, ConcurrentHashMap<Long, ReceiveResult>> getUDPPackage() {
		return UDPPackage;
	}

	public void send(Message message, InetSocketAddress socketAddress){
		threadPool.execute(new FutureTask<>(new ReliableTransmission(message, socketAddress, TransmissionType.RELIABLE_NOT_WAIT)));
	}
	public void send(Message message, InetSocketAddress socketAddress, TransmissionType waitType){
		if(waitType == TransmissionType.RELIABLE_WAIT_FOR_Response){
			sendWaitResponse(message, socketAddress, UDP_timeOut);
			return;
		}
		threadPool.execute(new FutureTask<>(new ReliableTransmission(message, socketAddress, waitType)));
	}
	public ReceiveResult sendWaitResponse(Message message, InetSocketAddress socketAddress, int timeOut){
		FutureTask<TransmissionResult> future = new FutureTask<>(new ReliableTransmission(message, socketAddress, TransmissionType.RELIABLE_WAIT_FOR_Response));
		threadPool.execute(future);
		try {
			TransmissionResult transmissionResult = future.get();
			if(! transmissionResult.result){
				System.out.println("sendWaitResponse failed: " + transmissionResult.reason);
				return null;
			}
			FutureTask<ReceiveResult> waitForResponse = new FutureTask<>(new WaitForResponse(socketAddress, message.getId(), timeOut));
			threadPool.execute(waitForResponse);
			//System.out.println(message.type.toString() + " waitForResponse");
			return waitForResponse.get();
		} catch (InterruptedException | ExecutionException e){
			e.printStackTrace();
		}
		return null;
	}
	protected byte[] beforeSend(byte[] msg, InetSocketAddress socketAddress){
		return msg;
	}
	protected Message afterReceive(byte[] msg, int length, InetSocketAddress socketAddress){return null;}
	private boolean udpSend(Message message, InetSocketAddress socketAddress){
		try{
			try{
				Thread.sleep(1);
			}catch (InterruptedException e){
				e.printStackTrace();
			}
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			ObjectOutputStream objectOut = new ObjectOutputStream(byteOut);
			objectOut.writeObject(message);
			byte[] t = beforeSend(byteOut.toByteArray(), socketAddress);
			udp.send(new DatagramPacket(t, t.length, socketAddress));
			return true;
		}catch (IOException e){
			e.printStackTrace();
			return false;
		}
	}

	private class WaitForResponse implements Callable<ReceiveResult>{ //等待指定id的数据包到达
		private long id;
		private int timeOut;
		private InetSocketAddress socketAddress;
		WaitForResponse(InetSocketAddress socketAddress, long id, int timeOut){
			this.id = id;
			this.timeOut = timeOut;
			this.socketAddress = socketAddress;
			if(! UDPPackage.containsKey(socketAddress)){
				UDPPackage.put(socketAddress, new ConcurrentHashMap<>());
			}
		}
		public ReceiveResult call(){
			ReceiveResult receiveResult;
			ConcurrentHashMap<Long, ReceiveResult> map = UDPPackage.get(this.socketAddress);
			while (this.timeOut > 0){
				receiveResult = map.get(Long.MAX_VALUE - this.id);
				if(receiveResult == null){
					try{
						Thread.sleep(10);
						this.timeOut -= 10;
					}catch (InterruptedException e){
						e.printStackTrace();
					}
				}else{
					map.remove(this.id);
					return receiveResult;
				}
			}
			return null;
		}
	}
	private class ThreadUDPReceive extends Thread{
		@Override
		public void run() {
			DatagramPacket pin = new DatagramPacket(new byte[1024],1024);
			System.out.println("ThreadUDPReceive");
			while(true){
				try{
					ReceiveResult result = new ReceiveResult();
					udp.receive(pin);
					result.socketAddress = (InetSocketAddress)pin.getSocketAddress();
					result.message = afterReceive(pin.getData(), pin.getLength(), result.socketAddress);
					if(result.message == null){
						continue;
					}
					if(! UDPPackageReceive.containsKey(result.socketAddress)){
						UDPPackageReceive.put(result.socketAddress, Collections.synchronizedList(new ArrayList<>()));
					}
					if(! UDPPackageWaitACK.containsKey(result.socketAddress)){
						UDPPackageWaitACK.put(result.socketAddress, Collections.synchronizedList(new ArrayList<>()));
					}
					if(! UDPPackage.containsKey(result.socketAddress)){
						UDPPackage.put(result.socketAddress, new ConcurrentHashMap<>());
					}

					List<Long> waitACK = UDPPackageWaitACK.get(result.socketAddress);
					List<Long> packageReceive = UDPPackageReceive.get(result.socketAddress);
					if(result.message.type == Message.Type.UDP_ACK){ //若为ACK包，从UDPPackageWaitACK中去除该ID
						waitACK.remove(result.message.getId());
						//System.out.println("receive ack: " + result.message.getId() + " " + UDPPackageWaitACK.get(result.socketAddress).indexOf(result.message.getId()));
					}else {
						Message ack = new Message();
						ack.type = Message.Type.UDP_ACK;
						ack.setID(result.message.getId());
						udpSend(ack, result.socketAddress);
						//System.out.println("ACK " + ack.getId() + " package: " + result.message.getId() + " " + result.message.type.toString());
						if(!result.message.type.toString().contains("_RESULT")){
							if(packageReceive.indexOf(result.message.getId()) != -1){
								System.out.println(result.socketAddress + " package: " + result.message.getId() + " " + result.message.type.toString() + " 接收到重复的数据包, 可能是ACK包丢失");
								continue;
							}else{
								packageReceive.add(result.message.getId()); //保存已经收到的Message ID
							}
							UDPPackage.get(result.socketAddress).put(result.message.getId(), result); //将收到的Message保存，等待处理
						}else {
							UDPPackage.get(result.socketAddress).put(Long.MAX_VALUE - result.message.getId(), result); //将收到的Message保存，等待处理
						}
					}
					//System.out.println("receive: " + result.message.type.toString());

				}catch (IOException e){
					e.printStackTrace();
					break;
				}
			}

		}
	}
	private class ReliableTransmission implements Callable<TransmissionResult> { //新开线程执行可靠传输相关逻辑
		private Message message;
		private InetSocketAddress socketAddress;
		private TransmissionType transmissionType;
		ReliableTransmission(Message message, InetSocketAddress socketAddress, TransmissionType transmissionType){
			this.message = message;
			this.socketAddress = socketAddress;
			this.transmissionType = transmissionType;
			if(! UDPPackageWaitACK.containsKey(socketAddress)){
				UDPPackageWaitACK.put(socketAddress, Collections.synchronizedList(new ArrayList<>()));
			}
		}
		public TransmissionResult call(){
			TransmissionResult transmissionResult = new TransmissionResult();
			int retryCount = 0;
			try{
				long id = this.message.getId();
				int time;
				List<Long> list = UDPPackageWaitACK.get(this.socketAddress);
				while(retryCount < UDP_retryCount){ //重试次数内
					if(! udpSend(this.message, this.socketAddress)){ //UDP发送
						transmissionResult.result = false;
						transmissionResult.reason = "UDP传输错误！";
						System.out.println("UDP传输错误！");
						return transmissionResult;
					}
					//System.out.println("ReliableTransmission: " + this.message.type.toString());
					if(transmissionType == TransmissionType.NOT_WAIT){
						transmissionResult.result = true;
						return transmissionResult;
					}
					list.add(id); //Message加入ACK等待
					time = 0;
					while(time < UDP_timeOut){
						Thread.sleep(100); //等待50ms
						if(list.indexOf(id) != -1){ //检查等待列表
							time += 100;
						}else{
							System.out.println("package: " + id + " ack");
							transmissionResult.result = true;
							return transmissionResult; //收到ACK，线程退出
						}
					}
					retryCount++; //超时，重试
				}
				transmissionResult.result = false;
				transmissionResult.reason = "所有尝试发送的操作均以失败";
			}catch (InterruptedException e){
				transmissionResult.result = false;
				transmissionResult.reason = "发生异常：" + e.getMessage();
			}
			return transmissionResult;
		}

	}
	public enum TransmissionType{
		RELIABLE_WAIT_FOR_Response, RELIABLE_NOT_WAIT, NOT_WAIT
		//处理丢包等待回应，处理丢包不等待，不处理丢包不等待
	}
	private class TransmissionResult{
		boolean result = false;
		String reason = "";
	}
}
