package com.chat.entity;

import lombok.Data;

/**
 * @author KSaMar
 * @version 1.0
 * 信息实体类
 */
@Data
public class Message {

    private String name;
    private String time;
    private String msg;
    private String to;
    private String rsapk;
    private String dsapk;
    private String sign;
    private String type;
    private String key;
    private String sendto;
}
