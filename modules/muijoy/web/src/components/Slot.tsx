import React from 'react'

export interface SlotProps {
  name: string
  children: React.ReactElement
}

/**
 * A named slot.
 * @param props
 * @constructor
 */
export const Slot: React.FC<SlotProps> = (props) => {
  return <>{props.children}</>
}
