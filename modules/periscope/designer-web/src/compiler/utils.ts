import { Position } from './protocol'

export function positionOf(source: string, offset: number): Position {
  const lines = source.slice(0, offset).split('\n')

  const line = lines.length
  const column = lines[lines.length - 1].length + 1

  return {
    line,
    column,
    offset,
  }
}
