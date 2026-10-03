import './SignupPage.css';
import { useState } from "react";
import { registerRequest } from "../api/auth";
import { Link, useNavigate } from "react-router";

function SignupPage() {
    const [email, setEmail] = useState('');
    const [nickname, setNickname] = useState('');
    const [password, setPassword] = useState('');
    const [passwordConfirm, setPasswordConfirm] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState('');

    const navigate = useNavigate();

    async function handleSubmit(e) {
        e.preventDefault();
        setError('');

        // 1. (서버 보내기 전) 비밀번호 일치 여부 검사
        if (password !== passwordConfirm) {
            setError('비밀번호가 일치하지 않습니다');
            return;
        }

        setIsLoading(true);

        try {
            // 2. api/auth.js - registerRequest 회원가입 요청
            await registerRequest(email, password, nickname);
            // 3. 성공 시
            navigate('/login')
        } catch (error) {
            // 4. 실패 시
            setError(error.message);
        } finally {
            // 5. 성공/실패 시 로딩 상태 종료
            setIsLoading(false);
        }
    }

    return (
        <div className="auth-layout">
            <div className="signup-page">
                <div className="signup-logo">
                    <span className="logo-badge">M</span>
                    <h1>MoneyLog</h1>
                    <p>무료로 시작하세요</p>
                </div>

                <form className="signup-form" onSubmit={handleSubmit}>
                    <h2>회원가입</h2>

                    <label>이메일</label>
                    <input type="email" placeholder="example@email.com" value={email} onChange={(e) => setEmail(e.target.value)}/>

                    <label>닉네임</label>
                    <input type="text" placeholder="표시될 이름" value={nickname} onChange={(e) => setNickname(e.target.value)}/>

                    <label>비밀번호</label>
                    <input type="password" placeholder="영문+숫자 조합 8자 이상" value={password} onChange={(e) => setPassword(e.target.value)} />
                    <p className="password-hint">영문+숫자 조합 8자 이상</p>

                    <label>비밀번호 확인</label>
                    <input type="password" placeholder="비밀번호 재입력" value={passwordConfirm} onChange={(e) => setPasswordConfirm(e.target.value)}/>

                    {error && <p className="error-message">{error}</p>}

                    <button type="submit" disabled={isLoading}>
                        {isLoading ? '가입 중...' : '회원가입'}
                    </button>
                </form>

                <p className="login-link">
                    이미 계정이 있으신가요? <Link to="/login">로그인</Link>
                </p>
            </div>
        </div>
    )
}

export default SignupPage;