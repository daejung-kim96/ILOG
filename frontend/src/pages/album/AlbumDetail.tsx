// src/pages/album/AlbumDetail.tsx
import { useState, useRef, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useGroupStore } from '@/stores/groupStore';
import {
  getDiaries,
  deletePhoto,
  uploadPhoto,
} from '@/api/diaryApi';

interface Photo {
  id: number;
  url: string;
  date: string;
}

const AlbumDetail = () => {
  const { year, month } = useParams<{ year: string; month: string }>();
  const navigate = useNavigate();
  const fileInputRef = useRef<HTMLInputElement>(null);

  const { currentGroup } = useGroupStore();

  const [photos, setPhotos] = useState<Photo[]>([]);
  const [isEditMode, setIsEditMode] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [uploading, setUploading] = useState(false);

  // ✅ URL 파라미터 검증
  if (!year || !month) {
    return (
      <div className="min-h-screen bg-gradient-to-b from-amber-50 to-orange-50 flex items-center justify-center">
        <div className="bg-red-100 rounded-3xl p-12 border-4 border-red-700 text-center">
          <p className="text-lg text-red-900 font-semibold">
            잘못된 URL입니다.
          </p>
          <button
            onClick={() => navigate('/album')}
            className="mt-4 bg-red-600 text-white px-6 py-2 rounded-full hover:bg-red-700"
          >
            앨범으로 돌아가기
          </button>
        </div>
      </div>
    );
  }

  const monthKey = `${year}-${month.padStart(2, '0')}`;

  // DB에서 해당 월의 모든 다이어리에서 사진 추출
  useEffect(() => {
    const fetchPhotos = async () => {
      if (!currentGroup) return;
      try {
        setLoading(true);
        // 모든 다이어리 불러오기
        const diaries = await getDiaries(currentGroup.id);
        // 해당 월의 다이어리만 필터링
        const filtered = diaries.filter((diary) => {
          const d = new Date(diary.createdAt);
          return d.getFullYear() === Number(year) && (d.getMonth() + 1) === Number(month);
        });
        // 모든 다이어리의 images를 평탄화
        const allPhotos = filtered.flatMap((diary) =>
          (diary.images || []).map((img) => ({
            id: img.id,
            url: img.url,
            date: diary.createdAt,
          }))
        );
        setPhotos(allPhotos);
        setError(null);
      } catch (err) {
        console.error('Failed to fetch photos:', err);
        setError(
          err instanceof Error
            ? err.message
            : '알 수 없는 오류가 발생했습니다.'
        );
        setPhotos([]);
      } finally {
        setLoading(false);
      }
    };
    fetchPhotos();
  }, [currentGroup, year, month]);

  // 🔴 사진 삭제 핸들러 (DB에서도 삭제)
  const handleDeletePhoto = async (photoId: number) => {
    try {
      // ✅ API 호출: 사진 삭제
      await deletePhoto(photoId);

      // 로컬 상태에서도 제거
      setPhotos(photos.filter(photo => photo.id !== photoId));
    } catch (err) {
      console.error('Failed to delete photo:', err);
      alert('사진 삭제 중 오류가 발생했습니다.');
    }
  };

  // 🔴 사진 추가 핸들러 (업로드 기능)
  const handleAddPhoto = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const files = event.target.files;
    if (!files || !currentGroup) return;

    try {
      setUploading(true);

      // 여러 파일을 순차적으로 업로드
      for (const file of Array.from(files)) {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('groupId', currentGroup.id.toString());
        formData.append('month', monthKey);
        formData.append('date', new Date().toISOString().split('T')[0]);

        try {
          // ✅ API 호출: 사진 업로드
          const uploadedPhoto = await uploadPhoto(formData);

          // 로컬 상태에 추가
          setPhotos(prev => [...prev, uploadedPhoto]);
        } catch (err) {
          console.error('Single photo upload failed:', err);
          alert(`${file.name} 업로드 중 오류가 발생했습니다.`);
        }
      }
    } catch (err) {
      console.error('Error processing files:', err);
      alert('파일 처리 중 오류가 발생했습니다.');
    } finally {
      setUploading(false);
      // 파일 입력 초기화
      event.target.value = '';
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-b from-amber-50 to-orange-50 py-10">
      <div className="max-w-6xl mx-auto px-6">
        {/* 상단 헤더 */}
        <div className="flex items-center justify-between mb-10">
          {/* 뒤로가기 버튼 */}
          <button
            onClick={() => navigate('/album')}
            className="bg-amber-800 text-white px-6 py-2 rounded-full
                     hover:bg-amber-900 transition-colors shadow-md
                     flex items-center gap-2"
          >
            <span>←</span>
            <span>목록으로</span>
          </button>

          {/* 제목 */}
          <h1 className="text-2xl font-bold bg-amber-800 text-white 
                       rounded-full px-10 py-3 shadow-md">
            {month}월
          </h1>

          {/* 수정하기 버튼 */}
          <button
            onClick={() => setIsEditMode(!isEditMode)}
            className={`px-6 py-2 rounded-full transition-colors shadow-md
                     ${
                       isEditMode
                         ? 'bg-green-600 hover:bg-green-700 text-white'
                         : 'bg-amber-800 hover:bg-amber-900 text-white'
                     }`}
          >
            {isEditMode ? '완료' : '수정하기'}
          </button>
        </div>

        {/* 로딩 상태 */}
        {loading && (
          <div className="max-w-4xl mx-auto">
            <div className="bg-amber-100 rounded-3xl p-12 border-4 border-amber-700 text-center">
              <p className="text-lg text-amber-900 font-semibold">
                사진을 불러오는 중...
              </p>
            </div>
          </div>
        )}

        {/* 에러 상태 */}
        {error && !loading && (
          <div className="max-w-4xl mx-auto">
            <div className="bg-red-100 rounded-3xl p-12 border-4 border-red-700 text-center">
              <p className="text-lg text-red-900 font-semibold">
                {error}
              </p>
            </div>
          </div>
        )}

        {/* 사진이 없을 때 */}
        {!loading && !error && photos.length === 0 && (
          <div className="max-w-4xl mx-auto">
            <div className="bg-amber-100 rounded-3xl p-12 border-4 border-amber-700">
              <div
                className="border-4 border-dashed border-amber-700 rounded-2xl 
                            bg-amber-50 p-16 text-center cursor-pointer
                            hover:bg-amber-100 transition-colors"
                onClick={() =>
                  isEditMode && fileInputRef.current?.click()
                }
              >
                <p className="text-xl text-amber-900 font-semibold mb-2">
                  이 달의 사진이 없습니다.
                </p>
                <p className="text-lg text-amber-700">
                  {isEditMode
                    ? '클릭하여 사진을 추가하세요!'
                    : '일기를 작성하면 사진이 자동으로 등록됩니다.'}
                </p>
              </div>
            </div>
          </div>
        )}

        {/* 사진 갤러리 */}
        {!loading && !error && photos.length > 0 && (
          <div className="bg-amber-100 rounded-3xl p-8 border-4 border-amber-700">
            <div className="grid grid-cols-4 gap-4">
              {photos.map(photo => (
                <div
                  key={photo.id}
                  className="aspect-square bg-white rounded-xl overflow-hidden
                           shadow-md hover:shadow-xl hover:scale-105
                           transition-all duration-300 cursor-pointer relative"
                >
                  <img
                    src={photo.url}
                    alt={`${photo.date} 사진`}
                    className="w-full h-full object-cover"
                  />

                  {/* 수정 모드일 때 삭제 버튼 표시 */}
                  {isEditMode && (
                    <button
                      onClick={() => handleDeletePhoto(photo.id)}
                      className="absolute top-2 right-2 bg-red-500 text-white
                               w-8 h-8 rounded-full hover:bg-red-600
                               flex items-center justify-center font-bold
                               shadow-lg z-10"
                    >
                      ✕
                    </button>
                  )}
                </div>
              ))}

              {/* 수정 모드일 때 사진 추가 버튼 */}
              {isEditMode && (
                <button
                  onClick={() => fileInputRef.current?.click()}
                  disabled={uploading}
                  className="aspect-square bg-white rounded-xl
                           border-4 border-dashed border-amber-700
                           hover:border-amber-900 hover:bg-amber-50
                           transition-all duration-300 cursor-pointer
                           flex flex-col items-center justify-center gap-2
                           disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  <div className="text-5xl text-amber-700">
                    {uploading ? '⏳' : '📷'}
                  </div>
                  <p className="text-amber-900 font-semibold text-sm">
                    {uploading
                      ? '업로드 중...'
                      : '클릭하여\n사진을 추가하세요'}
                  </p>
                </button>
              )}
            </div>

            {/* 숨겨진 파일 입력 */}
            <input
              ref={fileInputRef}
              type="file"
              accept="image/*"
              multiple
              onChange={handleAddPhoto}
              disabled={uploading}
              className="hidden"
            />

            {/* 사진 개수 표시 */}
            <div className="text-center mt-6">
              <p className="text-amber-900 font-semibold">
                총 {photos.length}장의 사진
              </p>
            </div>
          </div>
        )}

        {/* 위로가기 버튼 */}
        <button
          className="fixed bottom-8 right-8 bg-amber-800 text-white 
                   w-14 h-14 rounded-full shadow-lg
                   hover:bg-amber-900 transition-colors
                   flex items-center justify-center text-xl"
          onClick={() => window.scrollTo({ top: 0, behavior: 'smooth' })}
        >
          ↑
        </button>
      </div>
    </div>
  );
};

export default AlbumDetail;