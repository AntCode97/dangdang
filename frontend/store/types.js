const teamBoard = {
  GET_ROOMS: "GET_ROOMS",
  GET_ROOM_INFO: "GET_ROOM_INFO",
  CREATE_ROOM: "CREATE_ROOM",
  MY_ROOMS: "MY_ROOMS",
  WAITING_MEMBERS: "WAITING_MEMBERS",
};

const teamSpace = {
  GET_RESUME: "GET_RESUME",
  NO_RESUME: "NO_RESUME",
  GET_STUDYPOST: "GET_STUDYPOST",
};

const videoTypes = {
  SET_VIDEO: "SET_VIDEO",
  SET_CAMERA: "SET_CAMERA",
  SET_MIC: "SET_MIC",
  SET_SPEAKER: "SET_SPEAKER",
};

const questionTypes = {
  ADD_QUESTION: "ADD_QUESTION",
  REMOVE_QUESTION: "REMOVE_QUESTION",
  SET_QUESTIONS: "SET_QUESTIONS",
  SET_MY_QUESTIONS: "SET_MY_QUESTIONS",
};

const userTypes = {
  SET_ISLOGIN: "SET_ISLOGIN",
  SET_USERINFO: "SET_USERINFO",
  SET_SHOWMODAL: "SET_SHOWMODAL",
  SET_ISLOGINMODAL: "SET_ISLOGINMODAL",
  RESET_USERINFO: "RESET_USERINFO",
  MOVE_TEAMSTUDY: "MOVE_TEAMSTUDY",
};

const wsTypes = {
  CONNECT_SOCKET: "CONNECT_SOCKET",
  SET_WEB_SOCKET_SESSION_ID: "SET_WEB_SOCKET_SESSION_ID",
  PUSH_RECORDED_QUESTION_IDX: "PUSH_RECORDED_QUESTION_IDX",
  SET_SELECTED_QUESTION: "SET_SELECTED_QUESTION",
  SET_QUESTION_TOGGLE_STATE: "SET_QUESTION_TOGGLE_STATE",
};

const timerTypes = {
  START_TIMER: "START_TIMER",
  TIMER_TICK: "TIMER_TICK",
};

const types = {
  ...teamBoard,
  ...teamSpace,
  ...videoTypes,
  ...questionTypes,
  ...userTypes,
  ...wsTypes,
  ...timerTypes,
};
export default types;
