import { ReactComponent as Dollar } from "../../assets/others/dollar.svg";
import { ReactComponent as Orders } from "../../assets/others/orders.svg";
import { ReactComponent as Visitors } from "../../assets/others/visitors.svg";
import "./StatsCard.css";

const stats = [
  {
    title: "Total Sales",
    value: "$983,410",
    icon: Dollar,
  },
  {
    title: "Total Orders",
    value: "58,375",
    icon: Orders,
  },
  {
    title: "Total Visitors",
    value: "237,782",
    icon: Visitors,
  },
];

const StatsCard = ({ darkMode }) => {
  return (
    <div className="stats-cards">
      {stats.map((stat, i) => (
        <div className="stat-card active" key={i}>
          <div className="stat-info">
            <div>
              <p className="stat-title">{stat.title}</p>
              <h2 className="stat-value">{stat.value}</h2>
            </div>
            <div className="stat-icon-wrapper">
              <stat.icon className="stat-icon" />
            </div>
          </div>
        </div>
      ))}
    </div>
  );
};

export default StatsCard;
