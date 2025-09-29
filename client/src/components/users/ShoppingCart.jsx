import React, { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './ShoppingCart.css';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faHeart as faRegularHeart } from '@fortawesome/free-regular-svg-icons';
import { faHeart as faSolidHeart } from '@fortawesome/free-solid-svg-icons';
import { SectionWrapper } from '../wrapper/SectionWrapper';
import { toast } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import CheckoutStepper from './CheckoutStepper';
import { faTrashAlt } from '@fortawesome/free-regular-svg-icons';
import DeleteModal from '../common/modal/DeleteModal';
import Products from './Products';
import RatingsAndReviews from './RatingsAndReviews';
import OrderSummary from '../common/OrderSummary';

const ShoppingCart = ({ cartItems: initialCartItems = [] }) => {
  const [cartItems, setCartItems] = useState(initialCartItems);
  const [selectedItems, setSelectedItems] = useState(
    initialCartItems.map((_, i) => i)
  );
  const [savedItems, setSavedItems] = useState([]);
  const [discount, setDiscount] = useState(0);

  const [addresses, setAddresses] = useState([
    {
      id: 1,
      name: "John Doe",
      street: "123 Main St",
      city: "Bangalore",
      state: "KA",
      zip: "560001",
      phone: "9876543210",
      type: "primary",
      isDefault: false,
    },
    {
      id: 2,
      name: "Rohit Sharma",
      street: "45 Residency Rd",
      city: "Mumbai",
      state: "MH",
      zip: "400001",
      phone: "9123456789",
      type: "secondary",
      isDefault: false,
    },
    {
      id: 3,
      name: "Rohit Parida",
      street: "Mayurbhanj, Ward no. 14",
      city: "Karanjia",
      state: "OD",
      zip: "757037",
      phone: "7978251158",
      type: "default",
      isDefault: true,
    },
  ]);


  const navigate = useNavigate();
  const defaultAddress = addresses.find((addr) => addr.isDefault);

  const handleIncrease = (index) => {
    const newCart = [...cartItems];
    newCart[index].quantity += 1;
    setCartItems(newCart);
  };

  const handleDecrease = (index) => {
    const newCart = [...cartItems];
    if (newCart[index].quantity > 1) {
      newCart[index].quantity -= 1;
    } else {
      newCart.splice(index, 1);
    }
    setCartItems(newCart);
  };

  const handleDelete = (index) => {
    const newCart = [...cartItems];
    newCart.splice(index, 1);
    setCartItems(newCart);
  };

  const handleSaveForLater = (index) => {
    const newCart = [...cartItems];
    const [saved] = newCart.splice(index, 1);
    setCartItems(newCart);
    setSavedItems([...savedItems, saved]);
  };

  const handleDeleteAddress = (id) => {
    if (addresses.length === 1) {
      toast.error("❌ Cannot delete default address");
      return;
    }
    const address = addresses.find((a) => a.id === id);
    if (address.isDefault) {
      toast.error("❌ Cannot delete default address. Set another one first.");
      return;
    }
    setAddresses(addresses.filter((a) => a.id !== id));
    toast.success("🗑️ Address deleted");
  };

  const handleEditAddress = (id) => {
    const address = addresses.find((a) => a.id === id);
    navigate("/checkout/address", { state: { address } });
  };

  const [selectedAddressId, setSelectedAddressId] = useState(
    addresses.find(addr => addr.isDefault)?.id || null
  );

  const handleSelectAddress = (id) => {
    setSelectedAddressId(id);
  };

  const totalPrice = cartItems.reduce(
    (total, item) => total + item.total_price * item.quantity,
    0
  );

  const parentCheckboxRef = useRef(null);

  useEffect(() => {
    if (!parentCheckboxRef.current) return;

    if (selectedItems.length === 0) {
      parentCheckboxRef.current.indeterminate = false;
      parentCheckboxRef.current.checked = false;
    } else if (selectedItems.length === cartItems.length) {
      parentCheckboxRef.current.indeterminate = false;
      parentCheckboxRef.current.checked = true;
    } else {
      parentCheckboxRef.current.indeterminate = true;
    }
  }, [selectedItems, cartItems]);

  const handleItemToggle = (index) => {
    setSelectedItems((prev) =>
      prev.includes(index)
        ? prev.filter((i) => i !== index)
        : [...prev, index]
    );
  };

  const handleParentToggle = () => {
    if (selectedItems.length === cartItems.length) {
      setSelectedItems([]);
    } else {
      setSelectedItems(cartItems.map((_, i) => i));
    }
  };

  return (
    <>
      <div className="cart-page-container">
        {cartItems && cartItems.length > 0 && (
          <>
            <CheckoutStepper currentStep="cart" />
            <marquee>
              <p style={{ padding: "0.75rem", fontSize: "13px" }}>
                Use code <strong>SHOP10</strong> to get instant 10% OFF on your first purchase.
                Hurry, limited time offer! Add your favorite products to the cart now.
                Exclusive deal available. Don't miss out on big savings.
              </p>
            </marquee>
          </>
        )}
        <SectionWrapper heading="Review Cart" name="h3" />
        <div className="cart-page-container">
          {cartItems.length === 0 ? (
            <div className="empty-cart-wrapper">
              <p className="empty-cart">Your cart is empty</p>
              <button className="shop-now-btn" onClick={() => navigate("/products")}>
                Shop Now
              </button>
            </div>
          ) : (
            <>
              <div className="cart-summary-wrapper">
                <div className='wishlist-cart-col'>
                  <div className="deliver-to-wrapper">
                    {defaultAddress ? (
                      <>
                        <div className="deliver-to-address">
                          <div className='delivery-name-code'>
                            <p>Shipping to:</p>
                            <p>{defaultAddress.name}-{defaultAddress.zip}</p>
                          </div>
                          <div className='default-address-details'>
                            <p>{defaultAddress.street}, {defaultAddress.city}, {defaultAddress.state}</p>
                            <p>Phone: {defaultAddress.phone}</p>
                          </div>
                        </div>
                        <button className="change-address-btn">Change</button>
                      </>
                    ) : (
                      <div className="no-address">
                        <p>No address selected</p>
                        <button className="add-address-btn">Add Address</button>
                      </div>
                    )}
                  </div>
                  {/* <div className="checkbox-items-inliner">
                    <div className='checkbox-selected-items'>
                      <input
                        type="checkbox"
                        ref={parentCheckboxRef}
                        className='cart-item-checkbox'
                        onChange={handleParentToggle}
                      />
                      <p>
                        {selectedItems.length}/{cartItems.length} {cartItems.length > 1 ? "items" : "item"} selected
                      </p>
                    </div>
                    <div className='cart-parent-btn-group'>
                      <button className='parent-remove-btn'>Remove <FontAwesomeIcon icon={faTrashAlt} className="parent-delete-icon" /></button>
                      <button className="parent-wishlist-btn">Move to Wishlist
                        <span className="wishlist-icon-wrapper">
                          <FontAwesomeIcon
                            className="wishlist-icon regular"
                            icon={faRegularHeart}
                          />
                          <FontAwesomeIcon
                            className="wishlist-icon solid"
                            icon={faSolidHeart}
                          />
                        </span>
                      </button>
                    </div>
                  </div> */}
                  <div className={`cart-page ${cartItems.length > 1 ? "scroll-enabled" : ""}`}>
                    {cartItems.map((item, index) => (
                      <React.Fragment key={index}>
                        <div className="cart-item">
                          <input
                            type="checkbox"
                            checked={selectedItems.includes(index)}
                            onChange={() => handleItemToggle(index)}
                            className='cart-item-checkbox'
                          />
                          <img
                            src={item.image}
                            alt={item.name}
                            className="cart-item-image"
                          />
                          <div className="cart-item-details">
                            <div className="item-header">
                              <p className='item-name'>{item.name}</p>
                              <div className="item-actions">
                                <span className="wishlist-icon-wrapper-child">
                                  <FontAwesomeIcon
                                    className="wishlist-icon regular"
                                    icon={faRegularHeart}
                                  />
                                  <FontAwesomeIcon
                                    className="wishlist-icon solid"
                                    icon={faSolidHeart}
                                  />
                                </span>
                                <button
                                  className="save-btn"
                                  onClick={() => handleSaveForLater(index)}
                                >
                                  Save for later
                                </button>
                                <DeleteModal
                                  itemId={index}
                                  onDelete={handleDelete}
                                  message="Are you sure you want to remove this item?"
                                  trigger={
                                    <FontAwesomeIcon
                                      className="delete-icon"
                                      icon={faTrashAlt}
                                      title="Remove item"
                                    />
                                  }
                                />
                              </div>
                            </div>
                            <div className="price-discount-inline">
                              <p>Price: ₹{item.total_price * item.quantity}</p>
                              <p className="discount">({item.discount_percent}% OFF)</p>
                            </div>

                            <div className="price-discount-inline">
                              <p>Color: <span style={{ color: item.color }}>{item.color}</span></p>
                            </div>

                            <div className="quantity-controls">
                              <button onClick={() => handleDecrease(index)}>-</button>
                              <span>{item.quantity}</span>
                              <button onClick={() => handleIncrease(index)}>+</button>
                            </div>

                            <div className="item-stock-wishlist">
                              <div className="in-stock-cart">Only 3 left</div>
                              <div className="delivery-text">Delivery by {new Date(Date.now() + 5 * 24 * 60 * 60 * 1000).toDateString()}</div>
                            </div>
                          </div>
                        </div>
                        {cartItems.length > 1 && index < cartItems.length - 1 && (
                          <hr className="item-separator" />
                        )}
                      </React.Fragment>
                    ))}
                  </div>
                  <div className='wishlist-items-btn'>
                    <a className='wishlist-ref' href='/wishlist'>
                      <svg className='svg-icon' xmlns="http://www.w3.org/2000/svg" width="12" height="16" viewBox="0 0 12 16">
                        <path fill="#000" fill-rule="evenodd" d="M10.993 14.62a.067.067 0 0 1-.103.058l-4.571-2.77a.638.638 0 0 0-.64 0l-4.57 2.77a.067.067 0 0 1-.102-.058V1.133A.13.13 0 0 1 1.139 1H3.5V3.5c0 .298.18.543.486.543s.515-.245.515-.543V1h6.36a.13.13 0 0 1 .133.133V14.62zM11.307 0H.693A.687.687 0 0 0 0 .68v14.719A.61.61 0 0 0 .617 16a.63.63 0 0 0 .315-.086l4.996-3.026a.14.14 0 0 1 .144 0l4.996 3.026a.628.628 0 0 0 .315.086.61.61 0 0 0 .617-.602V.679C12 .306 11.69 0 11.307 0z"></path>
                      </svg> Add More From Wishlist
                      <svg className='cheveron-right-icon' xmlns="http://www.w3.org/2000/svg" width="7" height="12" viewBox="0 0 7 12">
                        <path fill-rule="evenodd" d="M6.797 5.529a.824.824 0 0 0-.042-.036L1.19.193a.724.724 0 0 0-.986 0 .643.643 0 0 0 0 .94L5.316 6 .203 10.868a.643.643 0 0 0 0 .938.724.724 0 0 0 .986 0l5.566-5.299a.644.644 0 0 0 .041-.978">
                        </path>
                      </svg>
                    </a>
                  </div>
                </div>
                {/* Order Summary Section */}
                <OrderSummary cartItems={cartItems} defaultAddress={defaultAddress} viewName={"cart"} />
              </div>
            </>
          )}
        </div>
      </div>
      <div style={{ margin: "0 2rem 0 2rem" }}>
        <SectionWrapper heading="Product Reviews" name="h3" />
        <RatingsAndReviews />
      </div>
      <div className="related-products">
        <SectionWrapper heading="Related Products" name="h3" />
        <Products />
      </div>
    </>
  );
};

export default ShoppingCart;
