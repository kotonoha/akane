package ws.kotonoha.akane

import ast.Node

/**
 * @author eiennohito
 * @since 15.08.12
 */

trait Renderer {
  def render(r: Node): Renderer
}
