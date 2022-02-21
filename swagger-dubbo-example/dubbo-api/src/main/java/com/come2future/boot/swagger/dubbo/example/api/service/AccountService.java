package com.come2future.boot.swagger.dubbo.example.api.service;

import io.swagger.v3.oas.annotations.Operation;

public interface AccountService {

	void logout(String account);

	boolean login(String account, String password);

	boolean login(String account, int code);

	void updateInfo(boolean isBoy, Integer number);

}
