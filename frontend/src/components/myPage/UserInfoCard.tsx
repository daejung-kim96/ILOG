import { useAuthStore } from "@/stores/authStore";
import { useGroupStore } from "@/stores/groupStore";
import { Scissors, X } from "lucide-react";
import UserStatItem from "./UserStatItem";

const UserInfoCard = () => {
  const { user } = useAuthStore();
  const { groups } = useGroupStore();

  const joinedGroupCount = groups.length;
  const writtenPostCount = 31; // 지금 하드 코딩인데 나중에 글 list 추가예정
  const createdReportCount = 4; // 얘도 레포트 list로 추가예정

  const handleWithdraw = () => {
    alert("회원 탈퇴 기능은 준비중입니다.");
  };

  return (
      <div
          className="
        w-full max-w-[550px]
        h-auto sm:h-[350px]
        mx-auto
        p-2.5
        rounded-[20px]
        outline outline-2 outline-offset-[-1px] outline-yellow-800
        flex flex-col justify-start items-center gap-3
        bg-amber-50
      "
      >
        {/* 헤더 */}
        <div
            className="
          w-full max-w-[520px]
          inline-flex justify-center items-center gap-6 sm:gap-14
        "
        >
          <div className="w-20 sm:w-36 h-7" />

          <div className="w-24 h-7 bg-yellow-800 rounded-[90px] flex justify-center items-center overflow-hidden">
          <span className="text-amber-50 text-lg sm:text-xl font-bold font-['Pretendard']">
            내 정보
          </span>
          </div>

          <div className="w-28 sm:w-36 flex justify-center items-center gap-1.5 sm:gap-2.5 overflow-hidden">
            <button className="w-16 h-7 py-0.5 bg-green-500 rounded-[90px] flex justify-center items-center gap-1 text-amber-50 hover:bg-green-600 transition-colors">
              <Scissors size={14} />
              <span className="text-sm sm:text-base font-bold font-['Pretendard']">
              수정
            </span>
            </button>

            <button
                onClick={handleWithdraw}
                className="w-16 h-7 bg-red-500 rounded-[90px] flex justify-center items-center gap-1 text-amber-50 hover:bg-red-600 transition-colors"
            >
              <X size={14} />
              <span className="text-sm sm:text-base font-bold font-['Pretendard']">
              탈퇴
            </span>
            </button>
          </div>
        </div>

        {/* 프로필 + 이름 */}
        <div className="w-full h-32 flex flex-col justify-center items-center gap-2">
          <div className="w-24 h-24 bg-yellow-800 rounded-full flex items-center justify-center overflow-hidden shadow">
            <img
                className="w-full h-full object-cover"
                src={user?.picture ?? "/profile.png"}
                alt="프로필 이미지"
            />
          </div>

          {/* 이름 */}
          <div className="w-full max-w-[260px] h-9 px-4 bg-white rounded-[10px] outline outline-1 outline-offset-[-1px] outline-yellow-800 flex justify-center items-center overflow-hidden">
          <span className="w-full text-center text-yellow-800 text-lg sm:text-xl font-bold font-['Pretendard'] truncate">
            {user?.name ?? "사용자"}
          </span>
          </div>
        </div>

        {/* 하단 통계 */}
        <div className="w-full flex-1 px-2.5 pt-2.5 pb-0 bg-amber-50 flex justify-center items-start gap-2.5">
          <UserStatItem title="가입된 그룹" value={joinedGroupCount} />
          <UserStatItem title="작성한 글" value={writtenPostCount} />
          <UserStatItem title="생성된 레포트" value={createdReportCount} />
        </div>
      </div>
  );
};

export default UserInfoCard;
