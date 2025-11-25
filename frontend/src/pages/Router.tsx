// pages/Router.tsx
import { Routes, Route } from "react-router-dom";
import RootLayout from "../layouts/RootLayout";
import Home from "./Home";
import DiaryWrite from "./diary/DiaryWrite";
import Album from "./album/Album";
import OurEye from "./oureye/OurEye";
import Cameras from "./oureye/Cameras";
import CameraNew from "./oureye/CameraNew";
import Settings from "./oureye/Settings";
import Report from "./report/Report";
import AlbumDetail from "./album/AlbumDetail";
import Login from "./login/Login";
import ProtectedRoute from "@/components/routeGuard/ProtectedRoute";
import PublicOnlyRoute from "@/components/routeGuard/PublicOnlyRoute";
import MyPage from "./mypage/Mypage";
import Group from "./group/Group";
import DiaryPage from "./diary/DiaryPage";
import DiaryDetail from "./diary/DiaryDetail";

const Router = () => {
  return (
    <Routes>
      {/* ========================================
        인증이 필요 *없는* 페이지 (Public Routes)
        ========================================
      */}
      <Route element={<PublicOnlyRoute />}>
        <Route path="/login" element={<Login />} />
      </Route>

      {/* ========================================
        인증이 *필요한* 페이지 (Protected Routes)
        ========================================
      */}
      {/* <Route element={<ProtectedRoute />}> ... </Route>로 감싸면
        ProtectedRoute가 경비원 역할을 합니다.
        (이전에 만든 ProtectedRoute.tsx 코드가 그대로 작동합니다)
      */}
      <Route element={<ProtectedRoute />}>
        <Route element={<RootLayout />}>
          <Route path="/" element={<Home />} />
          <Route path="/group" element={<Group />} />
          <Route path="/mypage" element={<MyPage />} />
          <Route path="/diary" element={<DiaryPage />} />
          <Route path="/diary/write" element={<DiaryWrite />} />
          <Route path="/diary/:id" element={<DiaryDetail />} />
          <Route path="/album" element={<Album />} />
          <Route path="/our-eye" element={<OurEye />} />
          <Route path="/our-eye/cameras" element={<Cameras />} />
          <Route path="/our-eye/cameras/new" element={<CameraNew />} />
          <Route path="/our-eye/settings/:id" element={<Settings />} />
          <Route path="/report" element={<Report />} />
          <Route path="/album/:year/:month" element={<AlbumDetail />} />
        </Route>
      </Route>
    </Routes>
  );
};

export default Router;
