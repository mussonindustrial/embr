import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useRef,
  useState,
  useSyncExternalStore,
} from 'react'

import type { Values } from './types'

type Listener = () => void

type PropStore = {
  props: Values
  write(path: string, value: unknown): void
  subscribe(listener: Listener): () => void
  notify(): void
}

export type PropSetter<T> = (value: T) => void

type Draft<T> = { value: T; base: unknown }

const ECHO_TIMEOUT = 2000
const INDEX = /^\d+$/

export function createPropStore(
  write: (path: string, value: unknown) => void
): PropStore {
  const listeners = new Set<Listener>()
  return {
    props: {},
    write,
    subscribe(listener) {
      listeners.add(listener)
      return () => listeners.delete(listener)
    },
    notify() {
      listeners.forEach((listener) => listener())
    },
  }
}

export const PropContext = createContext<PropStore | null>(null)

function toPath(path: string) {
  return path.split(/[.[\]]+/).filter(Boolean)
}

export function getIn(source: unknown, path: string): unknown {
  return toPath(path).reduce<unknown>(
    (value, key) => (value == null ? undefined : (value as Values)[key]),
    source
  )
}

function containerFor(source: unknown, key: string): Values {
  if (Array.isArray(source)) {
    return [...source] as never
  }

  if (source != null && typeof source === 'object') {
    return { ...source } as Values
  }

  return (INDEX.test(key) ? [] : {}) as Values
}

export function setIn(
  source: unknown,
  keys: string[],
  value: unknown
): unknown {
  if (keys.length === 0) {
    return value
  }

  const [key, ...rest] = keys
  const copy = containerFor(source, key)

  copy[key] = setIn(copy[key], rest, value)
  return copy
}

export function rootWrite(
  base: Values,
  path: string,
  value: unknown
): [name: string, value: unknown] {
  const [name, ...rest] = toPath(path)
  return [name, setIn(base[name], rest, value)]
}

export function replaceEqualDeep<T>(previous: unknown, next: T): T {
  if (Object.is(previous, next)) {
    return next
  }

  if (
    typeof previous !== 'object' ||
    typeof next !== 'object' ||
    previous == null ||
    next == null
  ) {
    return next
  }

  if (previous instanceof Date || next instanceof Date) {
    const isSame =
      previous instanceof Date &&
      next instanceof Date &&
      previous.getTime() === next.getTime()
    return isSame ? (previous as T) : next
  }

  if (Array.isArray(previous) !== Array.isArray(next)) {
    return next
  }

  const before = previous as Values
  const after = next as Values
  const keys = Object.keys(after)
  const result = (Array.isArray(next) ? [] : {}) as Values
  let isSame = Object.keys(before).length === keys.length

  keys.forEach((key) => {
    result[key] = replaceEqualDeep(before[key], after[key])

    if (result[key] !== before[key]) {
      isSame = false
    }
  })

  return (isSame ? previous : result) as T
}

function signatureOf(value: unknown) {
  return JSON.stringify(value) ?? 'undefined'
}

export function useProp<T = unknown>(path: string): [T, PropSetter<T>] {
  const store = useContext(PropContext)

  if (store == null) {
    throw new Error('useProp must be used inside a React Component.')
  }

  const readValue = useCallback(() => getIn(store.props, path), [store, path])
  const storedValue = useSyncExternalStore(
    store.subscribe,
    readValue,
    readValue
  ) as T

  const latestValue = useRef(storedValue)
  latestValue.current = storedValue

  const [draft, setDraft] = useState<Draft<T> | null>(null)
  const echoTimeout = useRef<ReturnType<typeof setTimeout>>()

  useEffect(() => {
    return () => clearTimeout(echoTimeout.current)
  }, [])

  useEffect(() => {
    setDraft((pending) => {
      const isStale = pending != null && !Object.is(pending.base, storedValue)
      return isStale ? null : pending
    })
  }, [storedValue])

  const setValue = useCallback<PropSetter<T>>(
    (value) => {
      const base = latestValue.current
      clearTimeout(echoTimeout.current)

      if (signatureOf(value) === signatureOf(base)) {
        setDraft(null)
        return
      }

      setDraft({ value, base })
      store.write(path, value)

      const dropUnechoedDraft = () => {
        setDraft((pending) => {
          const isUnechoed =
            pending != null && Object.is(pending.base, latestValue.current)
          return isUnechoed ? null : pending
        })
      }

      echoTimeout.current = setTimeout(dropUnechoedDraft, ECHO_TIMEOUT)
    },
    [store, path]
  )

  const isPending = draft != null && Object.is(draft.base, storedValue)
  return [isPending ? draft.value : storedValue, setValue]
}
