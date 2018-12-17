package Gomoku.Interface;

public interface IChessFrameToClient{
	void clientLeaveTable();
	void chatMessage(String msg);
	boolean playerGameStatus(boolean readyOrNot);
	boolean clientSubmitChess(int x, int y);
	void clientRunAway();
	String getCurrentUsername();
	void privateChat();
	void privateChatSetName(String hisName);
	void adminDefeat();
}