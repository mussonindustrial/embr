import React, { ReactElement, useCallback } from 'react'

export interface PropsDecoratorProps {
  children: React.ReactElement
}

/**
 * Applies all passed properties as attributes to its children.
 * @param props
 * @constructor
 */
// export const PropsDecorator: React.FC<PropsDecoratorProps> = (props) => {
//   const [parentElement, setParentElement] = useState<HTMLElement | null>(null)
//   const Wrapper = parentElement ? React.Fragment : 'div'
//   const ref = useRef<HTMLDivElement>(null)
//   const targetIndex = useRef(0)
//
//   const { children, ...rest } = props
//
//   useEffect(() => {
//     if (ref.current?.parentElement) {
//       targetIndex.current = Array.from(
//         ref.current.parentElement.children
//       ).indexOf(ref.current)
//       setParentElement(ref.current.parentElement)
//       return
//     }
//   }, [])
//
//   useEffect(() => {
//     Object.entries(rest).forEach(([key, value]) => {
//       parentElement?.children[targetIndex.current].setAttribute(
//         key,
//         value as string
//       )
//     })
//   }, [parentElement, targetIndex, rest])
//
//   return <Wrapper ref={parentElement ? undefined : ref}>{children}</Wrapper>
// }
export const PropsDecorator: React.FC<PropsDecoratorProps> = ({
  children,
  ...rest
}) => {
  const applyProps = useCallback(
    (node: HTMLElement | null) => {
      if (node) {
        for (const [key, value] of Object.entries(rest)) {
          if (value != null && node.setAttribute) {
            node.setAttribute(key, String(value))
          }
        }
      }
    },
    [rest]
  )

  return (
    <>
      {React.Children.map(children, (child) => {
        if (!React.isValidElement(child)) return child

        const el = child as ReactElement & { ref?: React.Ref<never> }
        const existingRef = el.ref

        return React.cloneElement(child, {
          ref: mergeRefs(applyProps, existingRef),
        } as never)
      })}
    </>
  )
}

function mergeRefs<T = never>(...refs: Array<React.Ref<T> | undefined>) {
  return (node: T) => {
    refs.forEach((ref) => {
      if (!ref) return
      if (typeof ref === 'function') {
        ref(node)
      } else {
        try {
          ;(ref as React.MutableRefObject<T | null>).current = node
        } catch {
          // ignore
        }
      }
    })
  }
}
