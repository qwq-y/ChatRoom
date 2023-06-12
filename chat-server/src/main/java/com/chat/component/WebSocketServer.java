package com.chat.component;

import com.alibaba.fastjson.JSON;
import com.chat.entity.*;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author KSaMar
 * @version 1.0
 * WebSocket 服务
 */
@Component
@ServerEndpoint("/socket/{username}")
public class WebSocketServer {

    /**
     * 存储对象 map
     */
    public static final Map<String, Session> sessionMap = new ConcurrentHashMap<>();
    public static final Map<String, String> RSAPKMap = new ConcurrentHashMap<>();
    public static final Map<String, String> DSAPKMap = new ConcurrentHashMap<>();

    /***
     * WebSocket 建立连接事件
     * 1.把登录的用户存到 sessionMap 中
     * 2.发送给所有人当前登录人员信息
     * 3.更新每个人的RSA和DSA表
     * 4.提醒leader更新密钥
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) {
        // 搜索名称是否存在
        boolean isExist = sessionMap.containsKey(username);
        if (!isExist) {
            System.out.println(username + "加入了聊天室");
            sessionMap.put(username, session);
            String list = setUserList();
            sendAllMessage(list);
            showUserList();
//            try {
//                System.out.println(list);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
        }
    }

    /**
     * WebSocket 关闭连接事件
     * 1.把登出的用户从 sessionMap 中剃除
     * 2.发送给所有人当前登录人员信息
     * 3.更新每个人的RSA和DSA表
     * 4.提醒leader更新密钥
     */
    @OnClose
    public void onClose(@PathParam("username") String username) {
        if (username != null) {
            System.out.println(username + "退出了聊天室");
            sessionMap.remove(username);
            RSAPKMap.remove(username);
            DSAPKMap.remove(username);
            sendAllMessage(setUserList());
            sendAllMessage(setDSA());
            sendAllMessage(setRSA());
            if (sessionMap.entrySet().stream().findFirst().isPresent()){
                String leader = sessionMap.entrySet().stream().findFirst().get().getKey();
                try {
                    sessionMap.get(leader).getBasicRemote().sendText(setupdate());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            showUserList();
            showDSAList();
            showRSAList();
        }
    }

    /**
     * WebSocket 接受信息事件
     * 接收处理客户端发来的数据
     * @param message 信息
     */
    @OnMessage
    public void onMessage(String message) {
            Message msg = JSON.parseObject(message, Message.class);
            String smsg = JSON.toJSONString(msg);
            sendAllMessage(JSON.toJSONString(msg));
//            try {
//                System.out.println(smsg);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
    }

    /**
     * WebSocket 错误事件
     * @param session 用户 Session
     * @param error 错误信息
     */
    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("发生错误");
        error.printStackTrace();
    }

    /**
     * 显示在线用户
     */
    private void showUserList() {
        System.out.println("------------------------------------------");
        System.out.println("当前在线用户");
        System.out.println("------------------------------------------");
        for (String username : sessionMap.keySet()) {
            System.out.println(username);
        }
        System.out.println("------------------------------------------");
        System.out.println();
    }

    /**
     * 显示RSA表
     */
    private void showRSAList() {
        System.out.println("------------------------------------------");
        System.out.println("RSA");
        System.out.println("------------------------------------------");
        for (String username : RSAPKMap.keySet()) {
            System.out.println(username + RSAPKMap.get(username));
        }
        System.out.println("------------------------------------------");
        System.out.println();
    }

    /**
     * 显示DSA表
     */
    private void showDSAList() {
        System.out.println("------------------------------------------");
        System.out.println("DSA");
        System.out.println("------------------------------------------");
        for (String username : DSAPKMap.keySet()) {
            System.out.println(username + DSAPKMap.get(username));
        }
        System.out.println("------------------------------------------");
        System.out.println();
    }

    /**
     * 设置接收消息的用户列表
     * @return 用户列表
     */
    private String setUserList(){
        ArrayList<String> list = new ArrayList<>(sessionMap.keySet());
        UserList userList = new UserList();
        userList.setUserlist(list);
        return JSON.toJSONString(userList);
    }

    /**
     * 设置RSA表
     * @return RSA表
     */
    private String setRSA(){
        Map<String,String> tmp = new HashMap<>();
        for (String username : RSAPKMap.keySet()) {
            tmp.put(username,RSAPKMap.get(username));
        }
        RSAMap rsamap = new RSAMap();
        rsamap.setRsamap(tmp);
        return JSON.toJSONString(rsamap);
    }

    /**
     * 设置DSA表
     * @return DSA表
     */
    private String setDSA(){
        Map<String,String> tmp = new HashMap<>();
        for (String username : DSAPKMap.keySet()) {
            tmp.put(username,DSAPKMap.get(username));
        }
        DSAMap dsamap = new DSAMap();
        dsamap.setDsamap(tmp);
        return JSON.toJSONString(dsamap);
    }

    /**
     * 生成提醒包（提醒leader更新）
     * @return 带有提醒意味的包
     */
    private String setupdate(){
        Update upd = new Update();
        upd.setUpd(1);
        return JSON.toJSONString(upd);
    }

    /**
     * 发送消息到所有用户种
     * @param message 消息
     */
    private void sendAllMessage(String message) {
        try {
//            System.out.println("msg+"+ message);
            String[] spt = message.split("to");
            String[] ifupd = message.split("sendto");
            if (ifupd.length==2){//更新会议密钥
                String sendto = message.split("sendto")[1];
                sendto = sendto.substring(3,sendto.length()-2);
//                System.out.println("key:"+message);
                sessionMap.get(sendto).getBasicRemote().sendText(message);
            }else {
                if(spt.length==2){//message是发送消息的操作
                    String sourcename = message.split("name")[1].split(",")[0];
                    sourcename = sourcename.substring(3,sourcename.length()-1);
                    String getto = spt[1].substring(3,spt[1].length()-2);
//                System.out.println(getto);
                    if (getto.equals("All@") || getto.equals("")){//群发
                        message = message.split("to")[0];
                        StringBuilder sb = new StringBuilder(message);
                        sb.append("to\":\"All Users\"}");
                        message = sb.toString();
                        for (Session session : sessionMap.values()) {
                            session.getBasicRemote().sendText(message);
                        }
                    }else if (sourcename.equals(getto)){//给自己发
                        sessionMap.get(getto).getBasicRemote().sendText(message);
                    } else {//一对一
                        sessionMap.get(getto).getBasicRemote().sendText(message);
                        sessionMap.get(sourcename).getBasicRemote().sendText(message);
                    }
                }else {
                    if (message.split("type").length!=1){//新用户加入
//                        System.out.println(message);
                        String dsapk = message.split("name")[0];
                        dsapk = dsapk.substring(10,dsapk.length()-3);
                        dsapk = dsapk.replace("\\","");
                        String name = message.split("name")[1].split("rsapk")[0];
                        name = name.substring(3,name.length()-3);
                        String rsapk = message.split("rsapk")[1].split("type")[0];
                        rsapk = rsapk.substring(3,rsapk.length()-3);
                        rsapk = rsapk.replace("\\r\\n","\r\n");
                        RSAPKMap.put(name, rsapk);
                        DSAPKMap.put(name, dsapk);
//                        System.out.println(setDSA());
//                        System.out.println(setRSA());
                        for (Session session : sessionMap.values()) {
                            session.getBasicRemote().sendText(setDSA());
                            session.getBasicRemote().sendText(setRSA());
                        }
                        if (sessionMap.entrySet().stream().findFirst().isPresent()){
                            String leader = sessionMap.entrySet().stream().findFirst().get().getKey();
                            sessionMap.get(leader).getBasicRemote().sendText(setupdate());
                        }
                        showRSAList();
                        showDSAList();
                        return;
                    }
                    for (Session session : sessionMap.values()) {
                        session.getBasicRemote().sendText(message);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
