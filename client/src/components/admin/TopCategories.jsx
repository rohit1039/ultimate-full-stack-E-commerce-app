import React from "react";
import "./TopCategories.css";

const TopCategories = ({ darkMode }) => {
  const totalSales = 3400000;
  const categories = [
    { name: "Electronics", color: "#FF8203", sales: 1200000 },
    { name: "Fashion", color: "#FFB466", sales: 950000 },
    { name: "Home & Kitchen", color: "#FED2A3", sales: 750000 },
    { name: "Beauty & Personal Care", color: "#FFE0C3", sales: 500000 },
  ];

  const getStrokeDashoffset = (value, total) => {
    const percentage = (value / total) * 100;
    const circumference = 2 * Math.PI * 100; // radius = 100
    return circumference - (percentage / 100) * circumference;
  };

  return (
    <div className={darkMode ? "top-categories-card-dark" : "top-categories-card"}>
      <div className="header">
        <h5>Top Categories</h5>
      </div>
      <div className="donut-chart">
        <svg width="240" height="240" viewBox="0 0 240 240">
          {(() => {
            const strokeWidth = 20;
            const center = 120;
            const radius = 100 - strokeWidth / 2;
            const gapAngle = 2;
            const totalSalesValue = categories.reduce((sum, c) => sum + c.sales, 0);

            let currentAngle = -90;

            const getCoordinates = (angle, radius) => {
              const rad = (Math.PI / 180) * angle;
              return [
                center + radius * Math.cos(rad),
                center + radius * Math.sin(rad),
              ];
            };

            return categories.map((category, idx) => {
              const valuePercent = category.sales / totalSalesValue;
              const sweepAngle = valuePercent * 360 - gapAngle;

              const start = getCoordinates(currentAngle, radius);
              const end = getCoordinates(currentAngle + sweepAngle, radius);
              const largeArcFlag = sweepAngle > 180 ? 1 : 0;

              const d = `
        M ${start[0]} ${start[1]}
        A ${radius} ${radius} 0 ${largeArcFlag} 1 ${end[0]} ${end[1]}
      `;

              const arc = (
                <path
                  key={idx}
                  d={d}
                  fill="none"
                  stroke={category.color}
                  strokeWidth={strokeWidth}
                  strokeLinecap="butt"
                />
              );

              currentAngle += sweepAngle + gapAngle;
              return arc;
            });
          })()}
        </svg>
        <div className="center-text">
          <p>Total Sales</p>
          <h3>${totalSales.toLocaleString()}</h3>
        </div>
      </div>

      <ul className="category-list">
        {categories.map((cat, idx) => (
          <li key={idx}>
            <span
              className="color-indicator"
              style={{ backgroundColor: cat.color }}
            ></span>
            <span style={{ textAlign: 'left' }}>{cat.name}</span>
            <span style={{ fontWeight: 'bold' }}>${cat.sales.toLocaleString()}</span>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default TopCategories;
