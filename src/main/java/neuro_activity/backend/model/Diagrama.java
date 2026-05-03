package neuro_activity.backend.model;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Document(collection = "diagramas")
public class Diagrama {

    @Id
    private String id;

    private String usuarioId;
    private String nombre;
    private String json;

    private Date created_at;
    private Date updated_at;

    // ── Colaboración ────────────────────────
    private String  linkToken;
    private boolean linkActive = false;

    // ── getters / setters ────────────────────

    public String  getId()                      { return id; }
    public void    setId(String id)             { this.id = id; }

    public String  getUsuarioId()               { return usuarioId; }
    public void    setUsuarioId(String u)       { this.usuarioId = u; }

    public String  getNombre()                  { return nombre; }
    public void    setNombre(String n)          { this.nombre = n; }

    public String  getJson()                    { return json; }
    public void    setJson(String j)            { this.json = j; }

    public Date    getCreated_at()              { return created_at; }
    public void    setCreated_at(Date d)        { this.created_at = d; }

    public Date    getUpdated_at()              { return updated_at; }
    public void    setUpdated_at(Date d)        { this.updated_at = d; }

    public String  getLinkToken()               { return linkToken; }
    public void    setLinkToken(String t)       { this.linkToken = t; }

    public boolean isLinkActive()               { return linkActive; }
    public void    setLinkActive(boolean a)     { this.linkActive = a; }
}