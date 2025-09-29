import "./Sidebar.css";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faTachometerAlt,
  faShoppingCart,
  faBoxOpen,
  faUsers,
  faChartBar,
  faTags
} from "@fortawesome/free-solid-svg-icons";
import PromoCard from "./PromoCard";

const menuItems = [
  { name: "Dashboard", icon: faTachometerAlt },
  { name: "Orders", icon: faShoppingCart },
  { name: "Products", icon: faBoxOpen },
  { name: "Customers", icon: faUsers },
  { name: "Reports", icon: faChartBar },
  { name: "Discounts", icon: faTags }
];

const Sidebar = ({ darkMode }) => {
  return (
    <aside className={darkMode ? "sidebar-dark" : "sidebar"}>
      <div className="sidebar-header">ShopMe</div>
      <ul className="sidebar-menu">
        {menuItems.map((item) => (
          <li key={item.name} className={darkMode ? "sidebar-item-dark" : "sidebar-item"}>
            <FontAwesomeIcon icon={item.icon} className="sidebar-icon" />
            <span>{item.name}</span>
          </li>
        ))}
      </ul>
      <div className={darkMode ? "sidebar-promo-dark" : "sidebar-promo"}>
        <PromoCard />
      </div>
    </aside>
  )
};

export default Sidebar;
