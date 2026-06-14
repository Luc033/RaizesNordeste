package com.luc.raizesdeserto.controller;

import com.luc.raizesdeserto.dto.usuario.RegisterResponse;
import com.luc.raizesdeserto.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
            return ResponseEntity.ok(new RegisterResponse(usuario.get()));
        }else{
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("HasAnyRole('ADMIN')")
    @DeleteMapping("{id}")
    public ResponseEntity<Void> usuarioExcluir(@PathVariable UUID id) {
        usuarioService.excluirUsuario(id);
        return  ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }

}
