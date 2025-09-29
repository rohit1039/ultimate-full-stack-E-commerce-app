import { useParams } from "react-router-dom";
import BreadCrumb, { formatSlug } from "../common/BreadCrumb";
import Products from "./Products";
import Pagination from "./Pagination";
import { useState } from "react";

const CategoryPage = () => {

  const [page, setPage] = useState(1);
  const { categoryName } = useParams();

  return (
    <div style={{ padding: "1rem", margin: '1rem' }}>
      <Products categorySlug={formatSlug(decodeURIComponent(categoryName))} />
      <Pagination totalPages={10} currentPage={page} onPageChange={(p) => setPage(p)} siblingCount={1} />
    </div>
  );
};

export default CategoryPage;
