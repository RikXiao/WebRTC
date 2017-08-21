package com.entity;

public class Data {
// 用户发送的data数据解析实体类（解析时根据变量名对应）
	public String room;

	public String socketId;
	public String sdp;
	public String label;
	public String candidate;
	
	public String getRoom() {
		return room;
	}

	public void setRoom(String room) {
		this.room = room;
	}
}
