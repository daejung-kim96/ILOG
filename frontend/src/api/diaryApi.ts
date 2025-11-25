import { DiaryData } from "@/types/diary";
import axiosInstance, { ApiResponse } from "./axiosInstance";

const API_BASE_URL = "/diaries";

// ✅ 일기 생성 API (응답값 반환)
export const createDiary = async (formData: FormData) => {
  try {
    const response = await axiosInstance.post(API_BASE_URL, formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    });
    // ✅ 응답 반환 (백엔드에서 생성된 일기 정보, 사진 정보 등)
    // 응답 형식:
    // {
    //   diaryId: 123,
    //   title: "...",
    //   photos: [
    //     { id: 1, url: "s3://...", date: "2025-01-20" },
    //     { id: 2, url: "s3://...", date: "2025-01-20" }
    //   ]
    // }
    return response.data.body;
  } catch (error) {
    console.error("Error creating diary:", error);
    throw error;
  }
};

/**
 * 일기 목록 조회 API
 * @param groupId - 조회할 그룹의 ID
 * @returns 일기 데이터 배열 (DiaryData[])
 */
export const getDiaries = async (groupId: number): Promise<DiaryData[]> => {
  try {
    // GET /diaries?groupId=1 형식으로 요청
    const response = await axiosInstance.get<ApiResponse<DiaryData[]>>(
      API_BASE_URL,
      {
        params: { groupId },
      }
    );
    return response.data.body;
  } catch (error) {
    console.error("Error fetching diaries:", error);
    throw error;
  }
};

/**
 * ✅ 월별 사진 조회 API
 * @param groupId - 조회할 그룹의 ID
 * @param month - 조회할 월 (예: "2025-01")
 * @returns 사진 데이터 배열
 */
export const getPhotosByMonth = async (
  groupId: number,
  month: string
): Promise<
  Array<{
    id: number;
    url: string;
    date: string;
  }>
> => {
  try {
    // GET /diaries/albums?groupId=1&month=2025-01
    const response = await axiosInstance.get<
      ApiResponse<
        Array<{
          id: number;
          url: string;
          date: string;
        }>
      >
    >(`${API_BASE_URL}/albums`, {
      params: { groupId, month },
    });
    return response.data.body;
  } catch (error) {
    console.error("Error fetching photos by month:", error);
    throw error;
  }
};

/**
 * ✅ 사진 삭제 API
 * @param photoId - 삭제할 사진의 ID
 */
export const deletePhoto = async (photoId: number): Promise<void> => {
  try {
    // DELETE /diaries/photos/:photoId
    await axiosInstance.delete(`${API_BASE_URL}/photos/${photoId}`);
  } catch (error) {
    console.error("Error deleting photo:", error);
    throw error;
  }
};

/**
 * ✅ 사진 업로드 API (별도로 추가하고 싶을 경우)
 * @param formData - FormData (file, groupId, month, date 포함)
 * @returns 업로드된 사진 정보
 */
export const uploadPhoto = async (
  formData: FormData
): Promise<{
  id: number;
  url: string;
  date: string;
}> => {
  try {
    // POST /diaries/photos/upload
    const response = await axiosInstance.post<
      ApiResponse<{
        id: number;
        url: string;
        date: string;
      }>
    >(`${API_BASE_URL}/photos/upload`, formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    });
    return response.data.body;
  } catch (error) {
    console.error("Error uploading photo:", error);
    throw error;
  }
};