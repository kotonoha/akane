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

package ws.kotonoha.akane.pipe.knp

import org.scalatest.{FreeSpec, Matchers}
import org.scalatest.matchers.ShouldMatchers
import ws.kotonoha.akane.pipe.knp.lisp._

import scala.util.parsing.input.{CharSequenceReader, Reader}
import java.io.InputStreamReader

import org.apache.commons.io.IOUtils
import ws.kotonoha.akane.helpers.lisp.{KList, LispParser}

/**
 * @author eiennohito
 * @since 2013-08-12
 */

class KnpExecutorTest extends FreeSpec with Matchers {
  import scala.concurrent.ExecutionContext.Implicits.global
  "knp executor" - {
    "works" ignore {
      val knp = KnpTreePipeParser()
      val lines = knp.parse("私は何も知りません")
      //lines.foreach(println)
      lines should not be empty
      knp.close()
    }
  }
}

class KnpParserTest extends FreeSpec with Matchers {
  implicit def input(s: String): Reader[Char] = {
    new CharSequenceReader(s, 0)
  }

  def parseString(s: String): KList = LispParser.list(s) match {
    case LispParser.Success(s, _) => s.asInstanceOf[KList]
    case x => throw new RuntimeException(x.toString)
  }

  val smallResult = """((2 (type:D) ((回る まわる 回る 動詞 2 * 0 子音動詞ラ行 10 基本形 2 "代表表記:回る/まわる 付属動詞候補（基本） 自他動詞:他:回す/まわす" (代表表記:回る/まわる 付属動詞候補（基本） 自他動詞:他:回す/まわす 正規化代表表記:回る/まわる 文末 表現文末 かな漢字 活用語 自立 内容語 タグ単位始 文節始 文節主辞))) (文末 用言:動 レベル:C 区切:5-5 ID:（文末） 提題受:30 主節 動態述語 正規化代表表記:回る/まわる 主辞代表表記:回る/まわる) NIL)
    ((1 (type:D) ((町 まち 町 名詞 6 普通名詞 1 * 0 * 0 "代表表記:町/まち 漢字読み:訓 住所末尾 カテゴリ:組織・団体;場所-その他 ドメイン:政治" (代表表記:町/まち 漢字読み:訓 住所末尾 カテゴリ:組織・団体;場所-その他 ドメイン:政治 正規化代表表記:町/まち 漢字 かな漢字 名詞相当語 自立 内容語 タグ単位始 文節始 文節主辞))(が が が 助詞 9 格助詞 1 * 0 * 0 NIL (かな漢字 ひらがな 付属 ))) (SM-主体 SM-組織 ガ 助詞 体言 一文字漢字 係:ガ格 区切:0-0 格要素 連用要素 正規化代表表記:町/まち 主辞代表表記:町/まち) NIL))
    ((0 (type:D) ((それ それ それ 指示詞 7 名詞形態指示詞 1 * 0 * 0 "疑似代表表記 代表表記:それ/それ" (疑似代表表記 代表表記:それ/それ 正規化代表表記:それ/それ 文頭 かな漢字 ひらがな 自立 内容語 タグ単位始 文節始 文節主辞))(でも でも でも 助詞 9 副助詞 2 * 0 * 0 NIL (かな漢字 ひらがな 付属 ))) (文頭 デモ 助詞 体言 指示詞 修飾 係:デ格 並キ:名:&ST:2.5&&デモ 区切:1-4 並列タイプ:AND 格要素 連用要素 正規化代表表記:それ/それ 主辞代表表記:それ/それ 並列類似度:-100.000) NIL)))"""

  "knp parser" - {
    "parses surface" in {
      val nodes = parseString("(繰り返す くりかえす 繰り返す 動詞 2 * 0 子音動詞サ行 5 基本形 2 \"代表表記:繰り返す/くりかえす 補文ト\" (代表表記:繰り返す/くりかえす 補文ト 正規化代表表記:繰り返す/くりかえす 文末 表現文末 かな漢字 活用語 自立 内容語 タグ単位始 文節始 文節主辞))) (文末 補文ト 用言:動 レベル:C 区切:5-5 ID:（文末） 提題受:30 主節 動態述語 正規化代表表記:繰り返す/くりかえす 主辞代表表記:繰り返す/くりかえす) NIL)")
      val res = KnpSexpParser.parseSurface(nodes :: Nil)
      res should not be Nil
      val item :: Nil = res
      item.surface should be ("繰り返す")
    }

    "parses small tree" in {
      val sexp = parseString(smallResult)
      val res = KnpSexpParser.parseTree(sexp)
      res should not be None
    }

    "parses a knp output" in {
      val resource = getClass.getClassLoader.getResourceAsStream("knp.answer.txt")
      val reader = new InputStreamReader(resource, "utf-8")
      val sexp = parseString(IOUtils.toString(reader))
      reader.close()
      val tree = KnpSexpParser.parseTree(sexp)
      tree should not be None
    }
  }
}
