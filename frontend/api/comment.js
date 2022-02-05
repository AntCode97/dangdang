import { apiInstance } from "./index";

const api = apiInstance();

//스터디룸 상세 공고 댓글, 대댓글 Post
export const createDetailComment = async (data, success, fail) => {
  await api
    .post(`/study/${data.studyId}/comment`, data.obj)
    .then(success)
    .catch(fail);
};

//스터디룸 상세 공고 댓글, 대댓글 Update
export const updateDetailComment = async (data, success, fail) => {
  await api
    .patch(`/study/{studyId}/comment/${data.commentId}`, data.obj)
    .then(success)
    .catch(fail);
};

//스터디룸 상세 공고 댓글, 대댓글 Delete
export const deleteDetailComment = async (data, success, fail) => {
  await api
    .delete(`/study/{studyId}/comment/${data.commentId}`)
    .then(success)
    .catch(fail);
};
