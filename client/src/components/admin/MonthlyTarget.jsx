import { useEffect, useRef, useState } from "react";
import "./MonthlyTarget.css";

const MonthlyTarget = ({ darkMode }) => {
  const progress = 85;
  const pathRef = useRef(null);
  const [dashArray, setDashArray] = useState("0 0");
  const [strokeColor, setStrokeColor] = useState("transparent");
  const [lineCap, setLineCap] = useState("butt");
  const [displayProgress, setDisplayProgress] = useState(0);

  useEffect(() => {
    if (pathRef.current) {
      const totalLength = pathRef.current.getTotalLength();

      // Reset
      setDashArray("0 0");
      setStrokeColor("transparent");
      setLineCap("butt");
      setDisplayProgress(0);

      // Start both arc and number animation at the same time
      setStrokeColor("#FF8201");
      setLineCap("round");

      const duration = 1500; // 1.5 sec
      const startTime = performance.now();

      const animate = (now) => {
        const elapsed = now - startTime;
        const percentage = Math.min((elapsed / duration) * progress, progress);

        const filledLength = (percentage / 100) * totalLength;
        setDashArray(`${filledLength} ${totalLength}`);
        setDisplayProgress(Math.round(percentage));

        if (percentage < progress) requestAnimationFrame(animate);
      };

      requestAnimationFrame(animate);
    }
  }, [progress]);

  return (
    <div className={darkMode ? "monthly-target-card-dark" : "monthly-target-card"}>
      <h3 className="widget-title">Monthly Target</h3>

      <div className="half-gauge">
        <svg viewBox="0 0 36 18" className="half-chart">
          <path className="half-bg" d="M2 18 A16 16 0 0 1 34 18" />

          <path
            ref={pathRef}
            className="half-progress"
            d="M2 18 A16 16 0 0 1 34 18"
            strokeDasharray={dashArray}
            strokeDashoffset="0"
            style={{ stroke: strokeColor, strokeLinecap: lineCap }}
          />
        </svg>
        <div className="half-text">{displayProgress}%</div>
      </div>

      <p className="progress-status">Great Progress! 🎯</p>
      <p className="progress-subtext">
        Our achievement reached <b className="achieved-number">$850,000</b> which is <b>42.1%</b> more than last month.
      </p>

      <div className="target-info">
        <p>Target: <b>$600,000</b></p>
        <p className="achieved">Achieved: <b>$850,000</b></p>
      </div>
    </div>
  );
};

export default MonthlyTarget;
