package ru.dijkstra.ranobe_parser

import ast.Node

/**
 * @author eiennohito
 * @since 15.08.12
 */

trait Renderer {
  def render(r: Node): Renderer
}
