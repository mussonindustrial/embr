import { View } from '@inductiveautomation/perspective-client'
import { CallingContext } from './ScriptingGlobals'
import { ViewProps } from '@inductiveautomation/perspective-client/build/dist/typedefs/app/View'
import React from 'react'

export type RenderView = (props: ViewProps) => void

export function createRenderView(context: CallingContext) {
  // eslint-disable-next-line react/display-name
  return (props: ViewProps) => {
    if (context.client === undefined) {
      console.warn(
        'Cannot render view, current context does not contain a client store.'
      )
      return
    }

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
}
