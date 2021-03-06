import { apiInstance } from "./index";

const api = apiInstance();

//자소서 create
export const createResume = async (req, success, fail) => {
  await api.post("/resume", req).then(success).catch(fail);
};

//자소서 read
export const getResume = async (userId, success, fail) => {
  await api.get(`/resume/${userId}`).then(success).catch(fail);
};

//자소서 update
export const updateResume = async (data, success, fail) => {
  await api
    .patch(`/resume/${data.resumeId}`, data.req)
    .then(success)
    .catch(fail);
};

//자소서 delete
export const deleteResume = async (param, success, fail) => {
  await api.delete(`/resume/${param}`).then(success).catch(fail);
};

//자소서 댓글, 대댓글 create
export const createResumeComment = async (data, success, fail) => {
  await api
    .post(`/resume/${data.resumeId}/comment`, data.obj)
    .then(success)
    .catch(fail);
};

//자소서 댓글, 대댓글 update
export const updateResumeComment = async (data, success, fail) => {
  await api
    .patch(`/resume/${data.resumeId}/comment/${data.commentId}`, data.obj)
    .then(success)
    .catch(fail);
};

//자소서 댓글, 대댓글 delete
export const deleteResumeComment = async (data, success, fail) => {
  await api
    .delete(`/resume/${data.resumeId}/comment/${data.commentId}`)
    .then(success)
    .catch(fail);
};
