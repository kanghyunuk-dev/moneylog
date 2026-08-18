const BASE_URL = 'http://localhost:8080';

// RefreshToken 으로 새 AccessToken 발급 받는 함수
async function refreshAccessToken() {
    const refreshToken = localStorage.getItem('refreshToken');

    // 1. 백엔드의 /api/auth/refresh 호출
    const response = await fetch(`${BASE_URL}/api/auth/refresh`, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({refreshToken}),
    });

    // 2. RefreshToken 유효하지 않으면(만료/탈취 등) 완전히 로그아웃
    if(!response.ok) {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        throw new Error('세션이 만료 되었습니다');
    }

    // 3. 새 AccessToken을 저장하고 반환
    const data = await response.json();
    localStorage.setItem('accessToken', data.accessToken);
    return data.accessToken;
}

// 모든 API 호출이 거쳐가는 공통 함수
export async function authFetch(path, options = {}) {
    const accessToken = localStorage.getItem('accessToken');

    // 1. 저장된 AccessToken을 헤더에 실어서 요청
    const response = await fetch(`${BASE_URL}${path}`,{
        ...options,
        headers: {
            'Content-Type': 'application/json',
            ...(accessToken ? {Authorization: `Bearer ${accessToken}`} : {}),
            ...options.headers,
        },
    });

    // 2. AccessToken 만료(401)면 자동으로 재발급 받고 원래 요청 재시도
    if(response.status === 401) {
        const newAccessToken = await refreshAccessToken();
        return fetch(`${BASE_URL}${path}`, {
            ...options,
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${newAccessToken}`,
                ...options.headers,
            },
        });
    }

    // 3. 정상 응답이면 그대로 반환
    return response;
}
