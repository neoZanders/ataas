import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import { CourseResponsibleMainPage } from "./Components/CourseResponsibleMainPage";

function App() {
    return (
        <Router>
            <Routes>
                <Route path="*" element={<CourseResponsibleMainPage />} />
            </Routes>
        </Router>
    );
}

export default App;