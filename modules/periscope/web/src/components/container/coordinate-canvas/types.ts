import {
  CoordinateUtils,
  PipeUtils,
} from '@inductiveautomation/perspective-client'
import { DragConfig, PinchConfig } from '@use-gesture/react'
import { ReactElement } from 'react'
import { SpringRef } from '@react-spring/web'

export type CoordinateContainerProps = {
  mode: CoordinateUtils.LayoutMode
  aspectRatio: string
  pipes: PipeUtils.Pipes
}

export type CoordinateCanvasSettingsProps = {
  pinch: PinchConfig
  drag: DragConfig
}

export type CoordinateCanvasProps = CoordinateContainerProps & {
  position: Position
  settings: CoordinateCanvasSettingsProps
}

export type WrapperApiProps = {
  x: number
  y: number
  scale: number
}

export type WrapperProps = {
  position: Position
  settings: CoordinateCanvasSettingsProps
  wrapped: () => ReactElement
  setApi: (api: SpringRef<WrapperApiProps>) => void
}

export type Position = {
  x: number
  y: number
}

export type Size = {
  width: number
  height: number
}

export type Rectangle = Position & Size

export type ChangeEvent<T> = {
  current: T
  previous: T
}
