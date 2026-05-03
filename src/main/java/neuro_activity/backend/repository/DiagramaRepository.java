package neuro_activity.backend.repository;

import neuro_activity.backend.model.Diagrama;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DiagramaRepository extends MongoRepository<Diagrama, String> {

    List<Diagrama> findByUsuarioId(String usuarioId);
}