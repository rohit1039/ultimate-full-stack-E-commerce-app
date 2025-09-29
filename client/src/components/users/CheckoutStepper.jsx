import React from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faShoppingCart, faMapMarkerAlt, faCreditCard, faTruck, faCheckCircle } from "@fortawesome/free-solid-svg-icons";
import "./CheckoutStepper.css";

const steps = [
    { id: "cart", label: "Cart", icon: faShoppingCart },
    { id: "address", label: "Address", icon: faMapMarkerAlt },
    { id: "payment", label: "Payment", icon: faCreditCard },
    { id: "shipping", label: "Shipping", icon: faTruck },
    { id: "confirm", label: "Confirm", icon: faCheckCircle },
];

const CheckoutStepper = ({ currentStep }) => {
    return (
        <div className="checkout-stepper">
            {steps.map((step, index) => {
                const stepIndex = steps.findIndex(s => s.id === currentStep);
                let status = "disabled";
                if (index < stepIndex) status = "completed";
                else if (index === stepIndex) status = "active";

                return (
                    <React.Fragment key={step.id}>
                        <div className={`step ${status}`}>
                            <div className="step-content">
                                <FontAwesomeIcon icon={step.icon} className="step-icon" />
                                <span className="step-label">{step.label}</span>
                            </div>
                        </div>
                        {index < steps.length - 1 && <div className="step-line"></div>}
                    </React.Fragment>
                );
            })}
        </div>
    );
};

export default CheckoutStepper;
