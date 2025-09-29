import Test from "./assets/others/Welcome.png";
import Kids from "./assets/others/kids.png";
import Men from "./assets/others/men.png";
import Watches from "./assets/others/watch.png";
import Shoes from "./assets/others/shoes.png";
import Women from "./assets/others/women.png";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faBagShopping, faChevronRight, faPlus } from "@fortawesome/free-solid-svg-icons";
import { useState } from "react";
import Pagination from "./components/users/Pagination";
import { FaEnvelope, FaFacebook, FaInstagram } from "react-icons/fa";
import { FaXTwitter } from "react-icons/fa6";
import Shipping from "./components/users/Shipping";
import RatingsAndReviews from "./components/users/RatingsAndReviews";
import { SectionWrapper } from "./components/wrapper/SectionWrapper";
import { Link, useParams } from "react-router-dom";
import Products from "./components/users/Products";
import { formatSlug } from "./components/common/BreadCrumb";
import { ToastContainer } from "react-toastify";

function App() {
    const [selectedColor, setSelectedColor] = useState("#ffffff");
    const [isColorOpen, setIsColorOpen] = useState(false);
    const [page, setPage] = useState(1);
    const totalPages = 25;

    const { categoryName } = useParams();

    return (
        <div className="page-container">
            <div>
                <div style={{ display: 'flex', width: '100%' }}>
                    <div style={{ width: '22.75%' }}>
                        <table
                            style={{
                                margin: "1rem 0rem 0rem 1rem",
                                border: "1px solid #e0e0e0",
                                width: "97%",
                                borderCollapse: "separate",
                                borderSpacing: 0,
                                borderRadius: "5px",
                                overflow: "hidden",
                                fontSize: "13px",
                            }}
                        >
                            <tbody>
                                {[
                                    "Women's Sarees",
                                    "Women's Fashion",
                                    "Men's Jeans",
                                    "Kid's Shirts",
                                    "Kid's Pants",
                                    "Men's Ethnic Wear",
                                    "Women's Kurti",
                                    "Men's Inner Wear",
                                    "Kid's Party Wear",
                                    "Men's Collections",
                                    "Men's Inner Wear",
                                ].map((category, idx) => (
                                    <tr
                                        key={idx}
                                        className="category-row"
                                        style={{ outline: idx % 2 === 0 ? "0.5px solid #e0e0e0" : "none" }}
                                    >
                                        <td style={{ padding: "0.75rem" }}>
                                            <Link
                                                to={category}
                                                style={{
                                                    textDecoration: "none",
                                                    color: "inherit",
                                                    display: "block",
                                                    width: "100%",
                                                }}
                                            >
                                                {category}
                                            </Link>
                                        </td>
                                        <td style={{ padding: "0.75rem" }}>
                                            <FontAwesomeIcon className="hover-icon" icon={faChevronRight} />
                                        </td>
                                    </tr>
                                ))}

                                {/* View All Categories */}
                                <tr
                                    className="category-row"
                                    style={{ outline: "0.5px solid #e0e0e0" }}
                                >
                                    <td
                                        style={{
                                            padding: "0.75rem",
                                            color: "#0D8DDE"
                                        }}
                                    >
                                        <Link
                                            to="/categories"
                                            style={{
                                                textDecoration: "none",
                                                color: "#0D8DDE",
                                                fontWeight: "bold",
                                                display: "block",
                                                width: "100%",
                                            }}
                                        >
                                            View All Categories
                                        </Link>
                                    </td>
                                    <td style={{ padding: "0.75rem" }}>
                                        <FontAwesomeIcon className="hover-icon" icon={faPlus} color="#0D8DDE" />
                                    </td>
                                </tr>
                            </tbody>
                        </table>

                    </div>
                    <div style={{ backgroundColor: '#EFF9FF', height: '501px', width: '92%', marginTop: '1rem', marginLeft: '1.5rem', borderRadius: '5px', position: 'relative' }}>
                        <img src={Test} alt="banner_img" width='100%' height='500px' style={{ objectFit: 'cover', borderRadius: '5px' }} />
                        <button className="shop-now">Shop Now :)</button>
                        <div className="social-icons">
                            <a href="https://instagram.com" target="_blank" rel="noopener noreferrer"><FaInstagram color="#E1306C" /></a>
                            <a href="https://facebook.com" target="_blank" rel="noopener noreferrer"><FaFacebook color="#1877F2" /></a>
                            <a href="https://twitter.com" target="_blank" rel="noopener noreferrer"><FaXTwitter color="#1DA1F2" /></a>
                            <a href="mailto:wearinservices@gmail.com" rel="noopener noreferrer"><FaEnvelope color="#D93025" /></a>
                        </div>
                    </div>
                </div>
            </div>
            <div>
                <div style={{ display: 'flex', alignItems: 'center', width: '98.75%', gap: '0.85rem', margin: '1rem'}}>
                    {/* Kid's Wear */}
                    <div style={{ display: 'flex', alignItems: 'center', height: '150px', width: '19.75%', borderRadius: '5px', backgroundColor: '#FFECF0', padding: '0.5rem' }}>
                        <img src={Kids} alt="kids" height="118px" style={{ marginRight: '0.5rem' }} />
                        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '0.85rem', fontSize: '14px' }}>
                            <span>Kid's Wear</span>
                            <span style={{ fontWeight: 'bold', fontSize: '1.15rem', color: "#333" }}>30% Sale</span>
                            <span>
                                <FontAwesomeIcon icon={faBagShopping} color="red" />&nbsp;
                                <span style={{ textDecoration: 'underline', color: 'red', fontSize: '0.90rem' }}>Buy Now</span>
                            </span>
                        </div>
                    </div>

                    {/* Men's Wear */}
                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-evenly', height: '150px', width: '19.75%', borderRadius: '5px', backgroundColor: '#EFF9FF', padding: '0.5rem' }}>
                        <img src={Men} alt="men" height="150px" style={{marginLeft: '0.25rem'}} />
                        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '0.85rem', fontSize: '14px' }}>
                            <span>Men's Wear</span>
                            <span style={{ fontWeight: 'bold', fontSize: '1.15rem', color: "#333" }}>15% Sale</span>
                            <span>
                                <FontAwesomeIcon icon={faBagShopping} color="red" />&nbsp;
                                <span style={{ textDecoration: 'underline', color: 'red', fontSize: '0.90rem' }}>Buy Now</span>
                            </span>
                        </div>
                    </div>

                    {/* Women's Wear */}
                    <div style={{ display: 'flex', alignItems: 'center', height: '150px', width: '19.5%', borderRadius: '5px', backgroundColor: '#FFF8F0', padding: '0.5rem' }}>
                        <img src={Women} alt="women" height="147px" style={{ marginTop: '0.125rem' }} />
                        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '0.85rem', fontSize: '14px' }}>
                            <span>Women's Wear</span>
                            <span style={{ fontWeight: 'bold', fontSize: '1.15rem', color: "#333" }}>10% Sale</span>
                            <span>
                                <FontAwesomeIcon icon={faBagShopping} color="red" />&nbsp;
                                <span style={{ textDecoration: 'underline', color: 'red', fontSize: '0.90rem' }}>Buy Now</span>
                            </span>
                        </div>
                    </div>

                    {/* Men's Foot Wear */}
                    <div style={{
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'space-between',
                        height: '150px',
                        width: '19.5%',
                        padding: '1rem',
                        borderRadius: '5px',
                        backgroundColor: '#EFF9FF',
                        position: 'relative'
                    }}>
                        <div style={{ position: 'relative' }}>
                            <div style={{
                                position: 'absolute',
                                top: '1.25rem',
                                backgroundColor: 'orange',
                                color: 'white',
                                fontWeight: 'bold',
                                padding: '2px 6px',
                                borderRadius: '4px',
                                fontSize: '11px',
                                zIndex: 1
                            }}>
                                BESTSELLER
                            </div>
                            <div style={{ position: 'relative', height: '140px', width: '100px' }}>
                                <img
                                    src={Shoes}
                                    height="100px"
                                    alt="shoes"
                                    style={{
                                        position: 'relative',
                                        zIndex: 2,
                                        marginLeft: '-0.75rem',
                                        marginTop: '2.5rem',
                                        display: 'block',
                                    }}
                                />
                                <div style={{
                                    position: 'absolute',
                                    top: "6.25rem",
                                    bottom: '6px',
                                    left: '30%',
                                    transform: 'translateX(-50%)',
                                    width: '70%',
                                    height: '10px',
                                    background: 'rgba(0, 0, 0, 0.3)',
                                    borderRadius: '50%',
                                    filter: 'blur(4px)',
                                    zIndex: 1
                                }}></div>
                            </div>
                        </div>
                        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '0.85rem', fontSize: '14px' }}>
                            <span>Foot Wears</span>
                            <span style={{ fontWeight: 'bold', fontSize: '1.25rem', color: "#333" }}>20% Sale</span>
                            <span>
                                <FontAwesomeIcon icon={faBagShopping} color="red" />&nbsp;
                                <span style={{ textDecoration: 'underline', color: 'red', fontSize: '0.90rem' }}>Buy Now</span>
                            </span>
                        </div>
                    </div>

                    {/* Watches Collection */}
                    <div style={{
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'space-between',
                        height: '150px',
                        width: '19.5%',
                        padding: '1rem',
                        borderRadius: '5px',
                        backgroundColor: '#F0FFF6',
                        position: 'relative'
                    }}>
                        <div style={{ position: 'relative' }}>
                            <div style={{
                                position: 'absolute',
                                top: '-0.5rem',
                                backgroundColor: '#F44336',
                                color: 'white',
                                fontWeight: 'bold',
                                padding: '2px 6px',
                                borderRadius: '4px',
                                fontSize: '11px',
                                zIndex: 1
                            }}>
                                TRENDING
                            </div>
                            <img
                                src={Watches}
                                height="100px"
                                width="100px"
                                alt="watches"
                                style={{ zIndex: 2, marginTop: '1rem' }}
                            />
                        </div>
                        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '0.85rem', fontSize: '14px' }}>
                            <span>Watches</span>
                            <span style={{ fontWeight: 'bold', fontSize: '1.25rem', color: "#333" }}>25% Sale</span>
                            <span>
                                <FontAwesomeIcon icon={faBagShopping} color="red" />&nbsp;
                                <span style={{ textDecoration: 'underline', color: 'red', fontSize: '0.90rem' }}>Buy Now</span>
                            </span>
                        </div>
                    </div>
                </div>
                <SectionWrapper heading="Featured Items" name="h3" />
                <Products categorySlug={categoryName && formatSlug(decodeURIComponent(categoryName))} />
                <SectionWrapper heading="Our Services" name="h3" />
                <Shipping />
                <SectionWrapper heading="What Our Customers Say" name="h3" />
                <RatingsAndReviews />
            </div >
        </div >
    )
}

export default App;