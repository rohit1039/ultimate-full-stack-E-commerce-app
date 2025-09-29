import { Link, useLocation } from "react-router-dom";
import "./BreadCrumb.css";

export const formatSlug = (slug) => slug
  .toLowerCase()
  .replace(/-/g, " ")
  .replace(/(^|\s)([a-z])/g, (_, sep, char) => sep + char.toUpperCase());

const BreadCrumb = () => {
  const location = useLocation();
  const pathnames = location.pathname.split("/").filter(Boolean);

  return (
    <div className="breadcrumb-container">
      <Link to="/">Home</Link>
      {pathnames.map((name, index) => {
        const routeTo = "/" + pathnames.slice(0, index + 1).join("/");
        const isLast = index === pathnames.length - 1;
        return (
          <span key={name}>
            <span className="breadcrumb-separator">{">"}</span>
            {isLast ? (
              <span>{formatSlug(decodeURIComponent(name))}</span>
            ) : (
              <Link to={routeTo}>{formatSlug(decodeURIComponent(name))}</Link>
            )}
          </span>

        );
      })}
    </div>
  );
};

export default BreadCrumb;
