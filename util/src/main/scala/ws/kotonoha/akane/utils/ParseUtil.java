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

package ws.kotonoha.akane.utils;

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
