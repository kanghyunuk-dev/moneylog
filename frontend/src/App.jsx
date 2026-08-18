import LoginPage from "./pages/LoginPage";
import {Routes, Route} from "react-router";

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage/>} />
    </Routes>
  );
}

export default App
