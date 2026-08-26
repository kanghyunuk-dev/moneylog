// 쿠키에서 이름으로 값을 꺼내는 함수 (CSRF 토큰 등 자바스크립트가 읽어야 하는 쿠키에 사용)
export function getCookie(name) {
    const match = document.cookie.match(new RegExp('(^| )' + name + '=([^;]+)'));
    return match ? match[2] : null;
}