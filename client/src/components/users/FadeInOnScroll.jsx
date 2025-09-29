import { motion, useInView } from "framer-motion";
import { useRef } from "react";

const FadeInOnScroll = ({ children, delay = 0 }) => {
  const ref = useRef(null);
  const isInView = useInView(ref, {
    once: false,
    margin: "-100px",
  });

  return (
    <motion.div
    >
      {children}
    </motion.div>
  );
};

export default FadeInOnScroll;
