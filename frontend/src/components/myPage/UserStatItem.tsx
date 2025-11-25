interface UserStatItemProps {
    title: string;
    value: number | string;
}

const UserStatItem = ({ title, value }: UserStatItemProps) => {
    return (
        <div className="w-36 h-28 px-5 rounded-[30px] outline outline-2 outline-offset-[-1px] outline-yellow-800 inline-flex flex-col justify-start items-center overflow-hidden bg-white">
            {/* 상단 갈색 라벨 */}
            <div className="w-36 h-12 px-1.5 py-1 bg-yellow-800 flex flex-col justify-center items-center gap-2.5 overflow-hidden">
                <div className="w-28 h-8 text-center justify-center text-amber-50 text-xl font-bold font-['Pretendard']">
                    {title}
                </div>
            </div>

            {/* 숫자 영역 */}
            <div className="self-stretch flex-1 relative overflow-hidden flex justify-center items-center">
                <div className="text-center justify-center text-yellow-800 text-2xl font-bold font-['Pretendard']">
                    {value}
                </div>
            </div>
        </div>
    );
};

export default UserStatItem;
