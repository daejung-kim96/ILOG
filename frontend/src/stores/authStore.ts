import { UserData } from "@/types/user";
import { create } from "zustand";
import { logout as logoutApi } from "@/api/authApi";
import { useGroupStore } from "@/stores/groupStore";

interface AuthState {
  isLoggedIn: boolean;
  isLoading: boolean;
  user: UserData | null;
  login: (user: UserData) => void;
  logout: () => Promise<void>;
  setLoading: (loading: boolean) => void;
}

export const useAuthStore = create<AuthState>((set, get) => ({
  isLoggedIn: false,
  isLoading: true,
  user: null,

  login: (user) => set({ isLoggedIn: true, user }),

  logout: async () => {
    const { user } = get();
    if (!user) return;

    try {
      await logoutApi(user.id);
    } catch (error) {
      console.error("로그아웃 중 오류 발생:", error);
    } finally {
      set({ isLoggedIn: false, user: null });
      // 그룹 스토어도 초기화
      useGroupStore.getState().clear();
    }
  },

  setLoading: (loading) => set({ isLoading: loading }),
}));
