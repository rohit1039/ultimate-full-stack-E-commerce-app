import { useLocation, useNavigate } from "react-router-dom";
import AddressDetails from "./AddressDetails";
import CheckoutStepper from "./CheckoutStepper";
import "./CheckoutAddress.css";

const CheckoutAddressPage = ({ finalPrice, address: defaultAddress }) => {
  const location = useLocation();
  const navigate = useNavigate();
  const { cartItems, totalPrice } = location.state || { cartItems: [], totalPrice: 0 };

  const handleAddressSubmit = (addressData) => {

    navigate("/checkout/payment", { state: { cartItems, totalPrice, addressData } });
  };

  return (
    <div className="checkout-address-container">
      <CheckoutStepper currentStep="address" />
      <AddressDetails cartItems={cartItems} address={location.state.address} onSubmit={handleAddressSubmit} />
    </div>
  );
};

export default CheckoutAddressPage;
