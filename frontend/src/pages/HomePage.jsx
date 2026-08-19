import { useNavigate } from "react-router";
import { useAuth } from "../context/AuthContext";

function HomePage() {
    const {logout} = useAuth();
    const navigate = useNavigate();

    function handleLogout() {
        logout();
        navigate('/login');
    }

    return (
        <div className="home-page">
            <h1>MoneyLog</h1>
            <p>로그인 되었습니다. 대시보드는 준비 중입니다.</p>
            <button onClick={handleLogout}>로그아웃</button>
        </div>
    );
}

export default HomePage;