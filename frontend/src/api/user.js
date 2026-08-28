import { authFetch } from "./authFetch";

// 내정보 조회
export async function getMyInfo() {
    const response = await authFetch('/api/users/me');

    if(!response.ok) {
        throw new Error('내 정보를 불러오지 못했습니다');
    }

    return response.json();
}

// 닉네임 수정
export async function updateNickname(nickname) {
    const response = await authFetch('/api/users/me', {
        method: 'PUT',
        body: JSON.stringify({nickname}),
    });

    if(!response.ok) {
        const data = await response.json();
        throw new Error(data.message);
    }
}

// 비밀번호 변경
export async function updatePassword(currentPassword, newPassword) {
    const response = await authFetch('/api/users/me/password', {
        method: 'PUT',
        body: JSON.stringify({currentPassword, newPassword}),
    });

    if(!response.ok) {
        const data = await response.json();
        throw new Error(data.message);
    }
}