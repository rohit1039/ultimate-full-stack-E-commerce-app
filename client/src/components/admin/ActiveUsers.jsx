import React from "react";
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  CartesianGrid,
} from "recharts";
import "./ActiveUsers.css";

const data = [
  { day: "Mon", users: 320 },
  { day: "Tue", users: 400 },
  { day: "Wed", users: 390 },
  { day: "Thu", users: 450 },
  { day: "Fri", users: 500 },
  { day: "Sat", users: 470 },
  { day: "Sun", users: 530 },
];

const ActiveUsers = () => {
  return (
    <div className="active-users">
      <div className="active-header">
        <h2>Active Users</h2>
        <span className="weekly-change">+12.8% this week</span>
      </div>

      <h3 className="active-count">1,934</h3>

      <ResponsiveContainer width="100%" height={180}>
        <AreaChart data={data}>
          <defs>
            <linearGradient id="usersColor" x1="0" y1="0" x2="0" y2="1">
              <stop offset="5%" stopColor="#0083db" stopOpacity={0.6} />
              <stop offset="95%" stopColor="#0083db" stopOpacity={0} />
            </linearGradient>
          </defs>
          <CartesianGrid strokeDasharray="3 3" stroke="#eee" />
          <XAxis dataKey="day" stroke="#888" />
          <YAxis stroke="#888" />
          <Tooltip />
          <Area
            type="monotone"
            dataKey="users"
            stroke="#0083db"
            fillOpacity={1}
            fill="url(#usersColor)"
          />
        </AreaChart>
      </ResponsiveContainer>
    </div>
  );
};

export default ActiveUsers;
