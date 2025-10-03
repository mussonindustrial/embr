import React, { useEffect, useRef, useState } from 'react'

export interface PropsDecoratorProps {
  children: React.ReactElement
}

/**
 * Applies all passed properties as attributes to its children.
 * @param props
 * @constructor
 */
export const PropsDecorator: React.FC<PropsDecoratorProps> = (props) => {
  const [parentElement, setParentElement] = useState<HTMLElement | null>(null)
  const Wrapper = parentElement ? React.Fragment : 'div'
  const ref = useRef<HTMLDivElement>(null)
  const targetIndex = useRef(0)

  const { children, ...rest } = props

  useEffect(() => {
    if (ref.current?.parentElement) {
      targetIndex.current = Array.from(
        ref.current.parentElement.children
      ).indexOf(ref.current)
      setParentElement(ref.current.parentElement)
      return
    }
  }, [])

  useEffect(() => {
    Object.entries(rest).forEach(([key, value]) => {
      parentElement?.children[targetIndex.current].setAttribute(
        key,
        value as string
      )
    })
  }, [parentElement, targetIndex, props])

  return <Wrapper ref={parentElement ? undefined : ref}>{children}</Wrapper>
}
