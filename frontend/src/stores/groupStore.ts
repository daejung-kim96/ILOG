// src/stores/groupStore.ts
import { getGroups } from "@/api/groupApi";
import { GroupResponse } from "@/types/group";
import { create } from "zustand";

interface GroupState {
  groups: GroupResponse[];
  currentGroup: GroupResponse | null;              // 현재 선택된 그룹
  setGroups: (groups: GroupResponse[]) => void;
  setCurrentGroup: (group: GroupResponse | null) => void;
  fetchGroups: () => Promise<GroupResponse[]>;
  clear: () => void;                               // 로그아웃 시 초기화
}

export const useGroupStore = create<GroupState>((set, get) => ({
  groups: [],
  currentGroup: null,

  setGroups: (groups) => set({ groups }),

  setCurrentGroup: (group) => set({ currentGroup: group }),

  fetchGroups: async () => {
    try {
      const groups = await getGroups();
      // 로그
      console.log("fetchGroups:", groups);

      // 그룹 리스트 저장
      set({ groups });

      // 아직 선택된 그룹이 없고, 그룹이 하나 이상이면 첫 번째 그룹으로 기본 선택
      const { currentGroup } = get();
      if (!currentGroup && groups.length > 0) {
        set({ currentGroup: groups[0] });
      }

      return groups;
    } catch (error) {
      console.error("Error fetching groups:", error);
      set({ groups: [], currentGroup: null });
      return [];
    }
  },

  clear: () => set({ groups: [], currentGroup: null }),
}));
