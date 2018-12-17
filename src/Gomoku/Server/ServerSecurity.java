package Server;

import java.io.FileInputStream;
import javax.crypto.SecretKey;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.security.KeyStore;
import java.security.PrivateKey;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class ServerSecurity { //服务器安全
	private String pwd_ks = "987654321";
	private String pwd_pk = "123456789";
	private PrivateKey privateKey;

	ConcurrentHashMap<InetSocketAddress, SecretKey> keys = new ConcurrentHashMap<>(); //保存所有客户的对称密钥，客户地址为键

	ServerSecurity()throws Exception{
		KeyStore ks = KeyStore.getInstance("JKS");//私钥库对象
		ks.load(new FileInputStream("src/key/server.keystore"),pwd_ks.toCharArray());
		privateKey = (PrivateKey)ks.getKey("server", pwd_pk.toCharArray());

	}

	byte[] pkey_decrypt(byte[] msg)throws Exception { //服务器私钥解密
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding"); //加密器
		cipher.init(Cipher.DECRYPT_MODE, this.privateKey);//设置加密模式：服务器私钥解密
		return cipher.doFinal(msg, 0, msg.length);
	}
	void saveKey(InetSocketAddress client, byte[] key){ //保存客户传来的密钥 SecretKey secretKey = new SecretKeySpec(key_dec, "AES");
		SecretKey secretKey = new SecretKeySpec(key, "AES");
		if (keys.containsKey(client)){
			keys.put(client ,secretKey);
		}else{
			keys.replace(client, secretKey);
		}
	}

	byte[] encryption(InetSocketAddress who, byte[] msg){ //使用客户对称密钥加密
		try {
			Cipher cipher_aes=Cipher.getInstance("AES");
			if(! keys.containsKey(who)){
				return null;
			}
			cipher_aes.init(Cipher.ENCRYPT_MODE, keys.get(who));//设置加密模式：对称密钥解密
			return cipher_aes.doFinal(msg);
		}catch (javax.crypto.IllegalBlockSizeException | javax.crypto.BadPaddingException |
				java.security.NoSuchAlgorithmException | javax.crypto.NoSuchPaddingException |
				java.security.InvalidKeyException error){
			return null;
		}
	}

	byte[] decrypt(InetSocketAddress who, byte[] msg){ //使用客户对称密钥解密
		try {
			Cipher cipher=Cipher.getInstance("AES");
			if(! keys.containsKey(who)){
				return null;
			}
			cipher.init(Cipher.DECRYPT_MODE,keys.get(who) );//设置加密模式：服务器私钥解密
			return cipher.doFinal(msg,0,msg.length);
		}catch (javax.crypto.IllegalBlockSizeException | javax.crypto.BadPaddingException |
				java.security.NoSuchAlgorithmException | javax.crypto.NoSuchPaddingException |
				java.security.InvalidKeyException error){
			return null;
		}
	}
}
