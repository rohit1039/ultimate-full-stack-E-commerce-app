import React from "react";
import "./TrafficSources.css";

const sources = [
  { label: "Organic Search", value: 55, color: "#4caf50" },
  { label: "Direct", value: 25, color: "#2196f3" },
  { label: "Referral", value: 12, color: "#ff9800" },
  { label: "Social Media", value: 8, color: "#e91e63" },
];

const TrafficSources = () => {
  return (
    <div className="traffic-sources">
      <h2 className="traffic-title">Traffic Sources</h2>
      <ul>
        {sources.map((source, i) => (
          <li key={i}>
            <div className="label">
              <span className="dot" style={{ backgroundColor: source.color }}></span>
              {source.label}
            </div>
            <div className="bar-container">
              <div
                className="bar"
                style={{
                  width: `${source.value}%`,
                  backgroundColor: source.color,
                }}
              ></div>
            </div>
            <span className="percentage">{source.value}%</span>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default TrafficSources;
