import { authFetch } from "./authFetch";

// 예산 목록 조회(월 단위)
export async function getBudgets(month) {
    const response = await authFetch(`/api/budgets?month=${month}`);

    if(!response.ok) {
        throw new Error("예산 목록을 불러오지 못했습니다");
    }

    return response.json();
}

// 예산 등록
export async function createBudget(budget) {
    const response = await authFetch(`/api/budgets`, {
        method: 'POST',
        body: JSON.stringify(budget),
    });

    if(!response.ok) {
        const data = await response.json();
        throw new Error(data.message);
    }

    return response.json();
}

// 예산 수정
export async function updateBudget(id, budget) {
    const response = await authFetch(`/api/budgets/${id}`, {
        method: 'PUT',
        body: JSON.stringify(budget),
    });

    if(!response.ok) {
        const data = await response.json();
        throw new Error(data.message);
    }

    return response.json();
}

// 예산 삭제
export async function deleteBudget(id) {
    const response = await authFetch(`/api/budgets/${id}`, {
        method: 'DELETE',
    });

    if(!response.ok) {
        const data = await response.json();
        throw new Error(data.message);
    }
}