import {
  ComponentMeta,
  ComponentProps,
  PComponent,
  PropertyTree,
  SizeObject,
} from "@inductiveautomation/perspective-client";
import {
  Chart,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  // ChartTypeRegistry,
} from "chart.js";
import "chartjs-adapter-luxon";
import React, { useState } from "react";
import { Line } from "react-chartjs-2";
import ChartStreaming from "@robloche/chartjs-plugin-streaming";
import zoomPlugin from "chartjs-plugin-zoom";

export const COMPONENT_TYPE = "mussonindustrial.chart.chart-js";

interface ChartJSComponentProps {
  text?: string;
  value?: number;
}

Chart.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  zoomPlugin,
  ChartStreaming,
);

const zoomOptions = {
  pan: {
    enabled: true, // Enable panning
    mode: "x", // Allow panning in the x direction
  },
  zoom: {
    pinch: {
      enabled: true, // Enable pinch zooming
    },
    wheel: {
      enabled: true, // Enable wheel zooming
    },
    mode: "x", // Allow zooming in the x direction
  },
  limits: {
    x: {
      min: 1000, // Min value of the delay option
      max: 60000, // Max value of the delay option
      minRange: 1000,
    },
  },
} as const;

let previousValue = 0;

export function ChartJSComponent(
  component: ComponentProps<ChartJSComponentProps>,
) {
  const [data] = useState({
    datasets: [
      {
        label: "Dataset 1",

        fill: false,
        lineTension: 0.4,
        backgroundColor: "#f44336",
        borderColor: "#f44336",
        borderJoinStyle: "miter" as const,
        pointRadius: 0,
        showLine: true,
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        data: [] as any,
      },
    ],
  });

  const [options] = useState({
    plugins: {
      zoom: zoomOptions,
      tooltip: {
        mode: "nearest" as const,
        intersect: false,
      },
    },
    scales: {
      x: {
        type: "realtime" as const,
        realtime: {
          // onRefresh: function (chart: Chart) {
          //   chart.data.datasets[0].data.push({
          //     x: Date.now(),
          //     y: component.store.props.readNumber("value", 0),
          //   });
          //   console.log(
          //     `refreshing with value: ${component.store.props.readNumber("value", 0)}`,
          //   );
          // },
          delay: 2000,
          duration: 30000,
          ttl: 100000000,
          frameRate: 120,
        },
      },
    },
  });

  component.store.props.subscribe((tree: PropertyTree) => {
    const newValue = tree.readNumber("value", 0);
    if (previousValue !== newValue) {
      previousValue = newValue;
      component.def?.propConfig;
      chart.props.data.datasets[0].data.push({
        x: Date.now(),
        y: component.store.props.readNumber("value", 0),
      });
      chart.props.update("silent");
    }
  });

  const chart = <Line data={data} options={options} />;

  return chart;
}

export class ChartJSComponentMeta implements ComponentMeta {
  getComponentType(): string {
    return COMPONENT_TYPE;
  }

  getDefaultSize(): SizeObject {
    return {
      width: 300,
      height: 300,
    };
  }

  getPropsReducer(tree: PropertyTree): ChartJSComponentProps {
    return {
      text: tree.readString("text", "Default Text!"),
      value: tree.readNumber("value", 0),
    };
  }

  getViewComponent(): PComponent {
    return ChartJSComponent;
  }
}
