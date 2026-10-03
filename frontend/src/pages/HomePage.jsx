import { useEffect, useState } from "react";
import { Link } from "react-router";
import './HomePage.css';
import { getMyInfo } from "../api/user";
import { getSummary } from "../api/dashboard";
import { getTransactions } from "../api/transaction";
import { getGoals } from "../api/goal";

function HomePage() {
    const [nickname, setNickname] = useState('');
    const [summary, setSummary] = useState(null);
    const [recentTransactions, setRecentTransactions] = useState([]);
    const [activeGoal, setActiveGoal] = useState(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
    const now = new Date();
    const monthParam = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;

    Promise.all([getMyInfo(), getSummary(monthParam), getTransactions(monthParam), getGoals()])
        .then(([userInfo, summaryData, transactions, goals]) => {
            setNickname(userInfo.nickname);
            setSummary(summaryData);
            setRecentTransactions(
                [...transactions]
                    .sort((a, b) => b.transactionDate.localeCompare(a.transactionDate))
                    .slice(0, 5)
            );
            setActiveGoal(goals.find((g) => g.status === 'ACTIVE') ?? null);
        })
        .catch((err) => setError(err.message))
        .finally(() => setIsLoading(false));
    }, []);

    return (
        <div className="home-page">
            {error && <p className="error-message">{error}</p>}

            {isLoading ? (
                <p>불러오는중 ...</p>
            ) : (
                <>
                    <p className="home-greeting-sub">안녕하세요</p>
                    <h1 className="home-greeting-name">{nickname}님 👋</h1>

                    {activeGoal ? (
                        <Link to="/goals" className="home-goal-card">
                            <div className="home-goal-header">
                                <span className="home-goal-label">진행 중인 목표</span>
                                <span className="home-goal-percent">
                                    {calcGoalPercent(activeGoal)}%
                                </span>
                            </div>
                            <h2>{activeGoal.name}</h2>
                            <div className="home-progress-track">
                                <div
                                    className="home-progress-fill"
                                    style={{ width: `${calcGoalPercent(activeGoal)}%` }}
                                />
                            </div>
                            <div className="home-goal-footer">
                                <span>{activeGoal.currentAmount.toLocaleString()}원</span>
                                <span>목표 {activeGoal.targetAmount.toLocaleString()}원</span>
                            </div>
                        </Link>
                    ) : (
                        <Link to="/goals" className="home-goal-empty">
                            <span>자산 목표 설정하기</span>
                        </Link>
                    )}

                    <div className="home-summary-cards">
                        <div className="home-summary-card">
                            <span className="home-summary-label">이번 달 수입</span>
                            <span className="home-summary-value income">+{summary.totalIncome.toLocaleString()}원</span>
                        </div>
                        <div className="home-summary-card">
                            <span className="home-summary-label">이번 달 지출</span>
                            <span className="home-summary-value">-{summary.totalExpense.toLocaleString()}원</span>
                        </div>
                        <div className="home-summary-card">
                            <span className="home-summary-label">순액</span>
                            <span className="home-summary-value income">{summary.netAmount >= 0 ? '+' : ''}{summary.netAmount.toLocaleString()}원</span>
                        </div>
                    </div>
                    <div className="home-panel">
                        <div className="home-panel-header">
                            <h2>최근 거래</h2>
                            <Link to="/transactions">전체보기</Link>
                        </div>
                        {recentTransactions.length === 0 ? (
                            <p className="empty-message">이 달의 거래 내역이 없습니다</p>
                        ) : (
                            <ul className="home-transaction-list">
                                {recentTransactions.map((t) => (
                                    <li key={t.id}>
                                        <div className="home-transaction-icon">{t.categoryIcon}</div>
                                        <div className="home-transaction-info">
                                            <div className="home-transaction-memo">{t.memo || t.categoryName}</div>
                                            <div className="home-transaction-meta">{t.categoryName}</div>
                                        </div>
                                        <div className={t.categoryType === 'INCOME' ? 'home-transaction-amount income' : 'home-transaction-amount expense'}>
                                            {t.categoryType === 'INCOME' ? '+' : '-'}{t.amount.toLocaleString()}
                                        </div>
                                    </li>
                                ))}
                            </ul>
                        )}
                    </div>
                    <div className="home-shortcuts">
                        <Link to="/transactions" className="home-shortcut-card">
                            <span className="home-shortcut-icon">⇅</span>
                            <span className="home-shortcut-title">거래 추가</span>
                            <span className="home-shortcut-desc">수입/지출 기록하기</span>
                        </Link>
                        <Link to="/dashboard" className="home-shortcut-card">
                            <span className="home-shortcut-icon">◻</span>
                            <span className="home-shortcut-title">이번 달 통계</span>
                            <span className="home-shortcut-desc">지출 현황 확인하기</span>
                        </Link>
                    </div>
                </>
            )}
        </div>
    );
}

function calcGoalPercent(goal) {
    if (goal.targetAmount === 0) return 0;
    return Math.max(0, Math.min(100, Math.round((goal.currentAmount / goal.targetAmount) * 100)));
}

export default HomePage;