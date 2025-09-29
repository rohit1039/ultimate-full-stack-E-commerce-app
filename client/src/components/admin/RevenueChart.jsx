import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Legend,
} from "recharts";
import "./RevenueChart.css";

const data = [
  { date: "Jan", revenue: 9400, orders: 4500 },
  { date: "Feb", revenue: 11200, orders: 5300 },
  { date: "Mar", revenue: 9800, orders: 4800 },
  { date: "Apr", revenue: 10500, orders: 5000 },
  { date: "May", revenue: 14500, orders: 7000 },
  { date: "Jun", revenue: 10200, orders: 4900 },
  { date: "Jul", revenue: 11300, orders: 5300 },
  { date: "Aug", revenue: 12000, orders: 5500 },
];


const GradientCursor = ({ points }) => {
  const { x } = points[0];
  const barHeight = 225;
  const chartHeight = 305;
  const y = (chartHeight - barHeight) / 2;

  return (
    <g>
      <defs>
        <linearGradient id="cursorGradient" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stopColor="#FF8201" stopOpacity={0.08} />
          <stop offset="100%" stopColor="#FF8201" stopOpacity={0.03} />
        </linearGradient>
      </defs>
      <rect
        x={x - 25}
        y={y}
        width={50}
        height={barHeight}
        fill="url(#cursorGradient)"
        pointerEvents="none"
      />
    </g>
  );
};

const renderCustomLegend = () => (
  <div
    style={{
      display: "flex",
      gap: "20px",
      marginTop: "-1.5rem",
      paddingLeft: "10px",
    }}
  >
    <div
      style={{
        display: "flex",
        alignItems: "center",
        gap: "6px",
        marginLeft: "9px",
      }}
    >
      <svg width="24" height="10">
        <line x1="0" y1="5" x2="24" y2="5" stroke="#FF8201" strokeWidth="3" />
      </svg>
      <span style={{ fontSize: "13px" }}>Revenue</span>
    </div>
    <div
      style={{
        display: "flex",
        alignItems: "center",
        gap: "6px",
        marginLeft: "9px",
      }}
    >
      <svg width="24" height="10">
        <line
          x1="0"
          y1="5"
          x2="24"
          y2="5"
          stroke="#FFC488"
          strokeWidth="2"
          strokeDasharray="5 5"
        />
      </svg>
      <span style={{ fontSize: "13px" }}>Orders</span>
    </div>
  </div>
);

const CustomTooltip = ({ active, payload, label, coordinate }) => {
  if (active && payload && payload.length && coordinate) {
    return (
      <div
        style={{
          position: "absolute",
          left: coordinate.x - 60,
          top: "-2rem",
          background: "#fff",
          borderRadius: "8px",
          padding: "6px 10px",
          fontSize: "12px",
          lineHeight: "1.5rem",
          boxShadow: "0 2px 6px rgba(0, 0, 0, 0.2)",
          color: "#333",
          width: "120px",
          pointerEvents: "none",
          zIndex: 10,
        }}
      >
        <strong>{label}</strong>
        {payload.map((entry, index) => (
          <div key={index} style={{ color: entry.color }}>
            {entry.name}: ${entry.value.toLocaleString()}
          </div>
        ))}
      </div>
    );
  }

  return null;
};


const RevenueChart = ({darkMode}) => {
  return (
    <div className={darkMode ? "revenue-chart-container-dark" : "revenue-chart-container"}>
      <div className="chart-header">
        <h2 className="chart-title">Revenue Analytics</h2>
        <button className="chart-date-range">Last 6 Months <span className="down-arrow">▼</span></button>
      </div>
      <div className="chart-content">
        <ResponsiveContainer width="100%" height={300}>
          <LineChart data={data} margin={{ top: 15, right: 15, left: 5, bottom: 5 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="#ddd" vertical={false} />
            <XAxis
              dataKey="date"
              stroke="#666"
              tick={{ fontSize: 12 }}
              interval={0}
              textAnchor="end"
              padding={{ left: 30, right: 10 }}
            />
            <YAxis
              tick={{ fontSize: 12 }}
              stroke="#666"
              tickFormatter={(value) => `${(value / 1000).toFixed(1)}k`}
            />
            <Tooltip
              content={<CustomTooltip />}
              cursor={<GradientCursor />}
            />
            <Legend verticalAlign="top" height={36} content={renderCustomLegend} />
            <Line
              type="monotone"
              dataKey="revenue"
              stroke="#FF8201"
              strokeWidth={3}
              dot={{ r: 5 }}
              activeDot={{ r: 7 }}
              name="Revenue"
            />
            <Line
              type="monotone"
              dataKey="orders"
              stroke="#FFC488"
              strokeWidth={2}
              strokeDasharray="5 5"
              dot={{ r: 4 }}
              activeDot={{ r: 6 }}
              name="Orders"
            />
          </LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
};

export default RevenueChart;
