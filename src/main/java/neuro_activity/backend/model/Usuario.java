package neuro_activity.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "usuarios")
public class Usuario {

    @Id
    private String id;

    private String nombre;
    private String apellido_p;
    private String apellido_m;
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password_hash;

    private String rol;
    private Date created_at;

    // 🔥 CAMPOS AVATAR
    private String avatar_color;
    private String avatar_initial;
}