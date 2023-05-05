package cn.ares.api.swagger.dubbo.demo.providers;

import cn.ares.api.swagger.dubbo.example.api.service.PushService;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class PushServiceImpl implements PushService{

	@Override
	public String push(String account) {
		return "hello swagger dubbo";
	}

}
