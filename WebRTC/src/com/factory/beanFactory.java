package com.factory;

import com.handler.JoinHanler;
import com.handler.SendAnswer;
import com.handler.SendICE;
import com.handler.SendOffer;
import com.handler.handl;

// 静态工厂类，返回相应的实例
public class beanFactory {

	public static handl getHandl(String eventName){
		if (eventName.equals("__join")) {
			return new JoinHanler();
			
		}
		if (eventName.equals("__offer")) {
			return new SendOffer();
		}
		if (eventName.equals("__answer")) {
			return new SendAnswer();
		}
		if(eventName.equals("__ice_candidate")){
			return new SendICE();
		}
		return null;
	}
}
