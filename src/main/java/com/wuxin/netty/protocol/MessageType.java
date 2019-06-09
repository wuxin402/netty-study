package com.wuxin.netty.protocol;

/**
 * 消息类型枚举
 */
public enum MessageType {
    /**
     * 0:业务请求消息
     * 1:业务响应消息
     * 2:业务ONE WAY 消息(既是请求又是响应消息)
     * 3:握手请求消息
     * 4:握手应答消息
     * 5:心跳请求消息
     * 6:心跳应答消息
     */

    LOGIN_REQ((byte)3),LOGIN_RESP((byte)4),HEARTBEAT_REQ((byte)5),HEARTBEAT_RESP((byte)6);

    private byte value;

    private MessageType(byte value) {
        this.value = value;
    }

    public byte value() {
        return value;
    }
}
