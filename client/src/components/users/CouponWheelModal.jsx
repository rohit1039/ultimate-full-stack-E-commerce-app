import React, { useState, useEffect, useRef } from "react";
import confetti from "canvas-confetti";
import "./CouponWheelModal.css";

const CouponWheelModal = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [spinning, setSpinning] = useState(false);
  const [result, setResult] = useState(null);
  const confettiRef = useRef(null);

  const coupons = [
    "5% OFF",
    "10% OFF",
    "15% OFF",
    "20% OFF",
    "FREE SHIPPING",
    "50% OFF",
  ];

  const colors = ["#FF6B6B", "#FFD93D", "#6BCB77", "#4D96FF", "#FFB347", "#C77DFF"];

  useEffect(() => {
    const timer = setTimeout(() => setIsOpen(true), 5000);
    return () => clearTimeout(timer);
  }, []);

  const spinWheel = () => {
    if (spinning) return;

    setSpinning(true);
    setResult(null);

    const sliceAngle = 360 / coupons.length;
    const randomIndex = Math.floor(Math.random() * coupons.length);

    // Pointer is at top (-90deg)
    const degrees = 360 * 5 + randomIndex * sliceAngle + sliceAngle / 2 - 90;

    const wheel = document.querySelector(".wheel");

    wheel.style.transition = "none";
    wheel.style.transform = "rotate(0deg)";

    requestAnimationFrame(() => {
      wheel.style.transition = "transform 4s ease-out";
      wheel.style.transform = `rotate(${degrees}deg)`;
    });

    setTimeout(() => {
      setSpinning(false);
      
      setResult(coupons[randomIndex]);

      const myConfetti = confetti.create(confettiRef.current, { resize: true, useWorker: true });
      myConfetti({
        particleCount: 400,
        spread: 360,
        startVelocity: 60,
        gravity: 0.8,
        origin: { x: 0.5, y: 0.5 },
      });
    }, 4000);
  };

  return (
    <>
      {isOpen && (
        <div className="modal-overlay">
          <div className="modal">
            <button className="close-btn" onClick={() => setIsOpen(false)}>✖</button>
            <h5>🎉 Spin the Wheel & Win a Coupon!</h5>

            <div className="wheel-container">
              <div className="wheel">
                {coupons.map((coupon, index) => {
                  const sliceAngle = 360 / coupons.length;
                  return (
                    <div
                      key={index}
                      className="slice"
                      style={{
                        transform: `rotate(${sliceAngle * index}deg)`,
                        backgroundColor: colors[index % colors.length],
                      }}
                    >
                      <span
                        className="slice-text"
                        style={{ transform: `rotate(${sliceAngle / 2}deg)` }}
                      >
                        {coupon}
                      </span>
                    </div>
                  );
                })}
              </div>
              <div className="pointer">▼</div>
            </div>

            <button className="spin-btn" onClick={spinWheel} disabled={spinning}>
              {spinning ? "Spinning..." : "Spin Now"}
            </button>

            {result && <p className="result">You won: 🎁 {result}</p>}
          </div>
          <canvas ref={confettiRef} className="confetti-canvas" />
        </div>
      )}
    </>
  );
};

export default CouponWheelModal;
