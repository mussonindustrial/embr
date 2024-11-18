import {
  ComponentStoreDelegate,
  View,
  ViewProps,
  ViewStore,
} from '@inductiveautomation/perspective-client'

export type JoinableViewProps = ViewProps & {
  delegate: ComponentStoreDelegate
}

/**
 * A `View` that sends a `view-join` message to a component delegate on startup,
 * instead of sending a `view-start` message to the page.
 * This allows for more control over how views are started.
 */
export class JoinableView extends View {
  props: JoinableViewProps

  constructor(props: JoinableViewProps) {
    super(props)
    this.props = props

    this.installOverrides = this.installOverrides.bind(this)
    this.installOverrides()
  }

  /**
   * Each time `stopInstance` is called, all overridden methods are lost.
   * This method reinstalls those overrides.
   */
  installOverrides(): void {
    this.installViewStore = (viewStore: ViewStore) => {
      const joiningViewStore = makeJoiningViewStore(
        this.props.delegate,
        viewStore
      )
      super.installViewStore.call(this, joiningViewStore)
    }

    this.stopInstance = () => {
      super.stopInstance.call(this)
      this.installOverrides.call(this)
    }
  }

  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  override installViewStore(_viewStore: ViewStore): void {
    return
  }

  override stopInstance(): void {
    return
  }
}

/**
 * A `ViewStore` that sends a `view-join` message to a component delegate on startup.
 */
type JoiningViewStore = ViewStore

/**
 * Turn a regular `ViewStore` into a `JoiningViewStore`, that sends a `view-join` message on startup.
 * @param delegate
 * @param viewStore
 * @returns
 */
function makeJoiningViewStore(
  delegate: ComponentStoreDelegate,
  viewStore: ViewStore
): JoiningViewStore {
  Reflect.defineProperty(viewStore, 'startup', {
    value: () => {
      notifyViewJoin(delegate, viewStore)
      viewStore.running = true
    },
  })

  return viewStore
}

function notifyViewJoin(
  delegate: ComponentStoreDelegate,
  viewStore: ViewStore
) {
  const params = viewStore.running
    ? viewStore.params.readEncoded('', false)
    : viewStore.initialParams

  delegate.fireEvent('view-join', {
    resourcePath: viewStore.resourcePath,
    mountPath: viewStore.mountPath,
    birthDate: viewStore.birthDate,
    params,
  })
  viewStore.running = true
}
