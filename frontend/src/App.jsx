import LoginPage from "./pages/LoginPage";
import {Routes, Route} from "react-router";
import SignupPage from "./pages/SignupPage";
import HomePage from "./pages/HomePage";

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage/>} />
      <Route path="/signup" element={<SignupPage/>} />
      <Route path="/" element={<HomePage/>} />
    </Routes>
  );
}

export default App
