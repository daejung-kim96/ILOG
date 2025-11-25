import { Navigate, Outlet } from "react-router-dom";
import { useAuthStore } from "../../stores/authStore";

const ProtectedRoute = () => {
  const { isLoggedIn, isLoading } = useAuthStore();

  // 1. 아직 로딩 중(새로고침 시 인증 확인 중)이면?
  if (isLoading) {
    // 🌀 여기에 로딩 스피너 컴포넌트를 보여줍니다.
    return <div>Loading...</div>;
  }

  // 2. 로딩이 끝났고, 로그인이 되어 있지 않다면?
  if (!isLoggedIn) {
    // 🛑 /login 페이지로 리다이렉트
    // 'replace' 옵션은 브라우저 히스토리에 현재 페이지를 남기지 않음
    // (뒤로 가기 눌렀을 때 다시 이 페이지로 돌아오는 것을 방지)
    return <Navigate to="/login" replace />;
  }

  // 3. 로딩이 끝났고, 로그인이 되어 있다면?
  // ✅ 자식 컴포넌트(요청한 페이지)를 보여줍니다.
  // <Outlet />은 react-router-dom v6에서 자식 라우트를 렌더링하는 컴포넌트입니다.
  return <Outlet />;
};

export default ProtectedRoute;
