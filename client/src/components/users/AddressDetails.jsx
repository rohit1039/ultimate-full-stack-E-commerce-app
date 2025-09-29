import { useEffect, useState } from "react";
import AddressCard from "./AddressCard";
import "./AddressDetails.css";
import AddressFormModal from "../common/modal/AddressFormModal";
import OrderSummary from "../common/OrderSummary";
import { SectionWrapper } from "../wrapper/SectionWrapper";
import { toast, ToastContainer } from "react-toastify";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faCirclePlus } from "@fortawesome/free-solid-svg-icons";
import 'leaflet/dist/leaflet.css';

const CheckoutAddress = ({ cartItems, onSelectAddress }) => {
  const [currentLocation, setCurrentLocation] = useState({ lat: 20.5937, lng: 78.9629 });
  const [addresses, setAddresses] = useState([
    {
      id: 1,
      name: "John Doe",
      phone: "9876543210",
      pincode: "400001",
      street: "123 Park Street",
      city: "Mumbai",
      state: "Maharashtra",
      country: "India",
      tag: "Home",
      isDefault: true,
    },
    {
      id: 2,
      name: "Jane Doe",
      phone: "9123456789",
      pincode: "560001",
      street: "45 MG Road",
      city: "Bengaluru",
      state: "Karnataka",
      country: "India",
      tag: "Work",
      isDefault: false,
    },
  ]);

  const [selected, setSelected] = useState(
    addresses.find(addr => addr.isDefault)?.id || null
  );
  const [showModal, setShowModal] = useState(false);
  const [editAddress, setEditAddress] = useState(null);

  // CRUD operations
  const handleAddAddress = (newAddress) => {
    if (addresses.length >= 3) {
      toast.error("You can only save up to 3 addresses");
      return;
    }
    const added = { ...newAddress, id: Date.now() };
    setAddresses([...addresses, added]);
    setSelected(added.id);
  };

  const handleUpdateAddress = (updated) => {
    setAddresses(addresses.map(addr => addr.id === updated.id ? updated : addr));
  };

  const handleDeleteAddress = (id) => {
    setAddresses(addresses.filter(addr => addr.id !== id));
    if (selected === id) setSelected(null);
  };

  const handleSetDefault = (id) => {
    setAddresses(
      addresses.map(addr => ({
        ...addr,
        isDefault: addr.id === id
      }))
    );
  };

  const handleContinue = () => {
    const selectedAddress = addresses.find(addr => addr.id === selected);
    if (!selectedAddress) {
      alert("Please select a delivery address");
      return;
    }
    onSelectAddress(selectedAddress);
  };

  const address = addresses.find(addr => addr.isDefault );

  useEffect(() => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (pos) => {
          setCurrentLocation({
            lat: pos.coords.latitude,
            lng: pos.coords.longitude
          });
        },
        (err) => {
          console.error("Error getting location:", err);
        }
      );
    }
  }, []);

  return (
    <>
      <SectionWrapper heading="Checkout" name="h3" />
      <div className="checkout-address">
        <div className="order-checkout-summary">
          <OrderSummary pincode={address.pincode} cartItems={cartItems} />
        </div>
        <div className="address-grid-wrapper">
          <h5>{addresses.length >= 1 ? "Select" : "Add"} Shipping Address</h5>
          <div className={`address-grid ${addresses.length === 1 ? "one" : addresses.length === 2 ? "two" : "three"}`}>
            {addresses.length >= 1 ?
              addresses.map(addr => (
                <div
                  key={addr.id}
                  className={`address-option ${addr.isDefault ? "selected" : ""}`}
                  onClick={() => setSelected(addr.id)}
                >
                  <AddressCard
                    address={addr}
                    onEdit={() => { setEditAddress(addr); setShowModal(true); }}
                    onDelete={() => handleDeleteAddress(addr.id)}
                    onSetDefault={() => handleSetDefault(addr.id)}
                  />
                </div>
              ))
              :
              <div className="empty-address">
                <p>No saved addresses found. Please add one to continue.</p>
              </div>
            }
            <div
              className={`address-actions ${addresses.length === 1
                ? "col-2"
                : "col-row"
                }`}
            >
              <button
                className="add-btn"
                onClick={() => { setEditAddress(null); setShowModal(true); }}
              >
                <FontAwesomeIcon className="plus-icon" icon={faCirclePlus} />Add {addresses.length >= 1 ? "New" : ""} Address
              </button>
              <button className="shipping-btn" onClick={handleContinue}>Use Default Address</button>
            </div>
          </div>
          {showModal && (
            <AddressFormModal
              onClose={() => setShowModal(false)}
              onSave={editAddress ? handleUpdateAddress : handleAddAddress}
              existing={editAddress}
              currentLocation={currentLocation}
            />
          )}
          <ToastContainer position="top-right" closeButton={false} />
        </div>
      </div>
    </>
  );
};

export default CheckoutAddress;
