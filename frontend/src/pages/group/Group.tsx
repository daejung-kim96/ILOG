import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { ArrowRight } from "lucide-react";
import { createGroup, inviteGroup } from "@/api/groupApi";
import { useGroupStore } from "@/stores/groupStore";

const Group = () => {
  const navigate = useNavigate();
  const { fetchGroups } = useGroupStore();

  const [childName, setChildName] = useState("");
  const [inviteCode, setInviteCode] = useState("");

  // 1. 새 그룹 생성
  const handleCreateGroup = async () => {
    if (!childName.trim()) {
      alert("아이 이름을 입력해주세요.");
      return;
    }

    try {
      const data = await createGroup(childName);
      console.log("그룹 생성 성공:", data);
      await fetchGroups();
      navigate("/mypage");
    } catch (error) {
      console.error("그룹 생성 실패:", error);
      alert("그룹 생성 중 오류가 발생했습니다.");
    }
  };

  // 2. 기존 그룹 참가
  const handleJoinGroup = async () => {
    if (!inviteCode.trim()) {
      alert("초대 코드를 입력해주세요.");
      return;
    }

    try {
      const data = await inviteGroup(inviteCode);
      console.log("그룹 참가 성공:", data);
      await fetchGroups();
      navigate("/mypage");
    } catch (error) {
      console.error("그룹 참가 실패:", error);
      alert("유효하지 않은 초대 코드이거나 참가에 실패했습니다.");
    }
  };

  return (
      // 전체 배경 + 가운데 정렬
      <div className="w-full min-h-[calc(100vh-8rem)] flex justify-center items-center py-12 bg-amber-50">
        {/* 안쪽 1100px 컨테이너 */}
        <div className="w-full max-w-[1100px] px-4 md:px-8">
          {/* 넓을 땐 2열, 좁아지면 1열로 쌓임 */}
          <div className="grid gap-10 md:grid-cols-2">
            {/* ================= 새 그룹 생성 카드 ================= */}
            <section className="w-full max-w-[500px] mx-auto px-6 py-5 bg-amber-50 rounded-[90px] outline outline-[5px] outline-offset-[-2.5px] outline-yellow-800 flex flex-col items-center gap-4">
              {/* 로고 */}
              <img
                  className="w-40 h-40 md:w-64 md:h-64 object-contain"
                  src="/logo.png"
                  alt="아이로그 아이콘"
              />

              {/* 입력 + 텍스트 + 버튼 */}
              <div className="w-full flex flex-col items-center gap-3 mt-2">
                {/* 아이 이름 입력 박스 */}
                <div className="w-full flex justify-center">
                  <div className="w-full max-w-xs h-12 md:h-14 py-2 md:py-3 bg-white rounded-2xl outline outline-2 outline-offset-[-2px] outline-stone-900 flex items-center px-2">
                    <input
                        type="text"
                        placeholder="아이 이름을 입력해주세요"
                        className="
                      w-full text-center
                      text-yellow-800 text-xl md:text-2xl font-bold font-['Pretendard']
                      bg-transparent outline-none
                      placeholder:text-gray-300
                    "
                        value={childName}
                        onChange={(e) => setChildName(e.target.value)}
                        onKeyDown={(e) => e.key === "Enter" && handleCreateGroup()}
                    />
                  </div>
                </div>

                {/* “으로 새 그룹 생성하기” + 버튼 */}
                <div className="inline-flex justify-center items-center gap-3 mt-1">
                  <div className="px-4 h-10 md:h-12 rounded-[30px] flex items-center">
                  <span className="text-center text-yellow-800 text-2xl md:text-3xl font-bold font-['Pretendard'] whitespace-nowrap">
                    으로 새 그룹 생성하기
                  </span>
                  </div>
                  <button
                      onClick={handleCreateGroup}
                      className="w-10 h-10 md:w-14 md:h-14 p-2.5 bg-yellow-800 rounded-2xl outline outline-2 outline-offset-[-1px] outline-stone-900 flex justify-center items-center hover:bg-yellow-900 transition-colors"
                  >
                    <ArrowRight size={28} color="#FCEFB4" />
                  </button>
                </div>
              </div>
            </section>

            {/* ================= 기존 그룹 들어가기 카드 ================= */}
            <section className="w-full max-w-[500px] mx-auto px-6 py-5 bg-amber-50 rounded-[90px] outline outline-[5px] outline-offset-[-2.5px] outline-yellow-800 flex flex-col items-center gap-4">
              {/* 로고 */}
              <img
                  className="w-40 h-40 md:w-64 md:h-64 object-contain"
                  src="/logo.png"
                  alt="아이로그 아이콘"
              />

              {/* 제목 + 코드 입력 */}
              <div className="w-full flex flex-col items-center gap-4 mt-2">
                {/* 제목 */}
                <div className="w-full text-center text-yellow-800 text-2xl md:text-3xl font-bold font-['Pretendard']">
                  기존 그룹 참가하기
                </div>

                {/* 입력박스 + 화살표 버튼 */}
                <div className="w-full flex justify-center">
                  <div className="flex items-center gap-3">
                    {/* 코드 입력 박스 */}
                    <div className="w-[220px] md:w-[260px] h-10 md:h-12 px-3 md:px-4 bg-white rounded-2xl outline outline-2 outline-offset-[-2px] outline-stone-900 flex items-center">
                      <input
                          type="text"
                          placeholder="초대코드를 입력해주세요"
                          className="
                        w-full
                        text-yellow-800 text-lg md:text-xl font-bold font-['Pretendard']
                        bg-transparent outline-none
                        placeholder:text-gray-300
                      "
                          value={inviteCode}
                          onChange={(e) => setInviteCode(e.target.value)}
                          onKeyDown={(e) => e.key === "Enter" && handleJoinGroup()}
                      />
                    </div>

                    {/* 화살표 버튼 */}
                    <button
                        onClick={handleJoinGroup}
                        className="w-9 h-9 md:w-10 md:h-10 flex justify-center items-center bg-yellow-800 rounded-xl outline outline-2 outline-offset-[-1px] outline-stone-900 hover:bg-yellow-900 transition-colors"
                    >
                      <ArrowRight size={22} color="#FCEFB4" />
                    </button>
                  </div>
                </div>
              </div>
            </section>
          </div>
        </div>
      </div>
  );
};

export default Group;
