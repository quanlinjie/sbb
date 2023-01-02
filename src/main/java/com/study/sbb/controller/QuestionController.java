package com.study.sbb.controller;

import java.security.Principal;
import javax.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.study.sbb.entity.Answer;
import com.study.sbb.entity.Question;
import com.study.sbb.entity.QuestionPage;
import com.study.sbb.entity.SiteUser;
import com.study.sbb.form.AnswerForm;
import com.study.sbb.form.QuestionForm;
import com.study.sbb.repository.QuestionPageRepository;
import com.study.sbb.service.AnswerService;
import com.study.sbb.service.QuestionService;
import com.study.sbb.service.UserService;

import lombok.RequiredArgsConstructor;

@RequestMapping("/question")
@RequiredArgsConstructor // @RequiredArgsConstructor는 자동으로 생성자를 생성
@Controller
public class QuestionController {
// Controller -> Service -> Repository 구조로 데이터를 처리
	private final QuestionService questionService; // questionService 객체는 생성자 방식으로 DI 규칙에 의해 주입
	private final UserService userService;
	private final QuestionPageRepository questionPageRepository;
	private final AnswerService answerService;
	
	@GetMapping("/list")
	public String list(
			Model model,  // Model 객체는 자바 클래스와 템플릿 간의 연결고리 역할
			@RequestParam(value="page", defaultValue="0") int page,
			@RequestParam(value = "kw", defaultValue = "") String kw) {  // kw 파라미터를 추가했고 디폴트값으로 빈 문자열을 설정
//		List<Question> questionList = this.questionService.getList();
//		model.addAttribute("questionList", questionList); // Model 클래스를 사용하여 템플릿에 전달
		Page<Question> paging = this.questionService.getList(page, kw);
        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
		return "question_list";
	}

	@GetMapping("/detail/{id}")
	public String detail(
			Model model, 
			@PathVariable("id") Integer id, 
			AnswerForm answerForm, // question_detail 템플릿이 AnswerForm을 사용하기 때문
			@RequestParam(value="page", defaultValue="0") int page) { 
		
		Question question = this.questionService.getQuestion(id); // QuestionService의 getQuestion 메서드를 호출하여 Question 객체를 템플릿에 전달
		model.addAttribute("question", question);
		
		Page<Answer> paging = this.answerService.getList(question, page); ///*댓글페이징*/
		model.addAttribute("paging", paging);
		
		
		
		/*이전글다음글번호와 제목을 html에서 불러올수있게 model.addAttribute() 작성*/
	    QuestionPage questionPage = questionPageRepository.findByPages(id);
	    model.addAttribute("prevID", questionPage.getPREVID());
	    model.addAttribute("prevSub", questionPage.getPREV_SUB());
	    model.addAttribute("nextID", questionPage.getNEXTID());
	    model.addAttribute("nextSub", questionPage.getNEXT_SUB());
		return "question_detail";
	}

	@PreAuthorize("isAuthenticated()") // @PreAuthorize("isAuthenticated()") 애너테이션이 붙은 메서드는 로그인이 필요한 메서드를 의미
	@GetMapping("/create")
	public String questionCreate(QuestionForm questionForm) { // QuestionForm과 같이 매개변수로 바인딩한 객체는 Model 객체로 전달하지 않아도 템플릿에서 사용이 가능
// "질문 등록하기" 버튼을 통해 GET 방식으로 요청되더라도 th:object에 의해 QuestionForm 객체가 필요하기 때문
		// GET 방식에서도 question_form 템플릿에 QuestionForm 객체가 전달
		return "question_form";
	}
	
	@PreAuthorize("isAuthenticated()") // @PreAuthorize("isAuthenticated()") 애너테이션이 붙은 메서드는 로그인이 필요한 메서드를 의미
	@PostMapping("/create")
	// POST 방식으로 요청한 /question/create URL을 처리하기 위해 @PostMapping 애너테이션을 지정한
	// questionCreate 메서드를 추가
	public String questionCreate(
			@Valid QuestionForm questionForm, 
			BindingResult bindingResult,
			Principal principal,
			MultipartFile file) throws Exception{ // 스프링 프레임워크의 바인딩 기능
		// 메서드명은 @GetMapping시 사용했던 questionCreate 메서드명과 동일하게 사용할 수 있다.
		// (단, 매개변수의 형태가 다른 경우에 가능하다. - 메서드 오버로딩)
		if (bindingResult.hasErrors()) {
			return "question_form";
		}
		SiteUser siteUser = this.userService.getUser(principal.getName()); 
		this.questionService.create( // QuestionService로 질문 데이터를 저장
				questionForm.getSubject(),  
				questionForm.getContent(), 
				siteUser,
				questionForm.getCategory(), //카테고리추가
				file);  
																							
		return "redirect:/question/list"; // 질문 저장 후 질문목록으로 이동
	}
	
	
	/*질문 수정하기 GET*/
	@PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String questionModify(QuestionForm questionForm, @PathVariable("id") Integer id, Principal principal) {
        Question question = this.questionService.getQuestion(id);
        if(!question.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        questionForm.setSubject(question.getSubject());
        questionForm.setContent(question.getContent());
        questionForm.setCategory(question.getCategory());
        return "question_form";
    }
	
	/*질문 수정하기 POST*/
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String questionModify(@Valid QuestionForm questionForm, BindingResult bindingResult,
                                 Principal principal, @PathVariable("id") Integer id,
                                 MultipartFile file)throws Exception{
        if (bindingResult.hasErrors()) {
            return "question_form";
        }
        Question question = this.questionService.getQuestion(id);
        if (!question.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        this.questionService.modify(question, questionForm.getSubject(), questionForm.getContent(), questionForm.getCategory(),file);
        return String.format("redirect:/question/detail/%s", id);
    }
	
	
	@PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{id}")
    public String questionDelete(Principal principal, @PathVariable("id") Integer id) {
        Question question = this.questionService.getQuestion(id);
        if (!question.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제권한이 없습니다.");
        }
        this.questionService.delete(question);
        return "redirect:/";
    }
	
	
	@PreAuthorize("isAuthenticated()")
    @GetMapping("/vote/{id}")
    public String questionVote(Principal principal, @PathVariable("id") Integer id) {
        Question question = this.questionService.getQuestion(id);
        SiteUser siteUser = this.userService.getUser(principal.getName());
        this.questionService.vote(question, siteUser);
        return String.format("redirect:/question/detail/%s", id);
    }
	


}