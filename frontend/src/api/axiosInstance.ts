import axios from "axios";
import createAuthRefreshInterceptor from "axios-auth-refresh";
import { reissueToken } from "./authApi";
import { useAuthStore } from "@/stores/authStore";

export interface ApiResponse<T> {
  success: boolean;
  status: number;
  body: T;
}

const apiClient = axios.create({
  baseURL: "/api/v1",
  withCredentials: true,
});

// 토큰 재발급 로직
const refreshAuthLogic = async () => {
  try {
    await reissueToken(); // 토큰 재발급
    return Promise.resolve(); // 요청 재시도
  } catch (error) {
    // 토큰 재발급 실패 시 로그아웃
    useAuthStore.getState().logout();
    return Promise.reject(error);
  }
};

// axios-auth-refresh 인터셉터 등록
// createAuthRefreshInterceptor(apiClient, refreshAuthLogic);
createAuthRefreshInterceptor(apiClient, refreshAuthLogic, {
  shouldRefresh: (error) => {
    return (
      error.response?.status === 401 &&
      !error.config?.url?.includes("/auth/reissue")
    );
  },
});

export default apiClient;
