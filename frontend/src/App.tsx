import {BrowserRouter as Router, Routes, Route, Navigate} from "react-router-dom";
import { CourseResponsibleMainPage } from "./Components/CourseResponsibleMainPage";
import {CourseResponsibleTAListPage} from "./Components/CourseResponsibleTAListPage.tsx";

function App() {
    return (
        <Router>
            <Routes>
                <Route path="/" element={<Navigate to="/calendar" replace />} />

                <Route path="/calendar" element={<CourseResponsibleMainPage />} />

                <Route path="/talist" element={<CourseResponsibleTAListPage />} />
            </Routes>
        </Router>
    );
}

export default App;