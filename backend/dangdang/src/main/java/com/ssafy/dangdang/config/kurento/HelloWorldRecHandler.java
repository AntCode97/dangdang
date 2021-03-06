/*
 * (C) Copyright 2015-2016 Kurento (http://kurento.org/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ssafy.dangdang.config.kurento;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.ssafy.dangdang.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.kurento.client.*;
import org.kurento.jsonrpc.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;

/**
 * Hello World with recording handler (application and media logic).
 *
 * @author Boni Garcia (bgarcia@gsyc.es)
 * @author David Fernandez (d.fernandezlop@gmail.com)
 * @author Radu Tom Vlad (rvlad@naevatec.com)
 * @author Ivan Gracia (igracia@kurento.org)
 * @since 6.1.1
 */


public class HelloWorldRecHandler extends TextWebSocketHandler {
  private static String RECORDER_FILE_PATH = "file:///tmp/"+"HelloWorldRecorded.webm"; //default name

  private final Logger log = LoggerFactory.getLogger(HelloWorldRecHandler.class);
  private static final Gson gson = new GsonBuilder().create();
  private Boolean isStop = false;
  @Autowired
  private UserRegistry registry;

  @Autowired
  private StorageService storageService;

  @Autowired
  private KurentoClient kurento;

  @Override
  public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    JsonObject jsonMessage = gson.fromJson(message.getPayload(), JsonObject.class);
    log.debug("Incoming message: {}", jsonMessage);

    UserSession user = registry.getBySession(session);

    log.info("============================================================");
    log.info("session id :{}",user);
    log.info("============================================================");
    if (user != null) {
      log.debug("Incoming message from user '{}': {}", user.getId(), jsonMessage);
    } else {
      log.debug("Incoming message from new user: {}", jsonMessage);
    }

    switch (jsonMessage.get("id").getAsString()) {
      case "start":
        log.debug("start");
        String saveName=jsonMessage.get("name").getAsString(); //?????????????????? ?????? ?????? ?????? ??????
        RECORDER_FILE_PATH = "file:///tmp/"+session.getId()+saveName+".webm"; // user session id+????????? ??????.webm
        start(session, saveName, jsonMessage);
        break;
      case "stop":
        if (user != null) {
          user.stop();
          isStop = true;
          log.debug("stop");
        }
      case "stopPlay":
        if (user != null) {
          user.release();
          log.debug("stopPlay");
        }
        break;
      case "play":
        play(user, session, jsonMessage);
        log.debug("play");
        break;
      case "onIceCandidate": {
        JsonObject jsonCandidate = jsonMessage.get("candidate").getAsJsonObject();

        if (user != null && !isStop) {
          IceCandidate candidate = new IceCandidate(jsonCandidate.get("candidate").getAsString(),
                  jsonCandidate.get("sdpMid").getAsString(),
                  jsonCandidate.get("sdpMLineIndex").getAsInt());
          user.addCandidate(candidate);
        }
        if(isStop) isStop = false;
        break;
      }
      case "del": 
        log.debug("del");
        if (user != null) {
          del(user);
        }
        break;
      default:
        sendError(session, "Invalid message with id " + jsonMessage.get("id").getAsString());
        break;
    }
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    super.afterConnectionClosed(session, status);
    registry.removeBySession(session);
  }

  // start ?????? -> ?????? ??????
  private void start(final WebSocketSession session, String saveName, JsonObject jsonMessage) {
    try {
      log.info("????????? ?????? name ?????? :: {}",RECORDER_FILE_PATH);
      // 1. Media logic (webRtcEndpoint in loopback)
      MediaPipeline pipeline = kurento.createMediaPipeline();
      WebRtcEndpoint webRtcEndpoint = new WebRtcEndpoint.Builder(pipeline).build();
      webRtcEndpoint.connect(webRtcEndpoint);

      // MediaProfileSpecType :: ????????? ?????????. ?????? WEBM, MKV, MP4 ??? JPEG??? ???????????????.
      // ?????????, ?????????, ?????????&????????? ??? ??? ????????????
      // https://doc-kurento.readthedocs.io/en/latest/_static/client-javadoc/org/kurento/client/MediaProfileSpecType.html
      MediaProfileSpecType profile = getMediaProfileFromMessage(jsonMessage);

      //recorder
      // ????????? ???????????? ???????????? ????????? ???????????????.
      // RecorderEndpoint??? ???????????? ?????? ????????? ??????????????? ?????? ???????????? ???????????? ?????? ??? ????????????.
      // ?????? ?????? MediaElement??? RecorderEndpoint??? ???????????? ???????????? ?????? ???????????? ????????? ?????? ???????????? ??????????????? ????????? ????????? ???????????????.
      RecorderEndpoint recorder = new RecorderEndpoint.Builder(pipeline, RECORDER_FILE_PATH)
              .withMediaProfile(profile).build();

      // 2. Store user session
      UserSession user= registry.getById(session.getId());

      if(user == null){
        user = new UserSession(session.getId(),session.getId(),session, pipeline);
        registry.register(user);
      }
      user.setMediaPipeline(pipeline);
      user.setWebRtcEndpoint(webRtcEndpoint);
      user.setRecorderEndpoint(recorder);

//      user.getVideos().add(user.getId()+saveName);
      user.addVideo(user.getId()+saveName); // set ??????
      log.info("???????????? ????????? ?????? ?????? :: {}",user.getVideos());

      // ?????? ??????
      recorder.addRecordingListener(new EventListener<RecordingEvent>() {

        @Override
        public void onEvent(RecordingEvent event) {
          JsonObject response = new JsonObject();
          response.addProperty("id", "recording");
          try {
            synchronized (session) {
              session.sendMessage(new TextMessage(response.toString()));
            }
          } catch (IOException e) {
            log.error(e.getMessage());
          }
        }

      });

      recorder.addStoppedListener(new EventListener<StoppedEvent>() {

        @Override
        public void onEvent(StoppedEvent event) {
          JsonObject response = new JsonObject();
          response.addProperty("id", "stopped");
          try {
            synchronized (session) {
              session.sendMessage(new TextMessage(response.toString()));
              log.debug("recoder ?????? ?????? {}",recorder);
            }
          } catch (IOException e) {
            log.error(e.getMessage());
          }
        }

      });

      recorder.addPausedListener(new EventListener<PausedEvent>() {

        @Override
        public void onEvent(PausedEvent event) {
          JsonObject response = new JsonObject();
          response.addProperty("id", "paused");
          try {
            synchronized (session) {
              session.sendMessage(new TextMessage(response.toString()));
            }
          } catch (IOException e) {
            log.error(e.getMessage());
          }
        }

      });

      connectAccordingToProfile(webRtcEndpoint, recorder, profile);

      // 3. SDP negotiation
      String sdpOffer = jsonMessage.get("sdpOffer").getAsString();
      String sdpAnswer = webRtcEndpoint.processOffer(sdpOffer);

      // 4. Gather ICE candidates
      webRtcEndpoint.addIceCandidateFoundListener(new EventListener<IceCandidateFoundEvent>() {

        @Override
        public void onEvent(IceCandidateFoundEvent event) {
          JsonObject response = new JsonObject();
          response.addProperty("id", "iceCandidate");
          response.add("candidate", JsonUtils.toJsonObject(event.getCandidate()));
          try {
            synchronized (session) {
              session.sendMessage(new TextMessage(response.toString()));
            }
          } catch (IOException e) {
            log.error(e.getMessage());
          }
        }
      });

      JsonObject response = new JsonObject();
      response.addProperty("id", "startResponse");
      response.addProperty("sdpAnswer", sdpAnswer);
      response.addProperty("sessionId",user.getId());

      synchronized (user) {
        session.sendMessage(new TextMessage(response.toString()));
      }

      webRtcEndpoint.gatherCandidates();

      recorder.record();
    } catch (Throwable t) {
      log.error("Start error", t);
      sendError(session, t.getMessage());
    }
  }

  private MediaProfileSpecType getMediaProfileFromMessage(JsonObject jsonMessage) {

    MediaProfileSpecType profile;
    switch (jsonMessage.get("mode").getAsString()) {
      case "audio-only":
        profile = MediaProfileSpecType.WEBM_AUDIO_ONLY;
        break;
      case "video-only":
        profile = MediaProfileSpecType.WEBM_VIDEO_ONLY;
        break;
      default:
        profile = MediaProfileSpecType.WEBM;
    }

    return profile;
  }

  private void connectAccordingToProfile(WebRtcEndpoint webRtcEndpoint, RecorderEndpoint recorder,
                                         MediaProfileSpecType profile) {
    switch (profile) {
      case WEBM:
        webRtcEndpoint.connect(recorder, MediaType.AUDIO);
        webRtcEndpoint.connect(recorder, MediaType.VIDEO);
        break;
      case WEBM_AUDIO_ONLY:
        webRtcEndpoint.connect(recorder, MediaType.AUDIO);
        break;
      case WEBM_VIDEO_ONLY:
        webRtcEndpoint.connect(recorder, MediaType.VIDEO);
        break;
      default:
        throw new UnsupportedOperationException("Unsupported profile for this tutorial: " + profile);
    }
  }
  // ????????? ??????( WebRtcEndpoint??? PlayerEndpoint)??? ???????????? ????????? ?????????????????? ????????? ???????????????. ?????? ?????? ????????? ???????????? ?????????????????? ????????????.
  private void play(UserSession user, final WebSocketSession session, JsonObject jsonMessage) {
    try {
      String path=jsonMessage.get("path").getAsString(); // ????????????????????? ????????? ??????
      System.out.println("????????????????????? ????????? path :: " + path);

      // 1. Media logic
      final MediaPipeline pipeline = kurento.createMediaPipeline();
      WebRtcEndpoint webRtcEndpoint = new WebRtcEndpoint.Builder(pipeline).build();
      PlayerEndpoint player = new PlayerEndpoint.Builder(pipeline, "file:///tmp/"+path).build(); //??????????????? ????????? ????????? ??????
      player.connect(webRtcEndpoint);

      // Player listeners
      player.addErrorListener(new EventListener<ErrorEvent>() {
        @Override
        public void onEvent(ErrorEvent event) {
          log.info("ErrorEvent for session '{}': {}", session.getId(), event.getDescription());
          sendPlayEnd(session, pipeline);
        }
      });
      player.addEndOfStreamListener(new EventListener<EndOfStreamEvent>() {
        @Override
        public void onEvent(EndOfStreamEvent event) {
          log.info("EndOfStreamEvent for session '{}'", session.getId());
          sendPlayEnd(session, pipeline);
        }
      });

      // 2. Store user session
      user.setMediaPipeline(pipeline);
      user.setWebRtcEndpoint(webRtcEndpoint);

      // SDP??? ??? ???????????? Session ????????? ?????? ?????? ??????????????????.
      // ?????? ??????, ????????? ????????? ????????? ????????? ?????? ??? ????????? ?????????, ????????? ???????????? IP ???... ?????? ?????? ???????????? Signaling??? ????????????.

      // 3. SDP negotiation
      String sdpOffer = jsonMessage.get("sdpOffer").getAsString();
      String sdpAnswer = webRtcEndpoint.processOffer(sdpOffer);

      JsonObject response = new JsonObject();
      response.addProperty("id", "playResponse");
      response.addProperty("sdpAnswer", sdpAnswer);

      // 4. Gather ICE candidates
      webRtcEndpoint.addIceCandidateFoundListener(new EventListener<IceCandidateFoundEvent>() {

        @Override
        public void onEvent(IceCandidateFoundEvent event) {
          JsonObject response = new JsonObject();
          response.addProperty("id", "iceCandidate");
          response.add("candidate", JsonUtils.toJsonObject(event.getCandidate()));
          try {
            synchronized (session) {
              session.sendMessage(new TextMessage(response.toString()));
            }
          } catch (IOException e) {
            log.error(e.getMessage());
          }
        }
      });

      // 5. Play recorded stream
      System.out.println("????????? ?????? play================");
      player.play();

      synchronized (session) {
        session.sendMessage(new TextMessage(response.toString()));
      }

      webRtcEndpoint.gatherCandidates();
    } catch (Throwable t) {
      log.error("Play error", t);
      sendError(session, t.getMessage());
    }

  }

  public void sendPlayEnd(WebSocketSession session, MediaPipeline pipeline) {
    try {
      JsonObject response = new JsonObject();
      response.addProperty("id", "playEnd");
      session.sendMessage(new TextMessage(response.toString()));
    } catch (IOException e) {
      log.error("Error sending playEndOfStream message", e);
    }
    // Release pipeline
    pipeline.release();
  }

  private void sendError(WebSocketSession session, String message) {
    try {
      JsonObject response = new JsonObject();
      response.addProperty("id", "error");
      response.addProperty("message", message);
      session.sendMessage(new TextMessage(response.toString()));
    } catch (IOException e) {
      log.error("Exception sending message", e);
    }
  }
  // delete ?????? 
  private void del(UserSession user) throws FileNotFoundException {
    log.info("delete ???????????? ??????");
    for (String video : user.getVideos()) {
//      String filePath = "/home/ssafy/share/files/"+video+".webm";
      String fileName = video+".webm";
//      log.info("filePath : {}",filePath);
      log.info("fileName : {}",fileName);
      try {
        storageService.deleteVideo(fileName);
      }catch (InvalidPathException e){
          e.printStackTrace();
          log.error("????????? ???????????????!");
      }

//      File deleteFile = new File(filePath);
//      // ????????? ??????????????? ?????? ??????????????? true, ???????????????????????? false
//      if(deleteFile.exists()) {
//        deleteFile.delete();
//        log.info("????????? ?????????????????????.");
//      } else {
//        log.info("????????? ????????????.");
//      }
    }
    user.getVideos().clear();
    return;
  }
}
