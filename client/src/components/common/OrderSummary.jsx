import { useState } from "react";
import { FaMapMarkerAlt } from "react-icons/fa";
import { useNavigate } from "react-router-dom";
import "./OrderSummary.css";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faChevronDown, faChevronUp } from "@fortawesome/free-solid-svg-icons";

const OrderSummary = ({ pincode, cartItems, defaultAddress, viewName }) => {

    const [coupon, setCoupon] = useState('');
    const [discount, setDiscount] = useState(0);
    const [message, setMessage] = useState("");
    const [messageType, setMessageType] = useState("");

    const navigate = useNavigate();
    const totalPrice = cartItems.reduce((total, item) => total + item.total_price * item.quantity, 0);
    const finalPrice = totalPrice - totalPrice * discount;

    const handleProceed = () => {
        navigate('/checkout/address', { state: { cartItems, finalPrice, address: defaultAddress } });
    };

    // --- COUPON HANDLING ---
    const availableCoupons = [
        { code: "SHOP10", desc: "Get 10% OFF on first purchase" },
        { code: "FREESHIP", desc: "Free Delivery on orders above ₹ 500" },
        { code: "SAVE50", desc: "Flat ₹ 50 OFF on orders above ₹ 999" },
    ];

    const handleSelectCoupon = (code) => {
        setCoupon(code);
    };

    const handleApplyCoupon = () => {
        if (coupon.toLowerCase() === "shop10") {
            setDiscount(0.1);
            setMessage("Coupon applied successfully ✅");
            setMessageType("success");
        } else if (coupon.toLowerCase() === "freeship") {
            setDiscount(0);
            setMessage("Free Shipping coupon applied ✅");
            setMessageType("success");
        } else if (coupon.toLowerCase() === "save50") {
            setDiscount(0.5);
            setMessage("Flat ₹50 OFF coupon applied ✅");
            setMessageType("success");
        } else {
            setDiscount(0);
            setMessage("Invalid coupon code ❌");
            setMessageType("error");
        }
    };

    return (
        <div className={viewName === "cart" ? "cart-total" : "cart-address-total"}>
            {/* Coupon Section */}
            {viewName === "cart" && (
                <div className="coupon-section">
                    <h5>Coupons</h5>
                    <div className="icon-txt-btn-wrapper">
                        <div className="icon-text-inline">
                            <svg className="coupon-icon" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 18 18">
                                <g fill="none" fill-rule="evenodd" transform="rotate(45 6.086 5.293)">
                                    <path stroke="#000" d="M17.5 10V1a1 1 0 0 0-1-1H5.495a1 1 0 0 0-.737.323l-4.136 4.5a1 1 0 0 0 0 1.354l4.136 4.5a1 1 0 0 0 .737.323H16.5a1 1 0 0 0 1-1z"></path>
                                    <circle cx="5.35" cy="5.35" r="1.35" fill="#000" fill-rule="nonzero"></circle>
                                </g>
                            </svg>
                            <p className="apply-coupon-txt">Apply Coupon</p>
                        </div>
                        <button className="apply-coupon-btn">Browse Coupons</button>
                    </div>
                </div>
            )}
            <h5 className="order-summary-title">Order Summary</h5>

            {viewName !== "cart" &&
                <div className="cart-total-row">
                    <span className="cart-total-label">Total Items</span>
                    <span className="cart-total-value">{cartItems.length}</span>
                </div>
            }

            <div className="cart-total-row">
                <span className="cart-total-label">Subtotal</span>
                <span className="cart-total-value">₹{totalPrice.toFixed(2)}</span>
            </div>

            {discount > 0 && (
                <div className="cart-total-row">
                    <span className="cart-total-label">
                        Discount ({(discount * 100).toFixed(0)}%)
                    </span>
                    <span className="cart-total-value">
                        - ₹{(totalPrice * discount).toFixed(2)}
                    </span>
                </div>
            )}

            <div className="cart-total-row">
                <span className="cart-total-label">Tax (5% GST)</span>
                <span className="cart-total-value">
                    ₹{(totalPrice * 0.05).toFixed(2)}
                </span>
            </div>

            <div className="cart-total-row">
                <span className="cart-total-label">Delivery Fee</span>
                <span className="cart-total-value">
                    {finalPrice > 1000 ? "Free" : "₹49.00"}
                </span>
            </div>

            <div className="cart-total-row">
                <span className="cart-total-label">Estimated Delivery</span>
                <span className="cart-total-value">3–5 business days</span>
            </div>

            {/* Highlight savings */}
            <div className="cart-savings">
                <div className="delivery-date">
                    <p>
                        Expected by{" "}
                        <strong>
                            {new Date(Date.now() + 5 * 24 * 60 * 60 * 1000).toDateString()}
                        </strong>
                    </p>
                </div>
                <div className="delivery-date">
                    <p>
                        You saved{" "}
                        <strong>
                            ₹
                            {(
                                totalPrice * discount +
                                (finalPrice > 1000 ? 49 : 0)
                            ).toFixed(2)}
                        </strong>
                    </p>
                </div>
            </div>

            {/* Final Price */}
            <div className="cart-total-row final">
                <span className="cart-total-label">Total Price</span>
                <span className="cart-total-value">
                    ₹
                    {(
                        finalPrice +
                        totalPrice * 0.05 +
                        (finalPrice > 1000 ? 0 : 49)
                    ).toFixed(2)}
                </span>
            </div>

            <p className="delivery-location-cart">
                <FaMapMarkerAlt
                    color="#e53935"
                    style={{ marginRight: "6px" }}
                />
                Shipping to <b>{pincode}</b> by default{" "}
                {viewName === "cart" &&
                    <a href="#" className="change-pin">
                        Click to Change
                    </a>
                }
            </p>

            {viewName === "" && (
                <div className="available-offers">
                    <div className="offers-title">
                        <svg className="offers-icon" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 20 20">
                            <g fill="#000" fill-rule="evenodd">
                                <path d="M15.292 10.687v.001c-.198.742.076 1.454.296 2.026l.045.12-.137.021c-.602.094-1.352.211-1.892.75-.538.54-.655 1.288-.748 1.89l-.022.138a22.096 22.096 0 0 1-.12-.045c-.443-.171-.946-.364-1.49-.364-.185 0-.366.023-.536.068-.728.194-1.198.78-1.577 1.249-.032.04-.07.088-.111.137l-.112-.138c-.378-.47-.848-1.054-1.575-1.248a2.092 2.092 0 0 0-.537-.068c-.543 0-1.046.193-1.49.364l-.12.045-.022-.138c-.093-.602-.21-1.35-.749-1.89-.539-.539-1.289-.656-1.891-.75l-.137-.022a15 15 0 0 1 .045-.118c.22-.573.494-1.286.296-2.027-.194-.728-.78-1.199-1.25-1.577L1.323 9l.137-.11c.47-.38 1.055-.85 1.249-1.577.198-.742-.076-1.455-.296-2.028l-.045-.118.137-.022c.602-.094 1.352-.211 1.891-.75.54-.539.656-1.289.75-1.891l.022-.137.119.045c.443.171.947.365 1.49.365.186 0 .367-.024.537-.07.727-.193 1.198-.778 1.576-1.248L9 1.322l.111.137c.379.47.85 1.055 1.576 1.249.17.045.352.069.537.069.544 0 1.047-.194 1.491-.365l.119-.045.022.137c.094.602.21 1.353.75 1.891.538.539 1.288.656 1.89.75l.138.022-.046.119c-.22.572-.494 1.285-.295 2.026.194.728.778 1.199 1.248 1.577.04.033.088.07.137.111l-.137.11c-.47.38-1.054.85-1.249 1.577M18 9c0-.744-1.459-1.286-1.642-1.972-.19-.71.797-1.907.437-2.529-.364-.63-1.898-.372-2.41-.884-.511-.511-.253-2.045-.883-2.41a.647.647 0 0 0-.33-.08c-.585 0-1.403.542-1.998.542a.778.778 0 0 1-.201-.025C10.286 1.46 9.743 0 9 0c-.744 0-1.286 1.459-1.972 1.642a.78.78 0 0 1-.2.025c-.596 0-1.414-.542-2-.542a.647.647 0 0 0-.33.08c-.63.365-.37 1.898-.883 2.41-.512.512-2.046.254-2.41.884-.36.62.627 1.819.437 2.529C1.46 7.714 0 8.256 0 9s1.459 1.286 1.642 1.972c.19.71-.797 1.908-.437 2.53.364.63 1.898.371 2.41.883.511.512.253 2.045.884 2.41.097.056.208.08.33.08.585 0 1.403-.542 1.998-.542a.78.78 0 0 1 .201.025C7.714 16.54 8.256 18 9 18s1.286-1.46 1.973-1.642a.774.774 0 0 1 .2-.025c.595 0 1.413.542 1.998.542a.647.647 0 0 0 .33-.08c.631-.365.373-1.898.884-2.41.512-.512 2.046-.254 2.41-.884.36-.62-.627-1.819-.437-2.529C16.54 10.286 18 9.744 18 9">
                                </path>
                                <path d="M10.897 6.34l-4.553 4.562a.536.536 0 0 0 .76.758l4.552-4.562a.536.536 0 0 0-.76-.758M6.75 7.875a1.126 1.126 0 0 0 0-2.25 1.126 1.126 0 0 0 0 2.25M11.25 10.125a1.126 1.126 0 0 0 0 2.25 1.126 1.126 0 0 0 0-2.25"></path>
                            </g>
                        </svg>
                        <h5>Available Offers</h5>
                    </div>
                    <ul className="offer-list">
                        <li>
                            10% Instant Discount on IDFC Credit Card on a min spend of ₹850.
                        </li>
                        <li>
                            10% Instant Discount on HDFC Credit Card EMI on a min spend of ₹5,000.
                        </li>
                        <li>
                            10% Instant Discount on HDFC Credit Card on a min spend of ₹850.
                        </li>
                        <li>
                            7.5% Assured Cashback on a minimum spend of ₹100 with ShopMe Axis Credit Card.
                        </li>
                        <li>
                            10% Instant Discount on ICICI Bank Credit Card on a min spend of ₹5,000.
                        </li>
                    </ul>
                </div>
            )}

            {/* Checkout Button */}
            {viewName === "cart" &&
                <button
                    className="proceed-btn"
                    onClick={handleProceed}
                    disabled={cartItems.length === 0}
                >
                    Checkout Now
                </button>
            }
        </div>
    )
}

export default OrderSummary;