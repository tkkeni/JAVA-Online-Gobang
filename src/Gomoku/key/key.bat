@echo off

echo 生成服务器的密钥库 server.keystore
keytool -genkey -alias server -keypass 123456789 -keyalg RSA -keysize 1024 -validity 365 -keystore ./server.keystore -storepass 987654321

echo 生成客户端的密钥库 client.keystore
keytool -genkey -alias client -keypass 123456789 -keyalg RSA -keysize 1024 -validity 365 -keystore ./client.keystore -storepass 987654321

echo 导出服务器的公钥证书 server.cer
keytool -export -alias server -file ./server.cer -keystore ./server.keystore -storepass 987654321

echo 导出客户端的公钥证书 client.cer
keytool -export -alias client -file ./client.cer -keystore ./client.keystore -storepass 987654321

echo 将服务器的公钥导入到客户端信任的公钥库
keytool -import -file ./server.cer -keystore ./trust_client.keystore -storepass 987654321 -alias server

echo 将客户端的公钥导入到服务器信任的公钥库
keytool -import -file ./client.cer -keystore ./trust_server.keystore -storepass 987654321

echo "done"