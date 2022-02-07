import { apiInstance } from "./index";

const api = apiInstance();

export function getInterviewQuestions(success, fail) {
  api.get(`/interview`).then(success).catch(fail);
}

export function addInterviewQuestion(params, success, fail) {
  api.post(`/interview`, params).then(success).catch(fail);
}