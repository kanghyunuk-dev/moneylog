import './Sidebar.css';
import { NavLink, useNavigate } from "react-router";
import { useAuth } from "../context/useAuth";

function Sidebar() {
    
    const {logout} = useAuth();
    const navigate = useNavigate();

    async function handleLogout() {
        await logout();
        navigate('/login');
    }

    return (
        <aside className="sidebar">
            <div className="sidebar-logo">
                <span className="logo-badge">M</span>
                <span>MoneyLog</span>
            </div>

            <nav className="sidebar-nav">
                <NavLink to="/" end>홈</NavLink>
                <NavLink to="/transactions">거래내역</NavLink>
                <NavLink to="/budgets">예산</NavLink>
                <NavLink to="/dashboard">통계</NavLink>
                <NavLink to="/goals">목표자산</NavLink>
                <NavLink to="/mypage">마이페이지</NavLink>
            </nav>

            <button type="button" className="sidebar-logout" onClick={handleLogout}>로그아웃</button>
        </aside>
    )

}

export default Sidebar;