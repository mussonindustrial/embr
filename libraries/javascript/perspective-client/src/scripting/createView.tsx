import { View } from '@inductiveautomation/perspective-client'
import { CallingContext } from './ScriptingGlobals'
import { ViewProps } from '@inductiveautomation/perspective-client/build/dist/typedefs/app/View'
import React from 'react'

export type CreateView = (props: ViewProps) => void

// let mountPath = 0
// const getMountPath = () => `embr:${mountPath++}`

export function createViewFunction(context: CallingContext) {
  const createView = (props: ViewProps) => {
    if (context.client === undefined) {
      console.warn(
        'Cannot render view, current context does not contain a client store.'
      )
      return
    }

    // const mountPath = useRef(getMountPath())
    // console.log(mountPath.current)

    return (
      <View
        store={context.client}
        resourcePath={props.resourcePath}
        mountPath={props.mountPath}
        params={props.params}
        useDefaultHeight={props.useDefaultHeight}
        useDefaultMinHeight={props.useDefaultMinHeight}
        useDefaultMinWidth={props.useDefaultMinWidth}
        useDefaultWidth={props.useDefaultWidth}
        rootStyle={props.rootStyle}
      />
    )
  }

  createView.displayName = 'createView'
  return createView
}
