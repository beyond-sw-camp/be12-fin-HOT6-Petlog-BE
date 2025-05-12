package com.hot6.backend.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BaseResponseStatus {

    /* 200 : 요청 성공 */
    SUCCESS(true, 200, "요청에 성공했습니다."),


    /* 1000 ~ 1999 : 회원 관련 에러 */
    //-- 1000 : 회원가입 에러
    USER_SIGNUP_FAILED(false, 1000, "회원가입에 실패했습니다."),
    USER_ALREADY_EXISTS(false, 1001, "이미 존재하는 사용자입니다."),
    //-- 1100 : 로그인 에러
    LOGIN_FAILED(false, 1100, "로그인에 실패했습니다."),
    INVALID_CREDENTIALS(false, 1101, "아이디 또는 비밀번호가 올바르지 않습니다."),
    EMAIL_VERIFY_FAIL(false, 1102, "이메일 인증이 완료되지 않았습니다."),
    INVALID_JWT(false, 1103, "유효하지 않은 JWT 토큰입니다."),
    //-- 1200 : 계정 정보 수정
    ACCOUNT_UPDATE_FAILED(false, 1200, "계정 정보 수정에 실패했습니다."),
    INVALID_USER_INFO(false, 1201, "잘못된 사용자 정보입니다."),
    //-- 1300 : 유저 정보 조회
    USER_NOT_FOUND(false, 1300, "찾을 수 없는 사용자입니다."),
    //-- 1400 : 탈퇴한 유저 로그인 에러
    USER_DELETED_LOGIN(false,1400,"탈퇴한 유저 입니다."),
    /* 2000 ~ 2999 : 채팅 관련 에러 */

    //-- 2000 : 채팅방 접근 / 참여 관련
    CHAT_ROOM_NOT_FOUND(false, 2000, "존재하지 않는 채팅방입니다."),
    CHAT_ROOM_ACCESS_DENIED(false, 2001, "해당 채팅방에 접근할 수 없습니다."),
    CHAT_ROOM_PARTICIPANT_NOT_FOUND(false, 2002, "채팅방 참여자가 아닙니다."),
    CHAT_ROOM_ALREADY_PARTICIPATED(false, 2003, "이미 참여 중인 채팅방입니다."),
    CHAT_ROOM_JOIN_FAILED(false, 2004, "채팅방 참여에 실패했습니다."),
    CHAT_ROOM_LEAVE_FAILED(false, 2005, "채팅방 나가기에 실패했습니다."),
    MAX_PARTICIPANT_LIMIT(false,2006,"채팅방 참여 인원이 모두 찼습니다."),
    CHAT_ROOM_FIRST_JOIN_ID_NULL(false,2007,"채팅방 참여 시 메시지를 가져올 때, 메시지 메타정보가 null 입니다."),

    //-- 2100 : 채팅방 생성/수정 관련
    CHAT_ROOM_CREATION_FAILED(false, 2100, "채팅방 생성에 실패했습니다."),
    CHAT_ROOM_USER_NOT_FOUNT(false,2104,"채팅방 수정 시, 채팅방 유저 조회에 실패하였습니다."),
    CHAT_ROOM_UPDATE_FAILED(false, 2101, "채팅방 수정에 실패했습니다."),
    CHAT_ROOM_DELETE_FAILED(false, 2102, "채팅방 삭제에 실패했습니다."),
    CHAT_ROOM_HASHTAG_FORMAT_INVALID(false, 2103, "해시태그 형식이 잘못되었습니다."),
    CHAT_ROOM_UPDATE_NO_PERMISSION(false, 2104, "채팅방 수정 권한이 없습니다."),

    //-- 2200 : 채팅 메시지 관련
    CHAT_MESSAGE_SEND_FAILED(false, 2200, "채팅 메시지 전송에 실패했습니다."),
    CHAT_MESSAGE_NOT_FOUND(false, 2201, "존재하지 않는 채팅 메시지입니다."),
    CHAT_MESSAGE_ACCESS_DENIED(false, 2202, "해당 메시지에 접근할 수 없습니다."),
    CHAT_MESSAGE_READ_UPDATE_FAILED(false, 2203, "메시지 읽음 처리에 실패했습니다."),

    //-- 2300 : WebSocket 관련
    CHAT_WEBSOCKET_CONNECT_FAILED(false, 2300, "웹소켓 연결에 실패했습니다."),
    CHAT_WEBSOCKET_UNAUTHORIZED(false, 2301, "웹소켓 인증에 실패했습니다."),
    CHAT_WEBSOCKET_INVALID_HEADER(false, 2302, "웹소켓 헤더 정보가 유효하지 않습니다."),

    //-- 2400 : 기타 채팅 도메인 에러
    CHAT_UNEXPECTED_ERROR(false, 2400, "알 수 없는 채팅 오류가 발생했습니다."),
    CHAT_FEATURE_NOT_IMPLEMENTED(false, 2401, "아직 구현되지 않은 채팅 기능입니다."),

    /* 3000~ 3999 : 일정 관련 에러 */
    //-- 3000 : 일정 조회
    SCHEDULE_NOT_FOUND(false, 3000, "일정 조회에 실패했습니다."),
    //-- 3100 : 일정 등록
    SCHEDULE_CREATE_FAILED(false, 3100, "일정 등록에 실패했습니다."),
    //-- 3200 : 일정 수정
    SCHEDULE_UPDATE_FAILED(false, 3200, "일정 수정에 실패했습니다."),
    //-- 3300 : 반려동물 일정 조회
    SHARED_SCHEDULE_NOT_FOUND(false,3300,"공유된 반려동물 일정을 찾을 수 없습니다."),
    //-- 3400 : 일정 반복
    SCHEDULE_INVALID_REPEAT_CYCLE(false, 3400, "반복 주기 값이 잘못되었습니다. (일, 주, 월만 가능) "),

    /* 4000~ 4999 : 기록 관련 에러 */
    //-- 4000 : 기록 조회
    DAILY_RECORD_NOT_FOUND(false, 4000, "기록 조회에 실패했습니다."),
    //-- 4100 : 기록 등록
    DAILY_RECORD_CREATE_FAILED(false, 4100, "기록 등록에 실패했습니다."),
    //-- 4200 : 기록 수정
    DAILY_RECORD_UPDATE_FAILED(false, 4200, "기록 수정에 실패했습니다."),


    /* 5000 ~ 5999 : 펫 관련 에러 */
    //-- 5000 : 펫 조회
    PET_NOT_FOUND(false, 5000, "반려동물 조회에 실패했습니다."),
    //-- 5001 : 펫 조회 + id
    PET_NOT_FOUND_WITH_ID(false, 5001, "반려동물 조회에 실패했습니다. PetId: {0}"),
    //-- 5100 : 펫 등록
    PET_CREATE_FAILED(false, 5100, "반려동물 등록에 실패했습니다."),
    //-- 5200 : 펫 수정
    PET_UPDATE_FAILED(false, 5200, "반려동물 정보 수정에 실패했습니다."),

    /* 6000 ~ 6999 : 게시판 관련 에러 */
    //-- 6000 : 게시글
    POST_NOT_FOUND(false, 6000, "게시글 조회에 실패했습니다."),

    POST_CREATE_FAILED(false, 6001, "게시글 등록에 실패했습니다."),

    POST_UPDATE_FAILED(false, 6002, "게시글 수정에 실패했습니다."),

    POST_DELETE_FAILED(false, 6003, "게시글 삭제에 실패했습니다."),

    POST_SEARCH_FAILED(false, 6004, "게시글 검색에 실패했습니다."),

    POST_DETAIL_FAILED(false, 6005, "게시글 상세조회에 실패했습니다."),

    POST_REQUIRED_TITLE(false, 6006, "제목이 입력되지 않았습니다"),

    POST_REQUIRED_CONTENT(false, 6007, "내용이 입력되지 않았습니다"),
    POST_BOARD_NOT_FOUND(false, 6008, "게시판 명 조회 실패."),

    //-- 6100 : 댓글
    COMMENT_NOT_FOUND(false, 6100,"댓글 조회에 실패했습니다."),

    COMMENT_CREATE_FAILED(false, 6101, "댓글 등록에 실패했습니다."),

    COMMENT_UPDATE_FAILED(false, 6102, "댓글 수정에 실패했습니다."),

    COMMENT_DELETE_FAILED(false, 6103, "댓글 삭제에 실패했습니다."),

    //-- 6200 : 질문, 해시태그
    QUESTION_NOT_FOUND(false, 6200,"질문 조회에 실패했습니다."),

    QUESTION_CREATE_FAILED(false, 6201, "질문 등록에 실패했습니다."),

    QUESTION_UPDATE_FAILED(false, 6202, "질문 수정에 실패했습니다."),

    QUESTION_DELETE_FAILED(false, 6203, "질문 삭제에 실패했습니다."),

    QUESTION_SEARCH_FAILED(false, 6204, "질문 검색에 실패했습니다."),

    QUESTION_DETAIL_FAILED(false, 6205, "질문 상세조회에 실패했습니다."),

    HASHTAG_SAVE_FAILED(false, 6206, "해시태그 저장에 실패했습니다."),

    HASHTAG_NOT_FOUND(false, 6207, "해시태그 조회에 실패했습니다."),

    QUESTION_REQUIRED_TITLE(false, 6208, "제목이 입력되지 않았습니다"),

    QUESTION_REQUIRED_CONTENT(false, 6209, "내용이 입력되지 않았습니다"),

    //-- 6300 : 답변
    ANSWER_NOT_FOUND(false, 6300,"답변 조회에 실패했습니다."),

    ANSWER_CREATE_FAILED(false, 6301, "답변 등록에 실패했습니다."),

    ANSWER_UPDATE_FAILED(false, 6302, "답변 수정에 실패했습니다."),

    ANSWER_DELETE_FAILED(false, 6303, "답변 삭제에 실패했습니다."),

    ANSWER_SELECTED_FAILED(false, 6304, "답변 채택에 실패했습니다."),

    ANSWER_DETAIL_FAILED(false, 6305, "답변 상세조회에 실패했습니다."),

    AI_ANSWER_GENERATE_FAILED(false, 6306, "AI 답변 생성에 실패했습니다."),

    AI_ANSWER_FORBIDDEN(false, 6307, "AI는 이 작업을 수행할 수 없습니다."),

    AI_USER_NOT_FOUND(false, 6308, "AI 유저를 찾을 수 없습니다."),

    /* 7000 ~ 7999 : 알림 관련 에러 */
    // 7000 : 알림 조회 실패
    NOTIFICATION_NOT_FOUND(false, 7000, "해당 알림이 존재하지 않습니다."),

    // 7100 : 알림 생성 시 스케줄 없음
    SCHEDULE_NOT_FOUND_FOR_NOTIFICATION(false, 7100, "알림 전송을 위한 스케줄이 존재하지 않습니다."),

    // 7200 : 알림 삭제 실패
    NOTIFICATION_DELETE_FAILED(false, 7200, "알림 삭제에 실패했습니다."),

    // 7300 : 웹소켓 전송 실패
    NOTIFICATION_SEND_FAILED(false, 7300, "알림 전송에 실패했습니다."),

    // 7400 : 알림 읽음 처리 실패
    NOTIFICATION_MARK_AS_READ_FAILED(false, 7400, "알림 읽음 처리에 실패했습니다."),

    /* 8000 ~ 8999 : 관리자 관련 에러 */
    ADMIN_CATEGORY_DELETE_FAILED_REASON_CONNECT_STORE(false, 8000, "삭제하려는 카테고리는 Store에 하나 이상 연결되어 있는 카테고리 입니다. Store에서 카테고리 수정을 먼저 한 후 삭제해주세요."),


    /* 9000 ~ 9999 : 카테고리 관련 에러 */
    CATEGORY_NOT_FOUND(false, 9000, "카테고리를 찾을 수 없습니다."),
    //-- 9100 : 카테고리 등록 에러
    CATEGORY_REGIST_FAILED(false, 9100, "카테고리 등록에 실패했습니다."),
    //-- 9200 : 부모 카테고리 조회 에러
    CATEGORY_NOT_FOUND_PARENT_CATEGORY(false, 9200, "부모 카테고리를 찾을 수 없습니다."),
    // 9300 : 유효하지 않은 카테고리 타입
    CATEGORY_INVALID_TYPE(false, 9300, "유효하지 않은 카테고리 타입입니다."),

    /* 10000 ~ 10999 : 이미지 관련 에러 */
    //-- 10000 : 이미지 업로드 실패
    IMAGE_UPLOAD_FAILED(false,10000,"이미지 업로드에 실패했습니다"),
    //-- 10100 : 이미지 다운로드 에러
    INVALID_FILE_TYPE(false, 10100, "사진 파일만 다운로드 가능합니다."),
    //-- 10200 : 파일 다운로드 에러
    FILE_DOWNLOAD_FAILED(false, 10200, "파일 다운로드에 실패했습니다.");


    private final boolean isSuccess;
    private final int code;
    private final String message;
}