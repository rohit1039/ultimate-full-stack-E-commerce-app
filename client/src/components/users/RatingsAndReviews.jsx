import { FaStar, FaStarHalfAlt, FaRegStar } from 'react-icons/fa';
import Product_1 from '../../assets/users/dp.jpg';
import Product_2 from '../../assets/others/women.png';
import Product_3 from '../../assets/users/priyanka.png';
import Product_4 from '../../assets/users/sid.png';

export const RatingStars = ({ rating }) => {
    const fullStars = Math.floor(rating);
    const hasHalfStar = rating % 1 >= 0.5;
    const totalStars = 5;
    const stars = [];

    for (let i = 0; i < fullStars; i++) {
        stars.push(<FaStar key={`full-${i}`} color="#FFC107" />);
    }

    if (hasHalfStar) {
        stars.push(<FaStarHalfAlt key="half" color="#FFC107" />);
    }

    while (stars.length < totalStars) {
        stars.push(<FaRegStar key={`empty-${stars.length}`} color="#FFC107" />);
    }

    return <div style={{ display: 'flex', gap: '4px' }}>{stars}</div>;
};

const RatingsAndReviews = () => {
    const reviews = [
        {
            name: "Rohit Parida",
            email: "rohit@example.com",
            city: "Bhubaneswar",
            country: "India",
            image: Product_1,
            review: "Loved the product! Fast delivery and great quality.",
            rating: 4.5,
            date: "2025-07-07"
        },
        {
            name: "Priya Mehra",
            email: "priya.mehra@example.com",
            city: "Mumbai",
            country: "India",
            image: Product_2,
            review: "Excellent packaging and on-time delivery.",
            rating: 4.5,
            date: "2025-07-02"
        },
        {
            name: "Siddharth Malhotra",
            email: "rohit@example.com",
            city: "New Delhi",
            country: "India",
            image: Product_4,
            review: "Loved the product! Fast delivery and great quality.",
            rating: 5,
            date: "2025-07-03"
        },
        {
            name: "Priyanka Chopra",
            email: "priya.mehra@example.com",
            city: "Pune",
            country: "India",
            image: Product_3,
            review: "Excellent packaging and on-time delivery.",
            rating: 5,
            date: "2025-07-09"
        }
    ];

    const getTimeAgo = (dateString) => {
        const reviewDate = new Date(dateString);
        const now = new Date();
        const diffMs = now - reviewDate;

        const diffMinutes = Math.floor(diffMs / 1000 / 60);
        const diffHours = Math.floor(diffMinutes / 60);
        const diffDays = Math.floor(diffHours / 24);

        if (diffMinutes < 1) return "just now";
        if (diffMinutes < 60) return `${diffMinutes} minute${diffMinutes > 1 ? 's' : ''} ago`;
        if (diffHours < 24) return `${diffHours} hour${diffHours > 1 ? 's' : ''} ago`;
        return `${diffDays} day${diffDays > 1 ? 's' : ''} ago`;
    };

    return (
        <div>
            <div style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(2, 1fr)',
                gap: '1.5rem',
                marginTop: '2rem',
                marginLeft: '0.75rem'
            }}>
                {reviews.map((review, index) => (
                    <div key={index} style={{
                        backgroundColor: '#ffffff',
                        borderRadius: '8px',
                        padding: '2rem',
                        display: 'flex',
                        gap: '1rem',
                        boxShadow: '0 4px 12px rgba(0, 0, 0, 0.08)'
                    }}>
                        <img
                            src={review.image}
                            alt={review.name}
                            width={80}
                            height={80}
                            style={{ borderRadius: '50%', objectFit: 'cover' }}
                        />
                        <div style={{ flex: 1 }}>
                            <div style={{
                                display: 'flex',
                                justifyContent: 'space-between',
                                alignItems: 'center',
                            }}>
                                <div>
                                    <p style={{ margin: 0, fontWeight: 600, fontSize: '15px' }}>{review.name}</p>
                                    <p style={{ margin: 0, fontSize: '13px', color: '#666' }}>
                                        {review.city}, {review.country} <span style={{ fontSize: '1rem', margin: '1rem 0.25rem 0 0.25rem', color: '#aaa' }}>•</span> {getTimeAgo(review.date)}
                                    </p>
                                </div>
                                <span style={{ fontSize: '13px', color: '#666', marginTop: '-1rem' }}>{review.date}</span>
                            </div>

                            <div style={{ marginTop: '0.5rem' }}>
                                <RatingStars rating={review.rating} />
                            </div>

                            <p style={{
                                marginTop: '0.75rem',
                                fontSize: '13px',
                                lineHeight: 1.5,
                                color: '#333'
                            }}>
                                {review.review}
                            </p>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default RatingsAndReviews;
