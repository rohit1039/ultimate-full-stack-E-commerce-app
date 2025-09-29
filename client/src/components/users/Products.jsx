import Product1 from "../../assets/products/product_1.png";
import Product2 from "../../assets/products/product_2.png";
import Product3 from "../../assets/products/product_3.png";
import Product4 from "../../assets/products/product_4.png";
import Product5 from "../../assets/products/product_5.png";
import { FaShoppingCart, FaBolt } from 'react-icons/fa';
import { RatingStars } from "./RatingsAndReviews";
import './Products.css';
import { Link } from "react-router-dom";

const Products = ({ categorySlug }) => {
    const products = [
        {
            img: Product1,
            productSlug: "be-active-kurta-for-men",
            name: "BE ACTIVE short kurta for men"
        },
        {
            img: Product2,
            productSlug: "slim-fit-denim-jeans",
            name: "Slim Fit Denim Jeans"
        },
        {
            img: Product3,
            productSlug: "cotton-printed-kurti",
            name: "Cotton Printed Kurti"
        },
        {
            img: Product4,
            productSlug: "kids-casual-shirt",
            name: "Kid's Casual Shirt"
        },
        {
            img: Product5,
            productSlug: "cotton-stretch-plain-shirt",
            name: "Cotton Stretch Plain Shirt"
        }
    ];

    return (
        <div className="products">
            {products.map((product, index) => (
                <div key={index} className="card-container">
                    <div className="card-header">
                        <div className="badge">61% OFF</div>

                        <div className="image-wrapper">
                            <img className="card-image" src={product.img} alt={product.name} />
                            <div className="image-overlay">
                                <button className="hover-button cart">
                                    <FaShoppingCart className="cart-icon" /> Add to Cart
                                </button>
                                <button className="hover-button buy">
                                    <FaBolt className='buy-icon' /> Buy Now
                                </button>
                            </div>
                        </div>
                    </div>

                    <div className="card-body">
                        <div className="product-desc-container">
                            <Link
                                to={categorySlug
                                    ? `/${categorySlug}/${product.productSlug}`
                                    : `/product/${product.productSlug}`}
                                className="product-link"
                            >
                                <p className="product-desc">{product.name}</p>
                            </Link>
                        </div>

                        <div className="rating-review-container">
                            <div className="rating-container">
                                <RatingStars rating={3.5} />
                                <span className="review-count">(55)</span>
                            </div>
                            <div>
                                <div className="in-stock">In Stock</div>
                            </div>
                        </div>

                        <div className="card-footer">
                            <div className="price-container">
                                <sup className="price-after-discount">₹</sup>
                                <span className="final-price" style={{ color: '#0083db' }}><b>899</b></span>
                                <span><del className="original-price">₹1299</del></span>
                            </div>
                        </div>
                    </div>
                </div>
            ))}
        </div>
    );
};

export default Products;
