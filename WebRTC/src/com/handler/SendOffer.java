package com.handler;

import java.io.IOException;
import java.util.HashMap;

import javax.websocket.Session;

import com.entity.Data;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rtcService.RTCService;

public class SendOffer implements handl {

	@Override
	public void excute(String data, Session session, RTCService rtcService) {
		// 收到offer数据，将其转发给目标用户
		Gson gson = new Gson();
		Data message = gson.fromJson(data, Data.class);
		Session des = (Session) RTCService.connect.get(message.socketId);
		synchronized (des) {
			if (des != null) {
				// 定义发送json数据的内容
				JsonObject eventoObject = new JsonObject();
				eventoObject.addProperty("eventName", "_offer");
				HashMap<String, Object> dataTo = new HashMap<String, Object>();
				dataTo.put("socketId", session.getId());
				dataTo.put("sdp", message.sdp);
				eventoObject.addProperty("data", gson.toJson(dataTo));
				// 开始发送offer数据
				try {
					des.getBasicRemote().sendText(eventoObject.toString());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}

	}

}
