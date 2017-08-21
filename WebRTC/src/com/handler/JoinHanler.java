package com.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.websocket.Session;

import com.entity.Data;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rtcService.RTCService;

public class JoinHanler implements handl {
	// 处理新用户加入时 的操作，通知其他用户
	@Override
	public void excute(String data, Session session, RTCService rtcService) {
		Gson gson = new Gson();
		Data join = gson.fromJson(data, Data.class);
		String room = join.room;
		@SuppressWarnings("unchecked")
		HashMap<String, Session> curRoom = (HashMap<String, Session>) RTCService.rooms
				.get(room);
		ArrayList<String> ids = new ArrayList<String>();
		// 根据房间名给用户分组
		if (curRoom == null) {
			curRoom = new HashMap<String, Session>();
			RTCService.rooms.put(room, curRoom);
		} else {
			// 像房间内其他用户发送新用户的Id
			Map<String, Object> result = new HashMap<String, Object>();// 定义发送json数据的内容
			for (Session session2 : curRoom.values()) {
				if (session2.getId() == session.getId()) {
					continue;
				}
				ids.add(session2.getId());
				result.put("eventName", "_new_peer");
				result.put("data", session.getId());
				try {
					System.out.println(gson.toJson(result));
					synchronized (session2) {
						session2.getBasicRemote().sendText(gson.toJson(result));
					}

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		curRoom.put(session.getId(), session);
		rtcService.room = room;
		// 返回当前房间内的所有其他用户给当前用户
		HashMap<String, Object> users = new HashMap<String, Object>();// 定义发送json数据的内容
		users.put("connections", ids);
		users.put("you", session.getId());
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("eventName", "_peers");
		String struser = gson.toJson(users);
		jsonObject.addProperty("data", struser);
		try {
			System.out.println(jsonObject.toString());

			session.getBasicRemote().sendText(jsonObject.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
