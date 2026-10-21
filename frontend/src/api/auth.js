import { getCookie } from "./cookie";

const BASE_URL = import.meta.env.VITE_API_BASE_URL || '';

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

// 둘러보기 요청 (체험 계정 생성 + 토큰 쿠키 저장)
export async function demoLoginRequest() {
    const response = await fetch(`${BASE_URL}/api/auth/demo`, {
        method: 'POST',
        headers: {
            'X-XSRF-TOKEN': getCookie('XSRF-TOKEN'),
        },
        credentials: 'include',
    });

    if (!response.ok) {
        // 429 - Nginx(JSON 아님)·서버 한도 공통 안내
        if (response.status === 429) {
            throw new Error('요청이 많습니다. 잠시 후 다시 시도해주세요');
        }
        const data = await response.json().catch(() => null);
        throw new Error(data?.message || '체험 계정을 만들지 못했습니다. 잠시 후 다시 시도해주세요');
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