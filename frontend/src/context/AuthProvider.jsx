import { useState, useEffect } from "react";
import { AuthContext } from "./AuthContext";
import { authFetch } from "../api/authFetch";

// 앱 전체를 감싸서, 하위 컴포넌트 어디서든 로그인 상태를 쓸 수 있게 해주는 컴포넌트
export function AuthProvider({children}) {
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const [isLoading, setIsLoading] = useState(true);

    // 컴포넌트 처음 렌더링 - 서버에 로그인 상태 확인
    useEffect(()=>{
        authFetch('/api/auth/me')
            .then((response) => setIsLoggedIn(response.ok))
            .catch(() => setIsLoggedIn(false))
            .finally(() => setIsLoading(false));
    }, []);
    
    // 로그인 성공 시 호출
    function login() {
        setIsLoggedIn(true);
    }

    // 로그아웃 시 호출
    async function logout() {
        await authFetch('/api/auth/logout', { method: 'POST' });
        setIsLoggedIn(false);
    }

    // 하위 컴포넌트에 전달할 값들을 하나로 묶음
    const value = {
        isLoggedIn,
        isLoading,
        login,
        logout,
    };

    // children(이 Provider로 감싸진 다른 컴포넌트들) 에게 value 를 전달
    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;

}