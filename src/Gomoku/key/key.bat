@echo off

echo ���ɷ���������Կ�� server.keystore
keytool -genkey -alias server -keypass 123456789 -keyalg RSA -keysize 1024 -validity 365 -keystore ./server.keystore -storepass 987654321

echo ���ɿͻ��˵���Կ�� client.keystore
keytool -genkey -alias client -keypass 123456789 -keyalg RSA -keysize 1024 -validity 365 -keystore ./client.keystore -storepass 987654321

echo �����������Ĺ�Կ֤�� server.cer
keytool -export -alias server -file ./server.cer -keystore ./server.keystore -storepass 987654321

echo �����ͻ��˵Ĺ�Կ֤�� client.cer
keytool -export -alias client -file ./client.cer -keystore ./client.keystore -storepass 987654321

echo ���������Ĺ�Կ���뵽�ͻ������εĹ�Կ��
keytool -import -file ./server.cer -keystore ./trust_client.keystore -storepass 987654321 -alias server

echo ���ͻ��˵Ĺ�Կ���뵽���������εĹ�Կ��
keytool -import -file ./client.cer -keystore ./trust_server.keystore -storepass 987654321

echo "done"