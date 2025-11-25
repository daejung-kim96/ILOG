import { useAuthStore } from "@/stores/authStore";
import { Navigate, Outlet } from "react-router-dom";

const PublicOnlyRoute = () => {
  const { isLoggedIn, isLoading } = useAuthStore();

  // 1. App.tsx에서 인증 확인이 로딩 중이면 대기
  if (isLoading) {
    return <div>Loading...</div>; // (ProtectedRoute와 동일한 로딩 UI)
  }

  // 2. 로딩이 끝났는데, *로그인*이 되어 있다면?
  if (isLoggedIn) {
    // 🛑 /login 페이지 대신 메인 페이지로 리다이렉트
    return <Navigate to="/" replace />;
  }

  // 3. 로그인이 안 되어 있다면?
  // ✅ /login, /signup 페이지를 그대로 보여줌
  return <Outlet />;
};

export default PublicOnlyRoute;
