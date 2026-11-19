import {
  Children,
  cloneElement,
  createContext,
  createElement,
  CSSProperties,
  ElementType,
  forwardRef,
  isValidElement,
  ReactElement,
  ReactNode,
  Ref,
  useContext,
  useEffect,
  useLayoutEffect,
  useMemo,
} from 'react'

type Props = Record<string, unknown>

type Handler = (...args: unknown[]) => unknown

export type RootOptions = {
  className?: string
  style?: CSSProperties
  ref?: Ref<HTMLElement>
}

export type RootHost = {
  attributes(options: RootOptions): object
  mount(): () => void
}

type RootProps = {
  as?: ElementType
  asChild?: boolean
  className?: string
  style?: CSSProperties
  children?: ReactNode
  [name: string]: unknown
}

const ROOT_OUTSIDE = 'Root must be used inside a library component'

const ROOT_CHILD = 'Root with asChild must have exactly one element child'

const useIsomorphicLayoutEffect =
  typeof window === 'undefined' ? useEffect : useLayoutEffect

export const RootContext = createContext<RootHost | null>(null)

export function joinClasses(...classes: unknown[]) {
  return classes.filter(Boolean).join(' ') || undefined
}

function isHandler(name: string, value: unknown): value is Handler {
  return /^on[A-Z]/.test(name) && typeof value === 'function'
}

export function mergeProps(base: Props, own: Props): Props {
  const merged: Props = { ...base, ...own }

  for (const [name, handler] of Object.entries(own)) {
    const inner = base[name]

    if (isHandler(name, handler) && isHandler(name, inner)) {
      merged[name] = (...args: unknown[]) => {
        handler(...args)
        inner(...args)
      }
    }
  }

  return merged
}

function assignRef<T>(ref: Ref<T> | undefined, value: T | null) {
  if (typeof ref === 'function') {
    ref(value)
  } else if (ref != null) {
    ;(ref as { current: T | null }).current = value
  }
}

function useComposedRef<T>(...refs: (Ref<T> | undefined)[]) {
  return useMemo(() => {
    const defined = refs.filter((ref) => ref != null)

    if (defined.length < 2) {
      return defined[0]
    }

    return (value: T | null) => defined.forEach((ref) => assignRef(ref, value))
  }, refs)
}

function onlyChild(children: ReactNode): ReactElement<Props> {
  const child = Children.only(children)

  if (!isValidElement(child)) {
    throw new Error(ROOT_CHILD)
  }

  return child as ReactElement<Props>
}

export const Root = forwardRef<HTMLElement, RootProps>(
  function Root(props, forwardedRef) {
    const { as, asChild, className, style, children, ...rest } =
      props as RootProps
    const host = useContext(RootContext)

    if (host == null) {
      throw new Error(ROOT_OUTSIDE)
    }

    useIsomorphicLayoutEffect(() => host.mount(), [host.mount])

    const child = asChild ? onlyChild(children) : null
    const {
      className: childClassName,
      style: childStyle,
      ...childProps
    } = (child?.props ?? {}) as RootProps

    const childRef = (child as { ref?: Ref<HTMLElement> } | null)?.ref
    const ref = useComposedRef(forwardedRef, childRef ?? undefined)

    const attributes = mergeProps(
      host.attributes({
        className: joinClasses(className, childClassName),
        style: { ...style, ...childStyle },
        ref,
      }) as Props,
      rest
    )

    if (child != null) {
      return cloneElement(child, mergeProps(attributes, childProps))
    }

    return createElement(as ?? 'div', attributes, children as ReactNode)
  }
)
