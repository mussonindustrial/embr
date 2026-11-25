import { describe, expect, it, vi } from 'vitest'
import { createDelegateEvents } from '../../src/api/delegateEvents'

describe('createDelegateEvents', () => {
  it('calls the handlers of the emitted event', () => {
    const events = createDelegateEvents()
    const rows = vi.fn()
    const other = vi.fn()

    events.on('rows', rows)
    events.on('other', other)
    events.emit('rows', { page: 2 })

    expect(rows).toHaveBeenCalledWith({ page: 2 })
    expect(other).not.toHaveBeenCalled()
  })

  it('stops calling a handler that stopped listening', () => {
    const events = createDelegateEvents()
    const first = vi.fn()
    const second = vi.fn()

    const stop = events.on('rows', first)
    events.on('rows', second)
    stop()
    events.emit('rows', {})

    expect(first).not.toHaveBeenCalled()
    expect(second).toHaveBeenCalledOnce()
  })
})
