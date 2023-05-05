package cn.ares.api.swagger.dubbo.example.api.service;

import cn.ares.api.swagger.dubbo.annotation.ApiOperationExpand;
import cn.ares.api.swagger.dubbo.example.api.dto.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@Tag(name = "user service")
public interface UserService {

  @Operation(summary = "by phone", description = "query user by phone",
      responses = {
          @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class)))})
  List<UserDto> query(@Parameter(description = "user phone") String phone);

  /*
  because name is same, so query user by phone is covered
   */
  @Operation(summary = "by code", description = "query user by city code",
      responses = {
          @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class)))})
  List<UserDto> query(int areaCode);

  @ApiOperationExpand(order = 8)
  @Operation(summary = "get user", description = "get user info by id",
      responses = {
          @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class)))})
  UserDto get(@Parameter(description = "user id") String id);

  @ApiOperationExpand(order = 10)
  @Operation(summary = "save user", description = "save user info")
  boolean save(@Parameter(description = "user info") UserDto userDto);

  @ApiOperationExpand(order = 5)
  @Operation(summary = "update user", description = "update user info")
  UserDto update(@Parameter(description = "user info") UserDto userDto);

  @ApiOperationExpand(order = 1)
  @Operation(summary = "delete user", description = "delete user info")
  boolean delete(@Parameter(description = "user id") String id);

  @Operation(summary = "compare user")
  int compare(@Parameter(description = "user src", required = true) UserDto src, UserDto dest);

  @Operation(summary = "mixed param")
  boolean mixed(UserDto user, String param);

}
