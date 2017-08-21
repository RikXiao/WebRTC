package com.rtcService;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;

import javax.enterprise.inject.New;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

import com.entity.Event;
import com.factory.beanFactory;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.handler.handl;

@ServerEndpoint("/RTCService")
public class RTCService {
	// 静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
	public static int onlineCount = 0;

	// connect key为session的ID，value为此对象this.session（保存的是全部的session对象）;
	public static final HashMap<String, Object> connect = new HashMap<String, Object>();
	// userMap key为session的ID，value为用户名 (全部)
	public static final HashMap<String, String> userMap = new HashMap<String, String>();
	// rooms,key 为房间名，value为房间内的session对象（同样用一个map保存，以房间为单位区分）
	public static final HashMap<String, Object> rooms = new HashMap<String, Object>();
	// 与某个客户端的连接会话，需要通过它来给客户端发送数据
	public Session session;
	// 判断是否是第一次接收的消息
	public boolean isfirst = true;

	public String room;

	/**
	 * 连接建立成功调用的方法
	 * 保存用户初始id等信息
	 * @param session
	 *            可选的参数。session为与某个客户端的连接会话，需要通过它来给客户端发送数据
	 */
	@OnOpen
	public void onOpen(Session session) {
		
		this.session = session;
		connect.put(session.getId(), session);// 获取Session，存入Hashmap中
		// 默认将id作为名字
		userMap.put(session.getId(), session.getId());
	}

	/**
	 * 连接关闭调用的方法
	 */
	@OnClose
	public void onClose(Session session) {
		// 连接关闭后移除相应的连接实例
		String usr = userMap.get(session.getId());
		userMap.remove(session.getId());
		connect.remove(session.getId());
		@SuppressWarnings("unchecked")
		HashMap<String, Session> rooMap = (HashMap<String, Session>) rooms
				.get(this.room);
		rooMap.remove(session.getId());
		JsonObject removeInfo = new JsonObject();
		removeInfo.addProperty("eventName", "_remove_peer");
		removeInfo.addProperty("data", session.getId());
		for (Session deSession : rooMap.values()) {

			synchronized (deSession) {
				try {
					deSession.getBasicRemote().sendText(removeInfo.toString());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		System.out.println(usr + "退出！当前在线人数为" + connect.size());
	}

	/**
	 * 收到客户端消息后调用的方法
	 * 
	 * @param message
	 *            客户端发送过来的消息
	 * @param session
	 *            可选的参数
	 *  //根据用户发送过来的消息里面的eventname获取相应的实例，执行不同的处理方法
	 */
	@OnMessage
	public void onMessage(String messag, Session session) {
		Gson gson = new Gson();
		System.out.println(messag);
		Event event = gson.fromJson(messag, Event.class);//解析json字符串
		String eventName = event.eventName;
		System.out.println(eventName);
		handl handl = beanFactory.getHandl(eventName);//根据eventname获取对象实例（工厂策略模式）
		if (handl != null) {
			handl.excute(event.data, session, this);
		} else {
			System.out.println("错误事件");
		}

	}

	@OnMessage
	public void onbinarry(ByteBuffer messag, Session session) {
		System.out.println("收到二进制数据");
	}

	/**
	 * 发生错误时调用
	 * 
	 * @param session
	 * @param error
	 */
	@OnError
	public void onError(Session session, Throwable error) {
		System.out.println("发生错误");
		error.printStackTrace();
	}

	/**
	 * 给所有人发送消息
	 * 
	 * @param msg
	 *            发送的消息
	 * @param session
	 */
	private void sendToAll(String msg, Session session) {
		String who = "";
		// 群发消息
		for (String key : connect.keySet()) {
			RTCService client = (RTCService) connect.get(key);
			if (key.equalsIgnoreCase(session.getId())) {
				who = "自己对大家说 : ";
			} else {
				who = userMap.get(session.getId()) + "对大家说 :";
			}
			synchronized (client) {
				try {
					client.session.getBasicRemote().sendText(who + msg);
				} catch (IOException e) {
					connect.remove(client);
					e.printStackTrace();
					try {
						client.session.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}

		}
	}

	/**
	 * 发送给指定用户
	 * 
	 * @param user
	 *            用户名
	 * @param msg
	 *            发送的消息
	 * @param session
	 */
	private void sendToUser(String user, String msg, Session session) {
		boolean you = false;// 标记是否找到发送的用户
		for (String key : userMap.keySet()) {
			if (user.equalsIgnoreCase(userMap.get(key))) {
				RTCService client = (RTCService) connect.get(key);
				synchronized (client) {
					try {
						client.session.getBasicRemote().sendText(
								userMap.get(session.getId()) + "对你说：" + msg);
					} catch (IOException e) {
						connect.remove(client);
						try {
							client.session.close();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}
				you = true;// 找到指定用户标记为true
				break;
			}

		}
		// you为true则在自己页面显示自己对xxx说xxxxx,否则显示系统：无此用户
		if (you) {
			try {
				session.getBasicRemote().sendText("自己对" + user + "说:" + msg);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				session.getBasicRemote().sendText("系统：无此用户");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
}
