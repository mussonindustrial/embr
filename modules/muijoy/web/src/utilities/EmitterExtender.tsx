import React, { useEffect } from 'react'
import {
  ComponentStore,
  EmitProps,
  Emitter,
} from '@inductiveautomation/perspective-client'

type PatchedComponentStore = ComponentStore & {
  emitterFactoryOriginal: ComponentStore['emitterFactory']
}

export function patchEmitterFactory(
  componentStore: ComponentStore,
  props: Record<string, never>
) {
  const originalFactory = componentStore.emitterFactory

  ;(componentStore as PatchedComponentStore).emitterFactoryOriginal =
    originalFactory

  componentStore.emitterFactory = (layout, rotationEnabled): Emitter => {
    return (
      emitProps?: EmitProps,
      layoutStyleOnly?: boolean,
      disableEvents?: boolean,
      noLayoutStyle?: boolean
    ) => {
      const emitter = originalFactory(layout, rotationEnabled)
      const originalProps = emitter(
        emitProps,
        layoutStyleOnly,
        disableEvents,
        noLayoutStyle
      )

      return {
        ...originalProps,
        ...props,
      }
    }
  }
}

// /**
//  * Symbol used to stash original emitterFactory so we can restore it.
//  */
// const ORIGINAL_EMITTER_FACTORY = Symbol('originalEmitterFactory')
//
// /**
//  * Patch a ComponentStore's emitterFactory so the function it returns will include injected props.
//  *
//  * - store: the ComponentStore instance (or anything with an `emitterFactory` method).
//  * - extraPropsOrSupplier: either an object to merge, or a function (store => props) returning an object for dynamic values.
//  * - opts: { position: 'after'|'before' } determines merge precedence. 'after' (default) -> extra overrides.
//  *
//  * Returns a small object with an `unpatch()` function.
//  */
// export function patchEmitterFactory(
//   store: any,
//   extraPropsOrSupplier:
//     | Record<string, any>
//     | ((store: any, ...factoryArgs: any[]) => Record<string, any>),
//   opts?: { position?: 'after' | 'before' }
// ) {
//   if (!store) throw new Error('No store provided to patchEmitterFactory.')
//   if (store[ORIGINAL_EMITTER_FACTORY]) {
//     // already patched — replace supplier? we'll just return existing unpatch handle.
//     return { unpatch: () => unpatchEmitterFactory(store) }
//   }
//
//   const originalFactory = store.emitterFactory
//   if (typeof originalFactory !== 'function') {
//     throw new Error('store.emitterFactory is not a function.')
//   }
//
//   // stash original
//   store[ORIGINAL_EMITTER_FACTORY] = originalFactory
//
//   // replace with a wrapper factory
//   store.emitterFactory = function patchedEmitterFactory(...factoryArgs: any[]) {
//     // call original to obtain the factory-return (likely a function)
//     const factoryReturn = originalFactory.apply(this, factoryArgs)
//
//     // If the original didn't return a function, just return it unchanged.
//     if (typeof factoryReturn !== 'function') {
//       return factoryReturn
//     }
//
//     // return a wrapped emitter function
//     return function patchedEmitter(...emitterArgs: any[]) {
//       // call original emitter to get the props object
//       const originalProps = factoryReturn.apply(this, emitterArgs)
//
//       // compute extra props (support supplier function)
//       const extra =
//         typeof extraPropsOrSupplier === 'function'
//           ? extraPropsOrSupplier.call(this, store, ...factoryArgs)
//           : extraPropsOrSupplier
//
//       console.log(originalProps, extra)
//
//       // merge and return (do not mutate originalProps)
//       return {
//         ...originalProps,
//         ...extra
//       }
//     }
//   }
//
//   return { unpatch: () => unpatchEmitterFactory(store) }
// }
type EmitterExtender = {
  componentStore: ComponentStore
  children: React.ReactElement
}

export const EmitterExtender: React.FC<EmitterExtender> = ({
  componentStore,
  children,
  ...rest
}) => {
  delete (rest as Record<string, never>)['events']

  useEffect(() => {
    patchEmitterFactory(componentStore, rest)
  }, [])

  return <>{children}</>
}
