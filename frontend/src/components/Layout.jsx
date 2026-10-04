import './Layout.css';
import { useState } from "react";
import { Outlet } from "react-router";
import Sidebar from "./Sidebar";

function Layout() {
    // 모바일에서만 쓰는 사이드바 열림 상태(데스크톱은 CSS가 항상 보이게 처리)
    const [isSidebarOpen, setIsSidebarOpen] = useState(false);

    return (
        <div className="app-layout">
            <button
                type="button"
                className="mobile-topbar-toggle"
                onClick={() => setIsSidebarOpen(true)}
                aria-label="메뉴 열기"
            >
                ☰
            </button>

            <Sidebar isOpen={isSidebarOpen} onClose={() => setIsSidebarOpen(false)} />

            <main className="app-content">
                <Outlet />
            </main>
        </div>
    );
}

export default Layout;
