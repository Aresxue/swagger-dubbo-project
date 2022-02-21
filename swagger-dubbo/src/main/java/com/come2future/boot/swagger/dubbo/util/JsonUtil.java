package com.come2future.boot.swagger.dubbo.util;


import static com.come2future.boot.swagger.dubbo.util.DateUtil.DATE_FORMAT_TIME;
import static com.come2future.boot.swagger.dubbo.util.DateUtil.DATE_FORMAT_WHIFFLETREE_DAY;
import static com.come2future.boot.swagger.dubbo.util.DateUtil.DATE_FORMAT_WHIFFLETREE_SECOND;

import com.come2future.boot.swagger.dubbo.exception.JsonException;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: Ares
 * @time: 2021-04-06 20:39:00
 * @description: json util
 * @version: JDK 1.8
 */
public class JsonUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtil.class);

  private static final ObjectMapper OBJECT_MAPPER = getObjectMapper();

  private static final TypeReference<HashMap<String, String>> STRING_MAP_TYPE_REFERENCE = new TypeReference<HashMap<String, String>>() {
  };

  public static void configTime(ObjectMapper objectMapper, String dateFormat) {
    configTime(objectMapper, dateFormat, null, null, null);
  }

  public static void configTime(String dateFormat) {
    configTime(OBJECT_MAPPER, dateFormat);
  }

  public static void configTime(ObjectMapper objectMapper, String localDateFormat,
      String localTimeFormat, String localDateTimeFormat) {
    configTime(objectMapper, null, localDateFormat, localTimeFormat, localDateTimeFormat);
  }

  public static void configTime(String localDateFormat,
      String localTimeFormat, String localDateTimeFormat) {
    configTime(OBJECT_MAPPER, localDateFormat, localTimeFormat, localDateTimeFormat);
  }

  /**
   * @author: Ares
   * @description: config time serializer
   * @description: 配置时间序列化
   * @time: 2021-11-27 05:40:00
   * @params: [objectMapper, dateFormat, localDateFormat, localTimeFormat, localDateTimeFormat]
   * @return: void
   */
  public static void configTime(ObjectMapper objectMapper, String dateFormat,
      String localDateFormat, String localTimeFormat, String localDateTimeFormat) {
    if (StringUtil.isNotEmpty(dateFormat)) {
      objectMapper.setDateFormat(DateUtil.getFormat(dateFormat));
    }

    boolean localDateIsNotEmpty = StringUtil.isNotEmpty(localDateFormat);
    boolean localTimeIsNotEmpty = StringUtil.isNotEmpty(localTimeFormat);
    boolean localDateTimeIsNotEmpty = StringUtil.isNotEmpty(localDateTimeFormat);
    if (localDateIsNotEmpty || localTimeIsNotEmpty || localDateTimeIsNotEmpty) {
      JavaTimeModule javaTimeModule = new JavaTimeModule();
      if (localDateIsNotEmpty) {
        javaTimeModule.addSerializer(LocalDate.class,
            new LocalDateSerializer(DateTimeFormatter.ofPattern(localDateFormat)));
        javaTimeModule.addDeserializer(LocalDate.class,
            new LocalDateDeserializer(DateTimeFormatter.ofPattern(localDateFormat)));
      }
      if (localTimeIsNotEmpty) {
        javaTimeModule.addSerializer(LocalTime.class,
            new LocalTimeSerializer(DateTimeFormatter.ofPattern(localTimeFormat)));
        javaTimeModule.addDeserializer(LocalTime.class,
            new LocalTimeDeserializer(DateTimeFormatter.ofPattern(localTimeFormat)));
      }
      if (localDateTimeIsNotEmpty) {
        javaTimeModule.addSerializer(LocalDateTime.class,
            new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(localDateTimeFormat)));
        javaTimeModule.addDeserializer(LocalDateTime.class,
            new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(localDateTimeFormat)));
      }
      objectMapper.registerModule(javaTimeModule);
    }
  }


  /**
   * @author: Ares
   * @description: get object mapper with default configure and time serialization strategy
   * @description: 获取设置了一些默认选项和时间序列化策略的ObjectMapper
   * @time: 2021-12-23 22:58:30
   * @params: []
   * @return: com.fasterxml.jackson.databind.ObjectMapper out 出参
   */
  public static ObjectMapper getObjectMapper(boolean nonNull) {
    ObjectMapper objectMapper = new ObjectMapper();
    // 忽略null值
    if (nonNull) {
      objectMapper.setSerializationInclusion(Include.NON_NULL);
    }
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    configTime(objectMapper, DATE_FORMAT_WHIFFLETREE_SECOND, DATE_FORMAT_WHIFFLETREE_DAY,
        DATE_FORMAT_TIME, DATE_FORMAT_WHIFFLETREE_SECOND);

//    SimpleFilterProvider filterProvider = new SimpleFilterProvider();
//    filterProvider.addFilter("table",
//        SimpleBeanPropertyFilter.filterOutAllExcept(Sets.newHashSet("field")));
//    objectMapper.setFilterProvider(filterProvider);

    return objectMapper;
  }

  public static ObjectMapper getObjectMapper() {
    return getObjectMapper(false);
  }

  /**
   * @author: Ares
   * @description: java object convert to json string(when fail use toString)
   * @description: java对象转成json字符串如果失败直接使用toString方法
   * @time: 2021-12-03 15:57:00
   * @params: [object] java object
   * @return: [java.lang.String] json string
   */
  public static String toJsonString(Object object) {
    return toJsonString(object, false);
  }

  /**
   * @author: Ares
   * @description: java object convert to json string(when fail use toString)
   * @description: java对象转成json字符串如果失败直接使用toString方法
   * @time: 2021-05-14 15:57:00
   * @params: [object, pretty] java object, pretty
   * @return: [java.lang.String] json string
   */
  public static String toJsonString(Object object, boolean pretty) {
    if (null == object || ClassUtil.isBaseOrWrapOrString(object.getClass())) {
      return String.valueOf(object);
    }

    String result;
    try {
      return pretty ? OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(object)
          : OBJECT_MAPPER.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      LOGGER.warn("java object convert to json string exception: ", e);
      result = String.valueOf(object);
    }

    return result;
  }

  /**
   * @author: Ares
   * @description: convert string to java object
   * @description: 把字符串转成java对象
   * @time: 2021-06-10 10:46:00
   * @params: [json, valueType] json string, java type
   * @return: T java object
   */
  public static <T> T toJavaObject(String json, JavaType valueType) {
    try {
      return OBJECT_MAPPER.readValue(json, valueType);
    } catch (JsonProcessingException e) {
      throw new JsonException("Json string can not convert to java object", e);
    }
  }

  /**
   * @author: Ares
   * @description: convert string to java object
   * @time: 2021-12-23 23:13:02
   * @params: [json, valueTypeRef] json string, typeReference
   * @return: T java object
   */
  public static <T> T toJavaObject(String json, TypeReference<T> valueTypeRef) {
    try {
      return OBJECT_MAPPER.readValue(json, valueTypeRef);
    } catch (JsonProcessingException e) {
      throw new JsonException("Json string can not convert to java object", e);
    }
  }

  /**
   * @author: Ares
   * @description: convert string to java object
   * @time: 2021-12-23 23:13:02
   * @params: [json, ownerClass, rawClass] json string, owner class, raw class
   * @return: T java object
   */
  public static <OWNER, RAW> OWNER toJavaObject(String json, Class<OWNER> ownerClass,
      Class<RAW> rawClass) {
    JavaType javaType = OBJECT_MAPPER.getTypeFactory()
        .constructParametricType(ownerClass, rawClass);
    try {
      return OBJECT_MAPPER.readValue(json, javaType);
    } catch (JsonProcessingException e) {
      throw new JsonException("Json string can not convert to java object", e);
    }
  }


  /**
   * @author: Ares
   * @description: build javaType
   * @time: 2021-12-23 23:36:35
   * @params: [ownerClass, rawClass] owner class, raw class
   * @return: com.fasterxml.jackson.databind.JavaType javaType
   */
  public static <OWNER, RAW> JavaType buildJavaType(Class<OWNER> ownerClass,
      Class<RAW> rawClass) {
    TypeFactory typeFactory = OBJECT_MAPPER.getTypeFactory();
    return typeFactory.constructParametricType(ownerClass, rawClass);
  }


  /**
   * @author: Ares
   * @description: parse json string to json node
   * @description: 解析json字符串为json节点
   * @time: 2021-08-19 20:02:00
   * @params: [content] json content
   * @return: com.fasterxml.jackson.databind.JsonNode json node
   */
  public static JsonNode parseObject(String json) {
    try {
      return OBJECT_MAPPER.readTree(json);
    } catch (JsonProcessingException e) {
      throw new JsonException("Json string can not convert to java node", e);
    }
  }

  /**
   * @author: Ares
   * @description: parse object
   * @time: 2021-12-23 22:58:09
   * @params: [object, clazz]
   * @return: T object
   */
  public static <T> T parseObject(Object object, Class<T> clazz) {
    return toJavaObject(toJsonString(object), clazz);
  }

  /**
   * @author: Ares
   * @description: parse object
   * @time: 2021-12-23 22:58:09
   * @params: [object, javaType]
   * @return: T object
   */
  public static <T> T parseObject(Object object, JavaType javaType) {
    return toJavaObject(toJsonString(object), javaType);
  }


  /**
   * @author: Ares
   * @description: parse entity to json node
   * @time: 2021-10-21 19:38:00
   * @params: [entity] entity
   * @return: com.fasterxml.jackson.databind.JsonNode json node
   */
  public static <T> JsonNode parseObject(T entity) {
    try {
      return OBJECT_MAPPER.readTree(toJsonString(entity));
    } catch (JsonProcessingException e) {
      throw new JsonException("Entity can not convert to java node", e);
    }
  }

  /**
   * @author: Ares
   * @description: convert string to java object
   * @time: 2021-06-10 10:46:00
   * @params: [json, valueType] json string, class
   * @return: T java entity
   */
  public static <T> T toJavaObject(String json, Class<T> valueType) {
    try {
      return OBJECT_MAPPER.readValue(json, valueType);
    } catch (JsonProcessingException e) {
      throw new JsonException("Json string can not convert to java object", e);
    }
  }

  /**
   * @author: Ares
   * @description: convert byte array to java object
   * @time: 2021-12-01 23:17:00
   * @params: [bytes, valueType] byte array, class
   * @return: T java entity
   */
  public static <T> T toJavaObject(byte[] bytes, Class<T> type) {
    try {
      return OBJECT_MAPPER.readValue(bytes, type);
    } catch (Exception e) {
      throw new JsonException("Byte array can not convert to java object", e);
    }
  }

  /**
   * @author: Ares
   * @description: java object convert to byte(when fail use toString and getBytes)
   * @time: 2021-12-01 23:12:00
   * @params: [object] java object
   * @return: byte[] byte array
   */
  public static byte[] toBytes(Object object) {
    try {
      return OBJECT_MAPPER.writeValueAsBytes(object);
    } catch (JsonProcessingException e) {
      LOGGER.warn("java object convert to byte array exception: ", e);
      return String.valueOf(object).getBytes(Charset.defaultCharset());
    }
  }

  /**
   * @author: Ares
   * @description: parse json array convert to list
   * @time: 2021-10-25 14:10:00
   * @params: [json, valueType] json string, raw class
   * @return: java.util.List<T> list of java entity
   */
  public static <T> List<T> parseArray(String json, Class<T> valueType) {
    try {
      JavaType javaType = OBJECT_MAPPER.getTypeFactory()
          .constructParametricType(List.class, valueType);
      return OBJECT_MAPPER.readValue(json, javaType);
    } catch (JsonProcessingException e) {
      throw new JsonException("Json array string can not convert to list of java object", e);
    }
  }


  /**
   * @author: Ares
   * @description: convert json string to string map
   * @time: 2021-12-23 23:03:46
   * @params: [json] json string
   * @return: java.util.Map<java.lang.String, java.lang.String> out 出参
   */
  public static Map<String, String> stringMapFromJsonString(String json) {
    if (null == json) {
      return null;
    }
    try {
      return OBJECT_MAPPER.readValue(json, STRING_MAP_TYPE_REFERENCE);
    } catch (IOException e) {
      throw new JsonException("Json string can not convert to string map", e);
    }
  }


  /**
   * @author: Ares
   * @description: parse byte array to list
   * @time: 2021-12-01 23:20:00
   * @params: [bytes, valueType] byte array, raw type
   * @return: java.util.List<T> list of java entity
   */
  public static <T> List<T> parseArray(byte[] bytes, Class<T> valueType) {
    try {
      JavaType javaType = OBJECT_MAPPER.getTypeFactory()
          .constructCollectionType(List.class, valueType);
      return OBJECT_MAPPER.readValue(bytes, javaType);
    } catch (IOException e) {
      throw new JsonException("Byte array can not convert to list of java object", e);
    }
  }

}
