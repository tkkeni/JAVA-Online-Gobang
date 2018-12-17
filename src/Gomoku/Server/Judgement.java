package Gomoku.Server;

public class Judgement {
	/** 连续多少子算赢 */
	public static int MAX = 5; 
	/**
	 * 根据棋盘状态评判胜负。
	 * 若水平、垂直、斜线或反斜线4个方向中，
	 * 任意一个方向存在连续MAX个同颜色的子，则胜负已分！
	 * @param data 棋盘状态（二维数组），0无子，1黑子，2白子
	 * @return 返回棋局评判，0胜负未分，1黑胜，2白胜，3平局
	 */
	public static int judge(int[][] data){
		int j1 = hLine(data);
		if(j1>0) return j1;
		
		int j2 = vLine(data);
		if(j2>0) return j2;
		
		int j3 = xLine(data);
		if(j3>0) return j3;
		
		int j4 = rxLine(data);
		if(j4>0) return j4;
		
		if( full(data) ) return 3;
		else return 0;
	}
	
	/* 棋盘所有位置皆已落子返回true，否则false */
	private static boolean full(int[][] data){
		for(int i=0; i<data.length; i++){
			for(int j=0; j<data[i].length; j++){
				if(data[i][j] == 0) return false;
			}
		}
		return true;
	}
	
	/** 
	 * 水平方向的胜负判断 
	 * @param data 棋盘状态（二维数组），0无子，1黑子，2白子
	 * @return 返回棋局评判，0尚无胜负，1黑胜，2白胜
	 */
	private static int hLine(int[][] data){
		int black=0,white=0;
		for(int i=0; i<data.length; i++){
			black=0; white=0;
			for(int j=0; j<data[i].length; j++){
				if(data[i][j]==1){
					black++;
					white = 0;
				}
				else if(data[i][j]==2){
					white++;
					black = 0;
				}else{
					black = white = 0;
				}
				if(black>=MAX) return 1;
				if(white>=MAX) return 2;
			}			
		}
		return 0;
	}	
	/** 
	 * 垂直方向的胜负判断 
 	 * @param data 棋盘状态（二维数组），0无子，1黑子，2白子
	 * @return 返回棋局评判，0尚无胜负，1黑胜，2白胜
	 */
	private static int vLine(int[][] data){
		int black=0,white=0;
		for(int i=0; i<data.length; i++){
			black=0; white=0;
			for(int j=0; j<data[i].length; j++){
				if(data[j][i]==1){
					black++;
					white = 0;
				}
				else if(data[j][i]==2){
					white++;
					black = 0;
				}else{
					black = white = 0;
				}
				if(black>=MAX) return 1;
				if(white>=MAX) return 2;
			}			
		}
		return 0;
	}
	/** 
	 * 斜线（右上-左下）方向 
	 * 同一条线上，x+y等于常数（规律为0到N+N）。
	 * @param data 棋盘状态（二维数组），0无子，1黑子，2白子
	 * @return 返回棋局评判，0尚无胜负，1黑胜，2白胜
	 */
	private static int xLine(int[][] data){
		int black=0,white=0;
		for(int sum=0; sum<=data.length; sum++){
			black=0; white=0;
			for(int i=0; i<sum; i++) {
				int j = sum - i;
				if(i>=data.length || i<0) continue;
				if(j>=data.length || j<0) continue;				
				if(data[i][j]==1){
					black++;
					white = 0;
				}
				else if(data[i][j]==2){
					white++;
					black = 0;
				}else{
					black = white = 0;
				}
				if(black>=MAX) return 1;
				if(white>=MAX) return 2;
			}			
		}
		return 0;
	}
	/** 
	 * 反斜线（左上-右下）方向 
	 * 同一条线上，x-y等于常数（规律为从负N-1到正N-1）
	 * @param data 棋盘状态（二维数组），0无子，1黑子，2白子
	 * @return 返回棋局评判，0尚无胜负，1黑胜，2白胜
	 */
	private static int rxLine(int[][] data){
		int black=0,white=0;
		for(int sub=-(data.length-1); sub<=data.length-1; sub++){
			black=0; white=0;
			for(int i=0; i<data.length; i++) {
				int j = i - sub;
				if(i>=data.length || i<0) continue;
				if(j>=data.length || j<0) continue;
				if(data[i][j]==1){
					black++;
					white = 0;
				}
				else if(data[i][j]==2){
					white++;
					black = 0;
				}else{
					black = white = 0;
				}
				if(black>=MAX) return 1;
				if(white>=MAX) return 2;
			}			
		}
		return 0;
	}
}