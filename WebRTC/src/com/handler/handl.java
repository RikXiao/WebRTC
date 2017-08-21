package com.handler;

import javax.websocket.Session;

import com.rtcService.RTCService;
// 处理函数接口类
public interface handl {

	public abstract void excute(String data,Session session,RTCService rtcService);
}
