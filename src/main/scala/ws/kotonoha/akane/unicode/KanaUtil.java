/*
 * Copyright 2012 eiennohito
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

package ws.kotonoha.akane.unicode;

/**
 * @author eiennohito
 * @since 22.10.12
 */

/**
 * Miscellaneous text utilities
 */
public class KanaUtil {
  /**
   * Converts all Katakana characters to hiragana characters
   */
  public static String kataToHira(String input) {
    StringBuffer buf = new StringBuffer(input);
    int length = buf.length();
    for (int i = 0; i < length; ++i) {
      char ch = buf.charAt(i);
      //if katakana then convert to hiragana
      if (ch >= 0x30a1 && ch < 0x30f4) {
        buf.setCharAt(i, (char)(ch - 96));
      }
    }
    return buf.toString();
  }
}
