package neuro_activity.backend.service;

import neuro_activity.backend.model.Usuario;
import neuro_activity.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Random;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 🎨 30 colores oscuros
    private final String[] colors = {
        "#1b2a41", "#1e3a5f", "#243b55", "#2c3e50", "#22313f",
        "#0f4c75", "#1b4965", "#145374", "#1f6f8b", "#2a4d69",
        "#2d1b3d", "#3d2c8d", "#4b2c5e", "#5f27cd", "#341f97",
        "#5a1a1a", "#7b2d26", "#922b21", "#c0392b", "#641e16",
        "#7d3c1a", "#a04000", "#b3541e", "#d35400", "#6e2c00",
        "#145a32", "#1e8449", "#196f3d", "#0b5345", "#1b4f3c"
    };

    public Usuario crearUsuario(Usuario usuario) {

        // validar email único
        Usuario existente = usuarioRepository.findByEmail(usuario.getEmail());
        if (existente != null) {
            throw new RuntimeException("El email ya está registrado");
        }

        // 🔐 encriptar contraseña
        usuario.setPassword_hash(
            passwordEncoder.encode(usuario.getPassword_hash())
        );

        // 📅 fecha creación
        usuario.setCreated_at(new Date());

        // 🔥 inicial del apellido paterno
        if (usuario.getApellido_p() != null && !usuario.getApellido_p().isEmpty()) {
            usuario.setAvatar_initial(
                usuario.getApellido_p().substring(0, 1).toUpperCase()
            );
        } else {
            usuario.setAvatar_initial("U");
        }

        // 🎨 color aleatorio
        Random random = new Random();
        String color = colors[random.nextInt(colors.length)];
        usuario.setAvatar_color(color);

        return usuarioRepository.save(usuario);
    }

    public Usuario login(String email, String password) {

        Usuario usuario = usuarioRepository.findByEmail(email);

        if (usuario == null ||
            !passwordEncoder.matches(password, usuario.getPassword_hash())) {
            return null;
        }

        return usuario;
    }
}