export interface Sentiment {
    userId: number;
    date: string;
    diaryCount: number;
    avg: { [key: string]: number }; // 감정 평균 (예: angry, sad 등)
    note: string;
    summaries: string[]; // 3개 요약
}