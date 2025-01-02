import { FullGestureState, useGesture } from '@use-gesture/react'
import React, {
  ReactElement,
  useCallback,
  useEffect,
  useRef,
  useState,
} from 'react'
import {
  animated,
  SpringConfig,
  SpringValue,
  useSpring,
  useSpringRef,
} from '@react-spring/web'
import { UserGestureConfig } from '@use-gesture/core/types'
import {
  ChangeEvent,
  Position,
  Rectangle,
  WrapperApiProps,
  WrapperProps,
} from './types'
import { get } from 'lodash'

function extractChildren(element: ReactElement) {
  const children = element.props.children
  delete element.props.children

  return {
    element,
    children,
  }
}

function getSpringGoal(spring: SpringValue<number>): number {
  return spring.isAnimating ? spring.goal : spring.get()
}

function transformViewport(
  viewport: Rectangle,
  mouse: Position,
  scale?: ChangeEvent<number>
) {
  const viewportCenter = {
    x: viewport.x + viewport.width / 2,
    y: viewport.y + viewport.height / 2,
  }

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

export function CoordinateContainerWrapper(props: WrapperProps) {
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

  const apiRef = useSpringRef<WrapperApiProps>()
  const component = useRef<HTMLDivElement>(null)
  const container = useRef<HTMLDivElement>(null)
  const [isDragging, setIsDragging] = useState(false)
  const [, setIsZoomed] = useState(false)
  const { element, children } = extractChildren(props.wrapped())
  const [debugMessage, setDebugMessage] = useState('')

  const panConfig: SpringConfig = {
    decay: true,
    frequency: 2,
  }

  const zoomConfig: SpringConfig = {
    tension: 200,
    friction: 30,
    clamp: true,
  }

  const [style, api] = useSpring(
    () => ({
      x: props.position.x,
      y: props.position.y,
      scale: 1,
      ref: apiRef,
      config: panConfig,
    }),
    [props.position.x, props.position.y]
  )

  useEffect(() => {
    props.setApi(apiRef)
  }, [apiRef])

  const mouseWheelStep = 0.5
  const mouseWheelUnits = 100
  const pinchSensitivity = 1

  const useGestureConfiguration: UserGestureConfig = {
    drag: {
      enabled: true,
      preventScroll: true,
      from: () => [style.x.get(), style.y.get()],
    },
    pinch: {
      enabled: true,
      preventDefault: true,
      pinchOnWheel: true,
      angleBounds: { min: 0, max: 0 },
      from: () => [style.scale.get() * pinchSensitivity, 0],
    },
    wheel: {
      enabled: true,
      preventDefault: true,
      from: () => [0, -style.scale.get() * mouseWheelUnits],
    },
    eventOptions: {
      passive: false,
    },
  }

  const handleDrag = useCallback((state: FullGestureState<'drag'>) => {
    if (state.pinching) return state.cancel()
    if (state.wheeling) return

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
        ...panConfig,
        velocity: vx * dx,
      },
      y: {
        ...panConfig,
        velocity: vy * dy,
      },
    }

    setDebugMessage(`Dragging: ${JSON.stringify(results)}`)

    api.start({
      to: results,
      immediate,
      config: (name) => get(config, name, {}),
    })
  }, [])

  const handleZoom = useCallback(
    (state: FullGestureState<'pinch' | 'wheel'>) => {
      if (state.last || !container.current) return

      const scalePrevious = style.scale.get()
      const updates = {
        dx: 0,
        dy: 0,
        scale: scalePrevious,
      }

      // if (state.type === 'pointermove' || state.type === 'pointerdown') {
      //   const {
      //     origin: [ox, oy],
      //     offset: [scale],
      //   } = state as FullGestureState<'pinch'>
      //
      //   if (state.first) {
      //     state.memo = [ox, oy]
      //   }
      //   const [startX, startY] = state.memo
      //
      //   const scaleNew = scale * pinchSensitivity
      //
      //   const { dx, dy } = transformViewport(
      //     container.current.getBoundingClientRect(),
      //     { x: ox, y: oy },
      //     { previous: scalePrevious, current: scaleNew }
      //   )
      //
      //   setDebugMessage(`start: [${startX}, ${startY}]`)
      //
      //   updates.dx = dx
      //   updates.dy = dy
      //   updates.scale = scaleNew
      // }

      if (state.type === 'wheel') {
        const {
          delta: [, dZoom],
          event,
        } = state as FullGestureState<'wheel'>
        const zoomMultiplier = Math.exp(
          (-dZoom / mouseWheelUnits) * mouseWheelStep
        )

        const scaleNew = scalePrevious * zoomMultiplier

        const { dx, dy } = transformViewport(
          container.current.getBoundingClientRect(),
          { x: event.clientX, y: event.clientY },
          { previous: scalePrevious, current: scaleNew }
        )

        updates.dx = dx
        updates.dy = dy
        updates.scale = scaleNew

        console.log('Zoom', updates)
      }

      const results = {
        x: style.x.get() + updates.dx,
        y: style.y.get() + updates.dy,
        scale: updates.scale,
      }

      setDebugMessage(`Zoom: ${JSON.stringify(results)}`)

      api.start({
        to: results,
        config: zoomConfig,
      })

      setIsZoomed(results.scale > 1)
    },
    []
  )

  useGesture(
    {
      onDragStart: () => setIsDragging(true),
      onDragEnd: () => setIsDragging(false),
      onDrag: (state) => handleDrag(state),
      onPinch: (state) => handleZoom(state),
      onWheel: (state) => handleZoom(state),
    },
    {
      ...useGestureConfiguration,
      target: component,
    }
  )

  // choose cursor
  let cursor
  if (isDragging) {
    cursor = 'grabbing'
  }

  return (
    <div {...element.props} ref={component}>
      <div>{debugMessage}</div>
      <div>{JSON.stringify(props.position)}</div>
      <animated.div
        ref={container}
        className="coordinate-canvas-inner"
        style={{
          position: 'absolute',
          height: '100%',
          width: '100%',
          cursor,
          ...style,
        }}
      >
        {children}
      </animated.div>
    </div>
  )
}
