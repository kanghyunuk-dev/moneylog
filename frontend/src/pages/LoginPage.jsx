import './LoginPage.css';
import { useState } from "react";
import { useAuth } from "../context/useAuth";
import { Link, useNavigate, useLocation } from "react-router";
import { loginRequest, demoLoginRequest } from "../api/auth";

function LoginPage() {

    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [isDemoLoading, setIsDemoLoading] = useState(false);
    const [error, setError] = useState('');

    // 로그인 상태갱신 함수
    const {login} = useAuth();
    const navigate = useNavigate();
    const location = useLocation();
    const from = location.state?.from?.pathname || '/';

    async function handleSubmit(e) {
        e.preventDefault();
        setError('');
        setIsLoading(true);

        try {
            // 1. api/auth.js - loginRequest 함수 요청 (토큰은 서버가 쿠키로 저장)
            await loginRequest(email, password);

            // 2. 성공하면 AuthContext의 로그인 상태만 갱신 - 홈으로 이동
            login();
            navigate(from);
        } catch (err) {
            // 3. 실패 시 에러 메세지를 화면에 표시
            setError(err.message);
        } finally {
            // 4. 성공/실패 시 로딩 상태는 종료
            setIsLoading(false);
        }
    }

    function handleGoogleLogin() {
        window.location.href = `${import.meta.env.VITE_API_BASE_URL || ''}/oauth2/authorization/google`;
    }

    // 둘러보기 - 체험 계정 로그인 후 홈 이동
    async function handleDemoLogin() {
        setError('');
        setIsDemoLoading(true);

        try {
            await demoLoginRequest();
            login();
            navigate('/');
        } catch (err) {
            setError(err.message);
        } finally {
            setIsDemoLoading(false);
        }
    }

    return (
        <div className="auth-layout">
            <div className="login-page">
                <title>로그인 · MoneyLog</title>
                <div className="login-logo">
                    <span className="logo-badge">M</span>
                    <h1>MoneyLog</h1>
                    <p>나만의 가계부를 시작해보세요</p>
                </div>

                <form className="login-form" onSubmit={handleSubmit}>
                    <h2>로그인</h2>

                    <label htmlFor="email">이메일</label>
                    <input id="email" type="email" placeholder="example@email.com" value={email} onChange={(e) => setEmail(e.target.value)}/>

                    <label htmlFor="password">비밀번호</label>
                    <input id="password" type="password" placeholder="비밀번호 입력" value={password} onChange={(e) => setPassword(e.target.value)}/>

                    {/* error 빈문자열이 아니면 error 보임*/}
                    {error && <p role="alert" className="error-message">{error}</p>}

                    <button type="submit" disabled={isLoading || isDemoLoading}>{isLoading ? '로그인 중...' : '로그인'}</button>

                    <div className="divider">또는</div>

                    <button type="button" className="google-button" onClick={handleGoogleLogin}>
                        G 구글로 로그인
                    </button>

                    <button type="button" className="demo-button" onClick={handleDemoLogin} disabled={isDemoLoading || isLoading}>
                        {isDemoLoading ? '체험 계정 준비 중...' : '로그인 없이 둘러보기'}
                    </button>
                    <p className="demo-hint">샘플 데이터가 담긴 체험 계정으로 바로 둘러볼 수 있어요 (24시간 후 자동 삭제)</p>
                </form>

                <p className="signup-link">
                    계정이 없으신가요? <Link to="/signup">회원가입</Link>
                </p>
            </div>
        </div>
    );
}

export default LoginPage;