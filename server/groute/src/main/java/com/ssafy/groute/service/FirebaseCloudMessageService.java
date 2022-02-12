package com.ssafy.groute.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.ssafy.groute.dto.FcmMessage;
import com.ssafy.groute.dto.Notification;
import com.ssafy.groute.dto.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
public class FirebaseCloudMessageService {
    private final String API_URL = "https://fcm.googleapis.com/v1/projects/iamgroute-a57c7/messages:send";

    private final ObjectMapper objectMapper;

    @Autowired
    UserService userService;

    @Autowired
    NotificationService notiService;

    /**
     * FCM에 push 요청을 보낼 때 인증을 위해 Header에 포함시킬 AccessToken 생성
     * @return
     * @throws IOException
     */
    private String getAccessToken() throws IOException {
        String firebaseConfigPath = "firebase/firebase_service_key.json";

        // GoogleApi를 사용하기 위해 oAuth2를 이용해 인증한 대상을 나타내는 객체
        GoogleCredentials googleCredentials = GoogleCredentials
                // 서버로부터 받은 service key 파일 활용
                .fromStream(new ClassPathResource(firebaseConfigPath).getInputStream())
                // 인증하는 서버에서 필요로 하는 권한 지정
                .createScoped(Arrays.asList("https://www.googleapis.com/auth/cloud-platform"));

        // accessToken 생성
        googleCredentials.refreshIfExpired();

        // GoogleCredential의 getAccessToken으로 토큰 받아온 뒤, getTokenValue로 최종적으로 받음
        // REST API로 FCM에 push 요청 보낼 때 Header에 설정하여 인증을 위해 사용
        return googleCredentials.getAccessToken().getTokenValue();
    }

    /**
     * FCM 알림 메시지 생성
     * @param targetToken
     * @param title
     * @param body
     * @param path
     * @return
     * @throws JsonProcessingException
     */
    private String makeMessage(String targetToken, String title, String body, String path) throws JsonProcessingException {
        FcmMessage fcmMessage = FcmMessage.builder()
                .message(FcmMessage.Message.builder()
                        .token(targetToken)
                        .notification(FcmMessage.Notification.builder()
                                .title(title)
                                .body(body)
                                .image(null)
                                .build()
                        )
                        .data(FcmMessage.Data.builder()
                                .path(path)
                                .build()
                        )
                        .build()
                ).validate_only(false).build();

        log.info(objectMapper.writeValueAsString(fcmMessage));
        return objectMapper.writeValueAsString(fcmMessage);
    }

    /**
     * targetToken에 해당하는 device로 FCM 푸시 알림 전송
     * @param targetToken
     * @param title
     * @param body
     * @param path
     * @throws Exception
     */
    public void sendMessageTo(String targetToken, String title, String body, String path) throws Exception {
        String message = makeMessage(targetToken, title, body, path);
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(message, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(API_URL)
                .post(requestBody)
                .addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .addHeader(HttpHeaders.CONTENT_TYPE, "application/json; UTF-8")
                .build();

        Response response  = client.newCall(request).execute();

        log.info(response.body().string());

        if(response.isSuccessful()) {
            User user = userService.selectUserByToken(targetToken);
            log.info("USERID  "+ user);
            notiService.insertNotification(new Notification(0, user.getId(), title, body, null));  // title == category
        }
    }

    /**
     * client의 Token이 변경되면 user 테이블 값을 갱신한다.
     * @param token
     * @param userId
     * @throws Exception
     */
    public void updateToken(String token, String userId) throws Exception{
        User user = new User();
        user.setId(userId);
        user.setToken(token);
        userService.updateTokenByUserId(user);
    }

    /**
     * 등록된 모든 토큰을 이용해서 broadcasting
     * @param title
     * @param body
     * @return
     * @throws Exception
     */
    public int broadCastMessage(String title, String body, String path) throws Exception {
        // path는 application 초기 화면
        List<User> users = userService.selectAllUser();
        for(int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            if(user.getToken() != null) {
//            if(user != null && !user.getToken().isEmpty() && user.getToken() != null) {
                log.debug("broadcastmessage : {},{},{}", user.getToken(), title, body);
                sendMessageTo(user.getToken(), title, body, path);
            }
        }
        return users.size();
    }

}
