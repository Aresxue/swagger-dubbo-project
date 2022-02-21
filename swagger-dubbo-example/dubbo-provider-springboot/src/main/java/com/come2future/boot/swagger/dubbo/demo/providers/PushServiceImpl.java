package com.come2future.boot.swagger.dubbo.demo.providers;

import com.come2future.boot.swagger.dubbo.example.api.service.PushService;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class PushServiceImpl implements PushService{

	@Override
	public String push(String account) {
		return "hello swagger dubbo";
	}

}
