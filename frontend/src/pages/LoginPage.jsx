import './LoginPage.css';
import { useState } from "react";
import { useAuth } from "../context/useAuth";
import { useNavigate } from "react-router";
import { loginRequest } from "../api/auth";

function LoginPage() {

    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState('');

    // 로그인 상태갱신 함수
    const {login} = useAuth();
    const navigate = useNavigate();

    async function handleSubmit(e) {
        e.preventDefault();
        setError('');
        setIsLoading(true);

        try {
            // 1. api/auth.js - loginRequest 함수 요청 (토큰은 서버가 쿠키로 저장)
            await loginRequest(email, password);

            // 2. 성공하면 AuthContext의 로그인 상태만 갱신 - 홈으로 이동
            login();
            navigate('/');
        } catch (err) {
            // 3. 실패 시 에러 메세지를 화면에 표시
            setError(err.message);
        } finally {
            // 4. 성공/실패 시 로딩 상태는 종료
            setIsLoading(false);
        }
    }

    return (
        <div className="auth-layout">
            <div className="login-page">
                <div className="login-logo">
                    <span className="logo-badge">M</span>
                    <h1>MoneyLog</h1>
                    <p>나만의 가계부를 시작해보세요</p>
                </div>

                <form className="login-form" onSubmit={handleSubmit}>
                    <h2>로그인</h2>

                    <label>이메일</label>
                    <input type="email" placeholder="example@email.com" value={email} onChange={(e) => setEmail(e.target.value)}/>

                    <label>비밀번호</label>
                    <input type="password" placeholder="비밀번호 입력" value={password} onChange={(e) => setPassword(e.target.value)}/>

                    {/* error 빈문자열이 아니면 error 보임*/}
                    {error && <p className="error-message">{error}</p>}

                    <button type="submit" disabled={isLoading}>{isLoading ? '로그인 중...' : '로그인'}</button>

                    <div className="divider">또는</div>

                    <button type="button" className="google-button">
                        G 구글로 로그인
                    </button>
                </form>

                <p className="signup-link">
                    계정이 없으신가요? <a href="/signup">회원가입</a>
                </p>
            </div>
        </div>
    );
}

export default LoginPage;