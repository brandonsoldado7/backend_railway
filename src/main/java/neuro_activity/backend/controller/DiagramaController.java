package neuro_activity.backend.controller;

import neuro_activity.backend.model.Diagrama;
import neuro_activity.backend.service.DiagramaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

class DiagramaIdRequest {
    public String diagramaId;
}

class DiagramaUpdateRequest {
    public String diagramaId;
    public String json;
}

@RestController
@RequestMapping("/diagramas")
@CrossOrigin
public class DiagramaController {

    @Autowired
    private DiagramaService diagramaService;

    @PostMapping
    public Diagrama crear(@RequestBody Diagrama diagrama) {
        return diagramaService.guardar(diagrama);
    }

    @PostMapping("/buscar")
    public Diagrama obtenerPorId(@RequestBody DiagramaIdRequest request) {
        return diagramaService.obtenerPorId(request.diagramaId);
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<Diagrama> porUsuario(@PathVariable String usuarioId) {
        return diagramaService.porUsuario(usuarioId);
    }

    @PutMapping
    public Diagrama actualizar(@RequestBody DiagramaUpdateRequest request) {
        return diagramaService.actualizarJson(request.diagramaId, request.json);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable String id) {
        diagramaService.eliminar(id);
    }
}