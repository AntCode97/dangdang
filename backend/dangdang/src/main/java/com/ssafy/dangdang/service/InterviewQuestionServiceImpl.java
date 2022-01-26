package com.ssafy.dangdang.service;

import com.ssafy.dangdang.domain.InterviewQuestion;
import com.ssafy.dangdang.domain.User;
import com.ssafy.dangdang.domain.dto.InterviewQuestionDto;
import com.ssafy.dangdang.repository.InterviewQuestionRepository;
import com.ssafy.dangdang.util.ApiUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.ssafy.dangdang.util.ApiUtils.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class InterviewQuestionServiceImpl implements InterviewQuestionService{


    private final InterviewQuestionRepository interviewQuestionRepository;

    @Override
    public InterviewQuestionDto writeQuestion(User user, InterviewQuestionDto interviewQuestionDto) {

        InterviewQuestion interviewQuestion = InterviewQuestion.of(interviewQuestionDto, user);
        interviewQuestionRepository.save(interviewQuestion);
        return InterviewQuestionDto.of(interviewQuestion);
    }

    @Override
    public ApiResult<String> deleteQuestion(User user, Long interviewQuestionId) {
        Optional<InterviewQuestion> question = interviewQuestionRepository.findById(interviewQuestionId);

        if (!question.isPresent()) return (ApiUtils.ApiResult<String>) error("없는 질문입니다.", HttpStatus.NOT_FOUND);
        if (question.get().getWriter().getId() == user.getId()) return (ApiResult<String>) error("작성자만 삭제할 수 있습니다.", HttpStatus.FORBIDDEN);
        interviewQuestionRepository.delete(question.get());
        return success("삭제 성공");
    }

    @Override
    public Optional<InterviewQuestion> findById(Long id){
        return interviewQuestionRepository.findById(id);
    }

    @Override
    public List<InterviewQuestionDto> getAllInterviewQustion(){
        List<InterviewQuestion> all = interviewQuestionRepository.findAllInterviewQuestion();
        List<InterviewQuestionDto> interviewQuestionDtos = all.stream().map(InterviewQuestionDto::of).collect(Collectors.toList());
        return interviewQuestionDtos;
    }

}