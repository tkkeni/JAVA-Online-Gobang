package Gomoku.Server;

interface ITimeOut{ //超时时调用该接口
	void timeOut(String username, int gameID);
}
