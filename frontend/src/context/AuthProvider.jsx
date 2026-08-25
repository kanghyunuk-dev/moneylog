import { useState } from "react";
import { AuthContext } from "./AuthContext";

// 앱 전체를 감싸서, 하위 컴포넌트 어디서든 로그인 상태를 쓸 수 있게 해주는 컴포넌트
export function AuthProvider({children}) {
    // 초기값
    const [accessToken, setAccessToken] = useState(localStorage.getItem('accessToken'));
    
    // 로그인 성공 시 호출
    function login(accessToken, refreshToken) {
        localStorage.setItem('accessToken', accessToken);
        localStorage.setItem('refreshToken', refreshToken);
        setAccessToken(accessToken);
    }

    // 로그아웃 시 호출
    function logout() {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        setAccessToken(null);
    }

    // 하위 컴포넌트에 전달할 값들을 하나로 묶음
    const value = {
        isLoggedIn: !!accessToken,
        login,
        logout,
    };

    // children(이 Provider로 감싸진 다른 컴포넌트들) 에게 value 를 전달
    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;

}