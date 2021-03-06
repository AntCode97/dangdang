import { apiInstance } from "./index";

const api = apiInstance();

export function getInterviewQuestions(params, success, fail) {
  api.get(`/interview`, {params}).then(success).catch(fail);
}

export function getMyInterviewQuestions(params, success, fail) {
  api.get(`/interview/mine`, params).then(success).catch(fail);
}

export function addInterviewQuestion(params, success, fail) {
  api.post(`/interview`, params).then(success).catch(fail);
}

export function updateInterviewQuestion(params, success, fail) {
  api.patch(`/interview/${params.id}`, params).then(success).catch(fail);
}

export function deleteInterviewQuestion(params, success, fail) {
  api.delete(`/interview/${params.id}`, params).then(success).catch(fail);
}
