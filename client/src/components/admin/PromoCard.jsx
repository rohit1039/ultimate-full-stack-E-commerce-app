import "./PromoCard.css";

const PromoCard = () => {
  return (
    <div className="promo-card">
      <div className="promo-content">
        <h2 className="promo-title">Limited Time Offer!</h2>
        <ul className="promo-features">
          <li>✓ 50% Off on New Items</li>
          <li>✓ Free Delivery Above $49</li>
          <li>✓ New T-shirt Arrivals!</li>
        </ul>
        <button className="promo-btn">Shop Now</button>
      </div>
    </div>
  );
};

export default PromoCard;
