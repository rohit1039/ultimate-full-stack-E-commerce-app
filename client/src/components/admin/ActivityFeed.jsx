import React from "react";
import "./ActivityFeed.css";

const activities = [
  { time: "10 mins ago", message: "New user JohnDoe registered" },
  { time: "30 mins ago", message: "Order #1023 has been shipped" },
  { time: "1 hour ago", message: "Inventory updated for product SKU-3842" },
  { time: "2 hours ago", message: "New review posted on 'Wireless Headphones'" },
  { time: "5 hours ago", message: "Email campaign sent to 1,200 users" },
];

const ActivityFeed = () => {
  return (
    <div className="activity-feed">
      <h2 className="activity-title">Activity Feed</h2>
      <ul className="activity-list">
        {activities.map((act, i) => (
          <li key={i} className="activity-item">
            <div className="bullet"></div>
            <div className="activity-content">
              <p className="message">{act.message}</p>
              <span className="timestamp">{act.time}</span>
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default ActivityFeed;
