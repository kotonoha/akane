package ws.kotonoha.akane.analyzers.juman

/**
 * @author eiennohito
 * @since 2013-09-04
 */
trait JumanStylePos {
  def pos: Int
  def subpos: Int
  def category: Int
  def conjugation: Int
}
