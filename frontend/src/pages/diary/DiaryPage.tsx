import { DiaryCard } from "@/components/diary/DiaryCard";
import { FloatingButtons } from "@/components/diary/FloatingButtons";
import { useDiaryStore } from "@/stores/diaryStore";
import { useGroupStore } from "@/stores/groupStore";
import { useAuthStore } from "@/stores/authStore";
import { TabType, DiaryStatus } from "@/types/diary";
import { Check, Search } from "lucide-react";
import React from "react";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

const DiaryPage: React.FC = () => {
  const { currentGroup } = useGroupStore();
  const { diaries, fetchDiaries, isLoading, error } = useDiaryStore();
  const { user } = useAuthStore();
  const currentUserId = user?.id;
  const [activeTab, setActiveTab] = useState<TabType>("all");
  const navigate = useNavigate();

  // 그룹 정보가 있거나 변경될 때 데이터 Fetch
  useEffect(() => {
    if (currentGroup?.id) {
      fetchDiaries(currentGroup.id);
    }
  }, [currentGroup, fetchDiaries]);

  // Filter diaries based on activeTab
  const filteredDiaries = diaries.filter((diary) => {
    if (activeTab === "my") {
      if (!currentUserId) return false;
      // "내 글만 보기" (My posts only)
      return diary.userId === Number(currentUserId);
    } else if (activeTab === "private") {
      // "나만 보기 글 목록" (Private posts only)
      return diary.status === DiaryStatus.PRIVATE;
    }
    // "전체 보기" (All posts)
    return true;
  });

  const tabs: { id: TabType; label: string }[] = [
    { id: "all", label: "전체 보기" },
    { id: "my", label: "내 글만 보기" },
    { id: "private", label: "나만 보기 글 목록" },
  ];

  return (
    <div className="w-full min-h-screen bg-[#FFFDF5] flex flex-col items-center py-10 overflow-x-hidden">
      <div className="w-full max-w-[1200px] flex flex-col items-center gap-6 relative animate-in fade-in duration-500">
        {/* 검색바 영역 */}
        <div className="w-full max-w-[1160px] h-16 px-6 bg-white rounded-[90px] outline outline-[5px] outline-offset-[-2.50px] outline-yellow-800 flex justify-between items-center">
          <input
            type="text"
            placeholder="검색어를 입력하세요"
            className="w-full h-full bg-transparent text-yellow-800 text-3xl font-normal font-['Pretendard'] placeholder-yellow-800/50 outline-none"
          />
          <Search
            className="text-yellow-800 cursor-pointer hover:opacity-70"
            size={36}
            strokeWidth={3}
          />
        </div>

        {/* 탭 네비게이션 영역 */}
        <div className="w-full max-w-[1160px] h-16 px-5 bg-amber-50 rounded-[30px] outline outline-[5px] outline-offset-[-2.50px] outline-yellow-800 flex justify-between items-center select-none">
          {tabs.map((tab, index) => {
            const isActive = activeTab === tab.id;
            return (
              <React.Fragment key={tab.id}>
                <div
                  onClick={() => setActiveTab(tab.id)}
                  className={`flex-1 h-12 flex justify-center items-center gap-3 cursor-pointer rounded-full transition-colors ${
                    isActive ? "bg-yellow-800/10" : "hover:bg-yellow-800/5"
                  }`}
                >
                  <div
                    className={`w-6 h-6 flex items-center justify-center bg-white border-2 border-yellow-800 transition-all ${
                      isActive ? "bg-yellow-100" : ""
                    }`}
                  >
                    {isActive && (
                      <Check
                        size={18}
                        className="text-yellow-800"
                        strokeWidth={4}
                      />
                    )}
                  </div>
                  <div className="text-yellow-800 text-2xl font-bold font-['Pretendard']">
                    {tab.label}
                  </div>
                </div>
                {index < tabs.length - 1 && (
                  <div className="w-[2px] h-8 bg-yellow-800/30"></div>
                )}
              </React.Fragment>
            );
          })}
        </div>

        {/* 일기 리스트 영역 */}
        <div className="w-full p-2.5 flex flex-col items-center gap-2.5 pb-32">
          {isLoading && (
            <div className="flex flex-col items-center justify-center py-20 gap-4">
              <div className="w-12 h-12 border-4 border-yellow-800 border-t-transparent rounded-full animate-spin"></div>
              <div className="text-yellow-800 font-bold text-xl animate-pulse">
                일기를 불러오는 중입니다...
              </div>
            </div>
          )}

          {error && (
            <div className="text-red-500 font-bold py-10 text-xl">{error}</div>
          )}

          {!isLoading && !error && filteredDiaries.length === 0 && (
            <div className="text-yellow-800/50 font-bold py-20 text-xl">
              아직 작성된 일기가 없어요. 첫 일기를 써보세요! 📝
            </div>
          )}

          {!isLoading &&
            filteredDiaries.map((diary) => (
              <DiaryCard
                key={diary.id}
                {...diary}
                onClick={() => navigate(`/diary/${diary.id}`)}
              />
            ))}
        </div>

        <FloatingButtons />
      </div>
    </div>
  );
};

export default DiaryPage;
