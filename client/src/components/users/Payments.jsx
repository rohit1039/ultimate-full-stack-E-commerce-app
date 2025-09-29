import React, { useState } from "react";
import "./Payments.css";
import CheckoutStepper from "./CheckoutStepper";

const Payments = ({ onClose }) => {
    const [method, setMethod] = useState("card");

    const handlePay = () => {
        alert(`Proceeding with ${method.toUpperCase()} payment`);
    };

    return (
        <div className="payments-container">
            <CheckoutStepper currentStep="payment" />
            <div className="payments-box">
                {/* Header */}
                <div className="payments-header">
                    <h4>Secure Checkout</h4>
                    <p>Choose your preferred payment method</p>
                </div>

                {/* Payment Method Tabs */}
                <div className="method-tabs">
                    <button className={method === "card" ? "active" : ""} onClick={() => setMethod("card")}>Card</button>
                    <button className={method === "upi" ? "active" : ""} onClick={() => setMethod("upi")}>UPI</button>
                    <button className={method === "netbanking" ? "active" : ""} onClick={() => setMethod("netbanking")}>Net Banking</button>
                    <button className={method === "wallet" ? "active" : ""} onClick={() => setMethod("wallet")}>Wallet</button>
                </div>

                <div className="payments-grid">
                    <div className="card-preview">
                        <div className="card-inner">
                            <div className="card-top">
                                <h4>Premium Card</h4>
                                <span className="card-chip"></span>
                            </div>
                            <p className="card-number">**** **** **** 1234</p>
                            <div className="card-footer">
                                <div>
                                    <p className="label">Card Holder</p>
                                    <p className="value">John Doe</p>
                                </div>
                                <div>
                                    <p className="label">Expiry</p>
                                    <p className="value">12/26</p>
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* Right: Payment Form */}
                    <div className="payment-form">
                        {method === "card" && (
                            <form autoComplete="off">
                                <label>Card Holder</label>
                                <input type="text" placeholder="John Doe" />

                                <label>Card Number</label>
                                <input type="text" placeholder="1234 5678 9012 3456" />

                                <div className="form-row">
                                    <div>
                                        <label>Expiry Date</label>
                                        <input type="text" placeholder="MM/YY" />
                                    </div>
                                    <div>
                                        <label>CVV</label>
                                        <input type="password" placeholder="***" />
                                    </div>
                                </div>
                            </form>
                        )}

                        {method === "upi" && (
                            <form autoComplete="off">
                                <label>UPI ID</label>
                                <input type="text" placeholder="username@upi" />
                            </form>
                        )}

                        {method === "netbanking" && (
                            <form autoComplete="off">
                                <label>Select Bank</label>
                                <select>
                                    <option>HDFC Bank</option>
                                    <option>ICICI Bank</option>
                                    <option>SBI</option>
                                    <option>Axis Bank</option>
                                </select>
                            </form>
                        )}

                        {method === "wallet" && (
                            <form autoComplete="off">
                                <label>Select Wallet</label>
                                <select>
                                    <option>Paytm</option>
                                    <option>PhonePe</option>
                                    <option>Mobikwik</option>
                                </select>
                            </form>
                        )}

                        <button type="button" className="pay-btn" onClick={handlePay}>
                            Pay Now
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Payments;
