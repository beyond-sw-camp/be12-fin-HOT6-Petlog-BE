package com.hot6.backend.board.answer;

import com.hot6.backend.board.answer.images.AnswerImageService;
import com.hot6.backend.board.answer.model.Answer;
import com.hot6.backend.board.answer.model.AnswerDto;
import com.hot6.backend.board.question.QuestionRepository;
import com.hot6.backend.board.question.model.Question;
import com.hot6.backend.common.BaseResponseStatus;
import com.hot6.backend.common.exception.BaseException;
import com.hot6.backend.user.UserRepository;
import com.hot6.backend.user.model.User;
import com.hot6.backend.user.model.UserType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final AnswerImageService answerImageService;

    @Transactional(readOnly = false)
    public void create(User user, AnswerDto.AnswerRequest request, List<MultipartFile> images) {
        if (user.getUserType() == UserType.AI) {
            throw new BaseException(BaseResponseStatus.AI_ANSWER_FORBIDDEN);
        }

        Question question = questionRepository.findById(request.getQuestionIdx())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.QUESTION_NOT_FOUND));

        Answer answer = Answer.builder()
                .question(question)
                .user(user)
                .content(request.getContent())
                .selected(false)
                .isAi(false)
                .build();

        try {
            Answer saved = answerRepository.save(answer);
            if (images != null && !images.isEmpty()) {
                answerImageService.saveImages(images, saved);
            }
        } catch (Exception e) {
            throw new BaseException(BaseResponseStatus.ANSWER_CREATE_FAILED);
        }
    }
    @Transactional(readOnly = true)
    public List<AnswerDto.AnswerResponse> listByQuestion(Long questionIdx) {
        return answerRepository.findByQuestionIdxWithAssociations(questionIdx).stream()
                .map(AnswerDto.AnswerResponse::from)
                .toList();
    }

    @Transactional(readOnly = false)
    public void select(Long idx) {
        Answer answer = answerRepository.findById(idx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.ANSWER_NOT_FOUND));

        if (answer.getUser().getUserType() == UserType.AI) {
            throw new BaseException(BaseResponseStatus.AI_ANSWER_FORBIDDEN);
        }

        try {
            answer.setSelected(true);
            answerRepository.save(answer);

            Question question = answer.getQuestion();
            question.setSelected(true);
            questionRepository.save(question);
        } catch (Exception e) {
            throw new BaseException(BaseResponseStatus.ANSWER_SELECTED_FAILED);
        }
    }

    @Transactional(readOnly = false)
    public void deleteByQuestionIdx(Long questionIdx) {
        List<Answer> answers = answerRepository.findByQuestion_IdxOrderByCreatedAtDesc(questionIdx);
        for (Answer answer : answers) {
            answerImageService.deleteImagesByAnswer(answer.getIdx());
            answerRepository.delete(answer);
        }
    }

    @Transactional(readOnly = false)
    public void update(Long idx, User currentUser, AnswerDto.AnswerRequest dto, List<MultipartFile> images) {
        Answer answer = answerRepository.findById(idx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.ANSWER_NOT_FOUND));

        if (answer.isSelected() || answer.getUser().getUserType() == UserType.AI) {
            throw new BaseException(BaseResponseStatus.AI_ANSWER_FORBIDDEN);
        }

        try {
            List<String> keepUrls = dto.getOriginalImageUrls() != null ? dto.getOriginalImageUrls() : List.of();
            answerImageService.deleteImagesExcept(answer, keepUrls);

            if (images != null && !images.isEmpty()) {
                answerImageService.saveImages(images, answer);
            }

            answer.setContent(dto.getContent());
            answerRepository.save(answer);
        } catch (Exception e) {
            throw new BaseException(BaseResponseStatus.ANSWER_UPDATE_FAILED);
        }
    }
    @Transactional(readOnly = true)
    public AnswerDto.AnswerResponse read(Long id) {
        Answer answer = answerRepository.findById(id)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.ANSWER_NOT_FOUND));
        return AnswerDto.AnswerResponse.from(answer);
    }

    @Transactional(readOnly = false)
    public void delete(Long idx, User user) {
        Answer answer = answerRepository.findById(idx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.ANSWER_NOT_FOUND));

        if (!answer.getUser().getIdx().equals(user.getIdx()) || answer.getUser().getUserType() == UserType.AI || answer.isSelected()) {
            throw new BaseException(BaseResponseStatus.AI_ANSWER_FORBIDDEN);
        }

        try {
            answerImageService.deleteImagesByAnswer(idx);
            answerRepository.delete(answer);
        } catch (Exception e) {
            throw new BaseException(BaseResponseStatus.ANSWER_DELETE_FAILED);
        }
    }

    @Transactional(readOnly = true)
    public int countByQuestionIdx(Long questionId) {
        return answerRepository.countByQuestion_IdxAndUser_UserTypeNot(questionId, UserType.AI);
    }
    @Transactional(readOnly = true)
    public List<AnswerDto.AnswerResponse> readByAnswer(Long userId) {
        return answerRepository.findByUser_IdxOrderByCreatedAtDesc(userId)
                .stream()
                .map(AnswerDto.AnswerResponse::from)
                .toList();
    }

    @Transactional(readOnly = false)
    public void createAiAnswerForQuestion(Question question, String aiContent) {
        if (aiContent == null || aiContent.trim().isEmpty()) {
            return;
        }

        User aiUser = userRepository.findByUserType(UserType.AI)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.AI_USER_NOT_FOUND));

        try {
            Answer aiAnswer = Answer.builder()
                    .question(question)
                    .user(aiUser)
                    .content(aiContent)
                    .isAi(true)
                    .build();

            answerRepository.save(aiAnswer);
        } catch (Exception e) {
            throw new BaseException(BaseResponseStatus.AI_ANSWER_GENERATE_FAILED);
        }
    }
}
