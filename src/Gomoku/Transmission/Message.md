**UDP ACK**
    
    Type: UDP_ACK
    Transmission: UDP
    Parmeter:
        null
        
**心跳**
    
    Type: HEARTBEAT
    Transmission: UDP
    Parmeter:
        String Username

**握手**
    
    Type: HELLO
    Transmission: UDP
    Parmeter:
        byte[] key
    Return:
        "Hello"

**登录**
    
    Type: LOGIN
    Transmission: UDP
    Parmeter:
        String Username, String Password
    Return:
        boolean result, [String text]
    
**注册**
    
    Type: REGISTER
    Transmission: UDP
    Parmeter:
        String Username, String Password
    Return:
        boolean result, [String text]
        
**登出**
    
    Type: LOGOUT
    Transmission: UDP
    Parmeter:
        String Username
        
**创建游戏**
    
    Type: GAME_CREATE
    Transmission: UDP
    Parmeter:
        String Username
    Return:
        boolean result, [int gameID][String reason]
        
**广播游戏列表**
    
    Type: Broadcast_GameList
    Transmission: UDP
    Parmeter:
        ArrayList<GameInfo> gamelist
        
**广播用户列表**

    Type: BROADCAST_USER_LIST
    Transmission: UDP
    Parmeter:
        ArrayList<String> userlist
      
**玩家进入棋局**
    
    Type: CLIENT_JOIN_TABLE
    Transmission: UDP
    Parmeter:
        int GameID, String username
    Return:
        boolean result, [int gameID][String reason]]
        
**玩家观战棋局**
    
    Type: CLIENT_WATCH_TABLE
    Transmission: UDP
    Parmeter:
        int GameID, String username
    Return:
        boolean result, [int gameID][String reason]
                
**玩家离开棋局**
    
    Type: CLIENT_LEAVE_TABLE
    Transmission: UDP
    Parmeter:
        int GameID, String username
    Return:
        boolean result, [String reason]   
        
**棋局关闭**
    
    Type: GAME_CLOSE
    Transmission: UDP
    Parmeter:
        int GameID     
        
**棋局信息**
    
    Type: BROADCAST_GAME_INFO
    Transmission: UDP
    Parmeter:
        Boolean isPlaying, String user1, boolean status, int time1
        String user2, boolean status, int time2, watcher...
    
**棋局聊天消息**
    
    Type: GAME_CHAT
    Transmission: UDP
    Parmeter:
        int GameID, Stirng Username, String msg
    Return:
        Stirng Username, String msg
        
**玩家游戏准备**
    
    Type: CLIENT_GAME_STATUS
    Transmission: UDP
    Parmeter:
        int gameID, String Username, boolean status
    Return:
        boolean result, [String reason]

**下棋**

    Type: CLIENT_CHESS
    Transmission: UDP
    Parmeter:
        int gameID, String username, int x, int y
    Return:
        boolean result, [String reason]

**下棋消息**

    Type: BRPADCAST_CLIENT_CHESS
    Transmission: UDP
    Parmeter:
        int gameID, int x, int y

**可以下棋**

    Type: BRPADCAST_CHESS_POLL
    Transmission: UDP
    Parmeter:
        String username
        
**胜利**

    Type: BRPADCAST_CHESS_WIN
    Transmission: UDP
    Parmeter:
        int gameID, String size

**观战者中途加入获取当前棋盘**

    Type: GAME_CHESS_DATA
    Transmission: UDP
    Parmeter:
        int gameID
    Return:
        int[][]chessData
        
**逃跑**

    Type: CLIENT_RUN_AWAY
    Transmission: UDP
    Parmeter:
        int gameID, String username

**认输**

    Type: GAME_ADMIN_DEFEAT
    Transmission: UDP
    Parmeter:
        int gameID, String username

**私聊消息**

    Type: CLIENT_PRIVATE_CHAT
    Transmission: UDP
    Parmeter:
        String username, int gameID, String msg

**挑战玩家**

    Type: CLIENT_CHALLENGE
    Transmission: UDP
    Parmeter:
        String username, String toWho
    Return:
        Boolean result, [String msg]
        
**挑战玩家结果**

    Type: CLIENT_CHALLENGE_RESPOND
    Transmission: UDP
    Parmeter:
        String username, String toWho, boolean accept
        
**开始挑战**

    Type: CLIENT_CHALLENGE_BEGIN
    Transmission: UDP
    Parmeter:
        int gameID(-1代表挑战被拒绝)