import * as React from 'react'
import {
    ComponentMeta,
    ComponentProps,
    PComponent,
    PropertyTree,
    SizeObject,
} from '@inductiveautomation/perspective-client'
import { useState } from 'react'

export const COMPONENT_TYPE = 'mussonindustrial.display.example'

interface ExampleComponentProps {
    text?: string
    value?: number
}

export function ExampleComponent(props: ComponentProps<ExampleComponentProps>) {
    const [counter, setCounter] = useState(props.props.value)
    const handleClick = () => {
        if (counter != undefined) {
            setCounter(counter + 1)
        } else {
            setCounter(0)
        }
        props.store.props.write('value', counter)
    }

    return (
        <div {...props.emit({ classes: ['examplecomponent'] })}>
            <span>This is a simple component, with only a "text" prop: </span>
            <span>{props.props.text}</span>
            <span>{props.props.value}</span>
            <button onClick={handleClick}>Increment</button>
        </div>
    )
}

export class ExampleComponentMeta implements ComponentMeta {
    getComponentType(): string {
        return COMPONENT_TYPE
    }

    getDefaultSize(): SizeObject {
        return {
            width: 160,
            height: 64,
        }
    }

    getPropsReducer(tree: PropertyTree): ExampleComponentProps {
        return {
            text: tree.readString('text', 'Default Text!'),
            value: tree.readNumber('value', 0),
        }
    }

    getViewComponent(): PComponent {
        return ExampleComponent
    }
}
