import { DiaryCardProps, ThemeColor, MediaType } from "@/types/diary";
import { format } from "date-fns";

export const DiaryCard: React.FC<DiaryCardProps> = ({
  createdAt,
  title,
  content,
  themeColor,
  images,
  videos,
  onClick,
}) => {
  const formattedDate = (() => {
    try {
      // createdAt이 문자열이라면 new Date()로 변환 후 포맷팅
      return format(new Date(createdAt), "yyyy-MM-dd");
    } catch (e) {
      return createdAt; // 에러 발생 시 원본 반환
    }
  })();
  // Record 유틸리티 타입을 사용하여 키와 값의 타입을 명시
  const colorVariants: Record<ThemeColor, { text: string; bg: string }> = {
    rose: { text: "text-rose-400", bg: "bg-red-200" },
    green: { text: "text-green-500", bg: "bg-green-500" },
    amber: { text: "text-amber-500", bg: "bg-amber-500" },
  };

  const currentTheme = colorVariants[themeColor];

  // Determine media to display as thumbnail
  const thumbnailMedia = images && images.length > 0 ? images[0] : (videos && videos.length > 0 ? videos[0] : null);

  return (
    <div
      onClick={onClick}
      className="w-full max-w-[1160px] h-44 p-5 bg-amber-50 rounded-[60px] outline outline-[5px] outline-offset-[-2.50px] outline-yellow-800 flex justify-between items-center mb-5 shadow-sm hover:translate-y-[-4px] transition-all duration-200 cursor-pointer hover:shadow-md"
    >
      {/* 날짜 */}
      <div
        className={`w-64 h-36 flex justify-center items-center text-5xl font-bold font-['Pretendard'] ${currentTheme.text}`}
      >
        {formattedDate}
      </div>

      {/* 텍스트 내용 */}
      <div className="flex-1 h-36 flex flex-col justify-center items-center gap-2 px-4 overflow-hidden">
        <div
          className={`w-full text-center text-4xl lg:text-[2.75rem] font-bold font-['Pretendard'] truncate ${currentTheme.text}`}
        >
          {title}
        </div>
        <div
          className={`w-full text-center text-lg font-normal font-['Pretendard'] truncate ${currentTheme.text}`}
        >
          {content}
        </div>
      </div>

      {/* 썸네일 이미지/비디오 박스 */}
      <div
        className={`w-36 h-36 shrink-0 rounded-[60px] outline outline-[5px] outline-offset-[-2.50px] outline-yellow-800 overflow-hidden ${!thumbnailMedia ? currentTheme.bg : ''}`}
      >
        {thumbnailMedia && thumbnailMedia.type === MediaType.IMAGE && (
          <img src={thumbnailMedia.url} alt="thumbnail" className="w-full h-full object-cover" />
        )}
        {thumbnailMedia && thumbnailMedia.type === MediaType.VIDEO && (
          <video src={thumbnailMedia.url} className="w-full h-full object-cover" muted />
        )}
      </div>
    </div>
  );
};
