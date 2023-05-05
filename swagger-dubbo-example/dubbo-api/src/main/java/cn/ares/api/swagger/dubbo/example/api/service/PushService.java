package cn.ares.api.swagger.dubbo.example.api.service;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

public interface PushService {

  @Operation(summary = "push", description = "push message to account")
  String push(@Parameter(name = "account") String account);

}
