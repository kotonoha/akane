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

package ws.kotonoha.akane.parser


class AozoraParserTest extends org.scalatest.FunSuite with org.scalatest.Matchers {
  test("parses something") {
    val inp = new AozoraStringInput("片足を棺桶《かんおけ》に突っ込んでる")
    val parser = new AozoraParser(inp)
    val nodes = parser.toList
    nodes should have length (1)
  }

  test("two sentences") {
    val inp = new AozoraStringInput("蟹に遭った戦場ヶ原も。\n　蝸牛に迷った八九寺も。")
    val parser = new AozoraParser(inp)
    val nodes = parser.toList
    nodes should have length (3)
  }

  test("highlight") {
    val inp = new AozoraStringInput("「怪異を知ると怪異に絡む［＃「怪異を知ると怪異に絡む」に傍点］――ですからね。巻き込むのならともかく――そっちが本筋になってしまえば、むしろ巻き込まれるのは阿良々木さんということになります」")
    val parser = new AozoraParser(inp)
    val nodes = parser.toList
    nodes should have length (3)
  }

  test("weird furigana") {
    val inp = new AozoraStringInput("その人垣を睨《にら》みつける。")
    val parser = new AozoraParser(inp)
    val nodes = parser.toList
    nodes should not be empty
  }

  test("2 bakutens a row") {
    val inp = new AozoraStringInput("やい［＃「やい」に傍点］てる［＃「てる」に傍点］のね。")
    val parser = new AozoraParser(inp)
    val nodes = parser.toList
    nodes should not be empty
  }
}
