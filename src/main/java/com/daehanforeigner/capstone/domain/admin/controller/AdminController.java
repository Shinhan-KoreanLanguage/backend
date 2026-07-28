package com.daehanforeigner.capstone.domain.admin.controller;

import com.daehanforeigner.capstone.domain.admin.dto.AdminProfileResponseDTO;
import com.daehanforeigner.capstone.domain.admin.service.AdminService;
import com.daehanforeigner.capstone.global.rsdata.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;

    @GetMapping
    public ResponseEntity<RsData<AdminProfileResponseDTO>> getAdminProfile(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(RsData.success(adminService.getAdminProfile(userId)));
    }
}
