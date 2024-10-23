import React, { useRef } from 'react'
import {
    ComponentMeta,
    ComponentProps,
    JsObject,
    PageStore,
    PComponent,
    PlainObject,
    PropertyTree,
    SizeObject,
    StyleObject,
    View,
} from '@inductiveautomation/perspective-client'

import { Swiper, SwiperSlide, SwiperProps, SwiperRef } from 'swiper/react'
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
import { unset } from 'lodash';
import { transformProps } from '@embr-js/utils';
import { getScriptTransform } from '../util';

const COMPONENT_TYPE = 'embr.chart.swiper'

type EmbeddedViewProps = {
    viewPath: string
    viewParams: JsObject
    viewStyle: StyleObject
    slideStyle: StyleObject
}
    
type SwiperComponentProps = {
    viewPath?: string
    viewParams?: JsObject
    viewStyle: StyleObject
    slideStyle: StyleObject
    instances: EmbeddedViewProps[]
    settings?: SwiperProps
    style?: StyleObject
}
type SlideData = {
    isActive: boolean;
    isVisible: boolean;
    isPrev: boolean;
    isNext: boolean;
  }

function getMountMountPath(props: ComponentProps<PlainObject>, id: any) {
    return `${props.store.viewMountPath}$${props.store.addressPathString}[${id}]`
}

function resolve(inputs: Array<unknown>) {
    let value: unknown
    for (let i = 0; i < inputs.length; i++) {
        value = inputs[i]
        if (value === undefined) {
            continue;
        }
        if (value !== undefined) {
            return value as any;
        }
    }
}

function resolveSlideProps(props: SwiperComponentProps, slideIndex: number): EmbeddedViewProps {
    const slideProps = props.instances[slideIndex]

    return {
        viewPath: resolve([slideProps.viewPath, props.viewPath]),
        viewParams: {
            ...props.viewParams,
            ...slideProps.viewParams,
            index: slideIndex
        },
        viewStyle: {
            ...props.viewStyle,
            ...slideProps.viewStyle
        },
        slideStyle: {
            ...props.slideStyle,
            ...slideProps.slideStyle
        }
    }
}


function EmbeddedSlide(props: {parent: ComponentProps<SwiperComponentProps>, index: number, slideData?: SlideData}) {
    const mountPath = getMountMountPath(props.parent, props.index)
    const viewProps = resolveSlideProps(props.parent.props, props.index)

    return (
        <View
            key={PageStore.instanceKeyFor(viewProps.viewPath, mountPath)}
            store={props.parent.store.view.page.parent}
            mountPath={mountPath}
            resourcePath={viewProps.viewPath}
            params={{
                ...viewProps.viewParams,
                index: props.index,
                slideData: props.slideData,
            }}
            rootStyle={{
                ...viewProps.viewStyle,
                height: '100%',
                width: '100%'
            }}
        />
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

export function SwiperComponent(props: ComponentProps<SwiperComponentProps>) {

    const swiperRef = useRef<SwiperRef>(null);

    const { props: transformedProps, settings } = extractSettings(props.props)
    const transformedSettings = transformProps(settings, [
        getScriptTransform({ self: props, client: window.__client })
    ]) as SwiperProps
    installSettings(transformedProps, transformedSettings)

    const slides = () => transformedProps.instances.map((instance, index) => {
        const slideStyle = {
            ...transformedProps.slideStyle,
            ...instance.slideStyle
        }

        return <SwiperSlide style={slideStyle}>
            {(slideData) => {
                return <EmbeddedSlide 
                    parent={props} 
                    index={index} 
                    slideData={slideData}
                />
            }}
        </SwiperSlide>
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
            {...slides()}
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
