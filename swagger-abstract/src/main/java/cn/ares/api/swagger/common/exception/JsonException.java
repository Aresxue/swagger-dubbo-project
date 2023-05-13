package cn.ares.api.swagger.common.exception;

/**
 * @author: Ares
 * @time: 2021-11-16 12:03:00
 * @description: json exception
 * @version: JDK 1.8
 */
public class JsonException extends RuntimeException {

  public JsonException() {
  }

  public JsonException(String message) {
    super(message);
  }

  public JsonException(String message, Throwable cause) {
    super(message, cause);
  }
}
