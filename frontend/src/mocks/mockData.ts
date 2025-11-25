// src/mocks/mockData.ts
// 프론트 제작을 위해 생성된 임시데이터
// api 연결 되면 삭제할 파일입니다.

// ---------------------------------------------
// 유저 정보
// ---------------------------------------------
export const mockUser = {
  id: 1,
  name: '김철수',
  childName: '아가',
  profileImage: 'src/assets/logo.png'
};

// ---------------------------------------------
// Diary(일기) Mock 데이터 추가
// ---------------------------------------------
export const mockDiaries = [
  {
    id: 1,
    date: '2025.10.23',
    title: '동현이의 우당탕탕 하루 일기',
    excerpt: '오늘은 정말 재미있는 하루였어...',
    content: '오늘도 동현이가 회의를 해야한다고 옹알거렸다. 귀찮지만 귀여우니 욘서',
    color: '#f87171',
    author: '동현서현맘',
    datetime: '2025.10.23 | 19:25:30',
    visibility: 'public',
  },
  {
    id: 2,
    date: '2025.10.22',
    title: '한 눈 판 사이 락스를 먹은 동현이',
    excerpt: '집중해서 맛있게 먹었다...',
    content: '오늘 아침에 정신을 잠시 놨는데...',
    color: '#22c55e',
    author: '동현서현빠',
    datetime: '2025.10.22 | 14:30:00',
    visibility: 'public',
  }
];

// ---------------------------------------------
// Photo 타입 정의
// ---------------------------------------------
export interface Photo {
  id: number;
  url: string;
  diaryId?: number;
  date: string;
}

// ---------------------------------------------
// 월별 사진 데이터 (AlbumDetail용)
// ---------------------------------------------
export const mockPhotosByMonth: Record<
  string,
  Array<{ id: number; url: string; diaryId: number; date: string }>
> = {
  '2025-11': [
    { id: 1, url: 'src/assets/logo.png', diaryId: 1, date: '2025.11.03' },
    { id: 2, url: 'src/assets/logo.png', diaryId: 1, date: '2025.11.03' },
    { id: 3, url: 'src/assets/logo.png', diaryId: 2, date: '2025.11.05' },
  ],
  '2025-10': [
    { id: 4, url: 'src/assets/logo.png', diaryId: 3, date: '2025.10.02' },
    { id: 5, url: 'src/assets/logo.png', diaryId: 3, date: '2025.10.02' },
    { id: 6, url: 'src/assets/logo.png', diaryId: 4, date: '2025.10.08' },
    { id: 7, url: 'src/assets/logo.png', diaryId: 5, date: '2025.10.15' },
    { id: 8, url: 'src/assets/logo.png', diaryId: 5, date: '2025.10.15' },
    { id: 9, url: 'src/assets/logo.png', diaryId: 5, date: '2025.10.15' },
    { id: 10, url: 'src/assets/logo.png', diaryId: 6, date: '2025.10.20' },
    { id: 11, url: 'src/assets/logo.png', diaryId: 7, date: '2025.10.25' },
    { id: 12, url: 'src/assets/logo.png', diaryId: 8, date: '2025.10.28' },
    { id: 13, url: 'src/assets/logo.png', diaryId: 8, date: '2025.10.28' }
  ],
  '2025-09': [
    { id: 14, url: 'src/assets/logo.png', diaryId: 9, date: '2025.09.01' },
    { id: 15, url: 'src/assets/logo.png', diaryId: 10, date: '2025.09.05' },
    { id: 16, url: 'src/assets/logo.png', diaryId: 10, date: '2025.09.05' },
    { id: 17, url: 'src/assets/logo.png', diaryId: 11, date: '2025.09.10' },
    { id: 18, url: 'src/assets/logo.png', diaryId: 12, date: '2025.09.15' },
    { id: 19, url: 'src/assets/logo.png', diaryId: 13, date: '2025.09.20' },
    { id: 20, url: 'src/assets/logo.png', diaryId: 13, date: '2025.09.20' },
    { id: 21, url: 'src/assets/logo.png', diaryId: 14, date: '2025.09.25' }
  ],
  '2025-08': [
    { id: 22, url: 'src/assets/logo.png', diaryId: 15, date: '2025.08.05' },
    { id: 23, url: 'src/assets/logo.png', diaryId: 16, date: '2025.08.12' },
    { id: 24, url: 'src/assets/logo.png', diaryId: 16, date: '2025.08.12' },
    { id: 25, url: 'src/assets/logo.png', diaryId: 17, date: '2025.08.18' },
    { id: 26, url: 'src/assets/logo.png', diaryId: 18, date: '2025.08.22' },
    { id: 27, url: 'src/assets/logo.png', diaryId: 19, date: '2025.08.28' }
  ],
  '2025-07': [
    { id: 28, url: 'src/assets/logo.png', diaryId: 20, date: '2025.07.03' },
    { id: 29, url: 'src/assets/logo.png', diaryId: 20, date: '2025.07.03' },
    { id: 30, url: 'src/assets/logo.png', diaryId: 21, date: '2025.07.10' },
    { id: 31, url: 'src/assets/logo.png', diaryId: 22, date: '2025.07.17' },
    { id: 32, url: 'src/assets/logo.png', diaryId: 23, date: '2025.07.24' },
    { id: 33, url: 'src/assets/logo.png', diaryId: 23, date: '2025.07.24' },
    { id: 34, url: 'src/assets/logo.png', diaryId: 23, date: '2025.07.24' }
  ],
  '2025-06': [
    { id: 35, url: 'src/assets/logo.png', diaryId: 24, date: '2025.06.02' },
    { id: 36, url: 'src/assets/logo.png', diaryId: 25, date: '2025.06.08' },
    { id: 37, url: 'src/assets/logo.png', diaryId: 26, date: '2025.06.15' },
    { id: 38, url: 'src/assets/logo.png', diaryId: 26, date: '2025.06.15' },
    { id: 39, url: 'src/assets/logo.png', diaryId: 27, date: '2025.06.22' }
  ]
};

// ---------------------------------------------
// 앨범 메인 리스트
// ---------------------------------------------
export const mockAlbumList = [
  {
    id: 1,
    month: 11,
    year: 2025,
    date: '2025.11',
    photoCount: mockPhotosByMonth['2025-11']?.length || 0,
    thumbnail: mockPhotosByMonth['2025-11']?.[0]?.url || ''
  },
  {
    id: 2,
    month: 10,
    year: 2025,
    date: '2025.10',
    photoCount: mockPhotosByMonth['2025-10']?.length || 0,
    thumbnail: mockPhotosByMonth['2025-10']?.[0]?.url || ''
  },
  {
    id: 3,
    month: 9,
    year: 2025,
    date: '2025.09',
    photoCount: mockPhotosByMonth['2025-09']?.length || 0,
    thumbnail: mockPhotosByMonth['2025-09']?.[0]?.url || ''
  },
  {
    id: 4,
    month: 8,
    year: 2025,
    date: '2025.08',
    photoCount: mockPhotosByMonth['2025-08']?.length || 0,
    thumbnail: mockPhotosByMonth['2025-08']?.[0]?.url || ''
  },
  {
    id: 5,
    month: 7,
    year: 2025,
    date: '2025.07',
    photoCount: mockPhotosByMonth['2025-07']?.length || 0,
    thumbnail: mockPhotosByMonth['2025-07']?.[0]?.url || ''
  },
  {
    id: 6,
    month: 6,
    year: 2025,
    date: '2025.06',
    photoCount: mockPhotosByMonth['2025-06']?.length || 0,
    thumbnail: mockPhotosByMonth['2025-06']?.[0]?.url || ''
  }
];
