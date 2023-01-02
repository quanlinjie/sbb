package com.study.sbb.form;

import javax.validation.constraints.NotEmpty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateForm {

    @NotEmpty(message = "닉네임은 필수항목입니다.")
    private String nickname;

    private String profileImage;

    private String ImageName;

}