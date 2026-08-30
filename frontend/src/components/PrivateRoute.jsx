import { Navigate } from "react-router";
import { useAuth } from "../context/useAuth";

function PrivateRoute({children}) {
    const {isLoggedIn, isLoading} = useAuth();

    // 서버 로그인 상태 확인 대기
    if(isLoading) {
        return null;
    }

    if (!isLoggedIn) {
        return <Navigate to="/login" replace />;
    }

    return children;
}

export default PrivateRoute;