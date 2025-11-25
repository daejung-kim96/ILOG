export interface GroupMemberResponse {
  id: number;
  userId: number;
  userName: string;
  createdAt: string;
  updatedAt: string;
}

// 백엔드의 GroupResponse DTO
export interface GroupResponse {
  id: number;
  name: string;
  inviteCode: string;
  members: GroupMemberResponse[]; // JSON에서는 Set이 배열(Array)로 변환됩니다.
  createdAt: string;
  updatedAt: string;
}
