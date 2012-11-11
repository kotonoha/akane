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

package ws.kotonoha.akane.bridj

import org.bridj.Pointer
import java.nio.charset.Charset

/**
 * @author eiennohito
 * @since 11.11.12 
 */

class RichPointer[T](p: Pointer[T]) {
  def u8s = p.getStringAtOffset(0L, Pointer.StringType.C, Charset.forName("UTF-8"))
}

object PointerUtil {
  implicit def ponter2richPointer[T](p: Pointer[T]) = new RichPointer[T](p)
}
