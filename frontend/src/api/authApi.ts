import { UserData } from "@/types/user";
import apiClient, { ApiResponse } from "./axiosInstance";

export const checkAuthStatus = async (): Promise<UserData | null> => {
  try {
    const response = await apiClient.get<ApiResponse<UserData>>("/users/me");

    if (response.data.success && response.data.body) {
      return response.data.body;
    }

    return null;
  } catch (error) {
    console.warn("인증 확인 실패 (로그아웃 상태):", error);
    return null;
  }
};

export const reissueToken = async (): Promise<void> => {
  try {
    await apiClient.post("/auth/reissue");
  } catch (error) {
    console.error("토큰 재발급 실패:", error);
    throw error;
  }
};

export const logout = async (userId: number): Promise<void> => {
  try {
    await apiClient.post(`/auth/logout/${userId}`);
  } catch (error) {
    console.error("로그아웃 실패:", error);
    throw error;
  }
};
