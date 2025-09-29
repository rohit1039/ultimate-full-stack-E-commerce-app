import { useState, useEffect } from "react";
import Sidebar from "./Sidebar";
import Header from "./Header";
import StatsCard from "./StatsCard";
import RevenueChart from "./RevenueChart";
import MonthlyTarget from "./MonthlyTarget";
import OrdersTable from "./OrdersTable";
import TopCategories from "./TopCategories";
import "./Layout.css";
import Footer from "./Footer";

export default function Layout() {
  const [darkMode, setDarkMode] = useState(false);

  // Load saved mode on mount
  useEffect(() => {
    const storedMode = localStorage.getItem("darkMode");
    if (storedMode === "true") {
      setDarkMode(true);
    }
  }, []);

  const toggleDarkMode = () => {
    const newMode = !darkMode;
    setDarkMode(newMode);
    localStorage.setItem("darkMode", newMode ? "true" : "false");
  };

  return (
    <div className={`app-container ${darkMode ? "dark-mode" : ""}`}>
      <Sidebar darkMode={darkMode} />
      <main className="main-content">
        <Header darkMode={darkMode} onToggleDarkMode={toggleDarkMode} />
        <div className="stats-section">
          <div className="stats-revenue-section">
            <StatsCard darkMode={darkMode} />
            <div className="target-revenue-section">
              <RevenueChart darkMode={darkMode} />
              <MonthlyTarget darkMode={darkMode} />
            </div>
          </div>
          <div>
            <TopCategories darkMode={darkMode} />
          </div>
        </div>
        <div className="orders-activity-section">
          <OrdersTable darkMode={darkMode} />
        </div>
        {/* <Footer /> */}
      </main>
    </div>
  );
}
