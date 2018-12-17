**UDP可靠传输**

    Host A:
         new Message{
            type: 消息类型 
            parameter: 附带的数据
            id: 消息ID
        }
        UDP send Message
        while(retryCount < 3)
            waitACK in 1s
            reSend Message
            retryCount + 1
        
    Host B:
        receive Message
        Message.parameter.clear()
        Message.type = UDP_ACK
        UDP send Message
         