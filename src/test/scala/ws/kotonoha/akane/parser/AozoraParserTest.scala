package ws.kotonoha.akane.parser


class AozoraParserTest extends org.scalatest.FunSuite with org.scalatest.matchers.ShouldMatchers {
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
    nodes should have length (2)
  }

  test("highlight") {
    val inp = new AozoraStringInput("「怪異を知ると怪異に絡む［＃「怪異を知ると怪異に絡む」に傍点］――ですからね。巻き込むのならともかく――そっちが本筋になってしまえば、むしろ巻き込まれるのは阿良々木さんということになります」")
    val parser = new AozoraParser(inp)
    val nodes = parser.toList
    nodes should have length (2)
  }

  test("weird furigana") {
    val inp = new AozoraStringInput("その人垣を睨《にら》みつける。")
    val parser = new AozoraParser(inp)
    val nodes = parser.toList
    nodes foreach (println(_))
  }
}
