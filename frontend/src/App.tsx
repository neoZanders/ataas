import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider, useAuth } from "./Components/AuthContext";
import { RoleRoute } from "./Components/RoleRoute";
import { LoginPage } from "./Components/LoginPage";
import { SignUpPage } from "./Components/SignUpPage";
import { ProfilePage } from "./Components/ProfilePage";
import { CourseResponsibleMainPage } from "./Components/CourseResponsibleMainPage";
import { CourseResponsibleTAListPage } from "./Components/CourseResponsibleTAListPage";
import { CourseResponsibleConstraintsPage } from "./Components/CourseResponsibleConstraintsPage";
import {TAMainPage} from "./Components/TAMainPage.tsx";

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
                        path="/ta/calendar"
                        element={
                            <RoleRoute allow={["TA"]}>
                               <TAMainPage />
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
        </AuthProvider>
    );
}

export default App;