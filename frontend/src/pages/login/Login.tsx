// src/pages/Login.tsx

export default function Login() {
    const handleSocialLogin = (provider: "google" | "naver" | "kakao") => {
        if (provider === "google") {
            // ✅ 실제 구글 소셜 로그인
            window.location.href = "/oauth2/authorization/google";
        } else {
            // ✅ 나머지는 준비중
            alert("해당 로그인은 준비중입니다.");
        }
    };

    return (
        // 전체 화면 배경 + 중앙 정렬
        <div className="min-h-screen w-full bg-amber-50 flex items-center justify-center px-4">
            {/* 로그인 카드 */}
            <div
                className="
          w-full max-w-[600px]
          px-6 md:px-24 py-10
          rounded-[30px]
          border-[5px] border-yellow-800
          bg-amber-50
          flex flex-col items-center gap-8
        "
            >
                {/* 상단 로고 */}
                <img
                    src="/logo.png"
                    alt="ILOG 로고"
                    className="w-40 h-40 md:w-56 md:h-56 object-contain"
                />

                {/* 서비스 설명 블록 */}
                <div className="w-full text-center space-y-2">
                    <p className="text-yellow-800 text-2xl md:text-3xl font-black font-['Pretendard']">
                        아이로그에 오신 것을 환영해요
                    </p>
                    <p className="text-yellow-800/80 text-sm md:text-base font-medium font-['Pretendard'] leading-relaxed">
                        여기에 우리 서비스 설명을 적어야하는디...
                        <br className="hidden md:block" />
                        기깔나는 아이디어 있는사람!
                    </p>
                </div>

                {/* 소셜 로그인 버튼들 */}
                <div className="w-full flex flex-col items-center gap-4">
                    {/* ───────── 구글로 시작하기 ───────── */}
                    <button
                        type="button"
                        onClick={() => handleSocialLogin("google")}
                        className="
              w-72 md:w-96
              h-16 md:h-20
              rounded-[90px]
              border-[5px] border-yellow-800
              flex items-center gap-6
              px-4
              hover:bg-amber-100
              transition-colors
            "
                    >
                        {/* ▶ 로고로 꽉 채우고, 동그란 outline만 보이게 */}
                        <div className="w-12 h-12 md:w-16 md:h-16 rounded-full border-[3px] md:border-[4px] border-yellow-800 flex items-center justify-center overflow-hidden bg-white">
                            <img
                                src="/googleLogo.png"
                                alt="Google"
                                className="w-full h-full object-contain"
                            />
                        </div>
                        <span className="text-yellow-800 text-xl md:text-3xl font-black font-['Pretendard']">
              구글로 시작하기
            </span>
                    </button>

                    {/* ───────── 네이버로 시작하기 ───────── */}
                    <button
                        type="button"
                        onClick={() => handleSocialLogin("naver")}
                        className="
              w-72 md:w-96
              h-16 md:h-20
              rounded-[90px]
              border-[5px] border-yellow-800
              flex items-center gap-6
              px-4
              hover:bg-amber-100
              transition-colors
            "
                    >
                        <div className="w-12 h-12 md:w-16 md:h-16 rounded-full border-[3px] md:border-[4px] border-yellow-800 flex items-center justify-center overflow-hidden bg-white">
                            <img
                                src="/naverLogo.png"
                                alt="Naver"
                                className="w-full h-full object-contain"
                            />
                        </div>
                        <span className="text-yellow-800 text-xl md:text-3xl font-black font-['Pretendard']">
              네이버로 시작하기
            </span>
                    </button>

                    {/* ───────── 카카오로 시작하기 ───────── */}
                    <button
                        type="button"
                        onClick={() => handleSocialLogin("kakao")}
                        className="
              w-72 md:w-96
              h-16 md:h-20
              rounded-[90px]
              border-[5px] border-yellow-800
              flex items-center gap-6
              px-4
              hover:bg-amber-100
              transition-colors
            "
                    >
                        <div className="w-12 h-12 md:w-16 md:h-16 rounded-full border-[3px] md:border-[4px] border-yellow-800 flex items-center justify-center overflow-hidden bg-white">
                            <img
                                src="/kakaoLogo.png"
                                alt="Kakao"
                                className="w-full h-full object-contain"
                            />
                        </div>
                        <span className="text-yellow-800 text-xl md:text-3xl font-black font-['Pretendard']">
              카카오로 시작하기
            </span>
                    </button>
                </div>
            </div>
        </div>
    );
}
