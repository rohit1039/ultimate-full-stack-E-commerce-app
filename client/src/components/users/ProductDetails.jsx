import { useEffect, useState } from "react";
import "./ProductDetails.css";
import Products from "./Products";
import { SectionWrapper } from "../wrapper/SectionWrapper";
import RatingsAndReviews, { RatingStars } from "./RatingsAndReviews";
import { FaBolt, FaMapMarkerAlt, FaShoppingCart } from "react-icons/fa";
import { formatSlug } from "../common/BreadCrumb";
import { toast, ToastContainer } from "react-toastify";
import Shipping from "../../assets/services/shipped.png";
import { ReviewsSummary } from "./RatingsSection";
import WriteReview from "./WriteReview";
import CouponWheelModal from "./CouponWheelModal";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faCartFlatbed, faCartShopping } from "@fortawesome/free-solid-svg-icons";

export const ProductDetails = ({ cartItems, setCartItems, products }) => {
  const productName = "REGULAR FIT PLAIN SHIRT BENEDICT - LIGHT BLUE";

  const product = products.find(
    (p) => p.product_name === formatSlug(productName)
  );


  const [showReviewModal, setShowReviewModal] = useState(false);
  const [selectedImage, setSelectedImage] = useState(product.product_images[0]);
  const [selectedColor, setSelectedColor] = useState(product.product_color[0]);
  const [selectedSize, setSelectedSize] = useState(product.product_sizes[0]);
  const [quantity, setQuantity] = useState(1);
  const [pincode, setPincode] = useState("");
  const [loading, setLoading] = useState(false);
  const [showWheel, setShowWheel] = useState(false);

  const isDelivered = false;

  const product_desc = product.long_desc.split(". ");

  const selectedVariant = product.variants.find(
    (v) => v.color === selectedColor && v.size === selectedSize
  );

  const incrementQuantity = () =>
    setQuantity((prev) =>
      selectedVariant && prev < selectedVariant.stock ? prev + 1 : prev
    );

  const decrementQuantity = () =>
    setQuantity((prev) => (prev > 1 ? prev - 1 : 1));

  const checkAvailability = async () => {
    if (!pincode || pincode.length !== 6) {
      toast.error("Please enter a valid 6-digit pincode");
      return;
    }
    setLoading(true);

    setTimeout(() => {
      const serviceablePincodes = [
        "110001",
        "560001",
        "400001",
        "757037",
        "751030",
      ];
      if (serviceablePincodes.includes(pincode)) {
        toast.success("Delivery available to your location");
      } else {
        toast.error("Sorry, we do not ship to this location");
      }
      setLoading(false);
    }, 1000);
  };

  const addToCart = () => {
    if (!selectedVariant || selectedVariant.stock === 0) {
      toast.error("Selected variant is out of stock!");
      return;
    }

    const existingItemIndex = cartItems.findIndex(
      (item) =>
        item._id === product._id &&
        item.color === selectedColor &&
        item.size === selectedSize
    );

    let updatedCart = [...cartItems];

    if (existingItemIndex !== -1) {
      updatedCart[existingItemIndex].quantity += quantity;
    } else {
      updatedCart.push({
        _id: product._id,
        name: product.product_name,
        brand: product.product_brand,
        total_price: product.total_price,
        discount_price: product.product_price,
        discount_percent: product.discount_percent,
        color: selectedColor,
        size: selectedSize,
        quantity: quantity,
        image: selectedImage,
      });
    }

    setCartItems(updatedCart);
    toast.success('Item added to cart');
  };

  useEffect(() => {
    if (isDelivered) setShowReviewModal(true);
  }, [isDelivered]);

  // Show wheel modal after 5s
  useEffect(() => {
    const timer = setTimeout(() => setShowWheel(true), 5000);
    return () => clearTimeout(timer);
  }, []);

  return (
    <>
      <div className="product-container">
        {/* Left Side - Images */}
        <div className="image-section">
          <div>
            <img src={selectedImage} className="main-product-image" />
          </div>
          <div className="thumbnail-container-vertical">
            {product.product_images.map((img, index) => (
              <img
                key={index}
                src={img}
                alt={`Thumbnail ${index}`}
                className={`thumbnail ${selectedImage === img ? "active-thumb" : ""}`}
                onClick={() => setSelectedImage(img)}
              />
            ))}
          </div>
        </div>

        {/* Right Side - Details */}
        <div className="details-section">
          <p className="brand">{product.product_brand}</p>
          <h4>{product.product_name}</h4>
          <p className="short-description">{product.short_desc}</p>

          {/* Ratings */}
          <div className="rating">
            <RatingStars rating={4.3} /> <span className="reviews-text">(11 reviews)</span><span className="line-text">|</span><p className="view-reviews-text">Click for Reviews </p>
          </div>

          {/* Price & Discount */}
          <div className="price-section">
            <span className="discount-price">
              <sup className="price-after-discount">₹</sup>
              {product.total_price}
            </span>
            <span className="original-price">₹{product.product_price}</span>
            <span className="discount">({product.discount_percent}% OFF)</span>
          </div>

          <p className="tax-pill">ShopMe's Choice</p>

          {/* Description */}
          <p className="product-desc-heading">
            Product Description{" "}
          </p>
          <ul className="description">
            {product_desc.map((desc, index) => (
              <li key={index}>{desc}</li>
            ))}
          </ul>

          {/* Color Selector */}
          <div className="color-selector">
            <p>Select Color:</p>
            <div className="color-options">
              {product.product_color.map((color, index) => (
                <span
                  key={index}
                  className={`color-circle ${selectedColor === color ? "selected" : ""}`}
                  style={{ backgroundColor: color }}
                  onClick={() => setSelectedColor(color)}
                ></span>
              ))}
            </div>
          </div>

          <div className="quantity-selector">
            <p>Quantity:</p>
            <div className="product-quantity-controls">
              <button onClick={decrementQuantity}>-</button>
              <span>{quantity}</span>
              <button onClick={incrementQuantity}>+</button>
            </div>
          </div>

          {/* Size Selector */}
          <div className="size-selector">
            <p>Select Size:</p>
            <div className="size-options">
              {product.product_sizes.map((size, index) => {
                const variant = product.variants.find(
                  (v) => v.color === selectedColor && v.size === size
                );
                return (
                  <button
                    key={index}
                    className={`size-btn ${selectedSize === size ? "selected" : ""}`}
                    disabled={variant?.stock === 0}
                    onClick={() => setSelectedSize(size)}
                  >
                    {size} {variant?.stock === 0 ? "(Out of stock)" : ""}
                  </button>
                );
              })}
            </div>
          </div>
        </div>
        <div className="product-highlights">
          <p className="product-desc-heading">Key Highlights</p>
          <ul className="highlights-list">
            <li>Premium quality materials for long-lasting</li>
            <li>Available in multiple colors & sizes to suit you</li>
            <li>Lightweight & comfortable for everyday use</li>
            <li>Lightweight & comfortable for everyday use</li>
            <li>Lightweight & comfortable for everyday use</li>
          </ul>
          <div className="button-section">
            <button
              className="add-to-cart"
              disabled={!selectedVariant || selectedVariant.stock === 0}
              onClick={addToCart}
            >
              Add to Cart
            </button>
            <button
              className="buy-now"
              disabled={!selectedVariant || selectedVariant.stock === 0}
            >
              <FaBolt className="buy-now-icon" /> Buy Now
            </button>
          </div>
          {/* Quantity & Pincode */}
          <div className="availablity-quantity-inliner">
            <div className="pincode-checker">
              <div className="check-shipment-wrapper">
                <span>Check Shipment</span>
                <img src={Shipping} className="check-shipment" alt="Shipment Icon" />
              </div>
              <div>
                <input
                  type="text"
                  placeholder="Enter pincode"
                  value={pincode}
                  onChange={(e) => setPincode(e.target.value)}
                />
                <button onClick={checkAvailability}>
                  {loading ? "Checking..." : "Check"}
                </button>
                <ToastContainer position="top-right" closeButton={false} />
              </div>
            </div>
          </div>
          <div className="delivery-estimate">
            <p className="product-desc-heading">Delivery Estimate</p>
            <p className="delivery-location">
              <FaMapMarkerAlt color="#e53935" style={{ marginRight: "6px" }} />
              Shipping to <b>757037</b> by default
              <a href="#" className="change-pin">Click to Change</a>
            </p>
            <p className="delivery-expected">Expected delivery by <b>10th Sep, 2025</b></p>
          </div>

          <div className="returns-refund-section">
            <p className="product-desc-heading">Returns and Refunds</p>
            <ol>
              <li>Easy 10-day return or replacement for damaged/defective products. </li>
              <li>Full refund processed to your original payment method within 5–7 business days.</li>
              <li>Items must be returned in unused condition with original packaging.</li>
            </ol>
          </div>
        </div>
      </div>

      <ReviewsSummary />
      {/* {showWheel && <CouponWheelModal onClose={() => setShowWheel(false)} />} */}
      <div style={{ margin: "0 0 0 2rem" }}>
        <SectionWrapper heading="Product Reviews" name="h3" />
        <RatingsAndReviews />
      </div>
      <WriteReview show={showReviewModal} onClose={() => setShowReviewModal(false)} />
      <div className="related-products">
        <SectionWrapper heading="Related Products" name="h3" />
        <Products />
      </div>
    </>
  );
};
