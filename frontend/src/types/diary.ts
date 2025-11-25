export enum DiaryStatus {
  PRIVATE = "PRIVATE",
  PUBLIC = "PUBLIC",
  DELETED = "DELETED",
}

export enum MediaType {
  IMAGE = "IMAGE",
  VIDEO = "VIDEO",
}

export interface Attachment {
  id: number;
  type: MediaType;
  url: string;
  file: File;
}

// 테마 색상을 엄격하게 제한 (오타 방지)
export type ThemeColor = "rose" | "green" | "amber";

// 일기 데이터 인터페이스
export interface DiaryData {
  id: number;
  userId: number;
  userName: string;
  createdAt: string;
  title: string;
  content: string;
  themeColor: ThemeColor;
  status: DiaryStatus;
  images: Attachment[];
  videos: Attachment[];
}

// DiaryCard 컴포넌트 Props 정의
export interface DiaryCardProps extends Omit<DiaryData, "id"> {
  onClick?: () => void;
}

// 탭 타입 정의
export type TabType = "all" | "my" | "private";
