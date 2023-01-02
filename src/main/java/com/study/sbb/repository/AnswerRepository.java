package com.study.sbb.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.study.sbb.entity.Answer;
import com.study.sbb.entity.Question;

public interface AnswerRepository extends JpaRepository<Answer, Integer>{
	//<Answer, Integer> 처럼 리포지터리의 대상이 되는 엔티티의 타입(Answer)과 해당 엔티티의 PK의 속성 타입(Integer)을 지정해야 한다. 이것은 JpaRepository를 생성하기 위한 규칙

		Page<Answer> findAllByQuestion(Question question, Pageable pageable); // 답변 페이징
}