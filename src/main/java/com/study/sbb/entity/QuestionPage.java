package com.study.sbb.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class QuestionPage {
    @Id
    private Integer id;
    private String PREVID;
    private String PREV_SUB;
    private String NEXTID;
    private String NEXT_SUB;
}