import Button from "./Button";
import styles from "../../scss/user/login-signup.module.scss";
import { useState } from "react";
import { connect } from "react-redux";
import { useRouter } from "next/router";
import {
  setShowModal,
  setIsLoginModal,
  setUserInfo,
  setIsMoveTeamStudy,
} from "../../store/actions/userAction";
import { getToken, getUserInfo } from "../../api/user";
import { BACKEND_URL } from "../../config";

function mapStateToProps({ userReducer }) {
  return {
    isLoginModal: userReducer.isLoginModal,
    isMoveTeamStudy: userReducer.isMoveTeamStudy,
  };
}

function mapDispatchToProps(dispatch) {
  return {
    setShowModal: (show) => dispatch(setShowModal(show)),
    setIsLoginModal: (isLoginModal) => dispatch(setIsLoginModal(isLoginModal)),
    setUserInfo: (userInfo) => dispatch(setUserInfo(userInfo)),
    setIsMoveTeamStudy: (isMoveTeamStudy) =>
      dispatch(setIsMoveTeamStudy(isMoveTeamStudy)),
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(Login);

function Login({
  setShowModal,
  isLoginModal,
  setIsLoginModal,
  setUserInfo,
  isMoveTeamStudy,
  setIsMoveTeamStudy,
}) {
  const router = useRouter();
  const [values, setValues] = useState({
    email: "",
    password: "",
  });

  const handleChange = ({ target: { id, value } }) => {
    const nextValues = {
      ...values,
      [id]: value,
    };
    setValues(nextValues);
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    getToken(
      values,
      (response) => {
        getUserInfo(
          ({ data: { response } }) => {
            const userInfo = {
              id: response.id,
              email: response.email,
              nickName: response.nickName,
              imageUrl: response.imageUrl,
            };
            setUserInfo(userInfo);
            if (isMoveTeamStudy) router.push("/user/mypage/myroom");
            setIsMoveTeamStudy(false);
          },
          (error) => {
            console.log(error);
          }
        );
        setShowModal(false);
      },
      (error) => {
        alert("???????????? ??????????????? ??????????????????.");
      }
    );
  };

  const socialLoginRequest = (provider) => {
    window.location.href = `${BACKEND_URL}/oauth2/authorize/${provider}?redirect_uri=http://localhost:3000/user/oauth2/redirect?destination=${router.pathname}`;
  };

  return (
    <div className={styles.body}>
      <h1>?????????</h1>
      <form className={styles.form} method="post" onSubmit={handleSubmit}>
        <div>
          <label htmlFor="email">
            ?????????
            <input
              id="email"
              type="email"
              value={values.email}
              onChange={handleChange}
              placeholder="?????????"
              required
            />
          </label>
        </div>
        <div>
          <label htmlFor="password">
            ????????????
            <input
              id="password"
              type="password"
              value={values.password}
              onChange={handleChange}
              placeholder="????????????"
            />
          </label>
        </div>
        <button type="submit">?????????</button>
      </form>
      <p>
        ????????? ?????????????{" "}
        <a onClick={() => setIsLoginModal(!isLoginModal)}>????????????</a>
      </p>
      <Button
        text="Google??? ?????????"
        onClick={() => socialLoginRequest("google")}
      />
      <Button
        text="Kakao??? ?????????"
        onClick={() => socialLoginRequest("kakao")}
      />
    </div>
  );
}
