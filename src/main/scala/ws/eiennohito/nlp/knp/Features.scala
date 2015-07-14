package ws.eiennohito.nlp.knp

import ws.kotonoha.akane.parser.FeatureLocation

/**
 * @author eiennohito
 * @since 15/07/14
 */
object Features {
  val predArg = "格解析結果"

  val normalizedWriting = "正規化代表表記"

  def normWriting(floc: FeatureLocation) = {
    floc.findFeature(normalizedWriting)
  }
}
