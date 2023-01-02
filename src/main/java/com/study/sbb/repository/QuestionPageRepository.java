package com.study.sbb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.study.sbb.entity.QuestionPage;

public interface QuestionPageRepository extends JpaRepository<QuestionPage, Integer> {

    @Query(value = "SELECT * FROM(SELECT ID, " +
            "LAG(ID, 1, 0) OVER(ORDER BY ID ASC) AS PREVID, " +
            "LAG(subject, 1, '이전 글이 없습니다.') OVER (ORDER BY ID ASC) AS PREV_SUB," +
            "LEAD(ID, 1, 0) OVER(ORDER BY ID ASC) AS NEXTID, "+
            "LEAD(subject, 1, '다음 글이 없습니다') OVER (ORDER BY ID ASC) AS NEXT_SUB " +
            "FROM QUESTION) WHERE id = :id",
            nativeQuery = true)

    QuestionPage findByPages(@Param("id") Integer id); // 파싱되기를 원하는 파라미터 변수에 @Param("쿼리에서 이용할 변수명")으로 매칭
}