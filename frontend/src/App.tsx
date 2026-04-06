import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider, useAuth } from "./Components/AuthContext";
import { RoleRoute } from "./Components/RoleRoute";
import { LoginPage } from "./Components/LoginPage";
import { SignUpPage } from "./Components/SignUpPage";
import { ProfilePage } from "./Components/ProfilePage";
import { CourseResponsibleMainPage } from "./Components/CR/CourseResponsibleMainPage.tsx";
import { CourseResponsibleTAListPage } from "./Components/CR/CourseResponsibleTAListPage.tsx";
import { CourseResponsibleAnnouncementPage } from "./Components/CR/CourseResponsibleAnnouncementPage.tsx";
import { TAMainPage } from "./Components/TA/TAMainPage.tsx";
import { TAAnnouncementPage } from "./Components/TA/TAAnnouncementPage.tsx";
import { TATaListPage} from "./Components/TA/TATaListPage.tsx";
import {TAConstraintsPage} from "./Components/TA/TAConstraintsPage.tsx";
import {CourseResponsibleCourse} from "./Components/CR/CourseResponsibleCourse.tsx";
import {CourseResponsibleConstraintsPage} from "./Components/CourseResponsibleConstraintsPage.tsx";
import {CurrentCourseProvider} from "./Components/CurrentCourseContext.tsx";

function RootRedirect() {
    const { user, isAuthReady } = useAuth();
    if (!isAuthReady) return null;
    if (!user) return <Navigate to="/login" replace />;

    return user.userType === "CR"
        ? <Navigate to="/cr/calendar" replace />
        : <Navigate to="/ta/calendar" replace />;
}

function PublicOnlyRoute({ children }: { children: React.ReactElement }) {
    const { user, isAuthReady } = useAuth();
    if (!isAuthReady) return null;

    if (!user) return children;

    return user.userType === "CR"
        ? <Navigate to="/cr/calendar" replace />
        : <Navigate to="/ta/calendar" replace />;
}

function App() {
    return (

        <AuthProvider>
            <CurrentCourseProvider >
            <Router>
                <Routes>
                    <Route path="/" element={<RootRedirect />} />

                    <Route
                        path="/login"
                        element={
                            <PublicOnlyRoute>
                                <LoginPage />
                            </PublicOnlyRoute>
                        }
                    />
                    <Route
                        path="/signup"
                        element={
                            <PublicOnlyRoute>
                                <SignUpPage />
                            </PublicOnlyRoute>
                        }
                    />

                    <Route
                        path="/account"
                        element={
                            <RoleRoute allow={["CR", "TA"]}>
                                <ProfilePage />
                            </RoleRoute>
                        }
                    />

                    <Route
                        path="/cr/calendar"
                        element={
                            <RoleRoute allow={["CR"]}>
                                <CourseResponsibleMainPage />
                            </RoleRoute>
                        }
                    />

                    <Route
                        path="/cr/course"
                        element={
                            <RoleRoute allow={["CR"]}>
                                <CourseResponsibleCourse />
                            </RoleRoute>
                        }
                    />

                    <Route
                        path="/cr/talist"
                        element={
                            <RoleRoute allow={["CR"]}>
                                <CourseResponsibleTAListPage />
                            </RoleRoute>
                        }
                    />

                    <Route
                        path="/cr/constraints"
                        element={
                            <RoleRoute allow={["CR"]}>
                                <CourseResponsibleConstraintsPage />
                            </RoleRoute>
                        }
                    />
                    <Route
                        path="cr/announcements"
                        element={
                            <RoleRoute allow={["CR"]}>
                                <CourseResponsibleAnnouncementPage/>
                            </RoleRoute>
                        }
                    />

                    <Route
                        path="/ta/calendar"
                        element={
                            <RoleRoute allow={["TA"]}>
                               <TAMainPage />
                            </RoleRoute>
                        }
                    />
                    <Route
                        path="/ta/announcements"
                        element={
                            <RoleRoute allow={["TA"]}>
                                <TAAnnouncementPage />
                            </RoleRoute>
                        }
                    />
                    <Route
                        path="/ta/talist"
                        element={
                            <RoleRoute allow={["TA"]}>
                                <TATaListPage />
                            </RoleRoute>
                        }
                    />
                    <Route
                        path="/ta/constraints"
                        element={
                            <RoleRoute allow={["TA"]}>
                                <TAConstraintsPage />
                            </RoleRoute>
                        }
                    />

                    <Route
                        path="/ta/course"
                        element={
                            <RoleRoute allow={["TA"]}>
                                <CourseResponsibleCourse />
                            </RoleRoute>
                        }
                    />

                    <Route
                        path="/ta/constraints"
                        element={
                            <RoleRoute allow={["TA"]}>
                                <div className="p-6">TA Constraints (placeholder)</div>
                            </RoleRoute>
                        }
                    />

                    <Route path="/unauthorized" element={
                        <PublicOnlyRoute>
                            <LoginPage />
                        </PublicOnlyRoute>
                    }
                    />
                </Routes>
            </Router>
            </CurrentCourseProvider>
        </AuthProvider>
    );
}

export default App;