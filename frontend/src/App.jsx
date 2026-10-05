import { lazy, Suspense } from 'react';
import {Routes, Route} from "react-router";
import PrivateRoute from "./components/PrivateRoute";
import Layout from "./components/Layout";

const LoginPage = lazy(() => import("./pages/LoginPage"));
const SignupPage = lazy(() => import("./pages/SignupPage"));
const HomePage = lazy(() => import("./pages/HomePage"));
const MyPage = lazy(() => import("./pages/MyPage"));
const TransactionsPage = lazy(() => import("./pages/TransactionsPage"));
const BudgetsPage = lazy(() => import("./pages/BudgetsPage"));
const DashboardPage = lazy(() => import("./pages/DashboardPage"));
const GoalsPage = lazy(() => import("./pages/GoalsPage"));
const NotFoundPage = lazy(() => import("./pages/NotFoundPage"));

function App() {
  return (
    <Suspense fallback={<p role="status">불러오는 중...</p>}>
      <Routes>
        <Route path="/login" element={<LoginPage/>} />
        <Route path="/signup" element={<SignupPage/>} />
        <Route element={<PrivateRoute><Layout/></PrivateRoute>}>
          <Route path="/" element={<HomePage/>} />
          <Route path="/mypage" element={<MyPage/>} />
          <Route path="/transactions" element={<TransactionsPage/>} />
          <Route path="/budgets" element={<BudgetsPage/>} />
          <Route path="/dashboard" element={<DashboardPage/>} />
          <Route path="/goals" element={<GoalsPage/>} />
        </Route>
        <Route path="*" element={<NotFoundPage/>} />
      </Routes>
    </Suspense>
  );
}

export default App
