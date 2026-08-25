import { Navigate } from "react-router";
import { useAuth } from "../context/useAuth";

function PrivateRoute({children}) {
    const {isLoggedIn} = useAuth();

    if (!isLoggedIn) {
        return <Navigate to="/login" replace />;
    }

    return children;
}

export default PrivateRoute;