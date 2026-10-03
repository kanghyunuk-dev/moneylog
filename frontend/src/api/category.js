import { authFetch } from "./authFetch";

// 카테고리 목록 조회
export async function getCategories() {
    const response = await authFetch('/api/categories');

    if(!response.ok) {
        throw new Error('카테고리를 불러오지 못했습니다');
    }

    return response.json();
}