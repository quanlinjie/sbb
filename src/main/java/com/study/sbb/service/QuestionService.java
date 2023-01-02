package com.study.sbb.service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.study.sbb.entity.Answer;
import com.study.sbb.entity.Question;
import com.study.sbb.entity.QuestionPage;
import com.study.sbb.entity.SiteUser;
import com.study.sbb.handler.DataNotFoundException;
import com.study.sbb.repository.QuestionPageRepository;
import com.study.sbb.repository.QuestionRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class QuestionService {
	private final QuestionRepository questionRepository;
	private final QuestionPageRepository questionPageRepository;

	private Specification<Question> search(String kw) { // 검색어(kw)를 입력받아 쿼리의 조인문과 where문을 생성하여 리턴하는 메서드
		return new Specification<>() {
			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<Question> q, CriteriaQuery<?> query, CriteriaBuilder cb) {
				query.distinct(true); // 중복을 제거
				Join<Question, SiteUser> u1 = q.join("author", JoinType.LEFT);
				Join<Question, Answer> a = q.join("answerList", JoinType.LEFT);
				Join<Answer, SiteUser> u2 = a.join("author", JoinType.LEFT);
				return cb.or(cb.like(q.get("subject"), "%" + kw + "%"), // 제목
						cb.like(q.get("content"), "%" + kw + "%"), // 내용
						cb.like(u1.get("username"), "%" + kw + "%"), // 질문 작성자
						cb.like(a.get("content"), "%" + kw + "%"), // 답변 내용
						cb.like(u2.get("username"), "%" + kw + "%")); // 답변 작성자
			}
		};
	}

	public List<Question> getList() {
		return this.questionRepository.findAll();
	}

	public Question getQuestion(Integer id) {
		Optional<Question> question = this.questionRepository.findById(id);
		if (question.isPresent()) {
			// 조회수
			Question question1 = question.get();
			question1.setCountview(question1.getCountview() + 1);
			this.questionRepository.save(question1);
			return question1;
			// 조회수끝
			// return question.get();
		} else {
			throw new DataNotFoundException("question not found");
		}
	}

	// 파일저장할위치
	@Value("${ImgLocation}")
	private String imgLocation;

	public void create(String subject, String content, SiteUser user, String category, MultipartFile file)
			throws Exception { /* 카테고리추가 */
		// 제목과 내용을 입력으로 하여 질문 데이터를 저장하는 create 메서드

		String projectPath = imgLocation; // 파일 저장 위치 = projectPath
		UUID uuid = UUID.randomUUID(); // 식별자.랜덤으로 이름 만들어 줌
		String fileName = uuid + "_" + file.getOriginalFilename(); // 저장될 파일 이름 지정=랜덤식별자_원래 파일 이름
		File saveFile = new File(projectPath, fileName); // 빈껍데기생성 이름은 fileName, projectPath라는 경로에 담김
		file.transferTo(saveFile);

		Question q = new Question();
		q.setSubject(subject);
		q.setContent(content);
		q.setCreateDate(LocalDateTime.now());
		q.setAuthor(user);
		q.setCategory(category); // 카테고리추가

		q.setFilename(fileName); // 파일이름
		q.setFilepath(projectPath + fileName); // 저장경로,파일이름

		this.questionRepository.save(q);
	}

	public Page<Question> getList(int page, String kw) {
		List<Sort.Order> sorts = new ArrayList<>();
		sorts.add(Sort.Order.desc("createDate")); // 작성일시 역순으로 조회하기
		Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
		Specification<Question> spec = search(kw); // kw를 getList에 추가하고 kw 값으로 Specification 객체를 생성하여 findAll 메서드 호출시 전달
		return this.questionRepository.findAll(spec, pageable);
//      return this.questionRepository.findAllByKeyword(kw, pageable);
	}

	public Question modify(Question question, String subject, String content, String category, MultipartFile file)
			throws Exception {
		String projectPath = imgLocation;

		if (file.getOriginalFilename().equals("")) {
			// 새 파일이 없을 때
			question.setFilename(question.getFilename());
			question.setFilepath(question.getFilepath());

		} else if (file.getOriginalFilename() != null) {
			// 새 파일이 있을 때
			File f = new File(question.getFilepath());

			if (f.exists()) { // 파일이 존재하면
				f.delete(); // 파일 삭제
			}

			UUID uuid = UUID.randomUUID();
			String fileName = uuid + "_" + file.getOriginalFilename();
			File saveFile = new File(projectPath, fileName);
			file.transferTo(saveFile);

			question.setFilename(fileName);
			question.setFilepath(projectPath + fileName);
		}

		question.setSubject(subject);
		question.setContent(content);
		question.setModifyDate(LocalDateTime.now());
		question.setCategory(category);

		this.questionRepository.save(question);

		return question;
	}

	public void delete(Question question) {
		this.questionRepository.delete(question);
	}

	public void vote(Question question, SiteUser siteUser) {
		question.getVoter().add(siteUser);
		this.questionRepository.save(question);
	}

	public QuestionPage getQuestionByPageId(Question question) {
		// 레파지토리에 작성해둔 findByPages 메서드에서 question엔티티의 id를 기준으로 쿼리문을 실행
		return questionPageRepository.findByPages(question.getId());
	}
}