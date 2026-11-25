import React, {
  createContext,
  ElementType,
  forwardRef,
  Fragment,
  ReactNode,
  useContext,
} from 'react'
import type {
  Child,
  ChildSlotProps,
  ComponentContext,
  PropSchema,
} from './periscopeApi'
import type { Values } from './types'

const DESIGN_ONLY_PROPS = ['positionChild', 'resize', 'placeholder']

export const ComponentReactContext = createContext<ComponentContext | null>(
  null
)

export function useComponent<
  S extends PropSchema = PropSchema,
  C extends PropSchema = PropSchema,
>(): ComponentContext<S, C> {
  const context = useContext(ComponentReactContext)

  if (context == null) {
    throw new Error('useComponent must be used inside a React Component.')
  }

  return context as never
}

function matchesWhere(position: Values, where?: Values): boolean {
  return Object.entries(where ?? {}).every(
    ([name, value]) => position[name] === value
  )
}

export function slotChildren(
  children: Child[],
  where?: Values,
  renderChild?: (child: Child) => ReactNode
): ReactNode[] {
  const matching = children.filter((child) =>
    matchesWhere(child.position, where)
  )

  return matching.map((child) => {
    if (renderChild == null) {
      return child.render()
    }

    return <Fragment key={child.key}>{renderChild(child)}</Fragment>
  })
}

export const ElementSlot = forwardRef<HTMLElement, ChildSlotProps>(
  function ElementSlot({ as, where, children: renderChild, ...rest }, ref) {
    const { children } = useComponent()
    const attributes = Object.fromEntries(
      Object.entries(rest).filter(([name]) => !DESIGN_ONLY_PROPS.includes(name))
    )
    const Tag = (as ?? 'div') as ElementType

    return (
      <Tag {...attributes} ref={ref}>
        {slotChildren(children, where, renderChild)}
      </Tag>
    )
  }
) as ComponentContext['ChildSlot']
