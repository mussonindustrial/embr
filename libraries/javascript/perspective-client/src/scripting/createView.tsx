import { View } from '@inductiveautomation/perspective-client'
import { ViewProps } from '@inductiveautomation/perspective-client/build/dist/typedefs/app/View'
import React from 'react'

import { CallingContext } from '../stores'

export type CreateView = (props: ViewProps) => void
export function createViewFunction(context: CallingContext) {
  const createView = (props: ViewProps) => {
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

  createView.displayName = 'createView'
  return createView
}
