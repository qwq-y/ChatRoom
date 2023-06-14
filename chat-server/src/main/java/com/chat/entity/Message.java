package com.chat.entity;

import java.util.List;
import lombok.Data;

/**
 * @author KSaMar
 * @version 1.0
 * 信息实体类
 */
@Data
public class Message {

    private String time;
    private Long sender;
    private List<Long> receiver;
    private String body;
    private String type;
    private String sign;

}
