package com.study.sbb.entity;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String content;

    @Column(insertable = false, updatable = false, columnDefinition = "date default LOCALTIMESTAMP")
    private Date createDate;

    /*수정일시*/
    @Column(insertable = true, updatable = true, columnDefinition = "date default LOCALTIMESTAMP")
    private Date modifyDate;

    @ManyToOne
    private Question question; //답변엔티티에 질문엔티티참조 (질문1:답변N)

    @ManyToOne //여러개의 질문이 한명의 사용자에게 작성될수있으므로 @ManyToOne
    private SiteUser author; /*author 속성추가*/

    @ManyToMany //하나의 질문에 여러사람이 추천, 한사람이 여러개의 질문을 추천(대등관계)
    Set<SiteUser> voter; /*추천인은 중복되면 안됨-Set*/
    
    @OneToMany(mappedBy = "answer")
    private List<Comment> commentList;
}