package com.hot6.backend.board.hashtagQuestion;

import com.hot6.backend.board.hashtagQuestion.model.Hashtag_Question;
import com.hot6.backend.board.hashtagQuestion.model.Hashtag_QuestionDto;
import com.hot6.backend.board.question.QuestionRepository;
import com.hot6.backend.board.question.model.Question;
import com.hot6.backend.common.BaseResponseStatus;
import com.hot6.backend.common.exception.BaseException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class Hashtag_QuestionService {

    private final Hashtag_QuestionRepository hashtagRepository;
    private final QuestionRepository questionRepository;

    public void saveTags(List<String> tags, Long questionIdx) {
        Question question = questionRepository.findById(questionIdx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.QUESTION_NOT_FOUND));

        try {
            List<Hashtag_Question> tagEntities = tags.stream()
                    .map(tag -> Hashtag_Question.builder().tag(tag).question(question).build())
                    .collect(Collectors.toList());

            hashtagRepository.saveAll(tagEntities);
        } catch (Exception e) {
            throw new BaseException(BaseResponseStatus.HASHTAG_SAVE_FAILED);
        }
    }

    public List<Hashtag_QuestionDto.Response> getTagsByQuestion(Long questionIdx) {
        try {
            return hashtagRepository.findByQuestionIdx(questionIdx).stream()
                    .map(entity -> new Hashtag_QuestionDto.Response(
                            entity.getIdx(),
                            entity.getTag(),
                            entity.getQuestion().getIdx()
                    )).collect(Collectors.toList());
        } catch (Exception e) {
            throw new BaseException(BaseResponseStatus.HASHTAG_NOT_FOUND);
        }
    }

    @Transactional
    public void deleteByQuestionIdx(Long questionIdx) {
        hashtagRepository.deleteByQuestionIdx(questionIdx);
    }
}
