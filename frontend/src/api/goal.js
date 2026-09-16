import { authFetch } from "./authFetch";

export async function getGoals() {
    const response = await authFetch(`/api/goals`);
    if (!response.ok) {
        throw new Error('목표 목록을 불러오지 못했습니다');
    }
    return response.json();
}

export async function createGoal(goal) {
    const response = await authFetch(`/api/goals`, {
        method: 'POST',
        body: JSON.stringify(goal),
    });
    if (!response.ok) {
        const data = await response.json();
        throw new Error(data.message);
    }
    return response.json();
}

export async function updateGoal(id, goal) {
    const response = await authFetch(`/api/goals/${id}`, {
        method: 'PUT',
        body: JSON.stringify(goal),
    });
    if (!response.ok) {
        const data = await response.json();
        throw new Error(data.message);
    }
    return response.json();
}

export async function deleteGoal(id) {
    const response = await authFetch(`/api/goals/${id}`, {
        method: 'DELETE',
    });
    if (!response.ok) {
        const data = await response.json();
        throw new Error(data.message);
    }
}
