package com.luc.raizesdeserto.controller;

import com.luc.raizesdeserto.dto.auth.LoginRequest;
import com.luc.raizesdeserto.dto.auth.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest){

        //// DESENVOLVER SISTEMA DE LOGIN DE USUARIO


        return null;
    }


}
