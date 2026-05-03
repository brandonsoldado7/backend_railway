package neuro_activity.backend.controller;

import neuro_activity.backend.model.Usuario;
import neuro_activity.backend.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
@CrossOrigin
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    // ✅ REGISTRO
    @PostMapping("/register")
    public Usuario register(@RequestBody Usuario usuario) {
        try {
            return usuarioService.crearUsuario(usuario);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // ✅ LOGIN
    @PostMapping("/login")
    public Usuario login(@RequestBody Usuario request) {

        Usuario usuario = usuarioService.login(
                request.getEmail(),
                request.getPassword_hash()
        );

        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas");
        }

        return usuario;
    }
}