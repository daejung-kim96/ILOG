const DEFAULT_GROUP_ID = 1;
const DEFAULT_USER_ID = 1;

export const clientContext = {
  get groupId() {
    return DEFAULT_GROUP_ID;
  },
  get userId() {
    return DEFAULT_USER_ID;
  },
};

export const getDefaultGroupId = () => clientContext.groupId;
export const getDefaultUserId = () => clientContext.userId;
