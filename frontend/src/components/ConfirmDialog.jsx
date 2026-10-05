import Modal from './Modal';
import './ConfirmDialog.css';

// window.confirm 대신 쓰는, 디자인에 맞춘 삭제 확인창
function ConfirmDialog({ isOpen, message, onConfirm, onCancel }) {
    return (
        <Modal isOpen={isOpen} onClose={onCancel} className="confirm-dialog">
            <p>{message}</p>
            <div className="confirm-dialog-actions">
                <button type="button" onClick={onCancel}>취소</button>
                <button type="button" className="confirm-delete-button" onClick={onConfirm}>삭제</button>
            </div>
        </Modal>
    );
}

export default ConfirmDialog;
