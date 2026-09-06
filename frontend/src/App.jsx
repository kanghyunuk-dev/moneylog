import LoginPage from "./pages/LoginPage";
import {Routes, Route} from "react-router";
import SignupPage from "./pages/SignupPage";
import PrivateRoute from "./components/PrivateRoute";
import HomePage from "./pages/HomePage";
import MyPage from "./pages/MyPage";
import Layout from "./components/Layout";
import TransactionsPage from "./pages/TransactionsPage";

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage/>} />
      <Route path="/signup" element={<SignupPage/>} />
      <Route element={<PrivateRoute><Layout/></PrivateRoute>}>
        <Route path="/" element={<HomePage/>} />
        <Route path="/mypage" element={<MyPage/>} />
        <Route path="/transactions" element={<TransactionsPage/>} />
      </Route>
    </Routes>
  );
}

export default App
