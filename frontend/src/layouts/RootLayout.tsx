import { Outlet } from 'react-router-dom';
import Navbar from '../components/layout/Navbar';
import Footer from '../components/layout/Footer';
import Header from '../components/layout/Header';

const RootLayout = () => {
  return (
    // 1. 전체 레이아웃: 화면 사이즈에 맞게로 변경
    <div className="min-h-screen w-full flex flex-col bg-amber-50">
      <Header />
      <Navbar />

      {/* 2. 메인 콘텐츠 영역: */}
      <div className="flex-1 overflow-y-auto">
        {/* 3. <main> 태그 (실제 콘텐츠 래퍼)*/}
        <main className="w-full max-w-7xl mx-auto p-8">
          <Outlet /> {/* 각 페이지 컴포넌트가 여기 렌더링됨 */}
        </main>
      </div>

      <Footer />
    </div>
  );
};

export default RootLayout;
