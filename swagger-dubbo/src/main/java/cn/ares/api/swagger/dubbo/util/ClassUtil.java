package cn.ares.api.swagger.dubbo.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Ares
 * @time: 2019-05-08 17:11:00
 * @description: class type util
 * @version: JDK 1.8
 */
public class ClassUtil {

  /**
   * base type wrapper list
   */
  private static final List<String> BASE_WRAP_TYPE_LIST = new ArrayList<>();
  /**
   * base type list
   */
  private static final List<String> BASE_TYPE_LIST = new ArrayList<>();

  static {
    BASE_TYPE_LIST.add("int");
    BASE_TYPE_LIST.add("double");
    BASE_TYPE_LIST.add("long");
    BASE_TYPE_LIST.add("short");
    BASE_TYPE_LIST.add("byte");
    BASE_TYPE_LIST.add("boolean");
    BASE_TYPE_LIST.add("char");
    BASE_TYPE_LIST.add("float");

    BASE_WRAP_TYPE_LIST.add("java.lang.Integer");
    BASE_WRAP_TYPE_LIST.add("java.lang.Double");
    BASE_WRAP_TYPE_LIST.add("java.lang.Float");
    BASE_WRAP_TYPE_LIST.add("java.lang.Long");
    BASE_WRAP_TYPE_LIST.add("java.lang.Short");
    BASE_WRAP_TYPE_LIST.add("java.lang.Byte");
    BASE_WRAP_TYPE_LIST.add("java.lang.Boolean");
    BASE_WRAP_TYPE_LIST.add("java.lang.Character");
  }

  /**
   * @author: Ares
   * @description: judge is base
   * @time: 2019-05-08 17:48:00
   * @params: [className] Class name
   * @return: boolean response
   **/
  public static boolean isPrimitive(String className) {
    return BASE_TYPE_LIST.contains(className);
  }

  /**
   * @author: Ares
   * @description: judge class name is base wrapper
   * @time: 2019-06-14 10:01:00
   * @params: [className] Class name
   * @return: boolean response
   */
  public static boolean isBaseWrap(String className) {
    return BASE_WRAP_TYPE_LIST.contains(className);
  }

  /**
   * @author: Ares
   * @description: judge class is base wrapper
   * @time: 2019-06-14 10:01:00
   * @params: [clazz] class
   * @return: boolean response
   */
  public static boolean isBaseWrap(Class<?> clazz) {
    return isBaseWrap(clazz.getCanonicalName());
  }

  /**
   * @author: Ares
   * @description: judge class name is base or base wrapper
   * @time: 2019-06-14 10:07:00
   * @params: [className] class name
   * @return: boolean response
   */
  public static boolean isBaseOrWrap(String className) {
    return isPrimitive(className) || isBaseWrap(className);
  }

  /**
   * @author: Ares
   * @description: judge class is base or base wrapper
   * @time: 2019-06-14 10:08:00
   * @params: [clazz] class
   * @return: boolean response
   */
  public static boolean isBaseOrWrap(Class<?> clazz) {
    return isBaseOrWrap(clazz.getCanonicalName());
  }

  /**
   * @author: Ares
   * @description: judge class is base or base wrapper or string
   * @time: 2021-07-02 15:21:00
   * @params: [clazz] request
   * @return: boolean response
   */
  public static boolean isBaseOrWrapOrString(Class<?> clazz) {
    return isBaseOrWrap(clazz.getCanonicalName()) || isSameClass(clazz, String.class);
  }

  /**
   * @author: Ares
   * @description: judge object is base or base wrapper
   * @time: 2019-06-14 10:09:00
   * @params: [object] request
   * @return: boolean response
   */
  public static boolean isBaseOrWrap(Object object) {
    return null != object && isBaseOrWrap(object.getClass());
  }

  /**
   * @author: Ares
   * @description: judge two class is same by classloader and canonicalName
   * @time: 2020-08-27 14:50:00
   * @params: [clazz, clz] request
   * @return: boolean response
   */
  public static boolean isSameClass(Class<?> clazz, Class<?> clz) {
    return clazz.isAssignableFrom(clz) && clz.isAssignableFrom(clazz)
        && clazz.getCanonicalName().equals(clz.getCanonicalName())
        && clazz.getClassLoader() == clz.getClassLoader();
  }

  public static String getShortClassName(String className) {
    if (className == null) {
      return null;
    } else {
      String[] ss = className.split("\\.");
      StringBuilder sb = new StringBuilder(className.length());

      for (int i = 0; i < ss.length; ++i) {
        String s = ss[i];
        if (i != ss.length - 1) {
          sb.append(s.charAt(0)).append('.');
        } else {
          sb.append(s);
        }
      }

      return sb.toString();
    }
  }

  /**
   * @author: Ares
   * @description: 判断是否是jdk原生的类型, 如List.class、Map.class
   * @time: 2021-12-13 19:36:00
   * @params: [clazz] request
   * @return: boolean response
   */
  public static boolean isOriginJdkType(Class<?> clazz) {
    return null == clazz.getClassLoader();
  }

}
