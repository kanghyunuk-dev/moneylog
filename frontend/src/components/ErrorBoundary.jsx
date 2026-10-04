import { Component } from 'react';
import './ErrorBoundary.css';

// 하위 트리 렌더링 중 에러 - (전체 화이트스크린 말고) 해당 화면을 보여줌
// getDerivedStateFromError는 Hooks로 대체 불가능
class ErrorBoundary extends Component {
    constructor(props) {
        super(props);
        this.state = { hasError: false };
    }

    static getDerivedStateFromError() {
        return { hasError: true };
    }

    componentDidCatch(error, info) {
        console.error('렌더링 에러:', error, info);
    }

    render() {
        if (this.state.hasError) {
            return (
                <div className="error-boundary">
                    <h1>문제가 발생했습니다</h1>
                    <p>페이지를 새로고침해 주세요.</p>
                    <button type="button" onClick={() => window.location.reload()}>새로고침</button>
                </div>
            );
        }

        return this.props.children;
    }
}

export default ErrorBoundary;
