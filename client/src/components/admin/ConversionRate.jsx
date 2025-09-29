import React from "react";
import {
  RadialBarChart,
  RadialBar,
  Legend,
  ResponsiveContainer,
} from "recharts";
import "./ConversionRate.css";

const data = [
  {
    name: "Conversion",
    value: 62,
    fill: "#ff7d47",
  },
];

const ConversionRate = () => {
  return (
    <div className="conversion-rate">
      <div className="conversion-header">
        <h2>Conversion Rate</h2>
        <span className="conversion-change">+4.6%</span>
      </div>
      <p className="conversion-sub">Weekly customer engagement</p>

      <div className="chart-wrapper">
        <ResponsiveContainer width="100%" height={200}>
          <RadialBarChart
            innerRadius="80%"
            outerRadius="100%"
            data={data}
            startAngle={90}
            endAngle={450}
          >
            <RadialBar
              minAngle={15}
              background
              clockWise
              dataKey="value"
            />
            <Legend
              iconSize={10}
              layout="vertical"
              verticalAlign="middle"
              align="center"
            />
          </RadialBarChart>
        </ResponsiveContainer>
        <div className="conversion-percent">62%</div>
      </div>
    </div>
  );
};

export default ConversionRate;
