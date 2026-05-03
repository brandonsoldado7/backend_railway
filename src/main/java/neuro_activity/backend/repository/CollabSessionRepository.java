package neuro_activity.backend.repository;

import neuro_activity.backend.model.CollabSession;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface CollabSessionRepository extends MongoRepository<CollabSession, String> {

    /** Todos los viewers activos en un diagrama */
    List<CollabSession> findByDiagramaId(String diagramaId);

    /** Sesión de un usuario específico en un diagrama */
    Optional<CollabSession> findByDiagramaIdAndUsuarioId(String diagramaId, String usuarioId);

    /** Para limpiar al hacer leave explícito */
    void deleteByDiagramaIdAndUsuarioId(String diagramaId, String usuarioId);

    /** Sesiones sin heartbeat reciente (para el cleaner) */
    List<CollabSession> findByLastSeenBefore(Date cutoff);

    /** Eliminar sesiones muertas en bloque */
    void deleteByLastSeenBefore(Date cutoff);
}