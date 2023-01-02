package com.study.sbb.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.study.sbb.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Integer> {
}