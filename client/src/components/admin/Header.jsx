import "./Header.css";
import img from "../../assets/users/dp.jpg";
import { FaBell, FaSearch, FaSun, FaMoon } from "react-icons/fa";

const Header = ({ darkMode, onToggleDarkMode }) => {
  return (
    <div className="header">
      <div className="header-left">
        <h2 className="header-title">Dashboard</h2>
        <div className="header-search">
          <input
            type="text"
            placeholder="Search stock, order, etc"
            className={darkMode ? "admin-search-input-dark" : "admin-search-input"}
          />
        </div>
      </div>

      <div className="header-right">
        <FaBell className="icon" />
        <button onClick={onToggleDarkMode} className="theme-toggle">
          <FaSun className="icon light" />
          <FaMoon className="icon dark" />
        </button>
        <div className="profile">
          <img
            src={img}
            alt="User"
            className="profile-img"
          />
          <div className="profile-info">
            <span className="profile-name">Rohit Parida</span>
            <span className="profile-role">Admin</span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Header;
