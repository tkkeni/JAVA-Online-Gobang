package Gomoku.Transmission;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;


class ClientSecurity { //客户端安全
	private String pwd_ks = "987654321";
	private SecretKey symmetricKey = null;//生成的对称密钥保存在这
	private PublicKey publicKey;

	ClientSecurity()throws Exception{
		KeyStore tks = KeyStore.getInstance("JKS");//公钥库对象
		tks.load(new FileInputStream("src/Gomoku/key/trust_client.keystore"),pwd_ks.toCharArray());
		publicKey = tks.getCertificate("server").getPublicKey();
	}
	byte[] generateKey(){ //生成对称密钥，并用服务器公钥加密
		try{
			KeyGenerator kg=KeyGenerator.getInstance("AES"); //获取密钥生成器
			kg.init(128);//初始化密钥生成器
			this.symmetricKey = kg.generateKey();

			System.out.println(Arrays.toString(this.symmetricKey.getEncoded()));
			Cipher cipher=Cipher.getInstance("RSA/ECB/PKCS1Padding"); //加密器
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);//设置加密模式：服务器公钥加密
			return cipher.doFinal(this.symmetricKey.getEncoded());
		}catch (Exception error){
			error.printStackTrace();
			return null;
		}
	}

	byte[] encrypt(byte[] msg){ //对称密钥加密
		try{
			Cipher cipher_aes=Cipher.getInstance("AES");
			cipher_aes.init(Cipher.ENCRYPT_MODE, this.symmetricKey);//设置加密模式：对称密钥加密
			return cipher_aes.doFinal(msg);
		}catch (Exception error){
			error.printStackTrace();
			return null;
		}
	}

	byte[] decrypt(byte[] msg, int length){ //对称密钥解密
		try{
			Cipher cipher=Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, this.symmetricKey);//设置加密模式：对称密钥加密
			return cipher.doFinal(msg, 0, length);
		}catch (Exception error){
			error.printStackTrace();
			return null;
		}
	}

}
