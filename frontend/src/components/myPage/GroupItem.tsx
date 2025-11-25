import { ChevronDown } from "lucide-react";
import { useState } from "react";
import type { GroupResponse, GroupMemberResponse } from "@/types/group";

interface GroupItemProps {
  group: GroupResponse;
}

// 멤버 표시용 라벨: userName 우선 사용
const getMemberLabel = (member: GroupMemberResponse, index: number) => {
  return member.userName || `멤버 ${index + 1}`;
};

const GroupItem = ({ group }: GroupItemProps) => {
  const [isOpen, setIsOpen] = useState(false);

  // members 배열 (타입 명시)
  const members: GroupMemberResponse[] = group.members ?? [];

  // pill 안에 표시할 메인 텍스트
  const memberNames = members.map((m, idx) => getMemberLabel(m, idx));

  const mainMemberText =
      memberNames.length === 0
          ? "그룹 멤버 없음"
          : memberNames.length === 1
              ? memberNames[0]
              : `${memberNames[0]} 외 ${memberNames.length - 1}명`;

  return (
      <div
          className="
        w-full
        rounded-[26px]
        border-2 border-yellow-800
        bg-white
        flex flex-col
        relative
      "
      >
        {/* 상단 갈색 헤더 */}
        <div className="w-full bg-yellow-800 rounded-t-[24px] px-6 py-2 flex items-center">
        <span
            className="
            text-amber-50
            font-bold font-['Pretendard']
            text-[clamp(1rem,1.1vw,1.25rem)]
            truncate
          "
        >
          {group.name}
        </span>
        </div>

        {/* 내용 영역 */}
        <div className="w-full px-5 py-3 flex items-center gap-3">
          {/* 그룹 멤버 드롭다운 (정보용) */}
          <div className="relative w-1/2">
            <button
                type="button"
                onClick={() => members.length > 0 && setIsOpen((prev) => !prev)}
                className="
              w-full h-10 px-4
              bg-white
              rounded-[30px]
              outline outline-1 outline-offset-[-1px] outline-yellow-800
              flex items-center justify-between gap-2
            "
            >
            <span
                className="
                text-yellow-800
                font-bold font-['Pretendard']
                text-[clamp(0.8rem,0.9vw,1rem)]
                truncate
              "
            >
              {mainMemberText}
            </span>
              <ChevronDown
                  className={`
                w-4 h-4 text-yellow-800
                transition-transform duration-200
                ${isOpen ? "rotate-180" : ""}
              `}
              />
            </button>

            {/* 드롭다운 - 단순 리스트만 표시 */}
            {isOpen && (
                <div
                    className="
                absolute left-0 right-0 top-[110%]
                bg-white
                rounded-2xl
                border border-yellow-800
                shadow-[0_8px_24px_rgba(0,0,0,0.15)]
                max-h-40 overflow-y-auto
                z-30
                custom-scroll
              "
                >
                  <div className="px-3 py-2 text-[11px] text-yellow-900 font-semibold border-b border-yellow-100">
                    그룹 멤버
                  </div>

                  {members.length === 0 ? (
                      <div className="px-4 py-2 text-xs text-stone-500">
                        등록된 멤버가 없습니다.
                      </div>
                  ) : (
                      members.map((member, index) => {
                        const label = getMemberLabel(member, index);
                        return (
                            <div
                                key={member.id ?? index}
                                className="w-full px-4 py-1.5 text-sm text-stone-800"
                            >
                              {label}
                            </div>
                        );
                      })
                  )}
                </div>
            )}
          </div>

          {/* 초대코드 */}
          <div
              className="
            w-1/2 h-10 px-4
            bg-white
            rounded-[30px]
            outline outline-1 outline-offset-[-1px] outline-yellow-800
            flex items-center gap-1
          "
          >
          <span
              className="
              text-yellow-800
              font-bold font-['Pretendard']
              text-[clamp(0.8rem,0.9vw,1rem)]
              whitespace-nowrap
            "
          >
            초대코드:
          </span>
            <span
                className="
              text-yellow-800
              font-bold font-['Pretendard']
              text-[clamp(0.8rem,0.9vw,1rem)]
              tracking-[0.12em]
              truncate
            "
                title={group.inviteCode}
            >
            {group.inviteCode}
          </span>
          </div>
        </div>
      </div>
  );
};

export default GroupItem;
