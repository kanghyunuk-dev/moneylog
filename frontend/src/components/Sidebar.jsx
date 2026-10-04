import './Sidebar.css';
import { useEffect } from "react";
import { Link, NavLink, useNavigate } from "react-router";
import { useAuth } from "../context/useAuth";

function Sidebar({ isOpen, onClose }) {

    const {logout} = useAuth();
    const navigate = useNavigate();

    // 모바일 드로어가 열려있는 동안만 ESC로 닫기 
    useEffect(() => {
        if (!isOpen) return;

        function handleKeyDown(e) {
            if (e.key === 'Escape') onClose();
        }

        document.addEventListener('keydown', handleKeyDown);
        return () => document.removeEventListener('keydown', handleKeyDown);
    }, [isOpen, onClose]);

    async function handleLogout() {
        onClose();
        navigate('/login', { replace: true, state: null });
        try {
            await logout();
        } catch {
            alert('로그아웃에 실패했습니다. 잠시 후 다시 시도해주세요.');
        }
    }

    return (
        <>
            {/* 모바일에서 사이드바가 열려있을 때 배경 - 클릭하면 닫힘 */}
            <div className={isOpen ? 'sidebar-backdrop open' : 'sidebar-backdrop'} onClick={onClose} />

            <aside className={isOpen ? 'sidebar open' : 'sidebar'}>
                <div className="sidebar-top">
                    <Link to="/" className="sidebar-logo" onClick={onClose}>
                        <span className="logo-badge">M</span>
                        <span>MoneyLog</span>
                    </Link>
                    <button type="button" className="sidebar-close" onClick={onClose} aria-label="메뉴 닫기">✕</button>
                </div>

                <nav className="sidebar-nav" onClick={onClose}>
                    <NavLink to="/" end>홈</NavLink>
                    <NavLink to="/transactions">거래내역</NavLink>
                    <NavLink to="/budgets">예산</NavLink>
                    <NavLink to="/dashboard">통계</NavLink>
                    <NavLink to="/goals">목표자산</NavLink>
                    <NavLink to="/mypage">마이페이지</NavLink>
                </nav>

                <button type="button" className="sidebar-logout" onClick={handleLogout}>로그아웃</button>
            </aside>
        </>
    )

}

export default Sidebar;
