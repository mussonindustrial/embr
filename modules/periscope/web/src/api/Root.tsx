import React, {
  Children,
  cloneElement,
  createContext,
  CSSProperties,
  ElementType,
  forwardRef,
  isValidElement,
  ReactElement,
  ReactNode,
  Ref,
  useContext,
} from 'react'
import { useIsomorphicLayoutEffect, useMergedRef } from '@embr-js/utils'

import type { ComponentContext } from './periscopeApi'
import type { Values } from './types'

type Handler = (...args: unknown[]) => unknown

export type RootOptions = {
  className?: string
  style?: CSSProperties
  ref?: Ref<HTMLElement>
}

export type RootHost = {
  attributes(options: RootOptions): object
  mount(): () => void
  element?: (element: Element | null) => void
}

type RootElementProps = {
  as?: ElementType
  asChild?: boolean
  className?: string
  style?: CSSProperties
  children?: ReactNode
  [name: string]: unknown
}

export const RootContext = createContext<RootHost | null>(null)

export function joinClasses(...classes: unknown[]) {
  return classes.filter(Boolean).join(' ') || undefined
}

function isHandler(name: string, value: unknown): value is Handler {
  return /^on[A-Z]/.test(name) && typeof value === 'function'
}

export function mergeProps(base: Values, own: Values): Values {
  const merged: Values = { ...base, ...own }

  Object.entries(own).forEach(([name, handler]) => {
    const inner = base[name]

    if (isHandler(name, handler) && isHandler(name, inner)) {
      merged[name] = (...args: unknown[]) => {
        handler(...args)
        inner(...args)
      }
    }
  })

  return merged
}

function onlyChild(children: ReactNode): ReactElement<Values> {
  const child = Children.only(children)

  if (!isValidElement(child)) {
    throw new Error('Root with asChild must have exactly one element child.')
  }

  return child as ReactElement<Values>
}

export const Root = forwardRef<HTMLElement, RootElementProps>(
  function Root(props, forwardedRef) {
    const { as, asChild, className, style, children, ...rest } =
      props as RootElementProps
    const host = useContext(RootContext)

    if (host == null) {
      throw new Error('Root must be used inside a React Component.')
    }

    useIsomorphicLayoutEffect(() => host.mount(), [host.mount])

    const child = asChild ? onlyChild(children) : null
    const {
      className: childClassName,
      style: childStyle,
      ...childProps
    } = (child?.props ?? {}) as RootElementProps

    const childRef = (child as { ref?: Ref<HTMLElement> } | null)?.ref
    const ref = useMergedRef<HTMLElement>(forwardedRef, childRef, host.element)

    const attributes = mergeProps(
      host.attributes({
        className: joinClasses(className, childClassName),
        style: { ...style, ...childStyle },
        ref,
      }) as Values,
      rest
    )

    if (child != null) {
      return cloneElement(child, mergeProps(attributes, childProps))
    }

    const Tag = (as ?? 'div') as ElementType

    return <Tag {...attributes}>{children}</Tag>
  }
) as ComponentContext['Root']
