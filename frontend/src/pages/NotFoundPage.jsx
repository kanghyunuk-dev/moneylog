import './NotFoundPage.css';
import { Link } from 'react-router';

function NotFoundPage() {
    return (
        <div className="not-found-page">
            <title>페이지를 찾을 수 없음 · MoneyLog</title>
            <h1>404</h1>
            <p>존재하지 않는 페이지입니다.</p>
            <Link to="/">홈으로 돌아가기</Link>
        </div>
    );
}

export default NotFoundPage;
