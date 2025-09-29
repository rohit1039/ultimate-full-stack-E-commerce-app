import ReactDOM from "react-dom/client";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import App from "./App";
import Layout from "./components/admin/Layout";
import "./index.css";
import CategoryProducts from "./components/users/CategoryProducts";
import NavBar from "./components/users/NavBar";
import { ProductDetails } from "./components/users/ProductDetails";
import BreadCrumb, { formatSlug } from "./components/common/BreadCrumb";
import Footer from "./components/users/Footer";
import { SectionWrapper } from "./components/wrapper/SectionWrapper";
import { useState } from "react";
import ShoppingCart from "./components/users/ShoppingCart";
import CheckoutAddressPage from "./components/users/CheckoutAddress";
import product_1 from "./assets/others/product_1.png";
import product_2 from "./assets/others/product_2.png";
import product_3 from "./assets/others/product_3.png";
import product_4 from "./assets/others/product_4.png";
import product_5 from "./assets/others/product_5.png";

import product_new_1 from "./assets/others/product_new_1.png";
import product_new_2 from "./assets/others/product_new_2.png";
import product_new_3 from "./assets/others/product_new_3.png";
import product_new_4 from "./assets/others/product_new_4.png";
import product_new_5 from "./assets/others/product_new_5.png";
import Payments from "./components/users/Payments";

const root = ReactDOM.createRoot(document.getElementById("root"));

function Root() {
  const products = [
    {
      _id: "REGULAR FIT PLAIN SHIRT BENEDICT - LIGHT BLUE",
      product_name: formatSlug("REGULAR FIT PLAIN SHIRT BENEDICT - LIGHT BLUE"),
      product_brand: "By BENEDICT",
      product_main_image: "main.png",
      product_images: [product_1, product_2, product_3, product_4, product_5],
      product_color: ["#333", "#42A5F5", "#EF5350", "green", "teal"],
      product_sizes: ["XS", "S", "M", "L", "XL"],
      variants: [
        { color: "#333", size: "XS", stock: 10 },
        { color: "#333", size: "S", stock: 5 },
        { color: "#42A5F5", size: "M", stock: 7 },
        { color: "#42A5F5", size: "L", stock: 2 },
        { color: "#EF5350", size: "XL", stock: 0 },
      ],
      discount_percent: 5,
      total_price: 1425,
      product_price: 1500,
      short_desc: "A half sleeve solid color polo, with a ribbed collar",
      long_desc:
        "A half sleeve solid color polo, with a ribbed collar. Made from 100% cotton for comfort. Perfect for casual outings or relaxed office wear. A half sleeve solid color polo, with a ribbed collar. Perfect for casual outings or relaxed office wear.",
    },
    {
      _id: "Sample Product - 2",
      product_name: formatSlug("Sample Product - 2"),
      product_brand: "BENEDICT",
      product_main_image: "main.png",
      product_images: [
        product_new_1,
        product_new_2,
        product_new_3,
        product_new_4,
        product_new_5,
      ],
      product_color: ["#333", "#42A5F5", "#EF5350"],
      product_sizes: ["S", "M", "L", "XL"],
      variants: [
        { color: "#333", size: "S", stock: 10 },
        { color: "#333", size: "M", stock: 5 },
        { color: "#333", size: "L", stock: 0 },
        { color: "#42A5F5", size: "M", stock: 7 },
        { color: "#42A5F5", size: "L", stock: 2 },
        { color: "#EF5350", size: "XL", stock: 0 },
      ],
      discount_percent: 5,
      total_price: 1425,
      product_price: 1500,
      short_desc:
        "A half sleeve solid color polo, with a ribbed collar and zipper",
      long_desc:
        "A half sleeve solid color polo, with a ribbed collar and zipper closure. Made from 100% cotton for comfort and breathability. Perfect for casual outings or relaxed office wear.",
    },
    {
      _id: "Sample Product - 3",
      product_name: formatSlug("Sample Product - 3"),
      product_brand: "BENEDICT",
      product_main_image: "main.png",
      product_images: [product_1, product_2, product_3, product_4, product_5],
      product_color: ["#333", "#42A5F5", "#EF5350"],
      product_sizes: ["S", "M", "L", "XL"],
      variants: [
        { color: "#333", size: "S", stock: 10 },
        { color: "#333", size: "M", stock: 5 },
        { color: "#333", size: "L", stock: 0 },
        { color: "#42A5F5", size: "M", stock: 7 },
        { color: "#42A5F5", size: "L", stock: 2 },
        { color: "#EF5350", size: "XL", stock: 0 },
      ],
      discount_percent: 5,
      total_price: 1425,
      product_price: 1500,
      short_desc:
        "A half sleeve solid color polo, with a ribbed collar and zipper",
      long_desc:
        "A half sleeve solid color polo, with a ribbed collar and zipper closure. Made from 100% cotton for comfort and breathability. Perfect for casual outings or relaxed office wear. A half sleeve solid color polo, with a ribbed collar and zipper closure. Made from 100% cotton for comfort and breathability. Perfect for casual outings or relaxed office wear. ",
    },
    {
      _id: "Sample Product - 4",
      product_name: formatSlug("Sample Product - 4"),
      product_brand: "BENEDICT",
      product_main_image: "main.png",
      product_images: [product_1, product_2, product_3, product_4, product_5],
      product_color: ["#333", "#42A5F5", "#EF5350"],
      product_sizes: ["S", "M", "L", "XL"],
      variants: [
        { color: "#333", size: "S", stock: 10 },
        { color: "#333", size: "M", stock: 5 },
        { color: "#333", size: "L", stock: 0 },
        { color: "#42A5F5", size: "M", stock: 7 },
        { color: "#42A5F5", size: "L", stock: 2 },
        { color: "#EF5350", size: "XL", stock: 0 },
      ],
      discount_percent: 5,
      total_price: 1425,
      product_price: 1500,
      short_desc:
        "A half sleeve solid color polo, with a ribbed collar and zipper",
      long_desc:
        "A half sleeve solid color polo, with a ribbed collar and zipper. Made from 100% cotton for comfort and breathability. Perfect for casual outings or relaxed office wear. A half sleeve solid color polo, with a ribbed collar and zipper. Made from 100% cotton for comfort and breathability.",
    },
  ];

  const [cartItems, setCartItems] = useState([]);

  const cartCount = cartItems.length;

  return (
    <>
      <BrowserRouter>
        <NavBar cartCount={cartCount} />
        <Routes>
          <Route path="/" element={<App />} />
          <Route
            path="/cart"
            element={<ShoppingCart cartItems={cartItems} />}
          />
          <Route path="/admin" element={<Layout />} />
          <Route
            path="/:categoryName"
            element={
              <div>
                <BreadCrumb />
                <CategoryProducts />
              </div>
            }
          />
          <Route
            path="/:categoryName/:productName"
            element={
              <>
                <BreadCrumb />
                <ProductDetails
                  cartItems={cartItems}
                  setCartItems={setCartItems}
                  products={products}
                />
              </>
            }
          />
          <Route
            path="/product/:productName"
            element={
              <>
                <BreadCrumb />
                <ProductDetails
                  cartItems={cartItems}
                  setCartItems={setCartItems}
                  products={products}
                />
              </>
            }
          />
          <Route path="/checkout/address" element={<CheckoutAddressPage />} />
          <Route path="/checkout/payment" element={<Payments />} />
        </Routes>
      </BrowserRouter>
      <div style={{ margin: "auto 2rem" }}>
        <SectionWrapper heading="About ShopMe" name="h3" />
      </div>
      <Footer />
    </>
  );
}

root.render(<Root />);
