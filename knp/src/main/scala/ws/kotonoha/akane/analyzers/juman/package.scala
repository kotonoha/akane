package ws.kotonoha.akane.analyzers

package object juman {
  @deprecated("use JumanMorpheme", "2018.03.30")
  type JumanLexeme = JumanMorpheme
  @deprecated("use JumanMorpheme", "2018.03.30")
  def JumanLexeme = JumanMorpheme

  @deprecated("use JumanFeature", "2018.03.30")
  type JumanOption = JumanFeature
  @deprecated("use JumanFeature", "2018.03.30")
  def JumanOption = JumanFeature

  @deprecated("use JumanSentence", "2018.03.30")
  type JumanSequence = JumanSentence
  @deprecated("use JumanSentence", "2018.03.30")
  def JumanSequence = JumanSentence
}
