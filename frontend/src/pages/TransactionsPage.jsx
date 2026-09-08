import { useCallback, useEffect, useState } from 'react';
import './TransactionsPage.css';
import { getTransactions, createTransaction, updateTransaction, deleteTransaction } from "../api/transaction";
import { getCategories } from "../api/category";

function TransactionsPage() {
    const now = new Date();

    // 현재 보고 있는 년-월
    const [year, setYear] = useState(now.getFullYear());
    const [month, setMonth] = useState(now.getMonth() + 1);

    const [transactions, setTransactions] = useState([]);
    const [categories, setCategories] = useState([]);
    const [isCategoriesLoading, setIsCategoriesLoading] = useState(true);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');

    // month 문자열 (yyyy-MM 형태, API 파라미터용)
    const monthParam = `${year}-${String(month).padStart(2, '0')}`;

    // 거래 목록 날짜별 그룹핑
    const groupedTransactions = groupByDate(transactions);

    // 등록/수정 모달
    const [editingTransaction, setEditingTransaction] = useState(null); // null=등록, 객체=수정
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [formCategoryId, setFormCategoryId] = useState('');
    const [formAmount, setFormAmount] = useState('');
    const [formDate, setFormDate] = useState('');
    const [formMemo, setFormMemo] = useState('');
    const [formError, setFormError] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);

    // 거래 목록 조회
    const refreshTransactions = useCallback(() => {
        getTransactions(monthParam)
            .then((data) => setTransactions(data))
            .catch((err) => setError(err.message))
            .finally(() => setIsLoading(false));
    }, [monthParam]);

    // 거래 목록 조회 (year, month 바뀔 때마다 재조회)
    useEffect(() => {
        refreshTransactions();
    }, [refreshTransactions]);

    // 카테고리 목록은 한 번만 조회
    useEffect(() => {
        getCategories()
            .then((data) => setCategories(data))
            .catch(() => setCategories([]))
            .finally(() => setIsCategoriesLoading(false));
    }, [])

    // 이전/다음 달 이동
    function handlePrevMonth() {
        if(month === 1) {
            setYear(year - 1);
            setMonth(12);
        } else {
            setMonth(month - 1);
        }
    }

    function handleNextMonth() {
        if(month === 12) {
            setYear(year + 1);
            setMonth(1);
        } else {
            setMonth(month + 1);
        }
    }

    // 등록 모달 열기
    function handleAddStart() {
        setEditingTransaction(null);
        setFormCategoryId('');
        setFormAmount('');
        setFormDate('');
        setFormMemo('');
        setFormError('');
        setIsModalOpen(true);
    }

    // 수정 모달 열기 (기존 값 채움)
    function handleEditStart(transaction) {
        setEditingTransaction(transaction);
        setFormCategoryId(String(transaction.categoryId));
        setFormAmount(String(transaction.amount));
        setFormDate(transaction.transactionDate);
        setFormMemo(transaction.memo ?? '');
        setFormError('');
        setIsModalOpen(true);
    }

    // 모달 닫기
    function handleModalClose() {
        setIsModalOpen(false);
    }

    // 등록/수정 제출
    async function handleFormSubmit(e) {
        e.preventDefault();
        setFormError('');
        setIsSubmitting(true);

        const payload = {
            categoryId: Number(formCategoryId),
            amount: Number(formAmount),
            transactionDate: formDate,
            memo: formMemo,
        };

        try {
            if (editingTransaction) {
                await updateTransaction(editingTransaction.id, payload);
            } else {
                await createTransaction(payload);
            }
            setIsModalOpen(false);
            // 목록 새로고침을 위해 현재 월을 다시 조회
            refreshTransactions();
        } catch (err) {
            setFormError(err.message);
        } finally {
            setIsSubmitting(false);
        }
    }

    // 거래 삭제
    async function handleDelete() {
        if(!window.confirm('정말 삭제하시겠습니까?')) {
            return;
        }

        setFormError('');
        setIsSubmitting(true);

        try {
            await deleteTransaction(editingTransaction.id);
            setIsModalOpen(false);
            refreshTransactions();
        } catch (error) {
            setFormError(error.message);
        } finally {
            setIsSubmitting(false);
        }
    }

    return (
        <div className="transactions-page">
            <div className="transactions-header">
                <div>
                    <h1>거래내역</h1>
                    <p>수입과 지출을 한눈에</p>
                </div>
                <button type="button" className="add-button" onClick={handleAddStart} disabled={isCategoriesLoading}>
                    {isCategoriesLoading ? '불러오는 중...' : '+ 거래 추가'}
                </button>
            </div>

            <div className="month-nav">
                <button type="button" onClick={handlePrevMonth}>‹</button>
                <span>{year}년 {month}월</span>
                <button type="button" onClick={handleNextMonth}>›</button>
            </div>

            {error && <p className='error-message'>{error}</p>}

            {isLoading ? (
                <p>불러오는중 ...</p>
            ) : (
                <div className="transaction-list">
                    {groupedTransactions.length === 0 && <p className='empty-message'>이 달의 내역이 없습니다</p>}
                    {groupedTransactions.map((group) => (
                        <div key={group.date} className="transaction-group">
                            <div className="group-date">{group.date}</div>
                            {group.items.map((item) => (
                                <div key={item.id} className="transaction-item" onClick={() => handleEditStart(item)}>
                                    <div>
                                        <div className="item-memo">{item.memo || item.categoryName}</div>
                                        <div className="item-category">{item.categoryName}</div>
                                    </div>
                                    <div className={item.categoryType === 'INCOME' ? 'item-amount income' : 'item-amount expense'}>
                                        {item.categoryType === 'INCOME' ? '+' : '-'}{item.amount.toLocaleString()}
                                    </div>
                                </div>
                            ))}
                        </div>
                    ))}
                </div>
            )}

            {isModalOpen && (
                <div className="modal-overlay">
                    <form className="modal-content" onSubmit={handleFormSubmit}>
                        <h2>{editingTransaction ? '거래 수정' : '거래 추가'}</h2>

                        <label>카테고리</label>
                        <select value={formCategoryId} onChange={(e) => setFormCategoryId(e.target.value)} required>
                            <option value="">선택하세요</option>
                            {categories.map((c) => (
                                <option key={c.id} value={c.id}>
                                    {c.type === 'INCOME' ? '수입' : '지출'} · {c.name}
                                </option>
                            ))}
                        </select>

                        <label>금액</label>
                        <input type="number" placeholder="금액 입력" value={formAmount} onChange={(e) => setFormAmount(e.target.value)} required />

                        <label>날짜</label>
                        <input type="date" value={formDate} onChange={(e) => setFormDate(e.target.value)} required />

                        <label>메모</label>
                        <input type="text" placeholder="메모 (선택)" value={formMemo} onChange={(e) => setFormMemo(e.target.value)} />

                        {formError && <p className="error-message">{formError}</p>}

                        <div className="modal-actions">
                            {editingTransaction && (
                                <button type='button' className='delete-button' onClick={handleDelete} disabled={isSubmitting}>
                                    {isSubmitting ? '삭제 중 ...' : '삭제'}
                                </button>
                            )}
                            <button type="button" onClick={handleModalClose} disabled={isSubmitting}>취소</button>
                            <button type="submit" disabled={isSubmitting}>
                                {isSubmitting ? '저장 중...' : '저장'}
                            </button>
                        </div>
                    </form>
                </div>
            )}     
        </div>
    );
}

// transactionDate("2026-09-01") 기준으로 그룹핑
function groupByDate(transactions) {
    const groups = new Map();

    for(const t of transactions) {
        if(!groups.has(t.transactionDate)) {
            groups.set(t.transactionDate, []);
        }
        groups.get(t.transactionDate).push(t);
    }

    return Array.from(groups, ([date, items]) => ({date, items}));
}

export default TransactionsPage;
