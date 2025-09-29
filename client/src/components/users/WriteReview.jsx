import { useState } from "react";
import { toast } from "react-toastify";
import { FaStar } from "react-icons/fa";
import "./WriteReview.css";

const WriteReview = ({ show, onClose }) => {
    const [rating, setRating] = useState(0);
    const [hover, setHover] = useState(null);
    const [reviewText, setReviewText] = useState("");
    const [name, setName] = useState("");

    if (!show) return null;

    const handleSubmit = (e) => {
        e.preventDefault();
        if (rating === 0 || reviewText.trim() === "") {
            toast.error("Please provide a rating and review text.");
            return;
        }

        console.log({
            rating,
            review: reviewText,
            reviewer: name || "Anonymous",
        });

        toast.success("Thank you for your review!");
        setRating(0);
        setReviewText("");
        setName("");
        onClose();
    };

    return (
        <div className="modal-overlay">
            <div className="modal-content">
                <button className="close-btn" onClick={onClose}>✖</button>
                <div className="write-review">
                    <h4>Write a Review</h4>
                    <form onSubmit={handleSubmit}>
                        {/* Rating stars */}
                        <div className="rating-input-container">
                            <div className="rating-input">
                                {[...Array(5)].map((_, index) => {
                                    const starValue = index + 1;
                                    return (
                                        <label key={starValue}>
                                            <input
                                                type="radio"
                                                name="rating"
                                                value={starValue}
                                                onClick={() => setRating(starValue)}
                                                style={{ display: "none" }}
                                            />
                                            <FaStar
                                                size={24}
                                                color={
                                                    starValue <= (hover || rating)
                                                        ? "#ffc107"
                                                        : "#e4e5e9"
                                                }
                                                onMouseEnter={() => setHover(starValue)}
                                                onMouseLeave={() => setHover(null)}
                                                style={{ cursor: "pointer" }}
                                            />
                                        </label>
                                    );
                                })}
                            </div>

                            {/* Review text */}
                            <div className="feedback-textarea">
                                <textarea
                                    placeholder="Your feedback matters..."
                                    maxLength={225}
                                    value={reviewText}
                                    onChange={(e) => setReviewText(e.target.value)}
                                />
                            </div>

                            {/* Reviewer name */}
                            <input
                                type="text"
                                placeholder="Your name (optional)"
                                value={name}
                                className="review-text-input"
                                onChange={(e) => setName(e.target.value)}
                            />
                            <div>
                                <button type="submit" className="submit-review">
                                    Submit Review
                                </button>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default WriteReview;
