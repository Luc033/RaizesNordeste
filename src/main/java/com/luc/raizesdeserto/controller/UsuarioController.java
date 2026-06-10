package com.luc.raizesdeserto.controller;

import com.luc.raizesdeserto.dto.usuario.RegisterResponse;
import com.luc.raizesdeserto.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController()
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("{id}")
    public ResponseEntity<RegisterResponse> usuarioRegistrado(@RequestBody UUID id) {
        var usuario = usuarioService.buscarPorId(id);
        if(usuario.isPresent()){
            return ResponseEntity.ok(new RegisterResponse(usuario.get().getNome(), usuario.get().getEmail()));
        }else{
            return ResponseEntity.notFound().build();
        }
    }

}
