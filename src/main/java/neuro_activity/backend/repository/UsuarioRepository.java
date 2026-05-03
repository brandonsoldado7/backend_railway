package neuro_activity.backend.repository;

import neuro_activity.backend.model.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UsuarioRepository extends MongoRepository<Usuario, String> {

    Usuario findByEmail(String email);
}