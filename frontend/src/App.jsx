import LoginPage from "./pages/LoginPage";
import {Routes, Route} from "react-router";
import SignupPage from "./pages/SignupPage";
import PrivateRoute from "./components/PrivateRoute";
import HomePage from "./pages/HomePage";
import MyPage from "./pages/MyPage";

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage/>} />
      <Route path="/signup" element={<SignupPage/>} />
      <Route path="/" element={<PrivateRoute><HomePage/></PrivateRoute>} />
      <Route path="/mypage" element={<PrivateRoute><MyPage/></PrivateRoute>} />
    </Routes>
  );
}

export default App
