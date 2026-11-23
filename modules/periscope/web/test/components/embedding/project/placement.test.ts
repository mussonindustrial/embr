import { describe, expect, it } from 'vitest'
import type { Bounds, Values } from '../../../../src/api/types'
import {
  childPositionAt,
  slotOptions,
} from '../../../../src/components/embedding/project/placement'

function zoneWith(positionChild: (bounds: Bounds) => Values) {
  const zone = {
    offsetWidth: 400,
    clientWidth: 400,
    clientHeight: 300,
    clientLeft: 0,
    clientTop: 0,
    scrollLeft: 0,
    scrollTop: 0,
    getAttribute: () => '{"region":"content"}',
    getBoundingClientRect: () => ({ left: 10, top: 20, width: 400 }),
  } as unknown as Element

  slotOptions.set(zone, {
    positionChild: ({ bounds }) => positionChild(bounds),
  })
  return zone
}

const client = { x: 60, y: 70, width: 120, height: 40 }

describe('childPositionAt', () => {
  it('leaves out an unchanged size on a move', () => {
    const zone = zoneWith((bounds) => bounds)

    expect(childPositionAt(zone, client, 'move')).toEqual({
      region: 'content',
      x: 50,
      y: 50,
    })
  })

  it('leaves out an unchanged size on a drop', () => {
    const zone = zoneWith((bounds) => bounds)

    expect(childPositionAt(zone, client, 'drop')).toEqual({
      region: 'content',
      x: 50,
      y: 50,
    })
  })

  it('keeps a size the slot changes on a move', () => {
    const zone = zoneWith((bounds) => ({ ...bounds, width: 200 }))

    expect(childPositionAt(zone, client, 'move')).toEqual({
      region: 'content',
      x: 50,
      y: 50,
      width: 200,
    })
  })

  it('keeps the size on a resize', () => {
    const zone = zoneWith((bounds) => bounds)

    expect(childPositionAt(zone, client, 'resize')).toEqual({
      region: 'content',
      x: 50,
      y: 50,
      width: 120,
      height: 40,
    })
  })
})
