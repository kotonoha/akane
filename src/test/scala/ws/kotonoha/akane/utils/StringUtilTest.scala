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

package ws.kotonoha.akane.utils

import org.scalatest.FreeSpec
import org.scalatest.matchers.ShouldMatchers

/**
 * @author eiennohito
 * @since 30.10.12 
 */

class StringUtilTest extends FreeSpec with ShouldMatchers {
  "common tail" - {
    "returns 3 on mayhem and anhem" in {
      StringUtil.commonTail("mayhem", "anhem") should equal (3)
    }

    "returns 0 on dic and dim" in {
      StringUtil.commonTail("dic", "dim") should equal (0)
    }

    "returns 3 on dic and fdic" in {
      StringUtil.commonTail("dic", "fdic") should equal (3)
    }

    "returns 4 on xfdic and fdic" in {
      StringUtil.commonTail("xfdic", "fdic") should equal (4)
    }
  }
}
