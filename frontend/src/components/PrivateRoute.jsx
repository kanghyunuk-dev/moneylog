import { Navigate, useLocation } from "react-router";
import { useAuth } from "../context/useAuth";

function PrivateRoute({children}) {
    const {isLoggedIn, isLoading} = useAuth();
    const location = useLocation();

    // 서버 로그인 상태 확인 대기
    if(isLoading) {
        return <p role="status">불러오는 중...</p>;
    }

    if (!isLoggedIn) {
        return <Navigate to="/login" state={{ from: location }} replace />;
    }

    return children;
}

export default PrivateRoute;