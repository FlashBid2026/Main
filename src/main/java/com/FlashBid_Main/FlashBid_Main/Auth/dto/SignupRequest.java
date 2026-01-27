package com.FlashBid_Main.FlashBid_Main.Auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {

  @NotBlank(message = "닉네임을 입력해주세요.")
  @Size(max = 30, message = "닉네임은 30자 이하로 입력해주세요.")
  private String nickname;

  @NotBlank(message = "아이디를 입력해주세요.")
  @Size(max = 50, message = "아이디는 50자 이하로 입력해주세요.")
  private String userId;

  @NotBlank(message = "비밀번호를 입력해주세요.")
  @Size(min = 8, message = "비밀번호는 8자 이상 입력해주세요.")
  private String password;

  @NotBlank(message = "비밀번호 확인을 입력해주세요.")
  private String confirmPassword;
}
