import React, { useMemo, useRef } from 'react'
import {
  ClientStore,
  ComponentMeta,
  ComponentProps,
  JsObject,
  PageStore,
  PComponent,
  PlainObject,
  PropertyTree,
  ReactResizeDetector,
  SizeObject,
  StyleObject,
  View,
} from '@inductiveautomation/perspective-client'

import { Swiper, SwiperSlide, SwiperProps, SwiperRef, useSwiperSlide } from 'swiper/react'
import { Virtual,
  Keyboard,
  Mousewheel,
  Navigation,
  Pagination,
  Scrollbar,
  Parallax,
  FreeMode,
  Grid,
  Manipulation,
  Zoom,
  Controller,
  A11y,
  History,
  HashNavigation,
  Autoplay,
  EffectFade,
  EffectCube,
  EffectFlip,
  EffectCoverflow,
  EffectCards,
  EffectCreative,
  Thumbs 
} from 'swiper/modules'

import 'swiper/css/bundle';
import { debounce, unset } from 'lodash';
import { transformProps } from '@embr-js/utils';
import { getScriptTransform, resolve } from '../util';

const COMPONENT_TYPE = 'embr.chart.swiper'

const EnabledSwiperModules = [
  Virtual,
  Keyboard,
  Mousewheel,
  Navigation,
  Pagination,
  Scrollbar,
  Parallax,
  FreeMode,
  Grid,
  Manipulation,
  Zoom,
  Controller,
  A11y,
  History,
  HashNavigation,
  Autoplay,
  EffectFade,
  EffectCube,
  EffectFlip,
  EffectCoverflow,
  EffectCards,
  EffectCreative,
  Thumbs
]

type SwiperComponentProps = {
  viewPath?: string
  viewParams?: JsObject
  viewStyle: StyleObject
  slideStyle: StyleObject
  instances: EmbeddedViewProps[]
  settings?: SwiperProps
  style?: StyleObject
}


type EmbeddedViewProps = {
  viewPath: string
  viewParams: JsObject
  viewStyle: StyleObject
  slideStyle: StyleObject
  useDefaultHeight: boolean
  useDefaultMinHeight: boolean
  useDefaultMinWidth: boolean
  useDefaultWidth: boolean
}
    
type EmbeddedSlideViewProps = {
  store: ClientStore
  mountPath: string
  view: EmbeddedViewProps
  listenResize?: boolean
  onResize:() => void
}

function getChildMountPath(props: ComponentProps<PlainObject>, childIndex: any) {
  return `${props.store.viewMountPath}$${props.store.addressPathString}[${childIndex}]`
}

function resolveViewProps(props: SwiperComponentProps, slideIndex: number): EmbeddedViewProps {
  const view = props.instances[slideIndex]

  return {
      viewPath: resolve([view.viewPath, props.viewPath]),
      viewParams: {
        ...props.viewParams,
        ...view.viewParams,
        index: slideIndex
      },
      viewStyle: {
        ...props.viewStyle,
        ...view.viewStyle
      },
      slideStyle: {
        ...props.slideStyle,
        ...view.slideStyle
      },
      useDefaultHeight: view.useDefaultHeight,
      useDefaultMinHeight: view.useDefaultMinHeight,
      useDefaultMinWidth: view.useDefaultMinWidth,
      useDefaultWidth: view.useDefaultWidth
  }
}

function EmbeddedSlideView(props: EmbeddedSlideViewProps) {
  const slide = useSwiperSlide()

  const resizeDetector = useMemo(() => {
    return props.listenResize ?
      <ReactResizeDetector
        onResize={props.onResize}
        handleWidth={true}
        handleHeight={true}
      />
    : null
  }, [props.listenResize, props.onResize])

  return (
    <>        
      <View
        key={PageStore.instanceKeyFor(props.view.viewPath, props.mountPath)}
        store={props.store}
        mountPath={props.mountPath}
        resourcePath={props.view.viewPath}
        useDefaultHeight={props.view.useDefaultHeight}
        useDefaultMinHeight={props.view.useDefaultMinHeight}
        useDefaultMinWidth={props.view.useDefaultMinWidth}
        useDefaultWidth={props.view.useDefaultWidth}
        params={{
          ...props.view.viewParams,
          swiperSlide: slide,
        }}
        rootStyle={{
          width: props.view.useDefaultWidth ? undefined : '100%',
          height: props.view.useDefaultHeight ? undefined : '100%',
          ...props.view.viewStyle,
        }}
      />
      {resizeDetector}
    </>
  )
}

function extractSettings(props: SwiperComponentProps) {
  const settings = props.settings
  unset(props, 'settings')
  return { props, settings }
}

function installSettings(
  props: SwiperComponentProps,
  settings: SwiperProps
) {
  props.settings = settings
}

export function SwiperComponent(props: ComponentProps<SwiperComponentProps>) {
    const swiperRef = useRef<SwiperRef>(null);

    const { props: transformedProps, settings } = extractSettings(props.props)
    const transformedSettings = transformProps(settings, [
      getScriptTransform({ self: props, client: window.__client })
    ]) as SwiperProps
    installSettings(transformedProps, transformedSettings)

    const updateOnResize = transformedProps.settings?.slidesPerView === 'auto'
    const handleResize = debounce(() => { 
      swiperRef.current?.swiper.updateSlides()
    }, 200, {
      leading: true,
      trailing: false
    })
    
    return (
      <div { ...props.emit() }>
        <Swiper
          ref={swiperRef}
          modules={EnabledSwiperModules}
          { ...transformedProps.settings }
          observer={true}
          observeSlideChildren={true}
          style={{ height: '100%', width: '100%' }}
        >
          { transformedProps.instances.map((_, index) => {
            const mountPath = getChildMountPath(props, index)
            const viewProps = resolveViewProps(transformedProps, index)
            viewProps.viewParams.index = index

            return (
              <SwiperSlide style={viewProps.slideStyle}>
                <EmbeddedSlideView 
                  store={props.store.view.page.parent} 
                  view={viewProps}
                  mountPath={mountPath}
                  listenResize={updateOnResize}
                  onResize={handleResize}
                />
              </SwiperSlide>
            )
          })}
        </Swiper>
      </div>
    )
}

export class SwiperComponentMeta implements ComponentMeta {
  getComponentType(): string {
    return COMPONENT_TYPE
  }

  getDefaultSize(): SizeObject {
    return {
      width: 300,
      height: 300,
    }
  }

  getPropsReducer(tree: PropertyTree): SwiperProps {
    return {
      viewPath: tree.read('viewPath'),
      viewParams: tree.read('viewParams'),
      elementStyle: tree.read('elementStyle', {}),
      slideStyle: tree.read('slideStyle', {}),
      instances: tree.read('instances', []),
      settings: tree.read('settings', {}),
      style: tree.read('style', {}),
    } as never
  }

  getViewComponent(): PComponent {
    return SwiperComponent as PComponent
  }
}
