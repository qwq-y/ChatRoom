package com.chat.component;

import com.alibaba.fastjson.JSON;
import com.chat.entity.*;
import com.chat.service.ChatRoomService;
import com.chat.service.ParticipantsService;
import com.chat.service.UserService;
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
@ServerEndpoint("/socket/{account}")
public class  WebSocketServer {

//    @Autowired
//    private ChatRoomService chatRoomService;
    @Autowired
    private ParticipantsService participantsService;
    @Autowired
    private UserService userService;

    public static final Map<Long, Session> sessionMap = new ConcurrentHashMap<>();   // 所有的用户会话 <userAccount, session>
    public static final Map<Long, String> RSAPKMap = new ConcurrentHashMap<>();   // 加密公钥
    public static final Map<Long, String> DSAPKMap = new ConcurrentHashMap<>();   // 签名公钥
    public static final Map<Long, String> userMap = new ConcurrentHashMap<>();  // 用户的账号和密码

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
    private void noticeLeader() {
//        Long leader = chatRoomService.getLeaderByNumber(roomNumber);
        Long leader = getRandomLeader();
        List<Long> receiver = new ArrayList<>();
        receiver.add(leader);

        Message message = new Message();
        message.setReceiver(receiver);
        message.setType("REMIND");

        sendMessage(message);
    }

    // 给大家伙更新用户列表
    private void updateUser() {
//        List<Long> receiver = participantsService.getUserAccountInRoom(roomNumber);

        Message message = new Message();
//        message.setReceiver(receiver);
        List<Long> all = participantsService.getAllUsersOnline();
        message.setReceiver(all);
        message.setType("USER");
        message.setBody(convertMapToString(userMap));

        sendMessage(message);
    }

    // 告诉大家伙更新加密公钥
    private void updateRSAPk() {
//        List<Long> receiver = participantsService.getUserAccountInRoom(roomNumber);

        Message message = new Message();
//        message.setReceiver(receiver);
        List<Long> all = participantsService.getAllUsersOnline();
        message.setReceiver(all);
        message.setType("RSA");
        message.setBody(convertMapToString(RSAPKMap));

        sendMessage(message);
    }

    // 告诉大家伙更新签名公钥
    private void updateDSAPk() {
//        List<Long> receiver = participantsService.getUserAccountInRoom(roomNumber);

        Message message = new Message();
//        message.setReceiver(receiver);
        List<Long> all = participantsService.getAllUsersOnline();
        message.setReceiver(all);
        message.setType("DSA");
        message.setBody(convertMapToString(DSAPKMap));

        sendMessage(message);
    }

    // 服务器储存加密公钥，提醒用户更新
    private void storeRSAPk(Message message) {
        RSAPKMap.put(message.getSender(), message.getBody());
        Long senderAccount = message.getSender();
//        Long roomNumber = participantsService.getRoomNumberOfUser(senderAccount);
        updateRSAPk();
        updateDSAPk();
        updateUser();
    }

    // 服务器储存签名公钥，提醒用户更新
    private void storeDSAPk(Message message) {
        DSAPKMap.put(message.getSender(), message.getBody());
        Long senderAccount = message.getSender();
//        Long roomNumber = participantsService.getRoomNumberOfUser(senderAccount);
        updateRSAPk();
        updateDSAPk();
        updateUser();
    }

    // 从用户列表选则一个领导人
    private Long getRandomLeader() {
        int size = userMap.size();
        int item = (int) (Math.random() * size);
        int i = 0;
        for (Long account : userMap.keySet()) {
            if (i == item) {
                return account;
            }
            i++;
        }
        return null;
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
        @PathParam("account") Long userAccount
//        @PathParam("number") Long roomNumber
        ) {
        boolean isExist = sessionMap.containsKey(userAccount);
        if (!isExist) {
//            ChatRoom chatRoom = chatRoomService.getChatRoom(roomNumber);
//            if (chatRoom != null) {
                System.out.println(userAccount + "加入了聊天室");
                participantsService.enterRoom(userAccount);
                sessionMap.put(userAccount, session);
                String userName = userService.getUserNameByAccount(userAccount);
                userMap.put(userAccount, userName);
                noticeLeader();
//            }
        }
    }

    // websocket断开事件
    @OnClose
    public void onClose(
        @PathParam("account") Long userAccount
//        @PathParam("number") Long roomNumber
    ) {
        if (userAccount != null) {
            System.out.println(userAccount + "退出了聊天室");
            participantsService.leaveRoom(userAccount);
            sessionMap.remove(userAccount);
            RSAPKMap.remove(userAccount);
            DSAPKMap.remove(userAccount);
            String userName = userService.getUserNameByAccount(userAccount);
            userMap.remove(userAccount, userName);
            noticeLeader();
            updateRSAPk();
            updateDSAPk();
            updateUser();
        }
    }

    // 服务器处理客户端发来的数据
    @OnMessage
    public void onMessage(String messageJason) {
        Message message = JSON.parseObject(messageJason, Message.class);
        if (message.getType().equals("CHAT")) {
            sendMessage(message);
        } else if (message.getType().equals("KEY")) {
            sendMessage(message);
        } else if (message.getType().equals("RSA")) {
            storeRSAPk(message);
        } else if (message.getType().equals("DSA")) {
            storeDSAPk(message);
        } else {
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
