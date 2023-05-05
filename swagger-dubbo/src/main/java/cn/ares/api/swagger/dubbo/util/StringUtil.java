package cn.ares.api.swagger.dubbo.util;

import static cn.ares.api.swagger.dubbo.constant.SymbolConstant.MINUS;
import static cn.ares.api.swagger.dubbo.constant.SymbolConstant.SPACE;
import static cn.ares.api.swagger.dubbo.constant.SymbolConstant.UNDERLINE;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author: Ares
 * @time: 2021-05-13 10:14:00
 * @description: 字符串工具类
 * @version: JDK 1.8
 */
public class StringUtil {

  private static final int INDEX_NOT_FOUND = -1;
  private static final int PAD_LIMIT = 8192;

  public static boolean isNotEmpty(String s) {
    return !isEmpty(s);
  }

  public static boolean isNotBlank(String s) {
    return !isBlank(s);
  }

  public static boolean isBlank(String s) {
    return null == s || s.trim().isEmpty();
  }

  public static boolean isEmpty(String s) {
    return null == s || s.isEmpty();
  }

  /**
   * @author: Ares
   * @description: 下划线转大驼峰式
   * @time: 2019-06-13 17:31:00
   * @params: [source] 请求参数
   * @return: java.lang.String 响应参数
   */
  public static String underlineToBigCamelCase(String source) {
    if (isEmpty(source)) {
      return source;
    }
    List<String> splitList = listSplit(source, UNDERLINE);
    StringBuilder stringBuffer = new StringBuilder();
    for (String s : splitList) {
      stringBuffer.append(upperFirst(s.toLowerCase()));
    }
    return String.valueOf(stringBuffer);
  }

  /**
   * @author: Ares
   * @description: 中划线转大驼峰式
   * @time: 2019-03-24 13:34:00
   * @params: [source] 请求参数
   * @return: java.lang.String 响应参数
   */
  public static String strikeToBigCamelCase(String source) {
    if (isEmpty(source)) {
      return source;
    }
    List<String> splitList = listSplit(source, MINUS);
    StringBuilder stringBuffer = new StringBuilder();
    for (String s : splitList) {
      stringBuffer.append(upperFirst(s.toLowerCase()));
    }
    return String.valueOf(stringBuffer);
  }

  /**
   * @author: Ares
   * @description: 中划线转小驼峰式
   * @time: 2019-06-13 17:31:00
   * @params: [source] 请求参数
   * @return: java.lang.String 响应参数
   */
  public static String strikeToLittleCamelCase(String source) {
    return lowerFirst(strikeToBigCamelCase(source));
  }

  /**
   * @author: Ares
   * @description: 中划线转下划线
   * @time: 2021-12-13 20:04:00
   * @params: [source] request
   * @return: java.lang.String response
   */
  public static String strikeToUnderline(String source) {
    if (isEmpty(source)) {
      return source;
    }
    List<String> splitList = listSplit(source, MINUS);
    StringJoiner result = new StringJoiner(UNDERLINE);
    for (String s : splitList) {
      result.add(s);
    }
    return String.valueOf(result);
  }

  /**
   * @author: Ares
   * @description: 下划线转小驼峰式
   * @time: 2019-06-13 17:31:00
   * @params: [source] 请求参数
   * @return: java.lang.String 响应参数
   */
  public static String underlineToLittleCamelCase(String source) {
    return lowerFirst(underlineToBigCamelCase(source));
  }

  /**
   * @author: Ares
   * @description: 首字母大写
   * @time: 2019-06-13 17:31:00
   * @params: [source] 请求参数
   * @return: java.lang.String 响应参数
   */
  public static String upperFirst(String source) {
    if (isEmpty(source)) {
      return source;
    }
    char[] chars = source.toCharArray();
    chars[0] = 97 <= chars[0] && chars[0] <= 122 ? (char) (chars[0] - 32) : chars[0];
    return String.valueOf(chars);
  }

  /**
   * @author: Ares
   * @description: 首字母小写
   * @time: 2019-06-13 17:33:00
   * @params: [source] 请求参数
   * @return: java.lang.String 响应参数
   */
  public static String lowerFirst(String source) {
    if (isEmpty(source)) {
      return source;
    }
    char[] chars = source.toCharArray();
    chars[0] = 65 <= chars[0] && chars[0] <= 90 ? (char) (chars[0] + 32) : chars[0];
    return String.valueOf(chars);
  }

  /**
   * @author: Ares
   * @description: 校验邮箱格式
   * @time: 2019-11-02 16:43:00
   * @params: [email] 请求参数
   * @return: boolean 响应参数
   */
  public static boolean validateEmailFormat(String email) {
    if (isEmpty(email)) {
      return false;
    }
    String regex = "^([a-z0-9A-Z]+[-|.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(email);
    return matcher.matches();
  }

  public static String trim(String originStr) {
    return trim(originStr, " ");
  }


  /**
   * @author: Ares
   * @description: 去除原始字符串中开头和结尾的指定字符串(多个都会去除)
   * @time: 2020-09-28 14:46:00
   * @params: [originStr, trim] 请求参数
   * @return: java.lang.String 响应参数
   */
  public static String trim(String originStr, String trim) {
    if (isEmpty(originStr)) {
      return originStr;
    }
    String result = trimStart(originStr, trim);
    return trimEnd(result, trim);
  }

  private static String trimStart(String str, String trim) {
    if (null == str || isEmpty(trim)) {
      return str;
    }
    while (true) {
      if (str.startsWith(trim)) {
        str = str.substring(trim.length());
      } else {
        break;
      }
    }
    return str;
  }

  private static String trimEnd(String str, String trim) {
    if (null == str || isEmpty(trim)) {
      return str;
    }
    while (true) {
      if (str.endsWith(trim)) {
        str = str.substring(0, str.length() - trim.length());
      } else {
        break;
      }
    }
    return str;
  }


  public static List<String> listSplit(String str, String separator) {
    return listSplit(str, separator, true);
  }

  public static String[] split(String str, String separator) {
    List<String> listSplit = listSplit(str, separator);
    String[] strArray = new String[listSplit.size()];
    listSplit.toArray(strArray);
    return strArray;
  }

  /**
   * @author: Ares
   * @description: 按照指定字符串分割源字符串
   * @time: 2021-05-21 17:04:00
   * @params: [str, delimiter, allowStrEmpty] 请求参数
   * @return: java.util.List<java.lang.String> 响应参数
   */
  public static List<String> listSplit(String str, String separator, boolean allowStrEmpty) {
    if (str == null) {
      return null;
    } else {
      int len = str.length();
      if (len == 0) {
        return Collections.emptyList();
      } else if (separator != null && !"".equals(separator)) {
        int separatorLength = separator.length();
        List<String> substrings = new ArrayList<>();
        int numberOfSubstrings = 0;
        int beg = 0;
        int end = 0;

        while (end < len) {
          end = str.indexOf(separator, beg);
          if (end > -1) {
            boolean flag = allowStrEmpty ? end >= beg : end > beg;
            if (flag) {
              ++numberOfSubstrings;
              if (numberOfSubstrings == -1) {
                end = len;
                substrings.add(str.substring(beg));
              } else {
                substrings.add(str.substring(beg, end));
                beg = end + separatorLength;
              }
            } else {

              beg = end + separatorLength;
            }
          } else {
            substrings.add(str.substring(beg));
            end = len;
          }
        }

        return substrings;
      } else {
        {
          List<String> list = new ArrayList<>();
          int sizePlus1 = 1;
          int i = 0;
          int start = 0;
          boolean match = false;
          if (separator != null) {
            label:
            while (true) {
              while (true) {
                if (i >= len) {
                  break label;
                }

                match = true;
                ++i;
              }
            }
          } else {
            label:
            while (true) {
              while (true) {
                if (i >= len) {
                  break label;
                }

                if (Character.isWhitespace(str.charAt(i))) {
                  if (match) {
                    if (sizePlus1++ == -1) {
                      i = len;
                    }

                    list.add(str.substring(start, i));
                    match = false;
                  }

                  ++i;
                  start = i;
                } else {
                  match = true;
                  ++i;
                }
              }
            }
          }

          if (match) {
            list.add(str.substring(start, i));
          }

          return list;
        }
      }
    }
  }

  /**
   * @author: Ares
   * @description: 判断字符序列是否为数字
   * @time: 2021-06-07 16:51:00
   * @params: [cs] 请求参数
   * @return: boolean 响应参数
   */
  public static boolean isNumeric(CharSequence cs) {
    if (isEmpty(cs)) {
      return false;
    } else {
      int sz = cs.length();

      for (int i = 0; i < sz; ++i) {
        if (!Character.isDigit(cs.charAt(i))) {
          return false;
        }
      }

      return true;
    }
  }

  public static boolean isEmpty(CharSequence cs) {
    return cs == null || cs.length() == 0;
  }

  public static String parseString(Object object) {
    if (null == object) {
      return null;
    }
    return object.toString();
  }

  public static String parseStringOrDefault(Object object, String defaultValue) {
    if (null == object) {
      return defaultValue;
    }
    return object.toString();
  }


  public static String parseSteamToString(InputStream inputStream) {
    if (null == inputStream) {
      throw new IllegalArgumentException("inputStream is null");
    }
    return new BufferedReader(new InputStreamReader(inputStream))
        .lines().parallel().collect(Collectors.joining(System.lineSeparator()));
  }

  public static InputStream parseStringToStream(String content, Charset charset) {
    if (isEmpty(content)) {
      throw new IllegalArgumentException("content is empty");
    }
    return new ByteArrayInputStream(content.getBytes(charset));
  }

  public static String random(int count) {
    return random(count, true, true);
  }

  public static String random(int count, boolean letters, boolean numbers) {
    return random(count, 0, 0, letters, numbers);
  }

  public static String random(int count, int start, int end, boolean letters, boolean numbers) {
    return random(count, start, end, letters, numbers, null);
  }

  public static String random(int count, int start, int end, final boolean letters,
      final boolean numbers, final char[] chars) {
    if (count == 0) {
      return "";
    } else if (count < 0) {
      throw new IllegalArgumentException(
          "Requested random string length " + count + " is less than 0.");
    }
    if (chars != null && chars.length == 0) {
      throw new IllegalArgumentException("The chars array must not be empty");
    }

    if (start == 0 && end == 0) {
      if (chars != null) {
        end = chars.length;
      } else if (!letters && !numbers) {
        end = Character.MAX_CODE_POINT;
      } else {
        end = 'z' + 1;
        start = ' ';
      }
    } else if (end <= start) {
      throw new IllegalArgumentException(
          "Parameter end (" + end + ") must be greater than start (" + start + ")");
    }

    final int zeroDigitAscii = 48;
    final int firstLetterAscii = 65;

    if (chars == null && (numbers && end <= zeroDigitAscii
        || letters && end <= firstLetterAscii)) {
      throw new IllegalArgumentException(
          "Parameter end (" + end + ") must be greater then (" + zeroDigitAscii
              + ") for generating digits " +
              "or greater then (" + firstLetterAscii + ") for generating letters.");
    }

    final StringBuilder builder = new StringBuilder(count);
    final int gap = end - start;

    while (count-- != 0) {
      final int codePoint;
      if (chars == null) {
        codePoint = ThreadLocalRandom.current().nextInt(gap) + start;

        switch (Character.getType(codePoint)) {
          case Character.UNASSIGNED:
          case Character.PRIVATE_USE:
          case Character.SURROGATE:
            count++;
            continue;
          default:
        }

      } else {
        codePoint = chars[ThreadLocalRandom.current().nextInt(gap) + start];
      }

      final int numberOfChars = Character.charCount(codePoint);
      if (count == 0 && numberOfChars > 1) {
        count++;
        continue;
      }

      if (letters && Character.isLetter(codePoint) || numbers && Character.isDigit(codePoint)
          || !letters && !numbers) {
        builder.appendCodePoint(codePoint);

        if (numberOfChars == 2) {
          count--;
        }

      } else {
        count++;
      }
    }
    return builder.toString();
  }

  public static void setIfPresent(String value, Consumer<String> setter) {
    if (isNotEmpty(value)) {
      setter.accept(value);
    }
  }

  public static String leftPad(final String str, final int size) {
    return leftPad(str, size, ' ');
  }


  public static String leftPad(final String str, final int size, final char padChar) {
    if (str == null) {
      return null;
    }
    final int pads = size - str.length();
    if (pads <= 0) {
      return str;
    }
    if (pads > PAD_LIMIT) {
      return leftPad(str, size, String.valueOf(padChar));
    }
    return repeat(padChar, pads).concat(str);
  }

  public static String leftPad(final String str, final int size, String padStr) {
    if (str == null) {
      return null;
    }
    if (isEmpty(padStr)) {
      padStr = SPACE;
    }
    final int padLen = padStr.length();
    final int strLen = str.length();
    final int pads = size - strLen;
    if (pads <= 0) {
      // returns original String when possible
      return str;
    }
    if (padLen == 1 && pads <= PAD_LIMIT) {
      return leftPad(str, size, padStr.charAt(0));
    }

    if (pads == padLen) {
      return padStr.concat(str);
    } else if (pads < padLen) {
      return padStr.substring(0, pads).concat(str);
    } else {
      final char[] padding = new char[pads];
      final char[] padChars = padStr.toCharArray();
      for (int i = 0; i < pads; i++) {
        padding[i] = padChars[i % padLen];
      }
      return new String(padding).concat(str);
    }
  }

  public static String repeat(final char ch, final int repeat) {
    if (repeat <= 0) {
      return "";
    }
    final char[] buf = new char[repeat];
    Arrays.fill(buf, ch);
    return new String(buf);
  }

  public static String join(final char delimiter, final String... strings) {
    if (strings.length == 0) {
      return null;
    }
    if (strings.length == 1) {
      return strings[0];
    }
    int length = strings.length - 1;
    for (final String s : strings) {
      if (s == null) {
        continue;
      }
      length += s.length();
    }
    final StringBuilder sb = new StringBuilder(length);
    if (strings[0] != null) {
      sb.append(strings[0]);
    }
    for (int i = 1; i < strings.length; ++i) {
      if (!isEmpty(strings[i])) {
        sb.append(delimiter).append(strings[i]);
      } else {
        sb.append(delimiter);
      }
    }
    return sb.toString();
  }

  public static String replace(final String text, final String searchString,
      final String replacement) {
    return replace(text, searchString, replacement, -1);
  }

  public static String replace(final String text, final String searchString,
      final String replacement, final int max) {
    return replace(text, searchString, replacement, max, false);
  }

  private static String replace(final String text, String searchString, final String replacement,
      int max, final boolean ignoreCase) {
    if (isEmpty(text) || isEmpty(searchString) || replacement == null || max == 0) {
      return text;
    }
    if (ignoreCase) {
      searchString = searchString.toLowerCase();
    }
    int start = 0;
    int end = ignoreCase ? indexOfIgnoreCase(text, searchString, start)
        : indexOf(text, searchString, start);
    if (end == INDEX_NOT_FOUND) {
      return text;
    }
    final int replLength = searchString.length();
    int increase = Math.max(replacement.length() - replLength, 0);
    increase *= max < 0 ? 16 : Math.min(max, 64);
    final StringBuilder buf = new StringBuilder(text.length() + increase);
    while (end != INDEX_NOT_FOUND) {
      buf.append(text, start, end).append(replacement);
      start = end + replLength;
      if (--max == 0) {
        break;
      }
      end = ignoreCase ? indexOfIgnoreCase(text, searchString, start)
          : indexOf(text, searchString, start);
    }
    buf.append(text, start, text.length());
    return buf.toString();
  }

  public static int indexOfIgnoreCase(final CharSequence str, final CharSequence searchStr) {
    return indexOfIgnoreCase(str, searchStr, 0);
  }

  public static int indexOfIgnoreCase(final CharSequence str, final CharSequence searchStr,
      int startPos) {
    if (str == null || searchStr == null) {
      return INDEX_NOT_FOUND;
    }
    if (startPos < 0) {
      startPos = 0;
    }
    final int endLimit = str.length() - searchStr.length() + 1;
    if (startPos > endLimit) {
      return INDEX_NOT_FOUND;
    }
    if (searchStr.length() == 0) {
      return startPos;
    }
    for (int i = startPos; i < endLimit; i++) {
      if (regionMatches(str, i, searchStr, searchStr.length())) {
        return i;
      }
    }
    return INDEX_NOT_FOUND;
  }

  public static int indexOf(final CharSequence sequence, final CharSequence searchSequence,
      final int startPos) {
    if (sequence == null || searchSequence == null) {
      return INDEX_NOT_FOUND;
    }
    if (sequence instanceof String) {
      return ((String) sequence).indexOf(searchSequence.toString(), startPos);
    } else if (sequence instanceof StringBuilder) {
      return ((StringBuilder) sequence).indexOf(searchSequence.toString(), startPos);
    } else if (sequence instanceof StringBuffer) {
      return ((StringBuffer) sequence).indexOf(searchSequence.toString(), startPos);
    }
    return sequence.toString().indexOf(searchSequence.toString(), startPos);
  }

  private static boolean regionMatches(final CharSequence cs,
      final int thisStart, final CharSequence substring, final int length) {
    if (cs instanceof String && substring instanceof String) {
      return ((String) cs).regionMatches(true, thisStart, (String) substring, 0, length);
    }
    int index1 = thisStart;
    int index2 = 0;
    int tmpLen = length;

    final int srcLen = cs.length() - thisStart;
    final int otherLen = substring.length();

    if ((thisStart < 0) || (length < 0)) {
      return false;
    }

    if (srcLen < length || otherLen < length) {
      return false;
    }

    while (tmpLen-- > 0) {
      final char c1 = cs.charAt(index1++);
      final char c2 = substring.charAt(index2++);

      if (c1 == c2) {
        continue;
      }

      final char u1 = Character.toUpperCase(c1);
      final char u2 = Character.toUpperCase(c2);
      if (u1 != u2 && Character.toLowerCase(u1) != Character.toLowerCase(u2)) {
        return false;
      }
    }

    return true;
  }

}
