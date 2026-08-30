import { getCookie } from "./cookie";

const BASE_URL = import.meta.env.VITE_API_BASE_URL;

// 로그인요청 (httpOnly 쿠키로 토큰 요청)
export async function loginRequest(email, password) {
    const response = await fetch(`${BASE_URL}/api/auth/login`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-XSRF-TOKEN': getCookie('XSRF-TOKEN'),
        },
        credentials: 'include',
        body: JSON.stringify({email,password}),
    });

    // 실패 시
    if (!response.ok) {
        const data = await response.json();
        throw new Error(data.message);
    }
}

// 회원가입 요청
export async function registerRequest(email, password, nickname) {
    const response = await fetch(`${BASE_URL}/api/auth/register`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-XSRF-TOKEN': getCookie('XSRF-TOKEN'),
        },
        credentials: 'include',
        body: JSON.stringify({email, password, nickname}),
    });

    if (!response.ok) {
        const data = await response.json();
        throw new Error(data.message);
    }

    return response.json();
}