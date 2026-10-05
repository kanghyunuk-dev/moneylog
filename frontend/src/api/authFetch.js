import { getCookie } from "./cookie";

const BASE_URL = import.meta.env.VITE_API_BASE_URL || '';

// 인증된 요청 공통 옵션(쿠키 전송 + CSRF 헤더)을 만드는 함수
function buildRequestOptions(options) {
    return {
        ...options,
        credentials: 'include',
        headers: {
            'Content-Type': 'application/json',
            'X-XSRF-TOKEN': getCookie('XSRF-TOKEN'),
            ...options.headers,
        },
    };
}

// 진행 중인 refresh 요청을 저장해두는 변수 (모듈 스코프 - 이 파일이 로드되는 동안 계속 유지됨)
let refreshPromise = null;

// RefreshToken(쿠키방식) 으로 새 AccessToken 발급 받는 함수
function refreshAccessToken() {
    // (이미 진행 중인 refresh 있는 경우) 새로 요청 안 보내고 결과 기다림
    if (refreshPromise) {
        return refreshPromise;
    }

    // 1. 백엔드의 /api/auth/refresh 호출 (refreshToken 은 쿠키로 자동 전송)
    //    fetch 체인 자체(아직 안 끝난 프로미스)를 refreshPromise에 저장 - 다른 요청이 같이 기다릴 수 있도록
    refreshPromise = fetch(`${BASE_URL}/api/auth/refresh`, {
        method: 'POST',
        credentials: "include",
        headers: {
            'X-XSRF-TOKEN': getCookie('XSRF-TOKEN'),
        },
    })
        .then((response) => response.ok)
        .finally(() => {
            // 끝나면(성공이든 실패든) 초기화 - 다음 번 401 다시 새로 요청
            refreshPromise = null;
        });

    // 2. 성공 여부만 반환 (실패 시 예외 던지지 않음 - 호출부가 401 응답으로 처리)
    return refreshPromise;
}

// 모든 API 호출이 거쳐가는 공통 함수
export async function authFetch(path, options = {}) {
    // 1. 쿠키방식 - CSRF 헤더만 추가, acceessToken 은 브라우저가 자동 실어 보냄
    const response = await fetch(`${BASE_URL}${path}`, buildRequestOptions(options));

    // 2. AccessToken 이 401(errorCode: TOKEN_INVALID)인 경우만 재발급 받고 원래 요청 재시도
    if(response.status === 401) {
        // 복사본 으로 errorCode만 확인
        const cloned = response.clone();
        const data = await cloned.json().catch(()=>null);

        // 토큰 문제로 인한 401인 경우 만 refresh 시도
        if(data?.errorCode !== 'TOKEN_INVALID') {
            return response;
        }

        const refreshed = await refreshAccessToken();
        
        // RefreshToken 도 만료/무효 시 - 재시도 하지 않고 원래 401 응답 반환
        if(!refreshed) {
            return response;
        }

        return fetch(`${BASE_URL}${path}`, buildRequestOptions(options));
    }

    // 3. 정상 응답이면 그대로 반환
    return response;
}
