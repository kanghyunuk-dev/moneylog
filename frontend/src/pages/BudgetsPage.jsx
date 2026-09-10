import './BudgetsPage.css';
import { useCallback, useEffect, useState } from 'react';
import { getBudgets, createBudget, updateBudget, deleteBudget } from "../api/budget";
import { getCategories } from "../api/category";

function BudgetsPage() {
    const now = new Date();

    // 현재 보고 있는 년-월
    const [year, setYear] = useState(now.getFullYear());
    const [month, setMonth] = useState(now.getMonth() + 1);

    const [budgets, setBudgets] = useState([]);
    const [categories, setCategories] = useState([]);
    const [isCategoriesLoading, setIsCategoriesLoading] = useState(true);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');

    // month 문자열 (yyyy-MM 형태, API 파라미터용)
    const monthParam = `${year}-${String(month).padStart(2, '0')}`;

    // 상단 요약 카드용 합계 계산
    const totalBudget = budgets.reduce((sum, b) => sum + b.amount, 0);
    const totalSpent = budgets.reduce((sum, b) => sum + b.spentAmount, 0);
    const overCount = budgets.filter((b) => b.spentAmount > b.amount).length;

    // 등록/수정 모달
    const [editingBudget, setEditingBudget] = useState(null); // null=등록, 객체=수정
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [formCategoryId, setFormCategoryId] = useState('');
    const [formAmount, setFormAmount] = useState('');
    const [formError, setFormError] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);

    // 예산 목록 조회
    const refreshBudgets = useCallback(() => {
        getBudgets(monthParam)
            .then((data) => setBudgets(data))
            .catch((err) => setError(err.message))
            .finally(() => setIsLoading(false));
    }, [monthParam]);

    // 예산 목록 조회 (year, month 바뀔 때마다 재조회)
    useEffect(() => {
        refreshBudgets();
    }, [refreshBudgets]);

    // 카테고리 목록은 한 번만 조회
    useEffect(() => {
        getCategories()
            .then((data) => setCategories(data))
            .catch(() => setCategories([]))
            .finally(() => setIsCategoriesLoading(false));
    }, []);

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
        setEditingBudget(null);
        setFormCategoryId('');
        setFormAmount('');
        setFormError('');
        setIsModalOpen(true);
    }

    // 수정 모달 열기 (기존 값 채움, 카테고리는 수정 불가)
    function handleEditStart(budget) {
        setEditingBudget(budget);
        setFormCategoryId(String(budget.categoryId));
        setFormAmount(String(budget.amount));
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

        try {
            if (editingBudget) {
                await updateBudget(editingBudget.id, { amount: Number(formAmount) });
            } else {
                await createBudget({
                    categoryId: Number(formCategoryId),
                    budgetMonth: monthParam,
                    amount: Number(formAmount),
                });
            }
            setIsModalOpen(false);
            refreshBudgets();
        } catch (err) {
            setFormError(err.message);
        } finally {
            setIsSubmitting(false);
        }
    }

    // 예산 삭제
    async function handleDelete() {
        if (!window.confirm('정말 삭제하시겠습니까?')) {
            return;
        }

        setFormError('');
        setIsSubmitting(true);

        try {
            await deleteBudget(editingBudget.id);
            setIsModalOpen(false);
            refreshBudgets();
        } catch (err) {
            setFormError(err.message);
        } finally {
            setIsSubmitting(false);
        }
    }

    return (
        <div className="budgets-page">
            <div className="budgets-header">
                <div>
                    <h1>예산 관리</h1>
                    <p>카테고리별 예산을 설정하고 지출을 관리하세요</p>
                </div>
                <button type="button" className="add-button" onClick={handleAddStart} disabled={isCategoriesLoading}>
                    {isCategoriesLoading ? '불러오는 중...' : '+ 예산 추가'}
                </button>
            </div>

            <div className="month-nav">
                <button type="button" onClick={handlePrevMonth}>‹</button>
                <span>{year}년 {month}월</span>
                <button type="button" onClick={handleNextMonth}>›</button>
            </div>

            {error && <p className='error-message'>{error}</p>}

            {/* 상단 요약 카드 3개 */}
            <div className="summary-cards">
                <div className="summary-card">
                    <span className="summary-label">총 예산</span>
                    <span className="summary-value">{totalBudget.toLocaleString()}원</span>
                </div>
                <div className="summary-card">
                    <span className="summary-label">총 지출</span>
                    <span className="summary-value">{totalSpent.toLocaleString()}원</span>
                </div>
                <div className="summary-card">
                    <span className="summary-label">초과 항목</span>
                    <span className={overCount > 0 ? 'summary-value over' : 'summary-value'}>{overCount}개 카테고리</span>
                </div>
            </div>

            {isLoading ? (
                <p>불러오는중 ...</p>
            ) : (
                <div className="budget-list">
                    {budgets.length === 0 && <p className='empty-message'>이 달에 설정된 예산이 없습니다</p>}
                    {budgets.map((b) => {
                        // 예산 대비 지출 퍼센트 계산 (표시는 100%에서 잘라내고 텍스트로 실제 초과율 안내)
                        const percent = b.amount === 0 ? 0 : Math.round((b.spentAmount / b.amount) * 100);
                        const isOver = b.spentAmount > b.amount;
                        return (
                            <div key={b.id} className="budget-item" onClick={() => handleEditStart(b)}>
                                <div className="budget-item-header">
                                    <span className="budget-category">
                                        {b.categoryName}
                                        {isOver && <span className="over-badge">초과</span>}
                                    </span>
                                    <span className={isOver ? 'budget-amount over' : 'budget-amount'}>
                                        {b.spentAmount.toLocaleString()} / {b.amount.toLocaleString()}원 ({percent}%)
                                    </span>
                                </div>
                                <div className="progress-track">
                                    <div
                                        className={isOver ? 'progress-fill over' : 'progress-fill'}
                                        style={{ width: `${Math.min(percent, 100)}%` }}
                                    />
                                </div>
                            </div>
                        );
                    })}
                </div>
            )}

            {/* 등록/수정 통합 모달 */}
            {isModalOpen && (
                <div className="modal-overlay">
                    <form className="modal-content" onSubmit={handleFormSubmit}>
                        <h2>{editingBudget ? '예산 수정' : '예산 추가'}</h2>

                        <label>카테고리</label>
                        <select value={formCategoryId} onChange={(e) => setFormCategoryId(e.target.value)} disabled={!!editingBudget} required>
                            <option value="">선택하세요</option>
                            {categories.filter((c) => c.type === 'EXPENSE').map((c) => (
                                <option key={c.id} value={c.id}>{c.name}</option>
                            ))}
                        </select>

                        <label>금액</label>
                        <input type="number" placeholder="예산 금액 입력" value={formAmount} onChange={(e) => setFormAmount(e.target.value)} required />

                        {formError && <p className="error-message">{formError}</p>}

                        <div className="modal-actions">
                            {editingBudget && (
                                <button type="button" className="delete-button" onClick={handleDelete} disabled={isSubmitting}>
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

export default BudgetsPage;