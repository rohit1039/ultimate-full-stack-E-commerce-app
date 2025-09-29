import { useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import InnerImageZoom from "react-inner-image-zoom";
import "react-inner-image-zoom/src/styles.css";
import "./ProductDetails.css";

export const ProductImageLayout = ({ images }) => {
  const [layout, setLayout] = useState("1x1");
  const [selectedImage, setSelectedImage] = useState(images[0]);

  return (
    <div className="image-layout-wrapper">
      <div className="layout-dropdown-wrapper">
        <label htmlFor="layout">View Layout:</label>
        <select
          id="layout"
          value={layout}
          onChange={(e) => setLayout(e.target.value)}
          className="layout-dropdown"
        >
          {["1x1", "2x2", "3x3"].map((opt) => (
            <option key={opt} value={opt}>
              {opt}
            </option>
          ))}
        </select>
      </div>

      <div className="image-section">
        {layout === "1x1" ? (
          <div className="one-by-one-layout">
            <div className="thumbnail-container-vertical">
              {images.map((img, index) => (
                <img
                  key={index}
                  src={img}
                  alt={`Thumbnail ${index}`}
                  className={`thumbnail ${selectedImage === img ? "active-thumb" : ""}`}
                  onClick={() => setSelectedImage(img)}
                />
              ))}
            </div>

            <div className="main-image-wrapper">
              <InnerImageZoom
                src={selectedImage}
                zoomSrc={selectedImage}
                zoomType="hover"
                zoomScale={1.2}
                hasSpacer={true}
                className="custom-zoom"
              />
            </div>
          </div>
        ) : (
          <motion.div
            key={layout}
            layout
            className={`image-grid grid-${layout}`}
            transition={{ duration: 0.6, ease: "easeInOut" }}
          >
            <AnimatePresence>
              {images.map((img, index) => (
                <motion.div
                  key={index}
                  layout
                  initial={{ opacity: 0, scale: 0.9 }}
                  animate={{ opacity: 1, scale: 1 }}
                  exit={{ opacity: 0 }}
                  transition={{ duration: 0.4 }}
                  className="grid-item"
                >
                  <InnerImageZoom
                    src={img}
                    zoomSrc={img}
                    zoomType="hover"
                    zoomScale={1.5}
                    hasSpacer={true}
                    className="custom-zoom"
                  />
                </motion.div>
              ))}
            </AnimatePresence>
          </motion.div>
        )}
      </div>
    </div>
  );
};
