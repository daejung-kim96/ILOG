import { create } from "zustand";
import { getDiaries } from "@/api/diaryApi";
import { DiaryData } from "@/types/diary";

interface DiaryState {
  // 상태 (State)
  diaries: DiaryData[];
  isLoading: boolean;
  error: string | null;

  // 액션 (Actions)
  fetchDiaries: (groupId: number) => Promise<void>;
}

export const useDiaryStore = create<DiaryState>((set) => ({
  // 초기 상태
  diaries: [],
  isLoading: false,
  error: null,

  // 일기 목록 가져오기 액션
  fetchDiaries: async (groupId: number) => {
    set({ isLoading: true, error: null }); // 로딩 시작, 에러 초기화
    try {
      const data = await getDiaries(groupId);
      set({ diaries: data, isLoading: false }); // 데이터 저장, 로딩 끝
    } catch (error) {
      console.error("Failed to fetch diaries:", error);
      set({ error: "일기를 불러오는데 실패했습니다.", isLoading: false });
    }
  },
}));
