package com.cooksys.quiz_api.mappers;

import java.util.List;

import org.mapstruct.Mapper;

import com.cooksys.quiz_api.dtos.AnswerResponseDto;
import com.cooksys.quiz_api.entities.Answer;

@Mapper(componentModel = "spring")
public interface AnswerMapper {

  AnswerResponseDto entityToDto(Answer entity);

  List<AnswerResponseDto> entitiesToDtos(List<Answer> entities);

}
