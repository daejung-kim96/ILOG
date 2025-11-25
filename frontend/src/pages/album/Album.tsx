// src/pages/album/Album.tsx
import { useNavigate } from 'react-router-dom';
import { useState, useEffect } from 'react';
import { mockUser } from '../../mocks/mockData';
import { getDiaries } from '../../api/diaryApi';
import { DiaryData, Attachment } from '../../types/diary';
import { useGroupStore } from '../../stores/groupStore';

// 현재 디비 없어서 로컬에서 직접 입력한 데이터를 가져와서 앨범 썸네일 넣었음
// 디비 만들어지면 api 요청해서 데이터 랜더링 할 수 있도록 로직 수정 필요

interface AlbumItem {
  id: string;
  year: number;
  month: number;
  date: string;
  thumbnail: string;
  photoCount: number;
}

interface AlbumProps {
  isPreview?: boolean;
  maxItems?: number;
}

const Album = ({ isPreview = false, maxItems = 2 }: AlbumProps) => {
  const navigate = useNavigate();
  const [albumList, setAlbumList] = useState<AlbumItem[]>([]);
  
  const currentGroup = useGroupStore((state) => state.currentGroup);
  useEffect(() => {
    const fetchAlbums = async () => {
      if (!currentGroup) {
        setAlbumList([]);
        return;
      }
      try {
        const diaries: DiaryData[] = await getDiaries(currentGroup.id);
        // Group by year/month, collect images from all diaries in the month
        const albumMap = new Map<string, { year: number; month: number; images: Attachment[] }>();
        diaries.forEach((diary) => {
          const date = new Date(diary.createdAt);
          const year = date.getFullYear();
          const month = date.getMonth() + 1;
          const key = `${year}-${month}`;
          if (!albumMap.has(key)) {
            albumMap.set(key, { year, month, images: [] });
          }
          if (diary.images && diary.images.length > 0) {
            albumMap.get(key)!.images.push(...diary.images);
          }
        });
        // Only keep months with at least one image in any diary
        const albums: AlbumItem[] = Array.from(albumMap.values())
          .filter((item) => item.images.length >= 1)
          .map((item) => ({
            id: `${item.year}-${item.month}`,
            year: item.year,
            month: item.month,
            date: `${item.year}.${String(item.month).padStart(2, '0')}`,
            thumbnail: item.images[0]?.url || '',
            photoCount: item.images.length,
          }))
          .sort((a, b) => {
            if (b.year !== a.year) return b.year - a.year;
            return b.month - a.month;
          });
        setAlbumList(albums);
      } catch (e) {
        setAlbumList([]);
      }
    };
    fetchAlbums();
  }, [currentGroup]);
  
  const handleAlbumClick = (year: number, month: number) => {
    navigate(`/album/${year}/${month}`);
  };

  const handleViewMore = () => {
    navigate('/album');
  };

  // 미리보기 모드일 때는 maxItems만큼만 표시
  const displayAlbums = isPreview ? albumList.slice(0, maxItems) : albumList;
  
  return (
    <div className={`bg-gradient-to-b from-amber-50 to-orange-50 ${isPreview ? 'py-8' : 'min-h-screen py-10'}`}>
      <div className="max-w-3xl mx-auto px-6">
        <div className="text-center mb-10">
          <h1 className="text-2xl font-bold bg-amber-800 text-white 
                         rounded-full px-10 py-3 inline-block shadow-md">
            우리 {mockUser.childName}의 앨범
          </h1>
        </div>

        {albumList.length === 0 ? (
          <div className="max-w-4xl mx-auto">
            <div className="bg-amber-100 rounded-3xl p-12 border-4 border-amber-700">
              <div className="border-4 border-dashed border-amber-700 rounded-2xl 
                            bg-amber-50 p-16 text-center">
                <p className="text-xl text-amber-900 font-semibold mb-2">
                  앨범이 없습니다.
                </p>
                <p className="text-lg text-amber-700">
                  월별 사진이 10개 이상 모이면 앨범이 자동으로 생성됩니다.
                </p>
              </div>
            </div>
          </div>
        ) : (
          <>
            <div className={`grid ${isPreview ? 'grid-cols-2' : 'grid-cols-4'} gap-6 mb-8`}>
              {displayAlbums.map(album => (
                <div 
                  key={album.id}
                  onClick={() => handleAlbumClick(album.year, album.month)}
                  className="bg-amber-100 rounded-3xl p-5 border-4 
                           border-amber-700 cursor-pointer 
                           hover:shadow-xl hover:scale-105 
                           transition-all duration-300"
                >
                  <div className="bg-white rounded-2xl p-4 mb-4 shadow-inner">
                    <div className="relative">
                      <div className="w-full aspect-square bg-purple-200 
                                    rounded-xl flex items-center justify-center overflow-hidden">
                        {album.thumbnail ? (
                          <img 
                            src={album.thumbnail}
                            alt={`${album.year}년 ${album.month}월 앨범`}
                            className="w-full h-full object-cover"
                          />
                        ) : (
                          <div className="text-amber-700">사진 없음</div>
                        )}
                      </div>
                    </div>
                  </div>
                  
                  <div className="text-center">
                    <p className="font-semibold text-lg text-amber-900">
                      {album.month}월의 {mockUser.childName}이
                    </p>
                    <p className="text-sm text-amber-700 mt-1">
                      {album.date}
                    </p>
                  </div>
                </div>
              ))}
            </div>

            {isPreview ? (
              <div className="text-center">
                <button 
                  onClick={handleViewMore}
                  className="bg-amber-800 text-white px-8 py-3 
                           rounded-full font-semibold
                           hover:bg-amber-900 transition-colors
                           shadow-md flex items-center gap-2 mx-auto"
                >
                  <span>앨범 전체보기</span>
                  <span>→</span>
                </button>
              </div>
            ) : (
              <div className="text-center">
                <button className="bg-amber-800 text-white px-8 py-3 
                                 rounded-full font-semibold
                                 hover:bg-amber-900 transition-colors
                                 shadow-md flex items-center gap-2 mx-auto">
                  <span>▼</span>
                  <span>더보기</span>
                </button>
              </div>
            )}
          </>
        )}

        {!isPreview && (
          <button 
            className="fixed bottom-8 right-8 bg-amber-800 text-white 
                     w-14 h-14 rounded-full shadow-lg
                     hover:bg-amber-900 transition-colors
                     flex items-center justify-center text-xl
                     z-50"
            onClick={() => window.scrollTo({ top: 0, behavior: 'smooth' })}
          >
            ↑
          </button>
        )}
      </div>
    </div>
  );
};

export default Album;