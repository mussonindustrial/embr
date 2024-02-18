import {
  ComponentMeta,
  ComponentRegistry,
} from "@inductiveautomation/perspective-client";
import { ChartJSComponent, ChartJSComponentMeta } from "./components/ChartJS";

export { ChartJSComponent };

const components: Array<ComponentMeta> = [new ChartJSComponentMeta()];

components.forEach((c: ComponentMeta) => ComponentRegistry.register(c));
