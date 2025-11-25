import { Link, useLocation } from 'react-router-dom';

const Navbar = () => {
  const location = useLocation();

  const isActive = (path: string) => location.pathname === path;

  const baseLinkClass =
      "inline-flex items-center justify-center text-center rounded-[999px] font-bold font-['Pretendard'] " +
      'px-2 sm:px-3 md:px-4 lg:px-4 py-1 sm:py-1.5 md:py-2 ' +
      'text-sm sm:text-base md:text-lg lg:text-xl ' +
      'transition-all duration-300';

  return (
      <nav className="bg-amber-100 outline outline-1 outline-offset-[-0.5px] outline-yellow-800">
        {/* 전체 폭 사용 + 양쪽 여백만 살짝 */}
        <div className="w-full px-4 sm:px-6">
          <div className="h-16 flex items-center overflow-x-auto">
            {/* 메뉴를 폭 전체에 깔아놓기 */}
            <div className="flex w-full items-center justify-between md:justify-evenly gap-4 md:gap-10 whitespace-nowrap">
              {/* 일기 */}
              <Link
                  to="/diary"
                  className={`${baseLinkClass} ${
                      isActive('/diary')
                          ? 'bg-yellow-800 text-amber-100 scale-105'
                          : 'bg-transparent text-yellow-800 hover:bg-yellow-800 hover:text-amber-100 hover:scale-105'
                  }`}
              >
                일기
              </Link>

              {/* 앨범 */}
              <Link
                  to="/album"
                  className={`${baseLinkClass} ${
                      isActive('/album')
                          ? 'bg-yellow-800 text-amber-100 scale-105'
                          : 'bg-transparent text-yellow-800 hover:bg-yellow-800 hover:text-amber-100 hover:scale-105'
                  }`}
              >
                앨범
              </Link>

              {/* 우리 아이 보기 */}
              <Link
                  to="/our-eye"
                  className={`${baseLinkClass} ${
                      isActive('/our-eye')
                          ? 'bg-yellow-800 text-amber-100 scale-105'
                          : 'bg-transparent text-yellow-800 hover:bg-yellow-800 hover:text-amber-100 hover:scale-105'
                  }`}
              >
                우리 아이 보기
              </Link>

              {/* 감정 레포트 */}
              <Link
                  to="/report"
                  className={`${baseLinkClass} ${
                      isActive('/report')
                          ? 'bg-yellow-800 text-amber-100 scale-105'
                          : 'bg-transparent text-yellow-800 hover:bg-yellow-800 hover:text-amber-100 hover:scale-105'
                  }`}
              >
                감정 레포트
              </Link>

              {/* 마이페이지 */}
              <Link
                  to="/mypage"
                  className={`${baseLinkClass} ${
                      isActive('/mypage')
                          ? 'bg-yellow-800 text-amber-100 scale-105'
                          : 'bg-transparent text-yellow-800 hover:bg-yellow-800 hover:text-amber-100 hover:scale-105'
                  }`}
              >
                마이페이지
              </Link>
            </div>
          </div>
        </div>
      </nav>
  );
};

export default Navbar;
