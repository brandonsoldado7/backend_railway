package neuro_activity.backend.service;
 
import neuro_activity.backend.model.Diagrama;
import neuro_activity.backend.repository.DiagramaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
 
import java.util.UUID;
 
@Service
public class Collablinkservice {
    
    @Autowired
    private DiagramaRepository diagramaRepository;
 
    /** URL base del frontend, ej: http://localhost:4200 */
    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;
 
    // ── DTO de respuesta ─────────────────────────────────────
 
    public static class CollabLinkResponse {
        public String  link;
        public String  token;
        public boolean active;
 
        public CollabLinkResponse(String link, String token, boolean active) {
            this.link   = link;
            this.token  = token;
            this.active = active;
        }
    }
 
    // ── Obtener o crear el link ───────────────────────────────
 
    public CollabLinkResponse getOrCreate(String diagramaId) {
        Diagrama d = diagramaRepository.findById(diagramaId)
                .orElseThrow(() -> new RuntimeException("Diagrama no encontrado: " + diagramaId));
 
        // Generar token si no existe
        if (d.getLinkToken() == null || d.getLinkToken().isBlank()) {
            d.setLinkToken(UUID.randomUUID().toString().replace("-", ""));
            // Por defecto el link empieza INACTIVO
            d.setLinkActive(false);
            diagramaRepository.save(d);
        }
 
        return buildResponse(d);
    }
 
    // ── Activar / desactivar ──────────────────────────────────
 
    public CollabLinkResponse toggle(String diagramaId, boolean active) {
        Diagrama d = diagramaRepository.findById(diagramaId)
                .orElseThrow(() -> new RuntimeException("Diagrama no encontrado: " + diagramaId));
 
        // Asegurar que haya token
        if (d.getLinkToken() == null || d.getLinkToken().isBlank()) {
            d.setLinkToken(UUID.randomUUID().toString().replace("-", ""));
        }
 
        d.setLinkActive(active);
        diagramaRepository.save(d);
 
        return buildResponse(d);
    }
 
    // ── Helper ────────────────────────────────────────────────
 
    private CollabLinkResponse buildResponse(Diagrama d) {
        // El link apunta al mismo editor con ?token=... y ?diagramaId=...
        String link = String.format(
            "%s/editor?diagramaId=%s&token=%s",
            frontendUrl, d.getId(), d.getLinkToken()
        );
        return new CollabLinkResponse(link, d.getLinkToken(), d.isLinkActive());
    }
}
