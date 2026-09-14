import { useCallback, useEffect, useState } from 'react';
import './DashboardPage.css';
import { getSummary, getCategoryBreakdown, getMonthlyTrend } from "../api/dashboard";
import { PieChart, Pie, Cell, Tooltip, ResponsiveContainer, BarChart, Bar, XAxis, YAxis, Legend } from 'recharts';

const CATEGORY_COLORS = ['#6366f1', '#f59e0b', '#ec4899', '#06b6d4', '#84cc16', '#8b5cf6', '#f97316', '#14b8a6', '#ef4444', '#3b82f6'];

function DashboardPage() {
    const now = new Date();
     // 현재 보고 있는 년-월
    const [year, setYear] = useState(now.getFullYear());
    const [month, setMonth] = useState(now.getMonth() + 1);
    // 최근 추이 차트 범위 (3/6/12개월)
    const [trendMonths, setTrendMonths] = useState(6);

    const [summary, setSummary] = useState(null);
    const [breakdown, setBreakdown] = useState([]);
    const [trend, setTrend] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');

    // month 문자열 (yyyy-MM 형태, API 파라미터용)
    const monthParam = `${year}-${String(month).padStart(2, '0')}`;

    // 요약 + 카테고리별 지출 조회 (같은 month 파라미터)
    const refresh = useCallback(() => {
        setIsLoading(true);
        Promise.all([getSummary(monthParam), getCategoryBreakdown(monthParam)])
            .then(([s, b]) => {
                setSummary(s);
                setBreakdown(b);
            })
            .catch((err) => setError(err.message))
            .finally(() => setIsLoading(false));
    }, [monthParam]);

    // year, month 바뀔 때마다 재조회
    useEffect(() => {
        refresh();
    }, [refresh]);

    // 추이 차트는 개월 수(trendMonths)만 바뀌면 재조회
    useEffect(() => {
        getMonthlyTrend(monthParam, trendMonths)
            .then((data) => setTrend(data))
            .catch((err) => setError(err.message));
    }, [monthParam, trendMonths]);

    // 이전/다음 달 이동
    function handlePrevMonth() {
        if (month === 1) {
            setYear(year - 1);
            setMonth(12);
        } else {
            setMonth(month - 1);
        }
    }

    function handleNextMonth() {
        if (month === 12) {
            setYear(year + 1);
            setMonth(1);
        } else {
            setMonth(month + 1);
        }
    }

    return (
        <div className="dashboard-page">
            <div className="dashboard-header">
                <div>
                    <h1>통계</h1>
                    <p>이번 달 수입/지출을 한눈에</p>
                </div>
            </div>

            <div className="month-nav">
                <button type="button" onClick={handlePrevMonth}>‹</button>
                <span>{year}년 {month}월</span>
                <button type="button" onClick={handleNextMonth}>›</button>
            </div>

            {error && <p className="error-message">{error}</p>}

            {isLoading || !summary ? (
                <p>불러오는중 ...</p>
            ) : (
                <>
                    <div className="summary-cards">
                        <div className="summary-card">
                            <span className="summary-label">총 수입</span>
                            <span className="summary-value income">{summary.totalIncome.toLocaleString()}원</span>
                        </div>
                        <div className="summary-card">
                            <span className="summary-label">총 지출</span>
                            <span className="summary-value">{summary.totalExpense.toLocaleString()}원</span>
                        </div>
                        <div className="summary-card">
                            <span className="summary-label">순액</span>
                            <span className="summary-value income">{summary.netAmount.toLocaleString()}원</span>
                        </div>
                        <div className="summary-card">
                            <span className="summary-label">저축률</span>
                            <span className="summary-value">{summary.savingsRate.toFixed(0)}%</span>
                        </div>
                    </div>

                    <div className="dashboard-panels">
                        <div className="panel">
                            <h2>카테고리별 지출</h2>
                            {breakdown.length === 0 ? (
                                <p className="empty-message">이 달의 지출이 없습니다</p>
                            ) : (
                                <>
                                    <ResponsiveContainer width="100%" height={220}>
                                        <PieChart>
                                            <Pie data={breakdown} dataKey="amount" nameKey="categoryName" innerRadius={60} outerRadius={90}>
                                                {breakdown.map((entry, index) => (
                                                    <Cell key={entry.categoryId} fill={CATEGORY_COLORS[index % CATEGORY_COLORS.length]} />
                                                ))}
                                            </Pie>
                                            <Tooltip formatter={(value) => `${value.toLocaleString()}원`} />
                                        </PieChart>
                                    </ResponsiveContainer>
                                    <ul className="breakdown-list">
                                        {breakdown.map((item, index) => (
                                            <li key={item.categoryId}>
                                                <span className="breakdown-dot" style={{ background: CATEGORY_COLORS[index % CATEGORY_COLORS.length] }} />
                                                <span className="breakdown-name">{item.categoryName}</span>
                                                <span className="breakdown-amount">{item.amount.toLocaleString()}</span>
                                                <span className="breakdown-percent">{item.percentage.toFixed(0)}%</span>
                                            </li>
                                        ))}
                                    </ul>
                                </>
                            )}
                        </div>

                        <div className="panel">
                            <div className="panel-header">
                                <h2>최근 {trendMonths}개월 추이</h2>
                                <div className="trend-range">
                                    {[3, 6, 12].map((m) => (
                                        <button key={m} type="button" className={trendMonths === m ? 'active' : ''} onClick={() => setTrendMonths(m)}>
                                            {m}개월
                                        </button>
                                    ))}
                                </div>
                            </div>
                            <ResponsiveContainer width="100%" height={260}>
                                <BarChart data={trend} margin={{ left: 0, right: 10, top: 10, bottom: 0 }}>
                                    <XAxis dataKey="month" />
                                    <YAxis tickFormatter={(value) => `${(value / 10000).toLocaleString()}만`} />
                                    <Tooltip formatter={(value) => `${value.toLocaleString()}원`} />
                                    <Legend />
                                    <Bar dataKey="income" name="수입" fill="#10b981" />
                                    <Bar dataKey="expense" name="지출" fill="#9ca3af" />
                                </BarChart>
                            </ResponsiveContainer>

                            <div className="change-row">
                                <div className="change-item">
                                    <span className="change-label">전월 대비 지출</span>
                                    <span className={formatChange(summary.expenseChange).className}>{formatChange(summary.expenseChange).text}</span>
                                </div>
                                <div className="change-item">
                                    <span className="change-label">전월 대비 수입</span>
                                    <span className={formatChange(summary.incomeChange).className}>{formatChange(summary.incomeChange).text}</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </>
            )}
        </div>
    );
}

// 증감액(텍스트+색상 변환) - 증가/감소/변화없음
function formatChange(value) {
    if (value === 0) return { text: '변화없음', className: 'change-value' };
    if (value > 0) return { text: `▲ ${value.toLocaleString()}원 증가`, className: 'change-value up' };
    return { text: `▼ ${Math.abs(value).toLocaleString()}원 감소`, className: 'change-value down' };
}

export default DashboardPage;
