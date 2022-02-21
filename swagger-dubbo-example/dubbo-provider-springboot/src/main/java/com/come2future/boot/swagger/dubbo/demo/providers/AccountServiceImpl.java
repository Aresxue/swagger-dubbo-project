package com.come2future.boot.swagger.dubbo.demo.providers;

import com.come2future.boot.swagger.dubbo.example.api.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
@Tag(name = "account service")
public class AccountServiceImpl implements AccountService {

  @Override
  @Operation(summary = "logout", description = "exit user info")
  public void logout(String account) {
  }

  @Override
  @Operation(summary = "login")
  public boolean login(@Parameter(description = "user account") String account,
      @Parameter(description = "user password") String password) {
    return false;
  }

  @Override
  @Operation(operationId = "byCode", summary = "login", description = "login by code")
  public boolean login(@Parameter(description = "user account") String account,
      @Parameter(name = "code", description = "code") int code) {
    return false;
  }

  @Override
  public void updateInfo(boolean isBoy, Integer number) {
  }

}
