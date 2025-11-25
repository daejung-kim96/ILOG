import React, { useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useDiaryStore } from "@/stores/diaryStore";
import { useAuthStore } from "@/stores/authStore";
import { ArrowLeft, PenLine, Trash2, User } from "lucide-react";

const DiaryDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const { diaries } = useDiaryStore();
  const { user } = useAuthStore();

  // TS6133 제거: setDiary 제거하고 diary만 사용
  const [diary] = useState(
    diaries.find((d) => d.id === Number(id))
  );

  // 날짜 포맷팅 함수
  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    const yyyy = date.getFullYear();
    const mm = String(date.getMonth() + 1).padStart(2, "0");
    const dd = String(date.getDate()).padStart(2, "0");
    const hh = String(date.getHours()).padStart(2, "0");
    const min = String(date.getMinutes()).padStart(2, "0");
    const ss = String(date.getSeconds()).padStart(2, "0");
    return `${yyyy}.${mm}.${dd} | ${hh} : ${min} : ${ss}`;
  };

  const handleDelete = async () => {
    if (window.confirm("정말로 이 일기를 삭제하시겠습니까?")) {
      alert("삭제되었습니다.");
      navigate("/diary");
    }
  };

  const handleEdit = () => {
    navigate(`/diary/edit/${id}`);
  };

  if (!diary) {
    return (
      <div className="w-full h-screen flex justify-center items-center bg-[#FFFDF5]">
        <div className="text-yellow-800 text-xl font-bold">
          일기를 찾을 수 없습니다. 😢
        </div>
      </div>
    );
  }

  const isMyDiary = user?.id === diary.userId;

  return (
    <div className="w-full min-h-screen bg-[#FFFDF5] flex flex-col items-center py-10">
      <div className="w-full max-w-[1100px] mb-4 px-2">
        <button
          onClick={() => navigate(-1)}
          className="flex items-center gap-2 text-yellow-800 font-bold text-xl hover:opacity-70 transition-opacity"
        >
          <ArrowLeft strokeWidth={3} /> 뒤로가기
        </button>
      </div>

      <div className="w-full max-w-[1120px] p-2.5 bg-amber-50 rounded-[10px] outline outline-2 outline-offset-[-1px] outline-yellow-800 flex flex-col justify-center items-center gap-4">
        
        {/* 제목 + 수정/삭제 */}
        <div className="w-full max-w-[1100px] h-auto min-h-[64px] px-5 py-3.5 bg-white rounded-[60px] outline outline-2 outline-offset-[-1px] outline-yellow-800 flex justify-between items-center gap-4">
          <div className="flex-1 text-center text-yellow-800 text-3xl md:text-4xl font-bold truncate px-4">
            {diary.title}
          </div>

          {isMyDiary && (
            <div className="shrink-0 flex justify-center items-center gap-2.5">
              <button
                onClick={handleEdit}
                className="w-24 h-10 bg-green-500 hover:bg-green-600 rounded-[90px] flex justify-center items-center gap-2 transition-colors"
              >
                <PenLine className="text-white w-5 h-5" strokeWidth={3} />
                <span className="text-amber-50 text-xl font-bold pt-0.5">
                  수정
                </span>
              </button>
              <button
                onClick={handleDelete}
                className="w-24 h-10 bg-red-500 hover:bg-red-600 rounded-[90px] flex justify-center items-center gap-2 transition-colors"
              >
                <Trash2 className="text-white w-5 h-5" strokeWidth={3} />
                <span className="text-amber-50 text-xl font-bold pt-0.5">
                  삭제
                </span>
              </button>
            </div>
          )}
        </div>

        {/* 작성자 + 날짜 */}
        <div className="w-full max-w-[1100px] h-auto min-h-[48px] px-5 py-2 bg-yellow-800 rounded-[60px] outline outline-2 outline-offset-[-1px] outline-yellow-800 flex flex-col md:flex-row justify-between items-center gap-2 md:gap-0">
          <div className="px-5 py-[5px] flex justify-center items-center gap-2.5">
            <div className="w-8 h-8 bg-white rounded-full flex justify-center items-center overflow-hidden">
              <User className="text-yellow-800 w-5 h-5" />
            </div>
            <div className="text-white text-2xl md:text-3xl font-bold pt-1">
              {diary.userName || "익명"}
            </div>
          </div>

          <div className="px-5 py-[5px] flex justify-center items-center">
            <div className="text-center text-white text-xl md:text-2xl font-bold pt-1">
              {formatDate(diary.createdAt)}
            </div>
          </div>
        </div>

        {/* 내용 */}
        <div className="w-full max-w-[1100px] min-h-[500px] px-8 py-8 bg-white rounded-[30px] outline outline-2 outline-offset-[-1px] outline-yellow-800 flex justify-start items-start overflow-hidden">
          <div className="w-full text-yellow-800 text-2xl md:text-3xl font-bold leading-relaxed whitespace-pre-wrap">
            {diary.content}
          </div>
        </div>

        {/* 이미지 / 비디오 */}
        {(diary.images?.length > 0 || diary.videos?.length > 0) && (
          <div className="w-full max-w-[1100px] mt-4">
            <h3 className="text-2xl font-bold text-yellow-800 mb-4 px-2">
              첨부된 미디어
            </h3>
            <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 p-4 bg-white rounded-[30px] outline outline-2 outline-offset-[-1px] outline-yellow-800">
              {diary.images.map((image) => (
                <div key={image.id} className="rounded-lg overflow-hidden aspect-square">
                  <img src={image.url} alt="diary image" className="w-full h-full object-cover" />
                </div>
              ))}
              {diary.videos.map((video) => (
                <div key={video.id} className="rounded-lg overflow-hidden aspect-square">
                  <video src={video.url} controls className="w-full h-full object-cover" />
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default DiaryDetail;
