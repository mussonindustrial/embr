import { FullGestureState, useGesture } from '@use-gesture/react'
import React, {
  forwardRef,
  useCallback,
  useEffect,
  useImperativeHandle,
  useMemo,
  useRef,
  useState,
} from 'react'
import {
  animated,
  SpringConfig,
  SpringRef,
  SpringValue,
  useSpring,
} from '@react-spring/web'
import { useResizeDetector } from 'react-resize-detector'
import { UserGestureConfig } from '@use-gesture/core/types'
import {
  ChangeEvent,
  CoordinateCanvasSettingsProps,
  Point,
  Rectangle,
} from './types'
import { get } from 'lodash'

export type SheetPosition = {
  x?: number
  y?: number
  scale?: number
}

export type ZoomScale = number
export type AnimationConfig = SpringConfig

export type PanZoomSheetRef = {
  x: SpringValue<number>
  y: SpringValue<number>
  scale: SpringValue<number>
  springRef: SpringRef<{ x: number; y: number; scale: number }>
  pan: (delta: Point, config?: AnimationConfig) => void
  panTo: (coordinates: Point, config?: AnimationConfig) => void
  zoom: (scale: ZoomScale, config?: AnimationConfig) => void
  zoomTo: (scale: ZoomScale, config?: AnimationConfig) => void
  centerOn: (rectangle: Rectangle, config?: AnimationConfig) => void
}

export type PanZoomSheetProps = CoordinateCanvasSettingsProps & {
  initial: SheetPosition
  children?: React.ReactNode
  containerProps?: Record<string, unknown>
  sheetProps?: Record<string, unknown>
}

const DEFAULT_POSITION = {
  x: 0,
  y: 0,
  scale: 1,
} as const

const DEFAULT_SPRINGS = {
  pan: {
    decay: true,
    frequency: 2,
  },
  zoom: {
    duration: 100,
  },
} as const

function getSpringGoal(spring: SpringValue<number>): number {
  return spring.isAnimating ? spring.goal : spring.get()
}

function usePrevious<T>(value: T) {
  const ref = useRef<T>()
  useEffect(() => {
    ref.current = value
  }, [value])
  return ref.current
}

function getCenter(rectangle: Rectangle): Point {
  return {
    x: rectangle.x + rectangle.width / 2,
    y: rectangle.y + rectangle.height / 2,
  }
}

function transformViewport(
  viewport: Rectangle,
  mouse: Point,
  scale?: ChangeEvent<number>
) {
  const viewportCenter = getCenter(viewport)

  const mouseOffset = {
    x: mouse.x - viewportCenter.x,
    y: mouse.y - viewportCenter.y,
  }

  if (scale) {
    return {
      dx: mouseOffset.x - (mouseOffset.x / scale.previous) * scale.current,
      dy: mouseOffset.y - (mouseOffset.y / scale.previous) * scale.current,
    }
  } else {
    return {
      dx: mouseOffset.x,
      dy: mouseOffset.y,
    }
  }
}

export const PanZoomSheet = forwardRef<PanZoomSheetRef, PanZoomSheetProps>(
  (props: PanZoomSheetProps, ref) => {
    const container = useRef<HTMLDivElement>(null)
    const sheet = useRef<HTMLDivElement>(null)
    const { width, height } = useResizeDetector({ targetRef: container })
    const [isDragging, setIsDragging] = useState(false)

    const springConfig = useMemo(
      () => props.springs ?? DEFAULT_SPRINGS,
      [props.springs]
    )

    const initialPosition = useMemo(
      () => ({
        x: props.initial.x ?? DEFAULT_POSITION.x,
        y: props.initial.y ?? DEFAULT_POSITION.y,
        scale: props.initial.scale ?? DEFAULT_POSITION.scale,
      }),
      [props.initial]
    )

    const [style, api] = useSpring(
      () => ({
        x: initialPosition.x,
        y: initialPosition.y,
        scale: initialPosition.scale,
        config: springConfig.pan,
      }),
      [initialPosition]
    )

    useEffect(() => {
      const handler = (e: Event) => e.preventDefault()
      document.addEventListener('gesturestart', handler)
      document.addEventListener('gesturechange', handler)
      document.addEventListener('gestureend', handler)
      return () => {
        document.removeEventListener('gesturestart', handler)
        document.removeEventListener('gesturechange', handler)
        document.removeEventListener('gestureend', handler)
      }
    }, [])

    const pan = useCallback(
      (delta: Point, config?: AnimationConfig) => {
        api.start({
          to: delta,
          config,
        })
      },
      [api]
    )

    const panTo = useCallback(
      (coordinates: Point, config?: AnimationConfig) => {
        api.start({
          to: coordinates,
          config,
        })
      },
      [api]
    )

    const zoom = useCallback(
      (scale: ZoomScale, config?: AnimationConfig) => {
        api.start({
          to: {
            scale,
          },
          config,
        })
      },
      [api]
    )

    const zoomTo = useCallback(
      (scale: ZoomScale, config?: AnimationConfig) => {
        api.start({
          to: {
            scale,
          },
          config,
        })
      },
      [api]
    )

    const centerOn = useCallback(
      (rectangle: Rectangle, config?: AnimationConfig) => {
        api.start({
          to: rectangle,
          config,
        })
      },
      [api]
    )

    useImperativeHandle(ref, () => {
      return {
        x: style.x,
        y: style.y,
        scale: style.scale,
        springRef: api,
        pan,
        panTo,
        zoom,
        zoomTo,
        centerOn,
      }
    })

    // When component width changes, recenter horizontally.
    const prevWidth = usePrevious(width)
    useEffect(() => {
      if (width && prevWidth) {
        const delta = width - prevWidth
        api.start({
          to: {
            x: getSpringGoal(style.x) + (delta * style.scale.get()) / 2,
          },
          immediate: true,
        })
      }
    }, [width])

    // When component height changes, recenter vertically.
    const prevHeight = usePrevious(height)
    useEffect(() => {
      if (height && prevHeight) {
        const delta = height - prevHeight
        api.start({
          to: {
            y: getSpringGoal(style.y) + (delta * style.scale.get()) / 2,
          },
          immediate: true,
        })
      }
    }, [height])

    const mouseWheelStep = 0.33
    const mouseWheelUnits = 100
    const pinchSensitivity = 1

    const useGestureConfiguration: UserGestureConfig = {
      drag: {
        from: () => [style.x.get(), style.y.get()],
        ...props.gestures.drag,
      },
      pinch: {
        from: () => [style.scale.get() * pinchSensitivity, 0],
        ...props.gestures.pinch,
      },
      wheel: {
        from: () => [0, -style.scale.get() * mouseWheelUnits],
        ...props.gestures.wheel,
      },
      eventOptions: {
        passive: false,
      },
    }

    const handleDrag = useCallback((state: FullGestureState<'drag'>) => {
      if (state.wheeling) return
      if (style.scale.isAnimating) return

      const {
        event,
        delta: [mx, my],
        direction: [dx, dy],
        velocity: [vx, vy],
      } = state

      event.preventDefault()
      event.stopPropagation()

      const immediate = !style.scale.isAnimating && state.down && !state.last

      const results = {
        x: state.first ? style.x.get() + mx : getSpringGoal(style.x) + mx,
        y: state.first ? style.y.get() + my : getSpringGoal(style.y) + my,
      }

      const config = {
        x: {
          ...springConfig.pan,
          velocity: vx * dx,
        },
        y: {
          ...springConfig.pan,
          velocity: vy * dy,
        },
      }

      api.start({
        to: results,
        immediate,
        config: (name) => get(config, name, {}),
      })
    }, [])

    const handleWheel = useCallback((state: FullGestureState<'wheel'>) => {
      if (state.last || !sheet.current) return

      const {
        delta: [, dZoom],
        event,
      } = state
      const zoomMultiplier = Math.exp(
        (-dZoom / mouseWheelUnits) * mouseWheelStep
      )

      const scalePrevious = style.scale.get()
      const scaleNew = scalePrevious * zoomMultiplier

      const { dx, dy } = transformViewport(
        sheet.current.getBoundingClientRect(),
        { x: event.clientX, y: event.clientY },
        { previous: scalePrevious, current: scaleNew }
      )

      const results = {
        x: style.x.get() + dx,
        y: style.y.get() + dy,
        scale: scaleNew,
      }

      api.start({
        to: results,
        config: springConfig.zoom,
      })
    }, [])

    const handlePinch = useCallback((state: FullGestureState<'pinch'>) => {
      if (state.last || !sheet.current) return

      if (state.type === 'pointermove' || state.type === 'pointerdown') {
        const {
          origin: [ox, oy],
          offset: [s],
        } = state

        const scalePrevious = style.scale.get()
        const scaleNew = s * pinchSensitivity

        const { dx, dy } = transformViewport(
          sheet.current.getBoundingClientRect(),
          { x: ox, y: oy },
          { previous: scalePrevious, current: scaleNew }
        )

        const results = {
          x: style.x.get() + dx,
          y: style.y.get() + dy,
          scale: scaleNew,
        }

        api.start({
          to: results,
          config: springConfig.zoom,
        })
      }
    }, [])

    useGesture(
      {
        onDragStart: () => setIsDragging(true),
        onDragEnd: () => setIsDragging(false),
        onDrag: handleDrag,
        onPinch: handlePinch,
        onWheel: handleWheel,
      },
      {
        ...useGestureConfiguration,
        target: container,
      }
    )

    // Set Cursor
    let cursor
    if (isDragging) {
      cursor = 'grabbing'
    }

    return (
      <div {...props.containerProps} ref={container}>
        <animated.div
          ref={sheet}
          {...props.sheetProps}
          className="coordinate-canvas-inner"
          style={{
            position: 'absolute',
            height: '100%',
            width: '100%',
            cursor,
            ...style,
          }}
        >
          {props.children}
        </animated.div>
      </div>
    )
  }
)

PanZoomSheet.displayName = 'PanZoomSheet'
