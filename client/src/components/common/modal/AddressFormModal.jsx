import React, { useState, useEffect } from "react";
import "./AddressFormModal.css";
import LocationPicker from "../LocationPicker";

const AddressFormModal = ({ onClose, onSave, existing, currentLocation }) => {
  const [form, setForm] = useState({
    id: null,
    name: "",
    phone: "",
    pincode: "",
    street: "",
    city: "",
    state: "",
    country: "India",
    tag: "Home",
    isDefault: false,
  });

  // Fill form if editing existing address
  useEffect(() => {
    if (existing) setForm(existing);
  }, [existing]);

  // Reverse geocode currentLocation
  useEffect(() => {
    if (currentLocation && !existing) {
      const { lat, lng } = currentLocation;

      fetch(`https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=${lat}&lon=${lng}`)
        .then((res) => res.json())
        .then((data) => {
          const address = data.address || {};
          setForm((prev) => ({
            ...prev,
            city: address.city || address.town || address.village || "",
            state: address.state || "",
            pincode: address.postcode || "",
            street: address.road || "",
            country: address.country || "India",
          }));
        })
        .catch((err) => console.error("Reverse geocoding failed:", err));
    }
  }, [currentLocation, existing]);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = () => {
    onSave(form);
    onClose();
  };

  return (
    <div className="addr-modal-overlay">
      <div className="addr-modal">
        <h3>{existing ? "Edit Address" : "Add New Address"}</h3>
        <div className="map-form-inliner">
          <LocationPicker currentLocation={currentLocation} />
          <div className="form-fields">
            <input name="name" placeholder="Full Name" value={form.name} onChange={handleChange} />
            <input name="phone" placeholder="Phone Number" value={form.phone} onChange={handleChange} />
            <input name="pincode" placeholder="Pincode" value={form.pincode} onChange={handleChange} />
            <input name="street" placeholder="Street / Building / House No." value={form.street} onChange={handleChange} />
            <input name="city" placeholder="City" value={form.city} onChange={handleChange} />
            <input name="state" placeholder="State" value={form.state} onChange={handleChange} />
            <input name="country" placeholder="Country" value={form.country} onChange={handleChange} />

            <select name="tag" value={form.tag} onChange={handleChange}>
              <option value="Home">Home</option>
              <option value="Work">Work</option>
              <option value="Other">Other</option>
            </select>

            <div className="actions">
              <button onClick={handleSubmit}>Save</button>
              <button className="cancel" onClick={onClose}>Cancel</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AddressFormModal;
