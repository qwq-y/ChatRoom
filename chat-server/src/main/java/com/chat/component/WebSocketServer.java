package com.chat.component;

import com.alibaba.fastjson.JSON;
import com.chat.entity.*;
import com.chat.service.ChatRoomService;
import com.chat.service.ParticipantsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/socket/{account}/{number}")
public class  WebSocketServer {

    @Autowired
    private ChatRoomService chatRoomService;
    @Autowired
    private ParticipantsService participantsService;

    public static final Map<Long, Session> sessionMap = new ConcurrentHashMap<>();   // 所有的用户会话 <userAccount, session>
    public static final Map<Long, String> RSAPKMap = new ConcurrentHashMap<>();   // 加密公钥
    public static final Map<Long, String> DSAPKMap = new ConcurrentHashMap<>();   // 签名公钥

    // 发送消息到指定用户
    private void sendMessage(Message message) {
        String messageJSON = JSON.toJSONString(message);
        try {
            List<Long> receiver = message.getReceiver();
            for (Long r : receiver) {
                Session session = sessionMap.get(r);
                session.getBasicRemote().sendText(messageJSON);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 提醒领导人更新密钥
    private void noticeLeader(Long roomNumber) {
        Long leader = chatRoomService.getLeaderByNumber(roomNumber);
        List<Long> receiver = new ArrayList<>();
        receiver.add(leader);

        Message message = new Message();
        message.setReceiver(receiver);
        message.setType(MsgType.REMIND);

        sendMessage(message);
    }

    // 告诉大家伙更新加密公钥
    private void updateRSAPk(Long roomNumber) {
        List<Long> receiver = participantsService.getUserAccountInRoom(roomNumber);

        Message message = new Message();
        message.setReceiver(receiver);
        message.setType(MsgType.RSA);
        message.setBody(convertMapToString(RSAPKMap));

        sendMessage(message);
    }

    // 告诉大家伙更新签名公钥
    private void updateDSAPk(Long roomNumber) {
        List<Long> receiver = participantsService.getUserAccountInRoom(roomNumber);

        Message message = new Message();
        message.setReceiver(receiver);
        message.setType(MsgType.DSA);
        message.setBody(convertMapToString(DSAPKMap));

        sendMessage(message);
    }

    // 服务器储存加密公钥
    private void storeRSAPk(Message message) {
        RSAPKMap.put(message.getSender(), message.getBody());
    }

    // 服务器储存签名公钥
    private void storeDSAPk(Message message) {
        DSAPKMap.put(message.getSender(), message.getBody());
    }

    // 把map转换为String
    private String convertMapToString (Map map) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    // websocket接入事件
    @OnOpen
    public void onOpen(
        Session session,
        @PathParam("account") Long userAccount,
        @PathParam("number") Long roomNumber
        ) {
        boolean isExist = sessionMap.containsKey(userAccount);
        if (!isExist) {
            ChatRoom chatRoom = chatRoomService.getChatRoom(roomNumber);
            if (chatRoom != null) {
                System.out.println(userAccount + "加入了聊天室");
                sessionMap.put(userAccount, session);
                noticeLeader(roomNumber);
                updateRSAPk(roomNumber);
                updateDSAPk(roomNumber);
            }
        }
    }

    // websocket断开事件
    @OnClose
    public void onClose(
        @PathParam("account") Long userAccount,
        @PathParam("number") Long roomNumber
    ) {
        if (userAccount != null) {
            System.out.println(userAccount + "退出了聊天室");
            sessionMap.remove(userAccount);
            RSAPKMap.remove(userAccount);
            DSAPKMap.remove(userAccount);
            noticeLeader(roomNumber);
            updateRSAPk(roomNumber);
            updateDSAPk(roomNumber);
        }
    }

    // 服务器处理客户端发来的数据
    @OnMessage
    public void onMessage(String messageJason) {
        Message message = JSON.parseObject(messageJason, Message.class);
        switch (message.getType()) {
            case CHAT:    // 转发聊天到指定接收者
                sendMessage(message);
                break;
            case RSA:   // 收到新加入者的加密公钥
                storeRSAPk(message);
                break;
            case DSA:   // 收到新加入者的签名公钥
                storeDSAPk(message);
                break;
            default:
                System.out.println("Invalid message type");
        }
    }

    // 异常
    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("发生错误");
        error.printStackTrace();
    }

}
