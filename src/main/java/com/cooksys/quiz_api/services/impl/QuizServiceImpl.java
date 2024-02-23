package com.cooksys.quiz_api.services.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.cooksys.quiz_api.dtos.QuestionRequestDto;
import com.cooksys.quiz_api.dtos.QuestionResponseDto;
import com.cooksys.quiz_api.dtos.QuizRequestDto;
import com.cooksys.quiz_api.dtos.QuizResponseDto;
import com.cooksys.quiz_api.entities.Answer;
import com.cooksys.quiz_api.entities.Question;
import com.cooksys.quiz_api.entities.Quiz;
import com.cooksys.quiz_api.mappers.QuestionMapper;
import com.cooksys.quiz_api.mappers.QuizMapper;
import com.cooksys.quiz_api.repositories.AnswerRepository;
import com.cooksys.quiz_api.repositories.QuestionRepository;
import com.cooksys.quiz_api.repositories.QuizRepository;
import com.cooksys.quiz_api.services.QuizService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

	private final QuizRepository quizRepository;
	private final QuestionRepository questionRepository;
	private final AnswerRepository answerRepository;
	private final QuizMapper quizMapper;
	private final QuestionMapper questionMapper;

	// save each question and answer to respective repository
	private void saveQuestionsAndAnswers(Quiz quiz) {
		for (Question question : quiz.getQuestions()) {
			question.setQuiz(quiz);
			question.setDeleted(false);
			questionRepository.saveAndFlush(question);
			for (Answer answer : question.getAnswers()) {
				answer.setQuestion(question);
				answerRepository.saveAndFlush(answer);
			}
		}
	}

	private Quiz getQuiz(Long id) {
		Optional<Quiz> optionalQuiz = quizRepository.findByIdAndDeletedFalse(id);
		return optionalQuiz.get();
	}

	@Override
	public List<QuizResponseDto> getAllQuizzes() {
		List<Quiz> quizzesWithoutDeletedQuestions = quizRepository.findAllByDeletedFalse();
		quizzesWithoutDeletedQuestions.forEach(quiz -> quiz.setQuestions(
				quiz.getQuestions().stream().filter(question -> !question.isDeleted()).collect(Collectors.toList())));
		return quizMapper.entitiesToDtos(quizzesWithoutDeletedQuestions);
		// return quizMapper.entitiesToDtos(quizRepository.findAllByDeletedFalse());
	}

	@Override
	public QuizResponseDto createQuiz(QuizRequestDto quizRequestDto) {
		Quiz quizToSave = quizMapper.requestDtoToEntity(quizRequestDto);
		quizRepository.saveAndFlush(quizToSave);
		saveQuestionsAndAnswers(quizToSave);
		quizToSave.setDeleted(false);
		return quizMapper.entityToDto(quizToSave);
	}

	@Override
	public QuizResponseDto deleteQuiz(Long id) {
		Quiz quizToDelete = getQuiz(id);
		quizToDelete.setDeleted(true);
		return quizMapper.entityToDto(quizRepository.saveAndFlush(quizToDelete));
	}

	@Override
	public QuizResponseDto updateQuiz(Long id, String newName) {
		Quiz quizToUpdate = getQuiz(id);
		quizToUpdate.setName(newName);
		return quizMapper.entityToDto(quizRepository.saveAndFlush(quizToUpdate));
	}

	@Override
	public QuestionResponseDto getRandomQuestion(Long id) {
		Quiz quizToGetRandomQuestion = getQuiz(id);
		List<Question> notDeletedQuestions = quizToGetRandomQuestion.getQuestions().stream()
				.filter(question -> !question.isDeleted()).collect(Collectors.toList());
		int randomQuestionIdx = (int) (Math.random() * notDeletedQuestions.size());
		return questionMapper.entityToDto(notDeletedQuestions.get(randomQuestionIdx));
	}

	@Override
	public QuizResponseDto addQuestion(Long id, QuestionRequestDto questionRequestDto) {
		Question questionToAdd = questionMapper.requestDtoToEntity(questionRequestDto);
		Quiz quizToUpdate = getQuiz(id);

		questionToAdd.setQuiz(quizToUpdate);
		quizToUpdate.getQuestions().add(questionToAdd);
		for (Answer answer : questionToAdd.getAnswers()) {
			answer.setQuestion(questionToAdd);
			if (answer.isCorrect()) {
				answer.setCorrect(true);
			}
		}

		quizRepository.saveAndFlush(quizToUpdate);
		questionRepository.saveAndFlush(questionToAdd);
		for (Answer answer : questionToAdd.getAnswers()) {
			answerRepository.saveAndFlush(answer);
		}

		return quizMapper.entityToDto(quizToUpdate);
	}

	@Override
	public QuestionResponseDto deleteQuestionFromQuiz(Long id, Long questionID) {
		Quiz quizToDeleteQuestionFrom = getQuiz(id);
		for (Question question : quizToDeleteQuestionFrom.getQuestions()) {
			if (question.getId().equals(questionID)) {
				question.setDeleted(true);
				quizRepository.saveAndFlush(quizToDeleteQuestionFrom);
				return questionMapper.entityToDto(question);
			}
		}
		return null;
	}

}
