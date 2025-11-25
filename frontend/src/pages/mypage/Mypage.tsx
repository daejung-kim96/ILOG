import { useState } from "react";
import { useAuthStore } from "@/stores/authStore"; //
import { getSentimentAnalysis, runSentimentAnalysis } from "@/api/sentimentApi"; // 감정 분석 API 호출 함수 가져오기
import { Sentiment } from "@/types/Sentiment"; // Sentiment 타입 가져오기
import GroupInfoCard from "@/components/myPage/GroupInfoCard";
import UserInfoCard from "@/components/myPage/UserInfoCard";

const MyPage = () => {
    const { isLoggedIn, user } = useAuthStore(); // 로그인 상태와 사용자 정보 가져오기
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [sentimentData, setSentimentData] = useState<Sentiment | null>(null); // 상태값 타입 지정
    const [isRunning, setIsRunning] = useState(false); // 데이터 수집 중인지 여부

    // 감정 분석 실행 (run)
    const handleRunSentiment = async () => {
        if (!isLoggedIn || !user) {
            alert("로그인 후 사용 가능합니다.");
            return;
        }

        try {
            const userId = Number(user.id); // 로그인된 사용자 ID
            setIsRunning(true); // 실행 상태로 설정

            // runSentimentAnalysis API 호출
            await runSentimentAnalysis(userId);

            alert("감정 분석이 완료되었습니다.");

        } catch (error) {
            console.error("Error running sentiment analysis", error);
            alert("감정 분석 실행 중 오류가 발생했습니다.");
        } finally {
            setIsRunning(false); // 실행 완료
        }
    };

    // 감정 분석 데이터 가져오기
    const handleGetSentiment = async () => {
        if (!isLoggedIn || !user) {
            alert("로그인 후 사용 가능합니다.");
            return;
        }

        try {
            const userId = Number(user.id); // 로그인된 사용자 ID
            // 완전 시연용으로 오늘 날짜 하드 코딩해야됨
            // 근데 이거 포스트그레 시간이랑 달라서 시간 체크해야돼
            const day = "2025-11-20";

            // API 호출: 일일 감정 분석 데이터 가져오기
            const data = await getSentimentAnalysis(userId, day); // 직접적으로 데이터를 받기

            if (data) {
                // @ts-ignore
                setSentimentData(data); // 받은 데이터를 상태에 설정
                setIsModalOpen(true); // 모달 열기
            } else {
                alert("감정 분석 결과가 없습니다.");
            }
        } catch (error) {
            console.error("Error fetching sentiment analysis data", error);
        }
    };

    const handleCloseModal = () => {
        setIsModalOpen(false);
    };

    return (
        <div className="w-full max-w-[1200px] mx-auto min-h-[780px] p-4 rounded-[10px] outline outline-2 outline-offset-[-1px] outline-yellow-800 flex flex-col items-center gap-4 bg-amber-50">
            {/* 상단: 내 정보 + 그룹 */}
            <div className="w-full flex flex-col lg:flex-row justify-between items-stretch gap-4">
                <UserInfoCard />
                <GroupInfoCard />
            </div>

            {/* 하단(임시 영역) */}
            <div className="w-full h-96 p-2.5 border border-yellow-800">
                {isLoggedIn ? (
                    <>
                        <button
                            className="p-2 bg-blue-500 text-white rounded mr-4"
                            onClick={handleRunSentiment}
                            disabled={isRunning}
                        >
                            {isRunning ? "실행 중..." : "감정 분석 실행"}
                        </button>
                        <button
                            className="p-2 bg-green-500 text-white rounded"
                            onClick={handleGetSentiment}
                        >
                            감정 분석 시연
                        </button>
                    </>
                ) : (
                    <p className="text-red-500">로그인 후 감정 분석을 시도하세요.</p>
                )}
            </div>

            {/* 감정 분석 모달 */}
            {isModalOpen && sentimentData && (
                <div className="fixed inset-0 bg-gray-500 bg-opacity-75 flex justify-center items-center z-50">
                    <div className="bg-white p-8 rounded-lg w-3/4"> {/* 너비를 3/4로 설정 */}
                        <h2 className="text-xl font-bold mb-4">감정 분석 결과</h2>
                        <pre>{JSON.stringify(sentimentData, null, 2)}</pre>
                        <button
                            className="mt-4 bg-red-500 text-white p-2 rounded"
                            onClick={handleCloseModal}
                        >
                            닫기
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
};

export default MyPage;
