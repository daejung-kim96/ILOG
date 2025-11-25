// App.tsx
import { BrowserRouter } from "react-router-dom";
import Router from "./pages/Router";
import { useAuthStore } from "./stores/authStore";
import { useEffect } from "react";
import { checkAuthStatus } from "./api/authApi";
import { useGroupStore } from "./stores/groupStore";

const App = () => {
  const { login, logout, setLoading } = useAuthStore();
  const { fetchGroups } = useGroupStore();

  useEffect(() => {
    const verifyAuth = async () => {
      try {
        const userData = await checkAuthStatus();

        if (userData) {
          login(userData);
          await fetchGroups(); // 스토어의 fetchGroups 함수 호출
          console.log(userData.name);
          console.log(userData.picture);
        } else {
          logout();
        }
      } catch (error) {
        console.error("인증 확인 실패:", error);
        logout();
      } finally {
        setLoading(false);
      }
    };

    verifyAuth();
  }, [login, logout, setLoading, fetchGroups]); // 의존성 배열 업데이트

  return (
    <BrowserRouter>
      <Router />
    </BrowserRouter>
  );
};

export default App;
