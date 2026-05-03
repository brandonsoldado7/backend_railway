package neuro_activity.backend.controller;

import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

@RestController
@RequestMapping("/diagramas")
@CrossOrigin(origins = {"http://localhost:4200", "https://staging.d1m6nxfkkxva9m.amplifyapp.com"})
public class linkController {

    private static class LinkData {
        String token;
        boolean active;
        boolean readOnly = true;
        String link;
    }

    private final Map<String, LinkData> storage = new ConcurrentHashMap<>();

    @GetMapping("/{id}/collab-link")
    public Map<String, Object> getOrCreate(@PathVariable String id) {
        LinkData data = storage.computeIfAbsent(id, k -> {
            LinkData d  = new LinkData();
            d.token     = UUID.randomUUID().toString();
            d.active    = false;
            d.readOnly  = true;
            d.link      = buildLink(id, d.token, d.readOnly);
            return d;
        });
        return toMap(id, data);
    }

    @PatchMapping("/{id}/collab-link")
    public Map<String, Object> toggle(
            @PathVariable String id,
            @RequestBody Map<String, Object> body) {

        LinkData data = storage.computeIfAbsent(id, k -> {
            LinkData d  = new LinkData();
            d.token     = UUID.randomUUID().toString();
            d.active    = false;
            d.readOnly  = true;
            d.link      = buildLink(id, d.token, d.readOnly);
            return d;
        });

        if (body.containsKey("active")) {
            data.active = Boolean.TRUE.equals(body.get("active"));
        }
        if (body.containsKey("readOnly")) {
            data.readOnly = Boolean.TRUE.equals(body.get("readOnly"));
            data.link = buildLink(id, data.token, data.readOnly);
        }

        return toMap(id, data);
    }

    private String buildLink(String id, String token, boolean readOnly) {
        String base = "https://staging.d1m6nxfkkxva9m.amplifyapp.com";
        String link = base + "/project/" + id + "?token=" + token;
        return readOnly ? link : link + "&mode=write";
    }

    private Map<String, Object> toMap(String id, LinkData d) {
        return Map.of(
            "link",     d.link,
            "token",    d.token,
            "active",   d.active,
            "readOnly", d.readOnly
        );
    }
}