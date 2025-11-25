import { Link, useNavigate } from "react-router-dom";
import logo from "../../assets/logo.png";
import down from "../../assets/Down.png";
import { useAuthStore } from "../../stores/authStore";
import { useGroupStore } from "@/stores/groupStore";
import { useEffect, useState } from "react";

const Header = () => {
  const navigate = useNavigate();
  const { user, logout } = useAuthStore();

  const { groups, currentGroup, fetchGroups, setCurrentGroup } = useGroupStore();
  const [isOpen, setIsOpen] = useState(false);

  const handleLogout = async () => {
    await logout();
    navigate("/login");
  };

  // 헤더 마운트 시 그룹 목록 가져오기
  useEffect(() => {
    fetchGroups();
  }, [fetchGroups]);

  // 그룹이 하나도 없으면 /group 페이지로 강제 이동
  useEffect(() => {
    if (groups.length === 0) {
      navigate("/group");
    }
  }, [groups.length, navigate]);

  const displayName = currentGroup?.name ?? "없음";

  const handleSelectGroup = (groupId: number) => {
    const g = groups.find((g) => g.id === groupId);
    if (g) {
      setCurrentGroup(g);
      setIsOpen(false);
    }
  };

  const arrowClass =
      "w-3 h-3 object-contain transform transition-transform duration-200 " +
      (isOpen ? "rotate-180" : "");

  return (
      <header className="bg-amber-100 outline outline-1 outline-offset-[-1px] outline-yellow-800">
        <div className="max-w-7xl mx-auto px-6">
          <div className="flex justify-between items-center h-16">
            {/* 왼쪽: 로고 */}
            <div
                onClick={() => navigate("/")}
                className="cursor-pointer hover:scale-105 transition-transform duration-300 flex items-center"
            >
              <img src={logo} alt="아이로그 로고" className="w-16 h-16 object-contain" />
            </div>

            {/* 오른쪽: 로그인 후 UI */}
            <div className="flex justify-start items-center gap-4">
              <div className="inline-flex justify-start items-center gap-4">
                {/* ===== 현재 그룹 선택 드롭다운 ===== */}
                <div className="relative w-40">
                  <button
                      type="button"
                      onClick={() => groups.length > 0 && setIsOpen((prev) => !prev)}
                      className="w-full inline-flex items-center justify-between gap-2 px-4 py-1.5
                             bg-white rounded-[999px] outline outline-1 outline-offset-[-1px]
                             outline-stone-900 shadow-sm hover:shadow-md transition-shadow"
                  >
                  <span className="text-stone-900 text-sm font-bold font-['Pretendard'] leading-none truncate">
                    {displayName}
                  </span>
                    <img src={down} alt="열기" className={arrowClass} />
                  </button>

                  {/* 드롭다운 목록 */}
                  {isOpen && groups.length > 0 && (
                      <div
                          className="absolute left-0 right-0 mt-2 rounded-2xl border border-yellow-800
                               bg-amber-50 shadow-[0_10px_30px_rgba(0,0,0,0.15)] z-50
                               overflow-hidden"
                      >
                        <div className="px-3 py-2 border-b border-yellow-200 text-[11px] text-yellow-900 font-semibold">
                          우리 아이 그룹 선택
                        </div>

                        <div className="max-h-60 overflow-y-auto">
                          {groups.map((group) => {
                            const isSelected = currentGroup?.id === group.id;
                            return (
                                <button
                                    key={group.id}
                                    type="button"
                                    onClick={() => handleSelectGroup(group.id)}
                                    className={`w-full flex items-center justify-between px-4 py-2 text-sm
                                        transition-colors ${
                                        isSelected
                                            ? "bg-amber-200 text-yellow-900 font-semibold"
                                            : "hover:bg-amber-100 text-stone-800"
                                    }`}
                                >
                                  <span className="truncate">{group.name}</span>
                                  {isSelected && (
                                      <span className="ml-2 text-[11px] px-2 py-0.5 rounded-full bg-yellow-800 text-amber-50">
                                현재
                              </span>
                                  )}
                                </button>
                            );
                          })}
                        </div>
                      </div>
                  )}
                </div>

                {/* 프로필 사진 */}
                <Link to="/mypage">
                  <div className="w-10 h-10">
                    <img
                        className="w-full h-full rounded-full object-cover"
                        src={user?.picture ?? "/profile.png"}
                        alt="사용자 프로필 사진"
                    />
                  </div>
                </Link>

                {/* 로그아웃 버튼 */}
                <button
                    type="button"
                    onClick={handleLogout}
                    className="px-3 py-1.5 bg-yellow-800 rounded-[90px] flex justify-center items-center gap-2.5"
                >
                <span className="text-orange-100 text-base font-bold font-['Pretendard']">
                  로그아웃
                </span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </header>
  );
};

export default Header;
