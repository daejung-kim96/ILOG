export interface UserType {
  name: string;
  picture: string;
  // ...email 등 기타 사용자 정보
}

export interface UserStatType {
  title: string;
  value: number | string;
}

export interface GroupType {
  id: string;
  name: string;
  parentName: string; // "동현서현맘" (그룹장/부모 이름)
  inviteCode: string;
  // ...기타 그룹원 정보 등
}
