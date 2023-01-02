package com.study.sbb.controller;

import java.security.Principal;

import javax.validation.Valid;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.study.sbb.entity.SiteUser;
import com.study.sbb.form.UserCreateForm;
import com.study.sbb.form.UserModifyForm;
import com.study.sbb.service.UserService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/user")
public class UserController {

	private final UserService userService;

	@GetMapping("/signup")
	public String signup(UserCreateForm userCreateForm) {
		return "signup_form";
	}

	@PostMapping("/signup")
	public String signup(@Valid UserCreateForm userCreateForm, BindingResult bindingResult, MultipartFile file)
			throws Exception {
		if (bindingResult.hasErrors()) {
			return "signup_form";
		}

		if (!userCreateForm.getPassword1().equals(userCreateForm.getPassword2())) {
			bindingResult.rejectValue("password2", "passwordInCorrect", "2개의 패스워드가 일치하지 않습니다.");
			return "signup_form";
		}

		/* 중복회원가입막기 */
		try {
			userService.create(userCreateForm.getUsername(), userCreateForm.getEmail(), userCreateForm.getPassword1(),
					userCreateForm.getNickname(), file);
		} catch (DataIntegrityViolationException e) {
			e.printStackTrace();
			bindingResult.reject("signupFailed", "이미 등록된 사용자입니다.");
			return "signup_form";
		} catch (Exception e) {
			e.printStackTrace();
			bindingResult.reject("signupFailed", e.getMessage());
			return "signup_form";
		}
		return "redirect:/";
	}

	@GetMapping("/login")
	public String login() {
		return "login_form";
	}

	 //로그인되어있는회원정보 뷰로던짐
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/info")
    public String userinfo(UserModifyForm userModifyForm, Model model, Principal principal) {
        SiteUser siteUser = this.userService.getUser(principal.getName());
        model.addAttribute("siteUser", siteUser);

        return "profile";
    }

  //로그인 되어 있는 회원정보 수정
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/info")
    public String usermodify(UserModifyForm userModifyForm, Principal principal, @RequestParam(value = "file", required = false) MultipartFile file) throws Exception {
        SiteUser siteUser = this.userService.getUser(principal.getName());

        this.userService.modify(siteUser, userModifyForm.getNickname(), userModifyForm.getEmail(), file);
        return "redirect:/user/info";
    }
}