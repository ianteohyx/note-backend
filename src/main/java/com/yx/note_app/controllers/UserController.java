package com.yx.note_app.controllers;

import com.yx.note_app.services.service.LogInService;
import com.yx.note_app.services.service.SignUpService;
import com.yx.note_app.services.reponse.ApiResponse;
import com.yx.note_app.services.request.LoginRequest;
import com.yx.note_app.services.request.SignUpRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private SignUpService signUpService;

    @Autowired
    private LogInService logInService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> createUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        ApiResponse response = signUpService.execute(signUpRequest);
        HttpStatus status = response.getResponseOutcome().getSuccess()
            ? HttpStatus.CREATED
            : response.getResponseOutcome().getHttpStatus();
        return ResponseEntity.status(status).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> verifyUser(@Valid @RequestBody LoginRequest loginRequest) {
        ApiResponse response = logInService.execute(loginRequest);
        return ResponseEntity.status(response.getResponseOutcome().getHttpStatus()).body(response);
    }
}
