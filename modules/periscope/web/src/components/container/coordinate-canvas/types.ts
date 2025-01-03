import {
  CoordinateUtils,
  PipeUtils,
} from '@inductiveautomation/perspective-client'
import { CoordinatesConfig, DragConfig, PinchConfig } from '@use-gesture/react'
import { SpringConfig } from '@react-spring/web'

export type CoordinateContainerProps = {
  mode: CoordinateUtils.LayoutMode
  aspectRatio: string
  pipes: PipeUtils.Pipes
}

export type CoordinateCanvasSettingsProps = {
  springs: {
    pan: SpringConfig
    zoom: SpringConfig
  }
  gestures: {
    drag: DragConfig
    pinch: PinchConfig
    wheel: CoordinatesConfig<'wheel'>
  }
}

export type CoordinateCanvasProps = CoordinateContainerProps & {
  position: Point
  settings: CoordinateCanvasSettingsProps
}

export type Point = {
  x: number
  y: number
}

export type Size = {
  width: number
  height: number
}

export type Rectangle = Point & Size

export type ChangeEvent<T> = {
  current: T
  previous: T
}
