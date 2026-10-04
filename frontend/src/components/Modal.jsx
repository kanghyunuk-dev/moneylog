import { useEffect, useRef } from 'react';
import './Modal.css';

function Modal({ isOpen, onClose, className, children }) {
    const dialogRef = useRef(null);

    useEffect(() => {
        const dialog = dialogRef.current;
        if (isOpen) {
            dialog.showModal();
        } else {
            dialog.close();
        }
    }, [isOpen]);

    function handleBackdropClick(e) {
        const rect = dialogRef.current.getBoundingClientRect();
        const isOutside =
            e.clientX < rect.left ||
            e.clientX > rect.right ||
            e.clientY < rect.top ||
            e.clientY > rect.bottom;

        if (isOutside) {
            onClose();
        }
    }

    return (
        <dialog ref={dialogRef} className={`modal ${className ?? ''}`} onClose={onClose} onClick={handleBackdropClick}>
            {children}
        </dialog>
    );
}

export default Modal;
