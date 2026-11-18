import {
  createContext,
  createElement,
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

type Values = Record<string, unknown>

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
    throw new Error('useComponent must be used inside a library component')
  }

  return context as unknown as ComponentContext<S, C>
}

function matchesWhere(position: Values, where?: Values): boolean {
  for (const [name, value] of Object.entries(where ?? {})) {
    if (position[name] !== value) {
      return false
    }
  }

  return true
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

    return createElement(Fragment, { key: child.key }, renderChild(child))
  })
}

export const ElementSlot = forwardRef<HTMLElement, ChildSlotProps>(
  function ElementSlot({ as, where, children: renderChild, ...rest }, ref) {
    const attributes: Values = { ...rest }

    for (const name of DESIGN_ONLY_PROPS) {
      delete attributes[name]
    }

    const { children } = useComponent()

    return createElement(
      as ?? 'div',
      { ...attributes, ref },
      slotChildren(children, where, renderChild)
    )
  }
)
