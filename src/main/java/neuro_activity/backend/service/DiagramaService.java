package neuro_activity.backend.service;

import neuro_activity.backend.model.Diagrama;
import neuro_activity.backend.repository.DiagramaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Versión actualizada de DiagramaService.
 * ÚNICO cambio respecto al original: inyecta CollabService y,
 * tras guardar, hace broadcast del nuevo JSON a los viewers activos.
 *
 * El resto del comportamiento es idéntico al original.
 */
@Service
public class DiagramaService {

    @Autowired private DiagramaRepository diagramaRepository;
    @Autowired private CollabService      collabService;   // ← nuevo

    public Diagrama guardar(Diagrama diagrama) {
        diagrama.setCreated_at(new Date());
        diagrama.setUpdated_at(new Date());
        return diagramaRepository.save(diagrama);
    }

    public List<Diagrama> porUsuario(String usuarioId) {
        return diagramaRepository.findByUsuarioId(usuarioId);
    }

    public Diagrama obtenerPorId(String id) {
        return diagramaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Diagrama no encontrado: " + id));
    }

    /**
     * Actualiza solo el JSON y notifica a todos los viewers por WS.
     */
    public Diagrama actualizarJson(String id, String json) {
        Diagrama existente = obtenerPorId(id);
        existente.setJson(json);
        existente.setUpdated_at(new Date());
        Diagrama saved = diagramaRepository.save(existente);

        // ── NUEVO: push colaborativo ──────────────────────────────
        collabService.broadcastDiagram(id, json, saved.getUpdated_at().getTime());
        // ─────────────────────────────────────────────────────────

        return saved;
    }

    public void eliminar(String id) {
        diagramaRepository.deleteById(id);
    }
}