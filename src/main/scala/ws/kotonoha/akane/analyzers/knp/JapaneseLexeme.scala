package ws.kotonoha.akane.analyzers.knp

import ws.kotonoha.akane.analyzers.juman.JumanStylePos

/**
 * @author eiennohito
 * @since 2015/09/18
 */
trait JapaneseLexeme extends FeatureAccess {
  def surface: String
  def reading: String
  def dicForm: String
  def pos: JumanStylePos
  def info: String
  //def tags: Seq[String]
  def canonicForm(): String
}
