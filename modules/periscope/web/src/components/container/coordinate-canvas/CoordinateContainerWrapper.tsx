import { FullGestureState, useGesture } from '@use-gesture/react'
import React, {
  ReactElement,
  useCallback,
  useEffect,
  useRef,
  useState,
} from 'react'
import { animated, useSpring, useSpringRef } from '@react-spring/web'
import { UserGestureConfig } from '@use-gesture/core/types'
import {
  ChangeEvent,
  Position,
  Rectangle,
  WrapperApiProps,
  WrapperProps,
} from './types'

function extractChildren(element: ReactElement) {
  const children = element.props.children
  delete element.props.children

  return {
    element,
    children,
  }
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
  const { element, children } = extractChildren(props.wrapped())
  const [debugMessage, setDebugMessage] = useState('')

  const [style, api] = useSpring(
    () => ({
      x: props.position.x,
      y: props.position.y,
      scale: 1,
      ref: apiRef,
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
    if (state.pinching) return
    const {
      delta: [dx, dy],
    } = state

    api.start({
      x: style.x.get() + dx,
      y: style.y.get() + dy,
      immediate: state.down,
    })
  }, [])

  const handleZoom = useCallback(
    (state: FullGestureState<'pinch' | 'wheel'>) => {
      if (state.last || !container.current) return

      const scalePrevious = style.scale.get()
      const animate = {
        x: style.x.get(),
        y: style.y.get(),
        scale: scalePrevious,
      }

      if (state.type === 'pointermove' || state.type === 'pointerdown') {
        const {
          origin: [ox, oy],
          offset: [scale],
        } = state as FullGestureState<'pinch'>

        if (state.first) {
          state.memo = [ox, oy]
        }
        const [startX, startY] = state.memo

        const scaleNew = scale * pinchSensitivity

        const { dx, dy } = transformViewport(
          container.current.getBoundingClientRect(),
          { x: ox, y: oy },
          { previous: scalePrevious, current: scaleNew }
        )

        setDebugMessage(`start: [${startX}, ${startY}]`)

        animate.x += dx
        animate.y += dy
        animate.scale = scaleNew
      }

      if (state.type === 'wheel') {
        const { delta, event } = state as FullGestureState<'wheel'>
        const zoomMultiplier = Math.exp(
          (-delta[1] / mouseWheelUnits) * mouseWheelStep
        )

        const scaleNew = scalePrevious * zoomMultiplier

        const { dx, dy } = transformViewport(
          container.current.getBoundingClientRect(),
          { x: event.clientX, y: event.clientY },
          { previous: scalePrevious, current: scaleNew }
        )

        animate.x += dx
        animate.y += dy
        animate.scale = scaleNew
      }

      api.start({
        ...animate,
        immediate: state.type === 'pointermove',
        config: {
          tension: 200,
          friction: 30,
        },
      })
      return state.memo
    },
    []
  )

  useGesture(
    {
      onDrag: (state) => handleDrag(state),
      onPinch: (state) => handleZoom(state),
      onWheel: (state) => handleZoom(state),
    },
    {
      ...useGestureConfiguration,
      target: component,
    }
  )

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
          ...style,
        }}
      >
        {children}
      </animated.div>
    </div>
  )
}
