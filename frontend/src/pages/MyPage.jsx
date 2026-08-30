import './MyPage.css';
import { useNavigate } from "react-router";
import { useAuth } from "../context/useAuth";
import { useEffect, useState } from "react";
import { getMyInfo, updateNickname, updatePassword, withdraw } from "../api/user";

function MyPage() {
    // 로그아웃 처리
    const navigate = useNavigate();
    const { logout } = useAuth();

    // 내 정보 조회
    const [userInfo, setUserInfo] = useState(null);
    const [isLoading, setIsLoading] = useState(true);

    // 닉네임 수정
    const [isEditingNickname, setIsEditingNickname] = useState(false);
    const [newNickname, setNewNickname] = useState('');
    const [nicknameError, setNicknameError] = useState('');

    // 비밀번호 변경
    const [currentPassword, setCurrentPassword] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [newPasswordConfirm, setNewPasswordConfirm] = useState('');
    const [passwordError, setPasswordError] = useState('');
    const [isPasswordLoading, setIsPasswordLoading] = useState(false);
    const [passwordSuccess, setPasswordSuccess] = useState('');

    //회원탈퇴
    const [isWithdrawModalOpen, setIsWithdrawModalOpen] = useState(false);
    const [withdrawPassword, setWithdrawPassword] = useState('');
    const [withdrawError, setWithdrawError] = useState('');
    const [isWithdrawLoading, setIsWithdrawLoading] = useState(false);

    
    // 컴포넌트 처음 렌더링 - 내 정보 조회
    useEffect(() => {
        getMyInfo()
        .then((data) => setUserInfo(data))
        .catch(() => setUserInfo(null))
        .finally(() => setIsLoading(false));
    }, []);

    // 닉네임 "수정" 버튼 - 입력 모드로 전환
    function handleNicknameEditStart() {
        setNewNickname(userInfo.nickname);
        setNicknameError('');
        setIsEditingNickname(true);
    }

    // 닉네임 변경 취소
    function handleNicknameCancel() {
        setNicknameError('');
        setIsEditingNickname(false);
    }

    // 닉네임 저장
    async function handleNicknameSave() {
        setNicknameError('');
        try {
            await updateNickname(newNickname);
            setUserInfo({...userInfo, nickname: newNickname});
            setIsEditingNickname(false);
        } catch (error) {
            setNicknameError(error.message);
        }
    }

    // 비밀번호 변경 폼 제출
    async function handlePasswordSubmit(e) {
        e.preventDefault();
        setPasswordError('');
        setPasswordSuccess('');

        if(newPassword !== newPasswordConfirm) {
            setPasswordError('새 비밀번호가 일치하지 않습니다');
            return;
        }

        setIsPasswordLoading(true);
        try {
            await updatePassword(currentPassword, newPassword);
            setCurrentPassword('');
            setNewPassword('');
            setNewPasswordConfirm('');
            setPasswordSuccess('비밀번호가 변경 되었습니다');
        } catch (error) {
            setPasswordError(error.message);
        } finally {
            setIsPasswordLoading(false);
        }
    }

    // 회원탈퇴 모달 열기
    function handleWithdrawStart() {
        setWithdrawPassword('');
        setWithdrawError('');
        setIsWithdrawModalOpen(true);
    }

    // 회원탈퇴 모달 취소
    function handleWithdrawCancel() {
        setIsWithdrawModalOpen(false);
    }

    // 회원탈퇴 확정
    async function handleWithdrawSubmit(e) {
        e.preventDefault();
        setWithdrawError('');
        setIsWithdrawLoading(true);
        try {
            await withdraw(withdrawPassword);
            await logout();
            navigate('/login');
        } catch (error) {
            setWithdrawError(error.message);
            setIsWithdrawLoading(false);
        }
    }

    // 정보를 불러오는 중이면 화면을 아직 그리지 않음
    if(isLoading) {
        return null;
    }


    return (
        <div className="mypage">
            <h1>마이페이지</h1>
            <p>계정 정보를 관리하세요</p>

            <div className="profile-card">
                <div className="profile-header">
                    <span className="avatar">{userInfo.nickname[0]}</span>
                    <div>
                        <h2>{userInfo.nickname}</h2>
                        <p>{userInfo.email}</p>
                        <div className="join-date">가입일: {userInfo.createdAt.slice(0, 10)}</div>
                    </div>
                </div>

                <div className="nickname-section">
                    <label>닉네임 변경</label>
                    {isEditingNickname? (
                        <div className="nickname-row">
                            <input type="text" value={newNickname} onChange={(e) => setNewNickname(e.target.value)}/>
                            <button type="button" onClick={handleNicknameSave}>저장</button>
                            <button type="button" onClick={handleNicknameCancel}>취소</button>
                        </div>        
                    ) : (
                        <div className="nickname-row">
                            <span>{userInfo.nickname}</span>
                            <button type="button" onClick={handleNicknameEditStart}>수정</button>
                        </div>    
                    )}
                    {nicknameError && <p className="error-message">{nicknameError}</p>}
                </div>
            </div>

            <form className="password-card" onSubmit={handlePasswordSubmit}>
                <h2>비밀번호 변경</h2>

                <label>현재 비밀번호</label>
                <input type="password" placeholder="현재 비밀번호" value={currentPassword} onChange={(e) => setCurrentPassword(e.target.value)}/>
                
                <label>새 비밀번호</label>
                <input type="password" placeholder="영문+숫자 조합 8자 이상" value={newPassword} onChange={(e) => setNewPassword(e.target.value)}/>
                
                <label>새 비밀번호 확인</label>
                <input type="password" placeholder="새 비밀번호 재입력" value={newPasswordConfirm} onChange={(e) => setNewPasswordConfirm(e.target.value)}/>
                
                {passwordError && <p className="error-message">{passwordError}</p>}
                {passwordSuccess && <p className="success-message">{passwordSuccess}</p>}

                <button type="submit" disabled={isPasswordLoading}>
                    {isPasswordLoading ? '변경 중...' : '비밀번호 변경'}
                </button>
            </form>

            <div className="danger-zone">
                <h2>계정 관리</h2>
                <p>되돌릴 수 없는 작업입니다</p>
                <a href="#" onClick={(e) => { e.preventDefault(); handleWithdrawStart(); }}>회원 탈퇴</a>
            </div>

            {isWithdrawModalOpen && (
                <div className='modal-overlay'>
                    <form className='modal-content' onSubmit={handleWithdrawSubmit}>
                        <h2>정말 탈퇴하시겠습니까?</h2>
                        <p>탈퇴 시 계정 정보가 삭제되며 되돌릴 수 없습니다.</p>

                        <label>비밀번호 확인</label>
                        <input type="password" placeholder='비밀번호 입력' value={withdrawPassword} onChange={(e) => setWithdrawPassword(e.target.value)}/>

                        {withdrawError && <p className='error-message'>{withdrawError}</p>}

                        <div className='modal-actions'>
                            <button type="button" onClick={handleWithdrawCancel} disabled={isWithdrawLoading}>취소</button>
                            <button type="submit" disabled={isWithdrawLoading}>
                                {isWithdrawLoading ? '탈퇴 중...' : '탈퇴하기'}
                            </button>
                        </div>
                    </form>
                </div>
            )}
        </div>
    );
}

export default MyPage;