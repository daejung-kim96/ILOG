// src/components/layout/Footer.tsx
const Footer = () => {
  return (
    // ✅ 헤더와 동일한 스타일: 전체 배경 + 얇은 outline
    <footer className="bg-amber-100 outline outline-1 outline-offset-[-1px] outline-yellow-800 mt-auto">
      {/* 가운데 정렬 컨테이너 (너무 좁지 않게 적당한 max-width) */}
      <div className="w-full max-w-6xl mx-auto px-4 py-3 flex flex-col gap-3">
        {/* 상단 줄: 좌측 저작권 / 가운데 약관 / 우측 국가 */}
        <div className="w-full flex flex-col md:flex-row items-center md:items-end justify-between gap-2">
          {/* 좌측: Copyright */}
          <div className="flex-1 flex justify-center md:justify-start">
            <p className="text-center md:text-left text-yellow-800 text-sm md:text-lg font-normal font-['Pretendard'] md:whitespace-nowrap">
              Copyright © 2025 ILOG. All rights reserved.
            </p>
          </div>

          {/* 중앙: 약관들 */}
          <div className="flex-1 flex justify-center">
            <p className="text-center text-yellow-800 text-xs md:text-sm font-normal font-['Pretendard'] tracking-wide md:whitespace-nowrap">
              이용약관&nbsp;|&nbsp;개인정보 처리방침&nbsp;|&nbsp;판매 및 환불&nbsp;|&nbsp;법적 고지
            </p>
          </div>

          {/* 우측: 국가 */}
          <div className="flex-1 flex justify-center md:justify-end">
            <p className="text-right text-yellow-800 text-sm md:text-base font-normal font-['Pretendard'] md:whitespace-nowrap">
              대한민국
            </p>
          </div>
        </div>

        {/* 하단 회사 정보 */}
        <div className="w-full">
          <p className="text-left text-yellow-800 text-xs md:text-base font-normal font-['Pretendard'] leading-relaxed">
            알빠노메일 | I-LOG | 대표자명: 장동현 | 주소: 서울 강남구 테헤란로 212 | 전화:
            010-0000-0000
            <br />
            사업자등록번호: 000-00-0000 | 호스팅 서비스 제공: ILOG Inc.
          </p>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
