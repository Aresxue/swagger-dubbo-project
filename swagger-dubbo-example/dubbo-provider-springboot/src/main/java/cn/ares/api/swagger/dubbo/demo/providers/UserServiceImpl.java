package cn.ares.api.swagger.dubbo.demo.providers;

import cn.ares.api.swagger.dubbo.example.api.dto.UserDto;
import cn.ares.api.swagger.dubbo.example.api.service.UserService;
import java.util.ArrayList;
import java.util.List;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class UserServiceImpl implements UserService {

  static UserDto userDto = new UserDto();
  static List<UserDto> list = new ArrayList<UserDto>();

  static {
    userDto.setId("520");
    userDto.setName("Ares");
    userDto.setSite("https://github.com/Aresxue/swagger-dubbo.git");
    list.add(userDto);
  }

  @Override
  public List<UserDto> query(String phone) {
    return list;
  }

  @Override
  public List<UserDto> query(int areaCode) {
    return list;
  }

  @Override
  public UserDto get(String id) {
    return userDto;
  }

  @Override
  public boolean save(UserDto userDto) {
    return true;
  }

  @Override
  public UserDto update(UserDto userDto) {
    return userDto;
  }

  @Override
  public boolean delete(String id) {
    return true;
  }

  @Override
  public int compare(UserDto src, UserDto dest) {
    return 0;
  }

  @Override
  public boolean mixed(UserDto user, String param) {
    return true;
  }

}
