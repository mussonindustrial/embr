import React, { memo, useMemo, useRef } from 'react'
import {
  ClientStore,
  ComponentMeta,
  ComponentProps,
  JsObject,
  OutputListener,
  PageStore,
  PComponent,
  PlainObject,
  PropertyTree,
  SizeObject,
  StyleObject,
  View,
} from '@inductiveautomation/perspective-client'

import 'swiper/css/bundle'
import {
  Swiper,
  SwiperSlide,
  SwiperProps,
  SwiperRef,
  useSwiperSlide,
} from 'swiper/react'
import {
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
  Thumbs,
} from 'swiper/modules'

import { debounce } from 'lodash'
import { transformProps } from '@embr-js/utils'
import {
  emitStyles,
  formatStyleNames,
  getScriptTransform,
  mergeStyles,
  resolve,
} from '../../util'

const COMPONENT_TYPE = 'embr.periscope.embedding.swiper'

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
  Thumbs,
]

type SwiperComponentProps = {
  instances: EmbeddedViewProps[]
  instanceCommon: EmbeddedViewProps
  settings?: SwiperProps
  style?: StyleObject
}

type EmbeddedViewProps = {
  key: React.Key
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
  onResize?: () => void
  key: React.Key
  outputListener?: OutputListener
}

function getChildMountPath(
  props: ComponentProps<PlainObject>,
  childIndex: any
) {
  return `${props.store.viewMountPath}$${props.store.addressPathString}[${childIndex}]`
}

function resolveViewProps(
  props: SwiperComponentProps,
  index: number
): EmbeddedViewProps {
  const view = props.instances[index]

  return {
    key: view.key && view.key !== '' ? view.key : index,
    viewPath: resolve([view.viewPath, props.instanceCommon.viewPath]),
    viewParams: {
      ...props.instanceCommon.viewParams,
      ...view.viewParams,
      index,
    },
    viewStyle: mergeStyles([props.instanceCommon.viewStyle, view.viewStyle]),
    slideStyle: mergeStyles([props.instanceCommon.slideStyle, view.slideStyle]),
    useDefaultHeight: resolve([
      view.useDefaultHeight,
      props.instanceCommon.useDefaultHeight,
    ]),
    useDefaultMinHeight: resolve([
      view.useDefaultMinHeight,
      props.instanceCommon.useDefaultMinHeight,
    ]),
    useDefaultMinWidth: resolve([
      view.useDefaultMinWidth,
      props.instanceCommon.useDefaultMinWidth,
    ]),
    useDefaultWidth: resolve([
      view.useDefaultWidth,
      props.instanceCommon.useDefaultWidth,
    ]),
  }
}

const EmbeddedSlideView = memo(
  ({
    store,
    mountPath,
    view,
    onResize,
    outputListener,
  }: EmbeddedSlideViewProps) => {
    const slide = useSwiperSlide()

    return (
      <>
        <View
          key={PageStore.instanceKeyFor(view.viewPath, mountPath)}
          store={store}
          mountPath={mountPath}
          resourcePath={view.viewPath}
          useDefaultHeight={view.useDefaultHeight}
          useDefaultMinHeight={view.useDefaultMinHeight}
          useDefaultMinWidth={view.useDefaultMinWidth}
          useDefaultWidth={view.useDefaultWidth}
          params={{
            ...view.viewParams,
            swiperSlide: slide,
          }}
          outputListener={outputListener}
          onViewSizeChange={() => onResize?.()}
          rootStyle={{
            width: view.useDefaultWidth ? undefined : '100%',
            height: view.useDefaultHeight ? undefined : '100%',
            ...view.viewStyle,
            classes: formatStyleNames(view.viewStyle.classes),
          }}
        />
      </>
    )
  }
)

function applyClassTransforms(settings: SwiperProps): SwiperProps {
  const transformProps = [
    {
      setting: settings,
      keys: ['containerModifierClass', 'lazyPreloaderClass', 'noSwipingClass'],
    },
    {
      setting: settings.navigation,
      keys: [
        'disabledClass',
        'hiddenClass',
        'lockClass',
        'navigationDisabledClass',
      ],
    },
    {
      setting: settings.pagination,
      keys: [
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
      ],
    },
    {
      setting: settings.scrollbar,
      keys: [
        'dragClass',
        'horizontalClass',
        'lockClass',
        'scrollbarDisabledClass',
        'verticalClass',
      ],
    },
    {
      setting: settings.thumbs,
      keys: ['slideThumbActiveClass', 'thumbsContainerClass'],
    },
    { setting: settings.zoom, keys: ['containerClass', 'zoomedSlideClass'] },
    { setting: settings.mousewheel, keys: ['noMousewheelClass'] },
    { setting: settings.a11y, keys: ['notificationClass'] },
  ]

  transformProps.forEach(({ setting, keys }) => {
    if (typeof setting === 'object') {
      keys.forEach((key) => {
        const property = (setting as any)[key]
        if (typeof property === 'string' && !property.startsWith('psc-')) {
          ;(setting as any)[key] = formatStyleNames(property)
        }
      })
    }
  })

  return settings
}

export function SwiperComponent(props: ComponentProps<SwiperComponentProps>) {
  const swiperRef = useRef<SwiperRef>(null)

  const transformedSettings = useMemo(() => {
    const settings = props.props.settings || {}
    const transformed = transformProps(settings, [
      getScriptTransform({ self: props, client: window.__client }),
    ]) as SwiperProps
    return applyClassTransforms(transformed)
  }, [props.props.settings])

  const onResize = debounce(
    () => {
      swiperRef.current?.swiper.updateSlides()
    },
    100,
    {
      leading: true,
      trailing: true,
    }
  )

  return (
    <div {...props.emit()}>
      <Swiper
        {...transformedSettings}
        ref={swiperRef}
        modules={EnabledSwiperModules}
        observer
        observeSlideChildren
        style={{ height: '100%', width: '100%' }}
      >
        {props.props.instances.map((_, index) => {
          const mountPath = getChildMountPath(props, index)
          const viewProps = resolveViewProps(props.props, index)
          const outputListener = (
            outputName: string,
            outputValue: any
          ): void => {
            props.store.props.write(
              `instances[${index}].viewParams.${outputName}`,
              outputValue
            )
          }

          return (
            <SwiperSlide
              {...emitStyles(viewProps.slideStyle)}
              virtualIndex={index}
              key={viewProps.key}
            >
              <EmbeddedSlideView
                store={props.store.view.page.parent}
                view={viewProps}
                mountPath={mountPath}
                listenResize={transformedSettings.slidesPerView === 'auto'}
                onResize={onResize}
                key={viewProps.key}
                outputListener={outputListener}
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
