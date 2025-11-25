import axiosInstance, { ApiResponse } from './axiosInstance';
import { Sentiment } from '@/types/Sentiment'; // Sentiment 타입 가져오기

// 감정 분석 시연: 데이터 가져오기
export const getSentimentAnalysis = async (userId: number, date: string) => {
    try {
        const response = await axiosInstance.get<ApiResponse<Sentiment>>('/demo/sentiment/day', {
            params: { userId, date }
        });

        console.log("response.data:", response.data);  // 전체 응답 데이터 확인
        // body 부분 없이 바로 response.data에서 데이터를 사용
        return response.data;  // response.data로 반환, 'body' 없이 바로 데이터 접근

    } catch (error) {
        console.error('Error fetching sentiment analysis data:', error);
        throw error;
    }
};

// 감정 분석 실행: 감정 분석 데이터 수집
export const runSentimentAnalysis = async (userId: number): Promise<void> => {
    try {
        const response = await axiosInstance.post<ApiResponse<string>>('/demo/sentiment/run-today', {
            userId
        });

        if (response.data.success) {
            console.log('감정 분석 실행 완료');
        }
    } catch (error) {
        console.error('Error running sentiment analysis:', error);
        throw error;
    }
};
