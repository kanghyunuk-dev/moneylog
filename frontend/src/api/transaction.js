import { authFetch } from "./authFetch";

// 거래 목록 조회 (월 단위)
export async function getTransactions(month) {
    const response = await authFetch(`/api/transactions?month=${month}`);
    
    if(!response.ok) {
        throw new Error('거래 내역을 불러오지 못했습니다');
    }
    
    return response.json();
}

// 거래 등록
export async function createTransaction(transaction) {
    const response = await authFetch(`/api/transactions`, {
        method: 'POST',
        body: JSON.stringify(transaction),
    });

    if(!response.ok) {
        const data = await response.json();
        throw new Error(data.message);
    }
    
    return response.json();
}

// 거래 수정
export async function updateTransaction(id, transaction) {
    const response = await authFetch(`/api/transactions/${id}`, {
        method: 'PUT',
        body: JSON.stringify(transaction),
    });
    
    if(!response.ok) {
        const data = await response.json();
        throw new Error(data.message);
    }

    return response.json();
}

// 거래 삭제
export async function deleteTransaction(id) {
    const response = await authFetch(`/api/transactions/${id}`, {
        method: 'DELETE',
    });
    
    if(!response.ok) {
        const data = await response.json();
        throw new Error(data.message);
    }
}