/**
 * Example of a component which displays an image, given a URL.
 */
import * as React from 'react';
import {
    Component,
    ComponentMeta,
    ComponentProps, PComponent,
    PropertyTree,
    SizeObject
} from '@inductiveautomation/perspective-client';

export const COMPONENT_TYPE = "mussonindustrial.display.example";

interface SimpleComponentProps {
    text?: string;
}

interface SimpleComponentState {
    tagCount: number;
    animating: boolean;
}

export class SimpleComponent extends Component<ComponentProps<SimpleComponentProps>, SimpleComponentState> {
    state: SimpleComponentState = {
        tagCount: 0,
        animating: false
    };

    componentDidMount(): void {

    }

    componentWillUnmount(): void {
    }

    render() {
        const { props, emit } = this.props;

        return (
            <div {...emit({ classes: ['simplecomponent'] })}>
                <span>This is a simple component, with only a "text" prop: </span>
                <span>{props.text}</span>
            </div>

        );
    }
}


// This is the actual thing that gets registered with the component registry.
export class SimpleComponentMeta implements ComponentMeta {
    getComponentType(): string {
        return COMPONENT_TYPE;
    }

    getDefaultSize(): SizeObject {
        return ({
            width: 160,
            height: 64
        });
    }

    getPropsReducer(tree: PropertyTree): SimpleComponentProps {
        return {
            text: tree.readString("text", "Default Text!")
        };
    }

    getViewComponent(): PComponent {
        return SimpleComponent;
    }
}