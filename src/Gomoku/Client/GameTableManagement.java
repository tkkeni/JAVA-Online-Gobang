package Gomoku.Client;

import Gomoku.Transmission.GameInfo;
import javax.swing.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import Gomoku.Interface.*;
/*
* 游戏大厅TablePanel管理
* */
class GameTableManagement {
	private JPanel tablePanel;
	private ConcurrentHashMap<Integer, TablePanel> tables = new ConcurrentHashMap<>();
	private IHallToClient hallToClient;
	GameTableManagement(JPanel panel, IHallToClient hallToClient){
		this.tablePanel = panel;
		this.hallToClient = hallToClient;
	}
	void updateGames(ArrayList<GameInfo> gameInfos){
		int id;
		TablePanel table;
		ArrayList<String> player;
		ArrayList<Integer> allGame = new ArrayList<>();
		for (GameInfo game : gameInfos) {
			id = game.getGameID();
			allGame.add(id);
			table = tables.get(id);
			player = game.getPlayers();
			if(player.size() == 0){
				System.out.println(id + ": Table 用户数量为0");
				continue;
			}
			if(table == null){
				table = new TablePanel(id, this.hallToClient);
				table.updateUsers(player.get(0), player.size() == 2 ? player.get(1) : null);
				tables.put(id, (TablePanel)tablePanel.add(table));
				System.out.println("tableID: " + id + " add: " + player.get(0) + " " + (player.size() == 2 ? player.get(1) : null));
			}else{
				table.updateUsers(player.get(0), player.size() == 2 ? player.get(1) : null);
			}
		}
		BiConsumer<Integer, TablePanel> tables_biConsumer = (gameID, t_tablePanel) -> {
			if(allGame.indexOf(gameID) == -1){
				System.out.println(gameID + " remove");
				tablePanel.remove(tables.get(gameID));
				tables.remove(gameID);
			}
		};
		tables.forEach(tables_biConsumer);
		tablePanel.updateUI();
	}

	/*private boolean removeTable(int tableID){
		TablePanel table = tables.get(tableID);
		if(table == null) return false;
		tablePanel.remove(table);
		tables.remove(tableID);
		System.out.println("tableID: " + tableID + " is been remove");
		return true;
	}

	private boolean leaveTable(String username, int tableID){
		TablePanel table = tables.get(tableID);
		if(table == null) return false;
		return table.leave(username);
	}*/
}
