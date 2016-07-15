/*
 * Copyright 2012-2016 eiennohito (Tolmachev Arseny)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
