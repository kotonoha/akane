package ws.kotonoha.akane.io;

import java.nio.ByteBuffer;

/**
 * @author eiennohito
 * @since 2015/10/07
 */
public class BufferUtils {
  public static int indexOf(final ByteBuffer haystack, byte[] needle) {
    return indexOf(haystack, haystack.position(), haystack.limit(), needle);
  }

  public static int indexOf(final ByteBuffer haystack, final int from, final int to, byte[] needle) {
    final byte[] arr = haystack.array();
    final int blen = needle.length - 1;

    int pos = from;
    int j = 0;

    for (; pos < to; ++ pos) {
      byte c = arr[pos];
      if (c == needle[j]) {
        if (j == blen) {
          return pos - j;
        }
        else j += 1;
      } else if (j != 0) {
        pos -= j;
        j = 0;
      }
    }

    return -1;
  }
}
