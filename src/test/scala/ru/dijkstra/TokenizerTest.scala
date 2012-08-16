package ru.dijkstra.ranobe_parser

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{FlatSpec, FreeSpec}
import tokenizer.Tokenizer


class TokenizerTest extends FlatSpec with ShouldMatchers {
  import ru.dijkstra.ranobe_parser.tokenizer._
  "BuildToken" should " recognize kanji extends" in {
    Tokenizer.buildToken(new StringBuilder("馬鹿")) should equal (KanjiExtent("馬鹿")::Nil)
    Tokenizer.buildToken(new StringBuilder("阿良々木")) should equal (KanjiExtent("阿良々木")::Nil)
    Tokenizer.buildToken(new StringBuilder("１匹")) should equal (KanjiExtent("１匹")::Nil)
  }
  it should "recognize kana extends" in {
    Tokenizer.buildToken(new StringBuilder("ばか")) should equal (KanaExtent("ばか")::Nil)
    Tokenizer.buildToken(new StringBuilder("バカ")) should equal (KanaExtent("バカ")::Nil)
  }
  it should "recognize html tags" in {
    Tokenizer.buildToken(new StringBuilder("<html>")) should equal (HtmlTag("<html>")::Nil)
    Tokenizer.buildToken(new StringBuilder("""<html><\html>""")) should equal (HtmlTag("<html>")::HtmlTag("""<\html>""")::Nil)
  }
  it should "recognize punctuation" in {
    Tokenizer.buildToken(new StringBuilder("。")) should equal (Punctuation('。')::NewSentence::Nil)
    Tokenizer.buildToken(new StringBuilder("。!")) should equal (Punctuation('。')::NewSentence::Punctuation('!')::NewSentence::Nil)
  }
}
