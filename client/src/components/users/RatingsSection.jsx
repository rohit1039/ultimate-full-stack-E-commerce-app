
import { RatingStars } from "./RatingsAndReviews";
import "./RatingsSection.css";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faBookOpen, faStar } from "@fortawesome/free-solid-svg-icons";
import { FaStar, FaStarHalfAlt } from "react-icons/fa";
import { SectionWrapper } from "../wrapper/SectionWrapper";

export const ReviewsSummary = () => {
    const ratingsData = [
        { stars: 5, count: 383, color: "#00a884" },
        { stars: 4, count: 123, color: "#42a5f5" },
        { stars: 3, count: 46, color: "#ffa000" },
        { stars: 2, count: 20, color: "#ffb300" },
        { stars: 1, count: 67, color: "#FF3E6C" },
    ];

    const total = ratingsData.reduce((sum, r) => sum + r.count, 0);
    const avg =
        (
            ratingsData.reduce((sum, r) => sum + r.stars * r.count, 0) / total
        ).toFixed(1);

    const truncateText = (text, limit = 85) => {
        return text.length > limit ? text.substring(0, limit) : text;
    };

    return (
        <div className="rating-reviews-container">
            <div className="ratings-container">
                <div className="rating-heading">
                    <h3>Ratings</h3>
                </div>
                <div className="reviews-summary-container">
                    {/* Left side - Average rating */}
                    <div className="average-rating">
                        <h2>{avg}</h2>
                        <RatingStars rating={avg} />
                        <p>{total} Verified Buyers</p>
                    </div>

                    {/* Right side - Distribution */}
                    <div className="ratings-breakdown">
                        {ratingsData.map((r, i) => {
                            const percent = ((r.count / total) * 100).toFixed(1);
                            return (
                                <div key={i} className="rating-row">
                                    <span className="star-label">{r.stars}&nbsp;<FaStar key="full" color="#FFC107" /></span>
                                    <div className="progress-bar">
                                        <div
                                            className="fill"
                                            style={{ width: `${percent}%`, backgroundColor: r.color }}
                                        ></div>
                                    </div>
                                    <span className="count">{r.count}</span>
                                </div>
                            );
                        })}
                    </div>
                </div>
            </div>
            {/* Right side - Reviews list */}
            <div className="reviews-container">
                <div className="review-heading">
                    <h3>Top Reviews</h3>
                </div>
                <div className="reviews-section">
                    <div className="review">
                        <div className="rating-by-user">
                            5 <FaStar color="#FFC107" />
                        </div>
                        <div>
                            <p>{truncateText("Excellent product! Totally worth the money. Loved it, must buy ❣️")}</p>
                            <span className="reviewer">– John D.</span>
                        </div>
                    </div>
                    <div className="review">
                        <div className="rating-by-user">
                            3 <FaStar color="#FFC107" />
                        </div>
                        <div>
                            <p>{truncateText("Good quality but delivery was delayed. Need improvement.")}</p>
                            <span className="reviewer">– Priya S.</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};
