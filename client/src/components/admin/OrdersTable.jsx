import React, { useState, useRef } from "react";
import "./OrdersTable.css";

const orders = [
  { id: "#10234", customer: "Amaya Weller", product: "Wireless Headphones", img: "🎧", qty: 2, total: "$100", status: "Shipped" },
  { id: "#10235", customer: "Sebastian Adams", product: "Running Shoes", img: "👟", qty: 1, total: "$75", status: "Processing" },
  { id: "#10236", customer: "Suzanne Bright", product: "Smartwatch", img: "⌚", qty: 1, total: "$150", status: "Delivered" },
  { id: "#10237", customer: "Peter Howl", product: "Coffee Maker", img: "☕", qty: 1, total: "$60", status: "Pending" },
  { id: "#10238", customer: "Anita Singh", product: "Bluetooth Speaker", img: "🔊", qty: 3, total: "$90", status: "Shipped" },
  { id: "#10239", customer: "Rohit Lucky Kumar Parida", product: "Gaming Laptop", img: "💻", qty: 1, total: "$1200", status: "Processing" },
  { id: "#10240", customer: "Jessica Jones", product: "Tablet", img: "📱", qty: 2, total: "$400", status: "Delivered" },
  { id: "#10241", customer: "David Brooks", product: "Mechanical Keyboard", img: "⌨️", qty: 1, total: "$90", status: "Shipped" },
  { id: "#10242", customer: "Laura Chen", product: "Wireless Mouse", img: "🖱️", qty: 2, total: "$50", status: "Delivered" },
  { id: "#10243", customer: "Michael Brown", product: "4K Monitor", img: "🖥️", qty: 1, total: "$300", status: "Processing" },
  { id: "#10244", customer: "Sophie Turner", product: "DSLR Camera", img: "📷", qty: 1, total: "$800", status: "Pending" },
  { id: "#10245", customer: "Ethan Clark", product: "Smartphone", img: "📱", qty: 1, total: "$700", status: "Delivered" },
  { id: "#10246", customer: "Priya Kapoor", product: "Power Bank", img: "🔋", qty: 2, total: "$40", status: "Shipped" },
];

const OrdersTable = ({ darkMode }) => {
  const [currentPage, setCurrentPage] = useState(1);
  const ordersPerPage = 5;
  const tableRef = useRef(null); // Reference to scroll to table

  const totalPages = Math.ceil(orders.length / ordersPerPage);
  const indexOfLastOrder = currentPage * ordersPerPage;
  const indexOfFirstOrder = indexOfLastOrder - ordersPerPage;
  const currentOrders = orders.slice(indexOfFirstOrder, indexOfLastOrder);

  const handlePageChange = (pageNumber) => {
    if (pageNumber >= 1 && pageNumber <= totalPages) {
      setCurrentPage(pageNumber);
      // Scroll table into view smoothly
      tableRef.current?.scrollIntoView({ behavior: "smooth", block: "start" });
    }
  };

  return (
    <div ref={tableRef} className={darkMode ? "recent-orders-dark" : "recent-orders"}>
      <div className={darkMode ? "orders-header-dark" : "orders-header"}>
        <h3>Recent Orders</h3>
        <input type="text" placeholder="Search product, customer, etc." />
        <button className="category-btn">All Categories <span className="down-arrow">▼</span></button>
      </div>

      <table className={darkMode ? "orders-table-dark" : "orders-table"}>
        <thead>
          <tr>
            <th>No <button className={darkMode ? "sort-btn-dark" : "sort-btn"}>▼</button></th>
            <th>Order ID <button className={darkMode ? "sort-btn-dark" : "sort-btn"}>▼</button></th>
            <th>Customer <button className={darkMode ? "sort-btn-dark" : "sort-btn"}>▼</button></th>
            <th>Product <button className={darkMode ? "sort-btn-dark" : "sort-btn"}>▼</button></th>
            <th>Qty <button className={darkMode ? "sort-btn-dark" : "sort-btn"}>▼</button></th>
            <th>Total <button className={darkMode ? "sort-btn-dark" : "sort-btn"}>▼</button></th>
            <th>Status <button className={darkMode ? "sort-btn-dark" : "sort-btn"}>▼</button></th>
          </tr>
        </thead>

        <tbody>
          {currentOrders.map((order, index) => (
            <tr key={order.id}>
              <td>{indexOfFirstOrder + index + 1}</td>
              <td>{order.id}</td>
              <td>{order.customer}</td>
              <td className="product-cell">
                <span className="product-img">{order.img}</span>
                {order.product}
              </td>
              <td>{order.qty}</td>
              <td>{order.total}</td>
              <td>
                <span className={`status ${order.status.toLowerCase()}`}>
                  {order.status}
                </span>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {/* Pagination */}
      <div className={darkMode ? "pagination-dark" : "pagination"}>
        <button
          onClick={() => handlePageChange(currentPage - 1)}
          disabled={currentPage === 1}
        >
          Prev
        </button>

        {Array.from({ length: totalPages }, (_, index) => (
          <button
            key={index + 1}
            className={currentPage === index + 1 ? "active" : ""}
            onClick={() => handlePageChange(index + 1)}
          >
            {index + 1}
          </button>
        ))}

        <button
          onClick={() => handlePageChange(currentPage + 1)}
          disabled={currentPage === totalPages}
        >
          Next
        </button>
      </div>
    </div>
  );
};

export default OrdersTable;
