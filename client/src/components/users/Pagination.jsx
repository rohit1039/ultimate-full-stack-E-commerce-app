import React, { useMemo } from "react";
import "./Pagination.css";

const DOTS = "…";

/**
 * Small helper that returns an integer array [e.g., range(2,4) → [2,3,4]]
 */
const range = (start, end) =>
  Array.from({ length: end - start + 1 }, (_, i) => i + start);

/**
 * @param totalPages      ‑ total number of pages
 * @param currentPage     ‑ currently selected page (1‑based)
 * @param onPageChange    ‑ callback(pageNumber) → void
 * @param siblingCount    ‑ how many neighbours to show around the current page
 */
export default function Pagination({
  totalPages,
  currentPage,
  onPageChange,
  siblingCount = 1,
}) {
  /* -------- derive the visible page numbers / ellipses -------- */
  const pages = useMemo(() => {
    const totalNumbers = siblingCount * 2 + 5; // prev + next + first + last + siblings
    if (totalNumbers >= totalPages) return range(1, totalPages);

    const left = Math.max(currentPage - siblingCount, 1);
    const right = Math.min(currentPage + siblingCount, totalPages);

    const showLeftDots = left > 2;
    const showRightDots = right < totalPages - 1;

    let items = [];

    if (!showLeftDots && showRightDots) {
      items = [...range(1, 3 + siblingCount * 2), DOTS, totalPages];
    } else if (showLeftDots && !showRightDots) {
      items = [1, DOTS, ...range(totalPages - (2 + siblingCount * 2), totalPages)];
    } else {
      items = [1, DOTS, ...range(left, right), DOTS, totalPages];
    }
    return items;
  }, [totalPages, currentPage, siblingCount]);

  /* -------------------------- handlers ------------------------ */
  const onPrev = () => currentPage > 1 && onPageChange(currentPage - 1);
  const onNext = () => currentPage < totalPages && onPageChange(currentPage + 1);

  /* ------------------------- render UI ------------------------ */
  return (
    <ul className="pg__container">
      <li
        className={`pg__item ${currentPage === 1 && "pg__disabled"}`}
        onClick={onPrev}
      >
        ‹
      </li>

      {pages.map((p, idx) =>
        p === DOTS ? (
          <li key={idx} className="pg__item pg__dots">
            {DOTS}
          </li>
        ) : (
          <li
            key={idx}
            className={`pg__item ${p === currentPage && "pg__selected"}`}
            onClick={() => onPageChange(p)}
          >
            {p}
          </li>
        )
      )}

      <li
        className={`pg__item ${currentPage === totalPages && "pg__disabled"}`}
        onClick={onNext}
      >
        ›
      </li>
    </ul>
  );
}
