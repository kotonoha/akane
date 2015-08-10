package ws.kotonoha.akane;

/**
 * @author eiennohito
 * @since 15/08/10
 */
public class ParseUtil {
  public static int parseInt(CharSequence cs) {
    final int offset = 0;
    final int strLen = cs.length();
    return parseInt(cs, offset, strLen);
  }

  public static int parseInt(CharSequence cs, int offset, int strLen) {
    char c;
    boolean positive = true;
    if ((c = cs.charAt(offset)) == '-') {
      positive = false;
      offset++;
    } else if (c == '+') {
      offset++;
    }

    int value = 0;
    while (offset < strLen) {
      c = cs.charAt(offset++);
      if (c >= '0' && c <= '9') {
        value = value * 10 + (c - '0');
      } else {
        break;
      }
    }
    return positive ? value : -value;
  }
}
