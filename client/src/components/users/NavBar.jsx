import { useState } from 'react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faCircleHalfStroke, faShoppingCart, faSearch, faUser, faMoon, faBell } from '@fortawesome/free-solid-svg-icons';
import { Link, useNavigate } from 'react-router-dom'; // Import React Router hook

const NavBar = ({ cartCount }) => {
  const [darkMode, setDarkMode] = useState(false);
  const [menuOpen, setMenuOpen] = useState(false);
  const navigate = useNavigate(); // Hook to navigate programmatically

  const toggleTheme = () => setDarkMode(!darkMode);
  const toggleMenu = () => setMenuOpen(!menuOpen);
  const goToCart = () => navigate('/cart'); // Navigate to Cart page

  const navStyle = {
    display: 'flex',
    gap: '5.75rem',
    alignItems: 'center',
    backgroundColor: darkMode ? '#242b2e' : '#0083db',
    padding: '1rem',
    borderRadius: '4px',
    position: 'relative',
    margin: 'auto 3rem',
    marginTop: '2.25rem',
    width: '93.75%'
  };

  const navItemsContainer = {
    display: 'flex',
    gap: '1.5rem',
    width: '100%',
    fontSize: '13px',
    alignItems: 'center'
  };

  const mobileMenuStyle = {
    display: menuOpen ? 'flex' : 'none',
    flexDirection: 'column',
    position: 'absolute',
    top: '100%',
    left: 0,
    width: '100%',
    backgroundColor: darkMode ? '#242b2e' : '#0088dd',
    padding: '1rem',
    gap: '1rem',
    zIndex: 1,
  };

  const toggleContainerStyle = {
    display: 'flex',
    alignItems: 'center',
    marginLeft: '-1rem'
  };

  return (
    <>
      {/* Top Transparent Navbar */}
      <div style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        padding: '0.5rem 1.5rem',
        backgroundColor: 'transparent',
        backdropFilter: 'blur(6px)',
        color: '#000000',
        fontWeight: '600',
        position: 'relative',
        marginBottom: '-2rem',
        marginLeft: '2rem',
        marginTop: '0.5rem',
        width: '95%',
      }}>

        {/* Logo */}
        <div style={{ textAlign: 'left' }}>
          <span className='left-logo' style={{ fontSize: '1.5rem', fontWeight: 'bold', marginLeft: '1rem' }}>ShopMe</span>
        </div>

        <div style={{ flex: '1', display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '1.75rem' }}>
          {/* Search Bar */}
          <div className='search-container'>
            <FontAwesomeIcon className='search-icon' icon={faSearch} />
            <input type="text" placeholder="Search for products" style={{ paddingLeft: "2.25rem" }} />
            <select>
              <option>All Categories</option>
              <option>Men's Wear</option>
              <option>Women's Wear</option>
              <option>Kid's Wear</option>
            </select>
          </div>

          {/* Cart Icon with Badge */}
          <div style={{ position: 'relative', display: 'flex', alignItems: 'center', cursor: 'pointer', marginLeft: "-2rem" }}
            onClick={goToCart}>
            <div style={{
              position: 'relative',
              display: 'inline-block',
              padding: '8px 12px',
              cursor: 'pointer',
              backgroundColor: '#fff',
              marginLeft: '1.5rem'
            }}>
              <FontAwesomeIcon className='cart' icon={faShoppingCart} color='#0083db' title="View Cart" />
              {cartCount > 0 && (
                <span style={{
                  position: 'absolute',
                  top: '-8px',
                  right: '-5px',
                  background: '#e53935',
                  color: '#fff',
                  borderRadius: '4px',
                  padding: '2px 4.5px',
                  fontSize: '13px',
                  fontWeight: 'bold',
                  lineHeight: 1,
                  minWidth: '18px',
                  textAlign: 'center',
                  whiteSpace: 'nowrap',
                  border: '1px solid #fff'
                }}>
                  {cartCount}
                </span>
              )}
            </div>
          </div>

          {/* Notifications Icon */}
          <div style={{
            position: 'relative',
            display: 'inline-block',
            padding: '8px 12px',
            cursor: 'pointer',
            marginLeft: '-1.25rem',
            backgroundColor: '#fff'
          }}>
            <FontAwesomeIcon className='cart' icon={faBell} color='#0088dd' title="Notifications" style={{ fontSize: '1.25rem' }} />
            {cartCount > 0 && (
              <span style={{
                position: 'absolute',
                top: '-8px',
                right: '-2px',
                background: '#e53935',
                color: '#fff',
                borderRadius: '4px',
                padding: '2px 4.5px',
                fontSize: '13px',
                fontWeight: 'bold',
                lineHeight: 1,
                minWidth: '18px',
                textAlign: 'center',
                whiteSpace: 'nowrap',
                border: '1px solid #fff'
              }}>
                {cartCount}
              </span>
            )}
          </div>

          {/* Theme Toggle */}
          <div style={toggleContainerStyle}>
            <span onClick={toggleTheme} title={darkMode ? 'Enable Light Mode' : 'Enable Dark Mode'} style={{
              width: '40px',
              height: '40px',
              borderRadius: '50%',
              backgroundColor: 'transparent',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              cursor: 'pointer',
              transition: 'all 0.3s ease'
            }}>
              {darkMode ?
                <FontAwesomeIcon icon={faMoon} style={{
                  fontSize: '20px',
                  color: 'black',
                  transform: darkMode ? 'rotate(0deg)' : 'rotate(180deg)',
                  transition: 'transform 0.3s ease, color 0.3s ease'
                }} />
                :
                <FontAwesomeIcon icon={faCircleHalfStroke} style={{
                  fontSize: '20px',
                  color: '#0088dd',
                  transform: darkMode ? 'rotate(0deg)' : 'rotate(180deg)',
                  transition: 'transform 0.3s ease, color 0.3s ease'
                }} />
              }
            </span>
          </div>
        </div>

        {/* Account Section */}
        <div style={{ display: 'flex', gap: '0.75rem', position: 'relative', cursor: 'pointer', marginLeft: '8rem' }}>
          <span style={{ fontSize: '13px', fontWeight: 'lighter' }}>Account</span>
          <FontAwesomeIcon className='orders' icon={faUser} color='#0088dd' title="Orders" />
        </div>
        <div style={{ flex: '1' }}></div>
      </div>

      {/* Main Category Navbar */}
      <nav style={navStyle}>
        <div style={{ display: 'flex', alignItems: 'center', fontSize: '14px', width: '22.5%' }}>
          <p>Welcome, Rohit!</p>
        </div>

        <div className='desktop-menu' style={{ ...navItemsContainer }}>
          <Link to="/" >Home</Link>
          <Link to="/products">Products</Link>
          <Link to="/categories">Categories</Link>
          <Link to="/mens-wear">Men's Wear</Link>
          <Link to="/womens-wear">Women's Wear</Link>
          <Link to="/kids-wear">Kid's Wear</Link>
          <Link to="/orders">Orders</Link>
          <Link to="/about">About us</Link>
          <Link to="/contact">Contact us</Link>
          <Link to="/register">Register</Link>
          <Link to="/login">Login</Link>
        </div>

        {/* Mobile Menu */}
        {menuOpen && (
          <div className='mobile-menu' style={mobileMenuStyle}>
            <div>Home</div>
            <div>Products</div>
            <div>Categories</div>
            <div>Men's Wear</div>
            <div>Women's Wear</div>
            <div>Kid's Wear</div>
            <div>About us</div>
            <div>Contact us</div>
            <div>Register</div>
            <div>Login</div>
            <div style={toggleContainerStyle}>
              <span onClick={toggleTheme} title={darkMode ? 'Enable Light Mode' : 'Enable Dark Mode'} style={{
                width: '40px',
                height: '40px',
                borderRadius: '50%',
                backgroundColor: 'transparent',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                cursor: 'pointer'
              }}>
                <FontAwesomeIcon icon={faCircleHalfStroke} style={{
                  fontSize: '20px',
                  color: darkMode ? '#f1c40f' : '#0088dd',
                  transform: darkMode ? 'rotate(180deg)' : 'rotate(0deg)',
                  transition: 'transform 0.3s ease, color 0.3s ease'
                }} />
              </span>
            </div>
          </div>
        )}

        {/* Responsive Styles */}
        <style>{`
          @media(min-width: 768px) {
            .desktop-menu {
              display: flex !important;
            }
            .mobile-menu {
              display: none !important;
            }
          }
        `}</style>
      </nav>
    </>
  );
};

export default NavBar;
