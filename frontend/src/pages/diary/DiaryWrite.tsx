import React, { useState, useRef } from "react";
import { useNavigate } from "react-router-dom"; // ✅ React Router 사용
import {
  ChevronDown,
  Bold,
  Italic,
  Underline,
  AlignLeft,
  AlignCenter,
  AlignRight,
  Image as ImageIcon,
  Video,
  Eye,
  EyeOff,
  X,
} from "lucide-react";

// ✅ 타입 및 스토어, API import (경로가 맞는지 확인해주세요)
import { DiaryStatus, MediaType, Attachment } from "@/types/diary";
import { useGroupStore } from "@/stores/groupStore";
import { createDiary } from "@/api/diaryApi";
import { useAuthStore } from "@/stores/authStore";

export default function DiaryEditor() {
  // ✅ Next.js의 useRouter 대신 useNavigate 사용
  const navigate = useNavigate();

  // 스토어에서 현재 그룹 정보 가져오기
  const { currentGroup } = useGroupStore();
  const { user } = useAuthStore(); // Get user from auth store

  // 상태 관리
  const [title, setTitle] = useState<string>(""); // 제목
  const [content, setContent] = useState<string>(""); // 내용
  const [status, setStatus] = useState<DiaryStatus>(DiaryStatus.PUBLIC); // 공개여부
  const [attachments, setAttachments] = useState<Attachment[]>([]); // 첨부파일

  // 파일 인풋 제어용 Ref
  const imageInputRef = useRef<HTMLInputElement>(null);
  const videoInputRef = useRef<HTMLInputElement>(null);

  // 버튼 클릭 트리거
  const handleImageBtnClick = () => imageInputRef.current?.click();
  const handleVideoBtnClick = () => videoInputRef.current?.click();

  // 파일 선택 핸들러 (미리보기 URL 생성)
  const handleFileChange = (
    e: React.ChangeEvent<HTMLInputElement>,
    type: MediaType
  ) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const previewUrl = URL.createObjectURL(file);

    const newAttachment: Attachment = {
      id: Date.now(),
      type,
      url: previewUrl,
      file,
    };

    setAttachments((prev) => [...prev, newAttachment]);

    // 같은 파일 재선택 가능하도록 초기화
    e.target.value = "";
  };

  // 첨부파일 삭제 핸들러
  const removeAttachment = (id: number) => {
    setAttachments((prev) => prev.filter((item) => item.id !== id));
  };

  // ✅ 작성 완료 및 서버 전송 핸들러
  const handleSubmit = async () => {
    // 1. 유효성 검사
    if (!currentGroup) {
      alert("그룹 정보가 없습니다.");
      return;
    }
    if (!user?.name) {
      alert("사용자 이름을 찾을 수 없습니다.");
      return;
    }
    if (!title.trim()) {
      alert("제목을 입력해주세요.");
      return;
    }
    if (!content.trim()) {
      alert("내용을 입력해주세요.");
      return;
    }

    try {
      // 2. FormData 생성
      const formData = new FormData();

      // 2-1. 텍스트 데이터 추가
      formData.append("groupId", currentGroup.id.toString());
      formData.append("userName", user.name);
      formData.append("title", title);
      formData.append("content", content);
      formData.append("status", status); // "PUBLISHED" or "DRAFT"

      // 2-2. 파일 데이터 분리하여 추가
      attachments.forEach((att) => {
        if (att.type === MediaType.IMAGE) {
          formData.append("images", att.file);
        } else if (att.type === MediaType.VIDEO) {
          formData.append("videos", att.file);
        }
      });

      // 3. API 호출
      await createDiary(formData);

      alert("일기가 성공적으로 작성되었습니다!");
      navigate(-1); // ✅ 뒤로 가기 (React Router)
    } catch (error) {
      console.error("Diary creation failed:", error);
      alert("일기 작성 중 오류가 발생했습니다.");
    }
  };

  return (
    <div className="w-full h-screen flex justify-center items-center bg-[#FDFCF8]">
      {/* 숨겨진 Input */}
      <input
        type="file"
        ref={imageInputRef}
        className="hidden"
        accept="image/*"
        onChange={(e) => handleFileChange(e, MediaType.IMAGE)}
      />
      <input
        type="file"
        ref={videoInputRef}
        className="hidden"
        accept="video/*"
        onChange={(e) => handleFileChange(e, MediaType.VIDEO)}
      />

      <div className="w-[1200px] px-5 py-2.5 rounded-[10px] bg-transparent flex flex-col justify-center items-center gap-4">
        {/* 제목 입력란 */}
        <div className="w-[1100px] h-12 bg-white rounded-[10px] outline outline-2 outline-yellow-800 flex items-center px-5">
          <input
            type="text"
            placeholder="제목을 입력하세요..."
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            className="w-full text-yellow-800 text-lg font-bold font-['Pretendard'] placeholder-yellow-800/50 outline-none bg-transparent"
          />
        </div>

        {/* 툴바 */}
        <div className="w-[1100px] px-3 py-2 bg-white rounded-[10px] outline outline-2 outline-yellow-800 flex justify-between items-center">
          {/* 왼쪽 툴바 그룹 */}
          <div className="flex items-center gap-3.5">
            <button className="w-24 h-8 px-2 bg-white rounded-[10px] outline outline-2 outline-yellow-800 flex justify-between items-center gap-0.5 text-yellow-800">
              <span className="text-sm font-bold font-['Pretendard'] ml-2">
                16px
              </span>
              <ChevronDown size={16} strokeWidth={3} />
            </button>
            <div className="w-0.5 h-5 bg-yellow-800" />

            <div className="flex gap-2">
              <button className="w-8 h-8 bg-white rounded-[10px] outline outline-2 outline-yellow-800 flex justify-center items-center text-yellow-800 hover:bg-yellow-50">
                <Bold size={18} strokeWidth={3} />
              </button>
              <button className="w-8 h-8 bg-white rounded-[10px] outline outline-2 outline-yellow-800 flex justify-center items-center text-yellow-800 hover:bg-yellow-50">
                <Italic size={18} strokeWidth={3} />
              </button>
              <button className="w-8 h-8 bg-white rounded-[10px] outline outline-2 outline-yellow-800 flex justify-center items-center text-yellow-800 hover:bg-yellow-50">
                <Underline size={18} strokeWidth={3} />
              </button>
            </div>
            <div className="w-0.5 h-5 bg-yellow-800" />

            <div className="flex gap-2">
              <button className="w-8 h-8 bg-white rounded-[10px] outline outline-2 outline-yellow-800 flex justify-center items-center text-yellow-800 hover:bg-yellow-50">
                <AlignLeft size={18} strokeWidth={3} />
              </button>
              <button className="w-8 h-8 bg-white rounded-[10px] outline outline-2 outline-yellow-800 flex justify-center items-center text-yellow-800 hover:bg-yellow-50">
                <AlignCenter size={18} strokeWidth={3} />
              </button>
              <button className="w-8 h-8 bg-white rounded-[10px] outline outline-2 outline-yellow-800 flex justify-center items-center text-yellow-800 hover:bg-yellow-50">
                <AlignRight size={18} strokeWidth={3} />
              </button>
            </div>
            <div className="w-0.5 h-5 bg-yellow-800" />

            <button
              onClick={handleImageBtnClick}
              className="w-32 h-8 px-2 bg-white rounded-[10px] outline outline-2 outline-yellow-800 flex justify-start items-center gap-2 hover:bg-yellow-50 active:bg-yellow-100 transition-colors"
            >
              <ImageIcon
                size={18}
                className="text-yellow-800 ml-1"
                strokeWidth={2.5}
              />
              <span className="text-yellow-800 text-sm font-bold font-['Pretendard']">
                이미지 첨부
              </span>
            </button>
            <button
              onClick={handleVideoBtnClick}
              className="w-32 h-8 px-2 bg-white rounded-[10px] outline outline-2 outline-yellow-800 flex justify-start items-center gap-2 hover:bg-yellow-50 active:bg-yellow-100 transition-colors"
            >
              <Video
                size={18}
                className="text-yellow-800 ml-1"
                strokeWidth={2.5}
              />
              <span className="text-yellow-800 text-sm font-bold font-['Pretendard']">
                영상 첨부
              </span>
            </button>
          </div>

          {/* 오른쪽 공개 설정 */}
          <div className="flex items-center gap-2">
            <button
              onClick={() => setStatus(DiaryStatus.PRIVATE)}
              className={`w-28 h-8 rounded-[10px] outline outline-2 outline-gray-700 flex justify-center items-center gap-2 transition-colors ${
                status === DiaryStatus.PRIVATE
                  ? "bg-red-500 text-white"
                  : "bg-white text-gray-500"
              }`}
            >
              <EyeOff size={16} strokeWidth={2.5} />
              <span className="text-sm font-bold font-['Pretendard']">
                나만 보기
              </span>
            </button>
            <button
              onClick={() => setStatus(DiaryStatus.PUBLIC)}
              className={`w-28 h-8 rounded-[10px] outline outline-2 outline-stone-900 flex justify-center items-center gap-2 transition-colors ${
                status === DiaryStatus.PUBLIC
                  ? "bg-green-500 text-white"
                  : "bg-white text-gray-500"
              }`}
            >
              <Eye size={16} strokeWidth={2.5} />
              <span className="text-sm font-bold font-['Pretendard']">
                전체 공개
              </span>
            </button>
          </div>
        </div>

        {/* 본문 및 미리보기 영역 */}
        <div className="w-[1100px] h-[540px] bg-white rounded-[10px] outline outline-2 outline-yellow-800 overflow-hidden flex flex-col">
          <textarea
            value={content}
            onChange={(e) => setContent(e.target.value)}
            className="w-full flex-1 p-5 resize-none outline-none text-yellow-800 text-base font-normal font-['Pretendard'] placeholder-yellow-800/50 bg-transparent"
            placeholder="오늘의 일기를 작성해주세요..."
          />

          {attachments.length > 0 && (
            <div className="w-full h-40 border-t-2 border-yellow-800/20 bg-yellow-50/50 p-4 flex gap-4 overflow-x-auto items-center">
              {attachments.map((item) => (
                <div key={item.id} className="relative flex-shrink-0 group">
                  <div className="w-32 h-32 rounded-lg outline outline-2 outline-yellow-800 overflow-hidden bg-white">
                    {item.type === MediaType.IMAGE ? (
                      <img
                        src={item.url}
                        alt="attachment"
                        className="w-full h-full object-cover"
                      />
                    ) : (
                      <video
                        src={item.url}
                        className="w-full h-full object-cover"
                      />
                    )}
                  </div>
                  <button
                    onClick={() => removeAttachment(item.id)}
                    className="absolute -top-2 -right-2 w-6 h-6 bg-red-500 rounded-full outline outline-1 outline-white flex justify-center items-center shadow-md hover:bg-red-600 transition-colors"
                  >
                    <X size={14} color="white" strokeWidth={3} />
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* 하단 버튼 */}
        <div className="w-[1100px] flex justify-end items-center gap-2.5 mt-2">
          <button
            onClick={() => navigate(-1)} // ✅ 취소 시 뒤로 가기
            className="w-24 h-9 bg-white rounded-full outline outline-2 outline-yellow-800 flex justify-center items-center hover:bg-gray-50"
          >
            <span className="text-stone-500 text-base font-bold font-['Pretendard']">
              취소
            </span>
          </button>
          <button
            onClick={handleSubmit} // ✅ 작성완료 시 핸들러 호출
            className="w-24 h-9 bg-blue-500 rounded-full outline outline-2 outline-black flex justify-center items-center hover:bg-blue-600"
          >
            <span className="text-white text-base font-bold font-['Pretendard']">
              작성완료
            </span>
          </button>
        </div>
      </div>
    </div>
  );
}
