import sbt._

/**
 * @author eiennohito
 * @since 16.08.12 
 */
class ParserBuild extends Build {
  lazy val root = Project("ranobe-parser", file("."))
}
