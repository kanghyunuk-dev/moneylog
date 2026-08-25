import { useContext } from "react";
import { AuthContext } from "./AuthContext";

// 다른 컴포넌트에서 로그인 상태를 꺼내 쓸 때 사용하는 함수
export function useAuth() {
    return useContext(AuthContext);
}