package ws.kotonoha.akane.pipe.knp

import ws.kotonoha.akane.analyzers.knp.FeatureAccess

/**
 * @author eiennohito
 * @since 15/07/14
 */
object Features {
  val predArg = "格解析結果"

  val normalizedWriting = "正規化代表表記"

  def normWriting(floc: FeatureAccess) = {
    floc.findFeature(normalizedWriting)
  }
}
