package com.bradf.springreact.controller;

import com.bradf.springreact.payload.ApiResponse;
import com.bradf.springreact.payload.ProtectedRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/protected")
public class ProtectedController {
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> protectMe(@Valid @RequestBody ProtectedRequest protectedRequest) {

        return ResponseEntity.ok()
                .body(new ApiResponse(true, "Protected Message Called Successfully"));
    }
}
