package com.yx.note_app.services.service;

import com.yx.note_app.exception.DuplicateResourceException;
import com.yx.note_app.models.User;
import com.yx.note_app.repositories.UserRepository;
import com.yx.note_app.services.reponse.ApiResponse;
import com.yx.note_app.services.reponse.ResponseDirectory;
import com.yx.note_app.services.request.SignUpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

@org.springframework.stereotype.Service
public class SignUpService extends Service<SignUpRequest, ApiResponse>{

    private static final Logger logger = LoggerFactory.getLogger(SignUpService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public ApiResponse doService(SignUpRequest request) {
        if (userRepository.existsByUsername(request.getUsername())){
            throw DuplicateResourceException.usernameExists(request.getUsername());
        }

        // Password validation is now handled by @Valid annotation in controller
        userRepository.save(buildUser(request));
        logger.info("User signed up successfully: {}", request.getUsername());
        return ResponseDirectory.buildSuccessResponse();
    }

    private User buildUser(SignUpRequest request){
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        return user;
    }
}
