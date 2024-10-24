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
import { emitStyles, formatStyleNames, getScriptTransform, mergeStyles, resolve } from '../util';

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
  instances: EmbeddedViewProps[]
  instanceCommon: EmbeddedViewProps
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
      viewPath: resolve([view.viewPath, props.instanceCommon.viewPath]),
      viewParams: {
        ...props.instanceCommon.viewParams,
        ...view.viewParams,
        index: slideIndex
      },
      viewStyle: mergeStyles([props.instanceCommon.viewStyle, view.viewStyle]),
      slideStyle: mergeStyles([props.instanceCommon.slideStyle, view.slideStyle]),
      useDefaultHeight: resolve([view.useDefaultHeight, props.instanceCommon.useDefaultHeight]),
      useDefaultMinHeight: resolve([view.useDefaultMinHeight, props.instanceCommon.useDefaultMinHeight]),
      useDefaultMinWidth: resolve([view.useDefaultMinWidth, props.instanceCommon.useDefaultMinWidth]),
      useDefaultWidth: resolve([view.useDefaultWidth, props.instanceCommon.useDefaultWidth])
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
          classes: formatStyleNames(props.view.viewStyle.classes)
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

function transformClassProperties<T>(object: T, properties: Array<keyof T>) {
  for (let index = 0; index < properties.length; index++) {
    const property = properties[index];
    if (typeof(object[property]) === 'string' && object[property] != '' && !object[property].startsWith('psc-')) {
      console.log((property as string )+ ' ' + object[property])
      object[property] = formatStyleNames(object[property] as string) as never
    } 
  }  
}

function applyClassTransforms(settings: SwiperProps): SwiperProps {

  transformClassProperties(settings, [
    'containerModifierClass',
    'lazyPreloaderClass',
    'noSwipingClass',
  ])

  if (typeof(settings.navigation) == 'object') {
    transformClassProperties(settings.navigation, [
      'disabledClass',
      'hiddenClass',
      'lockClass',
      'navigationDisabledClass'
    ])
  }

  if (typeof(settings.pagination) == 'object') {
    transformClassProperties(settings.pagination, [
      'bulletActiveClass',
      'bulletClass',
      'clickableClass',
      'currentClass',
      'hiddenClass',
      'horizontalClass',
      'lockClass',
      'modifierClass',
      'paginationDisabledClass',
      'progressbarFillClass',
      'progressbarOppositeClass',
      'totalClass',
      'verticalClass',
    ])
  }

  if (typeof(settings.scrollbar) == 'object') {
    transformClassProperties(settings.scrollbar, [
      'dragClass',
      'horizontalClass',
      'lockClass',
      'scrollbarDisabledClass',
      'verticalClass'
    ])
  }

  if (typeof(settings.thumbs) == 'object') {
    transformClassProperties(settings.thumbs, [
      'slideThumbActiveClass',
      'thumbsContainerClass'
    ])
  }

  if (typeof(settings.zoom) == 'object') {
    transformClassProperties(settings.zoom, [
      'containerClass',
      'zoomedSlideClass'
    ])
  }

  if (typeof(settings.mousewheel) == 'object') {
    transformClassProperties(settings.mousewheel, [
      'noMousewheelClass'
    ])
  }

  if (typeof(settings.a11y) == 'object') {
    transformClassProperties(settings.a11y, [
      'notificationClass'
    ])
  }


  return settings
}

export function SwiperComponent(props: ComponentProps<SwiperComponentProps>) {
    const swiperRef = useRef<SwiperRef>(null);
    
    const transformedSettings = useMemo(() => {
      const { settings } = extractSettings(props.props)
      const transformedSettings = transformProps(settings, [
        getScriptTransform({ self: props, client: window.__client })
      ]) as SwiperProps
      applyClassTransforms(transformedSettings)
      return transformedSettings
    }, [props.props.settings])

    installSettings(props.props, transformedSettings)
    const transformedProps = props.props

    const updateOnResize = transformedProps.settings?.slidesPerView === 'auto'
    const handleResize = debounce(() => { 
      console.log('resized')
      swiperRef.current?.swiper.updateSlides()
    }, 100, {
      leading: true,
      trailing: true
    })
    
    return (
      <div { ...props.emit() }>
        <Swiper
          { ...transformedProps.settings }
          ref={swiperRef}
          modules={EnabledSwiperModules}
          observer={true}
          observeSlideChildren={true}
          style={{ height: '100%', width: '100%' }}
        >
          { transformedProps.instances.map((_, index) => {
            const mountPath = getChildMountPath(props, index)
            const viewProps = resolveViewProps(transformedProps, index)
            viewProps.viewParams.index = index

            return (
              <SwiperSlide { ...emitStyles(viewProps.slideStyle) }>
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
      instances: tree.read('instances', []),
      instanceCommon: tree.read('instanceCommon', {}),
      settings: tree.read('settings', {}),
      style: tree.read('style', {}),
    } as never
  }

  getViewComponent(): PComponent {
    return SwiperComponent as PComponent
  }
}
