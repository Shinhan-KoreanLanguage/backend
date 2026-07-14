package com.daehanforeigner.capstone.domain.user.controller;

import com.daehanforeigner.capstone.domain.user.dto.login.LoginRequestDTO;
import com.daehanforeigner.capstone.domain.user.dto.login.LoginResponseDTO;
import com.daehanforeigner.capstone.domain.user.dto.register.SignupRequestDTO;
import com.daehanforeigner.capstone.domain.user.service.UserService;
import com.daehanforeigner.capstone.global.rsdata.RsData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<RsData<String>> signup(@Valid @RequestBody SignupRequestDTO signupRequestDTO) {
        userService.signup(signupRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(RsData.success("회원가입이 완료되었습니다."));
    }

    @PostMapping("/login")
    public ResponseEntity<RsData<LoginResponseDTO>> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {
        LoginResponseDTO loginResponseDTO = userService.login(loginRequestDTO);
        return ResponseEntity.status(HttpStatus.OK).body(RsData.success(loginResponseDTO));
    }
}