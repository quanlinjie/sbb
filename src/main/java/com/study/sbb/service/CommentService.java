package com.study.sbb.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.study.sbb.entity.Answer;
import com.study.sbb.entity.Comment;
import com.study.sbb.entity.Question;
import com.study.sbb.entity.SiteUser;
import com.study.sbb.repository.CommentRepository;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    public Comment create(Question question, SiteUser author, String content) {
        Comment c = new Comment();
        c.setContent(content);
        c.setCreateDate(LocalDateTime.now());
        c.setQuestion(question);
        c.setAuthor(author);
        c = this.commentRepository.save(c);
        return c;
    }

    public Optional<Comment> getComment(Integer id) {
        return this.commentRepository.findById(id);
    }

    public Comment modify(Comment c, String content) {
        c.setContent(content);
        c.setModifyDate(LocalDateTime.now());
        c = this.commentRepository.save(c);
        return c;
    }

    public void delete(Comment c) {
        this.commentRepository.delete(c);
    }

	public Comment create(Answer answer, SiteUser author, String content) {
		// TODO Auto-generated method stub
		Comment c1 = new Comment();
		c1.setContent(content);
        c1.setCreateDate(LocalDateTime.now());
        c1.setAnswer(answer);
        c1.setAuthor(author);
        c1 = this.commentRepository.save(c1);
		return c1;
	}
}