import { Plus } from "lucide-react";
import { Link } from "react-router-dom";
import GroupItem from "./GroupItem";
import { useGroupStore } from "@/stores/groupStore";

const GroupInfoCard = () => {
    const { groups } = useGroupStore();

    return (
        <div
            className="
        w-full max-w-[550px]
        h-auto sm:h-[350px]
        mx-auto
        p-2.5
        rounded-[20px]
        outline outline-2 outline-offset-[-1px] outline-yellow-800
        flex flex-col items-center gap-2.5
        bg-amber-50
      "
        >
            {/* 헤더 */}
            <div
                className="
          w-full max-w-[520px]
          grid grid-cols-[1fr_auto_1fr]
          items-center
        "
            >
                <div />

                <div className="justify-self-center">
                    <div className="h-7 px-5 bg-yellow-800 rounded-[999px] flex items-center justify-center">
            <span className="text-amber-50 text-lg sm:text-xl font-bold font-['Pretendard']">
              그룹
            </span>
                    </div>
                </div>

                <div className="justify-self-end">
                    <Link
                        to="/group"
                        className="
              h-7 px-4
              bg-amber-50
              rounded-[999px]
              outline outline-1 outline-offset-[-1px] outline-yellow-800
              flex items-center gap-1.5
              text-yellow-800
              hover:bg-amber-100 transition-colors
              text-xs sm:text-sm
            "
                    >
                        <Plus size={16} />
                        <span className="font-bold font-['Pretendard']">그룹 추가</span>
                    </Link>
                </div>
            </div>

            {/* 그룹 리스트 */}
            <div
                className="
          w-full max-w-[520px]
          flex-1
          bg-amber-50
          rounded-[30px]
          outline outline-1 outline-offset-[-1px]
          px-3 sm:px-4 py-3 sm:py-4
          flex flex-col gap-3
          overflow-y-auto
          custom-scroll
        "
            >
                {groups.length === 0 ? (
                    <div className="w-full py-6 text-center text-yellow-800 text-xs sm:text-sm font-['Pretendard']">
                        아직 등록된 그룹이 없어요. <br />
                        <span className="font-bold">[그룹 추가]</span> 버튼을 눌러 새 그룹을 만들어 주세요.
                    </div>
                ) : (
                    groups.map((group) => <GroupItem key={group.id} group={group} />)
                )}
            </div>
        </div>
    );
};

export default GroupInfoCard;
