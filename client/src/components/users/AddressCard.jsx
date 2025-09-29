import React from "react";
import "./AddressCard.css";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faEdit, faHomeAlt, faPhone, faTrashAlt } from "@fortawesome/free-solid-svg-icons";
import { faCheckCircle } from "@fortawesome/free-solid-svg-icons";
import DeleteModal from "../common/modal/DeleteModal";

const AddressCard = ({ address, onEdit, onDelete, onSetDefault }) => {

  const truncate = (text, maxLength) => {
    return text.length > maxLength ? text.slice(0, maxLength) + "..." : text;
  };

  return (
    <div className={`address-card ${address.isDefault ? "default" : ""}`}>
      <div className="name-default-btn-inliner">
        <p className="text">
          <FontAwesomeIcon className="home-icon" icon={faHomeAlt} />
          <strong>{address.name}</strong>
          {address.isDefault && <p className="default-tag">DEFAULT</p>}
        </p>
        <div className="address-menu">
          <button className="menu-trigger">•••</button>
          <div className="menu-options">
            {!address.isDefault && (
              <p className="menu-option" onClick={onSetDefault}>Set as Default</p>
            )}
            <p className="menu-option" onClick={onEdit}>Edit</p>
            <DeleteModal
              itemId={address.id}
              onDelete={onDelete}
              trigger={
                <p className="menu-option" onClick={onDelete}>Delete</p>
              }
            />
          </div>
        </div>

      </div>
      <div className="address-details-extras">
        <p className="text">{address.street}</p>
        <p className="text">
          {truncate(`${address.city}, ${address.state} - ${address.pincode}`, 32)}
        </p>
        <p className="text">{address.country}</p>
      </div>
      <div className="name-default-btn-inliner">
        <p className="text"><FontAwesomeIcon className="phone-icon" icon={faPhone} />{address.phone}</p>
      </div>
    </div>
  );
};

export default AddressCard;
