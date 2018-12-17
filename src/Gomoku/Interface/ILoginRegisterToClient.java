package Gomoku.Interface;

public interface ILoginRegisterToClient {
	boolean login(String username, String password);
	boolean register(String username, String password);
	void toHall();
}
