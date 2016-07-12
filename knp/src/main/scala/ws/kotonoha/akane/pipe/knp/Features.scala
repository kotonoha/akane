package ws.kotonoha.akane.pipe.knp

import ws.kotonoha.akane.analyzers.knp.FeatureAccess

/**
 * @author eiennohito
 * @since 15/07/14
 */
object Features {

  val yougen: String = "用言"
  val prefix: String = "接頭"
  val predArg = "格解析結果"
  val normalizedWriting = "正規化代表表記"

  def isYougen(api: FeatureAccess): Boolean = api.featureExists(yougen)

  def normWriting(floc: FeatureAccess) = {
    floc.valueOfFeature(normalizedWriting)
  }
}
