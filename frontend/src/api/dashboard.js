import { authFetch } from "./authFetch";

export async function getSummary(month, signal) {
    const response = await authFetch(`/api/dashboard/summary?month=${month}`, { signal });
    if (!response.ok) {
        throw new Error('요약 정보를 불러오지 못했습니다');
    }
    return response.json();
}

export async function getCategoryBreakdown(month, signal) {
    const response = await authFetch(`/api/dashboard/category-breakdown?month=${month}`, { signal });
    if (!response.ok) {
        throw new Error('카테고리별 지출을 불러오지 못했습니다');
    }
    return response.json();
}

export async function getMonthlyTrend(month, months, signal) {
     const response = await authFetch(`/api/dashboard/monthly-trend?month=${month}&months=${months}`, { signal });
    if (!response.ok) {
        throw new Error('월별 추이를 불러오지 못했습니다');
    }
    return response.json();
}
