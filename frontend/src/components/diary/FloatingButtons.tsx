import { ArrowUp, PenLine } from "lucide-react";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

export const FloatingButtons: React.FC = () => {
  const navigate = useNavigate();
  const [isVisible, setIsVisible] = useState<boolean>(false);

  useEffect(() => {
    const toggleVisibility = () => {
      // window.pageYOffset은 최신 브라우저에서 window.scrollY와 동일
      if (window.scrollY > 300) {
        setIsVisible(true);
      } else {
        setIsVisible(false);
      }
    };

    window.addEventListener("scroll", toggleVisibility);
    return () => window.removeEventListener("scroll", toggleVisibility);
  }, []);

  const scrollToTop = () => {
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const handleWriteClick = () => {
    navigate("/diary/write"); // 지정하신 경로로 이동
  };
  return (
    <div className="fixed bottom-10 right-10 z-50 flex flex-col items-end gap-4">
      {/* 위로 가기 버튼 */}
      <button
        onClick={scrollToTop}
        className={`w-12 h-12 bg-yellow-900/80 hover:bg-yellow-900 text-amber-50 rounded-full flex justify-center items-center shadow-lg transition-all duration-300 backdrop-blur-sm
          ${
            isVisible
              ? "opacity-100 translate-y-0"
              : "opacity-0 translate-y-4 pointer-events-none"
          }`}
        aria-label="맨 위로"
      >
        <ArrowUp size={24} strokeWidth={3} />
      </button>

      {/* 글쓰기 버튼 */}
      <button
        onClick={handleWriteClick}
        className="flex items-center gap-3 bg-[#3E2723] hover:bg-[#2a1a17] text-white px-8 py-4 rounded-full shadow-xl transition-all hover:scale-105 active:scale-95"
      >
        <PenLine size={24} />
        <span className="text-xl font-bold font-['Pretendard']">글쓰기</span>
      </button>
    </div>
  );
};
