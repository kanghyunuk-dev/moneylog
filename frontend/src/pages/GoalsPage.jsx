import { useCallback, useEffect, useState } from 'react';
import './GoalsPage.css';
import { getGoals, createGoal, updateGoal, deleteGoal } from "../api/goal";

function GoalsPage() {
    const [goals, setGoals] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');

    const [editingGoal, setEditingGoal] = useState(null);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [formName, setFormName] = useState('');
    const [formTargetAmount, setFormTargetAmount] = useState('');
    const [formDeadline, setFormDeadline] = useState('');
    const [formError, setFormError] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);

    // 목표 목록 조회
    const refresh = useCallback(() => {
        getGoals()
            .then((data) => setGoals(data))
            .catch((err) => setError(err.message))
            .finally(() => setIsLoading(false));
    }, []);

    useEffect(() => {
        refresh();
    }, [refresh]);

    const activeGoal = goals.find((g) => g.status === 'ACTIVE');
    const waitingGoals = goals.filter((g) => g.status === 'WAITING');
    const doneGoals = goals.filter((g) => g.status === 'DONE');

    // 등록 모달 열기
    function handleAddStart() {
        setEditingGoal(null);
        setFormName('');
        setFormTargetAmount('');
        setFormDeadline('');
        setFormError('');
        setIsModalOpen(true);
    }

    // 수정 모달 열기
    function handleEditStart(goal) {
        setEditingGoal(goal);
        setFormName(goal.name);
        setFormTargetAmount(String(goal.targetAmount));
        setFormDeadline(goal.deadline ?? '');
        setFormError('');
        setIsModalOpen(true);
    }

    function handleModalClose() {
        setIsModalOpen(false);
    }

    async function handleFormSubmit(e) {
        e.preventDefault();
        setFormError('');
        setIsSubmitting(true);

        const payload = {
            name: formName,
            targetAmount: Number(formTargetAmount),
            deadline: formDeadline || null,
        };

        try {
            if (editingGoal) {
                await updateGoal(editingGoal.id, payload);
            } else {
                await createGoal(payload);
            }
            setIsModalOpen(false);
            refresh();
        } catch (err) {
            setFormError(err.message);
        } finally {
            setIsSubmitting(false);
        }
    }

    async function handleDelete() {
        if (!window.confirm('정말 삭제하시겠습니까?')) {
            return;
        }

        setFormError('');
        setIsSubmitting(true);

        try {
            await deleteGoal(editingGoal.id);
            setIsModalOpen(false);
            refresh();
        } catch (err) {
            setFormError(err.message);
        } finally {
            setIsSubmitting(false);
        }
    }

    function renderProgress(goal) {
        const percent = goal.targetAmount === 0 ? 0 : Math.max(0, Math.min(100, Math.round((goal.currentAmount / goal.targetAmount) * 100)));
        return (
            <div className="progress-track">
                <div className="progress-fill" style={{ width: `${percent}%` }} />
            </div>
        );
    }

    return (
        <div className="goals-page">
            <div className="goals-header">
                <div>
                    <h1>목표자산</h1>
                    <p>목표를 세우고 진행률을 추적하세요</p>
                </div>
                <button type="button" className="add-button" onClick={handleAddStart}>+ 목표 추가</button>
            </div>

            {error && <p className="error-message">{error}</p>}

            {isLoading ? (
                <p>불러오는중 ...</p>
            ) : (
                <>
                    {activeGoal && (
                        <div className="active-goal-card" onClick={() => handleEditStart(activeGoal)}>
                            <span className="active-badge">진행 중인 목표</span>
                            <h2>{activeGoal.name}</h2>
                            <p className="goal-date">{activeGoal.activatedAt.slice(0, 10)} 시작</p>
                            {renderProgress(activeGoal)}
                            <div className="active-goal-footer">
                                <span>{activeGoal.currentAmount.toLocaleString()}원</span>
                                <span>목표 {activeGoal.targetAmount.toLocaleString()}원</span>
                            </div>
                        </div>
                    )}

                    {goals.length === 0 && <p className="empty-message">등록된 목표가 없습니다</p>}

                    {waitingGoals.length > 0 && (
                        <div className="goal-section">
                            <h3>대기 중</h3>
                            {waitingGoals.map((g) => (
                                <div key={g.id} className="goal-item" onClick={() => handleEditStart(g)}>
                                    <span>{g.name}</span>
                                    <span>{g.targetAmount.toLocaleString()}원</span>
                                </div>
                            ))}
                        </div>
                    )}

                    {doneGoals.length > 0 && (
                        <div className="goal-section">
                            <h3>완료됨</h3>
                            {doneGoals.map((g) => (
                                <div key={g.id} className="goal-item done" onClick={() => handleEditStart(g)}>
                                    <span>✓ {g.name}</span>
                                    <span>{g.targetAmount.toLocaleString()}원</span>
                                </div>
                            ))}
                        </div>
                    )}
                </>
            )}

            {isModalOpen && (
                <div className="modal-overlay">
                    <form className="modal-content" onSubmit={handleFormSubmit}>
                        <h2>{editingGoal?.status === 'DONE' ? '완료된 목표' : editingGoal ? '목표 수정' : '목표 추가'}</h2>

                        <label>목표명</label>
                        <input type="text" placeholder="예: 내 집 마련" value={formName} onChange={(e) => setFormName(e.target.value)} disabled={editingGoal?.status === 'DONE'} required />

                        <label>목표 금액</label>
                        <input type="number" placeholder="목표 금액 입력" value={formTargetAmount} onChange={(e) => setFormTargetAmount(e.target.value)} disabled={editingGoal?.status === 'DONE'} required />

                        <label>마감일 (선택)</label>
                        <input type="date" value={formDeadline} onChange={(e) => setFormDeadline(e.target.value)} disabled={editingGoal?.status === 'DONE'} />

                        {formError && <p className="error-message">{formError}</p>}

                        <div className="modal-actions">
                            {editingGoal && (
                                <button type="button" className="delete-button" onClick={handleDelete} disabled={isSubmitting}>
                                    {isSubmitting ? '삭제 중 ...' : '삭제'}
                                </button>
                            )}
                            <button type="button" onClick={handleModalClose} disabled={isSubmitting}>취소</button>
                            {editingGoal?.status !== 'DONE' && (
                                <button type="submit" disabled={isSubmitting}>
                                    {isSubmitting ? '저장 중...' : '저장'}
                                </button>
                            )}
                        </div>
                    </form>
                </div>
            )}
        </div>
    );
}

export default GoalsPage;
