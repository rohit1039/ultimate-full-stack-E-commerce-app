import { useState, cloneElement } from "react";
import "./DeleteModal.css";

const DeleteModal = ({ trigger, onDelete, itemId, message }) => {
    const [isOpen, setIsOpen] = useState(false);

    const openModal = (e) => {
        e.stopPropagation();
        setIsOpen(true);
    };

    const closeModal = () => {
        setIsOpen(false);
    };

    const confirmDelete = () => {
        if (onDelete && itemId !== undefined) {
            onDelete(itemId);
        }
        closeModal();
    };

    return (
        <>
            {cloneElement(trigger, { onClick: openModal })}
            {isOpen && (
                <div className="modal-overlay">
                    <div className="modal-content">
                        <button className="modal-close" onClick={closeModal}>
                            &times;
                        </button>
                        <h4>Confirm Delete</h4>
                        <div className="model-content-actions">
                            <p>{message || "Are you sure, you want to delete this address?"}</p>
                            <div className="modal-actions">
                                <button className="confirm-btn" onClick={confirmDelete}>
                                    Yes
                                </button>
                                <button className="cancel-btn" onClick={closeModal}>
                                    No
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </>
    );
};

export default DeleteModal;
