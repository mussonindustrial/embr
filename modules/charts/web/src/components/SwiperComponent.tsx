import React, { useMemo } from 'react'
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

import { Swiper, SwiperSlide, SwiperProps } from 'swiper/react'
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
    viewParams?: JsObject
    style?: StyleObject
}
type SwiperComponentProps = {
    instances: EmbeddedViewProps[]
    settings?: SwiperProps
    style?: StyleObject
}

function getMountMountPath(props: ComponentProps<PlainObject>, id: any) {
    return `${props.store.viewMountPath}$${props.store.addressPathString}[${id}]`
}


function EmbeddedView(props: {context: ComponentProps<any>, view: EmbeddedViewProps, id: any}) {
    const mountPath = getMountMountPath(props.context, props.id)
    return (
        <View
            key={PageStore.instanceKeyFor(props.view.viewPath, mountPath)}
            store={props.context.store.view.page.parent}
            mountPath={mountPath}
            resourcePath={props.view.viewPath}
            params={props.view.viewParams}
            rootStyle={props.view.style}
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

    const transformedProps = useMemo(() => {

        const { props: configProps, settings } = extractSettings(props.props)
        const transformedSettings = transformProps(settings, [
            getScriptTransform({ self: props, client: window.__client })
        ]) as SwiperProps

        installSettings(configProps, transformedSettings)
        return configProps

    }, [props.props.settings])


    return (
        <div { ...props.emit() }>
            <Swiper
                modules={EnabledSwiperModules}
                { ...transformedProps.settings }
                style={{ height: '100%', width: '100%' }}
            >
                {transformedProps.instances.map((instance, index) => 
                    <SwiperSlide>
                        <EmbeddedView context={props} view={instance} id={index}/>
                    </SwiperSlide>
                )}
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
            settings: tree.read('settings', {}),
            style: tree.read('style', {}),
        } as never
    }

    getViewComponent(): PComponent {
        return SwiperComponent as PComponent
    }
}
