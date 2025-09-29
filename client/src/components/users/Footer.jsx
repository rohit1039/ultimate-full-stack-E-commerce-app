import "./Footer.css";
import { FaFacebook, FaInstagram, FaTwitter, FaLinkedin } from 'react-icons/fa';

const Footer = () => {
  return (
    <footer className="footer">
      {/* About */}
      <div className="footer-section-first-child">
        <p className="footer-about-text">
          ShopMe is your one-stop destination for trendy fashion wear. Explore curated clothing and accessories that fit all styles and seasons.
        </p>
      </div>

      <div className="footer-container">
        {/* Quick Links */}
        <div className="footer-section">
          <h3>Quick Links</h3>
          <ul>
            {['Home', 'Shop', 'About', 'Contact'].map((text) => (
              <li key={text}>
                <a href={`/${text.toLowerCase()}`}>{text}</a>
              </li>
            ))}
          </ul>
        </div>

        {/* Popular Categories */}
        <div className="footer-section">
          <h3>Top Categories</h3>
          <ul>
            {["Men's Wear", "Women's Wear", "Kid's Wear", "Men's Footwear"].map((cat) => (
              <li key={cat}>
                <a href={`/category/${cat.toLowerCase().replace(/\s/g, '')}`}>{cat}</a>
              </li>
            ))}
          </ul>
        </div>

        {/* Our Policies */}
        <div className="footer-section">
          <h3>Our Policies</h3>
          <ul>
            {['Privacy Policy', 'Terms of Service', 'Refund Policy'].map((policy) => (
              <li key={policy}>
                <a href={`/${policy.toLowerCase().replace(/\s/g, '-')}`}>{policy}</a>
              </li>
            ))}
          </ul>
        </div>

        {/* Top Brands */}
        <div className="footer-section">
          <h3>Top Brands</h3>
          <ul>
            {['Nike', 'Adidas', 'Zara', 'H&M'].map((brand) => (
              <li key={brand}>
                <a href={`/brand/${brand.toLowerCase()}`}>{brand}</a>
              </li>
            ))}
          </ul>
        </div>

        {/* Customer Service */}
        <div className="footer-section">
          <h3>Customer Service</h3>
          <ul>
            {['FAQs', 'Returns', 'Shipping', 'Support'].map((text) => (
              <li key={text}>
                <a href={`/${text.toLowerCase()}`}>{text}</a>
              </li>
            ))}
          </ul>
        </div>

        {/* Contact Info */}
        <div className="footer-section footer-contact">
          <h3>Contact Us</h3>
          <p>Fashion St, New Delhi, India</p>
          <p>wearinservice@gmail.com</p>
          <p>+91 9876543210</p>
        </div>
      </div>

      <div className="footer-section footer-social">
        <h3>Follow Us</h3>
        <div className="social-links">
          <a href="https://facebook.com" target="_blank" rel="noopener noreferrer" aria-label="Facebook">
            <FaFacebook />
          </a>
          <a href="https://instagram.com" target="_blank" rel="noopener noreferrer" aria-label="Instagram">
            <FaInstagram />
          </a>
          <a href="https://twitter.com" target="_blank" rel="noopener noreferrer" aria-label="Twitter">
            <FaTwitter />
          </a>
          <a href="https://linkedin.com" target="_blank" rel="noopener noreferrer" aria-label="LinkedIn">
            <FaLinkedin />
          </a>
        </div>
      </div>

      {/* Bottom Bar */}
      <div className="footer-bottom">
        Copyright © 2025 <b>ShopMe</b>. All rights reserved. Authored by - <b>Rohit Parida</b>
      </div>
    </footer>
  );
};

export default Footer;
