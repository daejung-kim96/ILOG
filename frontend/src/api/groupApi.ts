import axiosInstance, { ApiResponse } from "./axiosInstance";
import { GroupResponse } from "@/types/group";
import { useAuthStore } from "@/stores/authStore";

export const createGroup = async (name: string): Promise<GroupResponse> => {
  const { user } = useAuthStore.getState();
  try {
    const response = await axiosInstance.post<ApiResponse<GroupResponse>>(
      `/groups`,
      { name: name, userName: user?.name }
    );
    return response.data.body;
  } catch (error) {
    console.error("Error creating group:", error);
    throw error;
  }
};

export const inviteGroup = async (
  inviteCode: string
): Promise<GroupResponse> => {
  const { user } = useAuthStore.getState();
  try {
    const response = await axiosInstance.post<ApiResponse<GroupResponse>>(
      `/groups/invite`,
      {
        inviteCode: inviteCode,
        userName: user?.name,
      }
    );
    return response.data.body;
  } catch (error) {
    console.error("Error inviting to group:", error);
    throw error;
  }
};

export const getGroups = async (): Promise<GroupResponse[]> => {
  const response = await axiosInstance.get<ApiResponse<GroupResponse[]>>(
    "/groups"
  );
  return response.data.body;
};
