package Gomoku.Interface;

public interface IHallToClient {
	void createGame();
	void quit();
	boolean challenge(String username);
	boolean joinGame(int gameID);
	boolean watchGame(int gameID);
}
