package neuro_activity.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class IAService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate;

    @Value("${openai.api.key:}")
    private String openaiApiKey;

    // ✅ ENDPOINT NUEVO
    private static final String OPENAI_URL = "https://api.openai.com/v1/responses";

    // ✅ MODELO ACTUAL
    private static final String OPENAI_MODEL = "gpt-4.1-mini";

    public IAService() {
        org.springframework.http.client.SimpleClientHttpRequestFactory factory =
                new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30000);
        factory.setReadTimeout(120000);
        this.restTemplate = new RestTemplate(factory);
    }

    // 🔍 DEBUG + VALIDACIÓN
    @PostConstruct
    public void init() {
        if (openaiApiKey == null || openaiApiKey.isEmpty()) {
            throw new RuntimeException("❌ OPENAI_API_KEY no está configurada");
        }

        // eliminar comillas si Railway las mete
        openaiApiKey = openaiApiKey.replace("\"", "");

        log.info("✅ OpenAI API Key cargada correctamente");
    }

    // ─────────────────────────────────────────────
    // LLAMADA A OPENAI (NUEVA API)
    // ─────────────────────────────────────────────
    private String llamarOpenAI(String prompt) {
        try {
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", "Bearer " + openaiApiKey);
            headers.set("Content-Type", "application/json");

            Map<String, Object> body = new HashMap<>();
            body.put("model", OPENAI_MODEL);
            body.put("input", prompt);

            org.springframework.http.HttpEntity<Map<String, Object>> request =
                    new org.springframework.http.HttpEntity<>(body, headers);

            JsonNode response = restTemplate.postForObject(OPENAI_URL, request, JsonNode.class);

            // ✅ NUEVA FORMA DE LEER RESPUESTA
            return response
                    .path("output")
                    .get(0)
                    .path("content")
                    .get(0)
                    .path("text")
                    .asText()
                    .trim();

        } catch (HttpClientErrorException e) {
            log.error("❌ Error HTTP OpenAI: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Error OpenAI: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("❌ Error interno OpenAI: {}", e.getMessage());
            throw new RuntimeException("Error interno OpenAI: " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────
    // MÉTODO PÚBLICO DE EJEMPLO
    // ─────────────────────────────────────────────
    public JsonNode generarJsonDesdePrompt(String prompt) {
        try {
            String respuestaTexto = llamarOpenAI(prompt);

            // limpiar markdown si viene con ```json
            String cleaned = respuestaTexto
                    .replaceAll("(?s)^```json\\s*", "")
                    .replaceAll("(?s)```\\s*$", "")
                    .trim();

            return objectMapper.readTree(cleaned);

        } catch (Exception e) {
            throw new RuntimeException("Error procesando respuesta IA: " + e.getMessage(), e);
        }
    }
}

    // ─────────────────────────────────────────────────────────────
    // Prompts
    // ─────────────────────────────────────────────────────────────

        private static final String GOJS_PROMPT_INSTRUCTION_GENERAR = """
        Eres un experto en diagramas de actividades UML con GoJS.
        El usuario enviará un prompt en texto libre, y tu tarea es devolver
        ÚNICAMENTE un JSON válido que represente un diagrama de actividades en formato GoJS.
        REGLAS ESTRICTAS DEL MODELO GoJS:
        1. El JSON debe tener exactamente esta estructura raíz:
           { "class": "GraphLinksModel", "nodeDataArray": [...], "linkDataArray": [...] }
        2. CATEGORÍAS DE NODOS PERMITIDAS y sus reglas:
           - "initial": Nodo de inicio del diagrama (círculo negro). Solo debe haber uno.
             SIEMPRE debe tener un nodo "floatingLabel" asociado con texto "Inicio".
             { "category": "initial", "text": "", "loc": "x y", "key": -N }
           - "final": Nodo de fin del diagrama (círculo con borde). Solo debe haber uno.
             SIEMPRE debe tener un nodo "floatingLabel" asociado con texto "Final".
             { "category": "final", "text": "", "loc": "x y", "key": -N }
           - "flow-final": Nodo de fin de flujo alternativo (círculo con X).
             SIEMPRE debe tener un nodo "floatingLabel" asociado con texto "Flow Final".
             { "category": "flow-final", "text": "", "loc": "x y", "key": -N }
           - "floatingLabel": Etiqueta flotante asociada a otro nodo (initial, final, flow-final, decision, merge, synch).
             Su "loc" debe estar desplazada ~36 unidades en Y por debajo del nodo al que etiqueta.
             Se conecta a su nodo mediante un link de categoría "label-link".
             { "category": "floatingLabel", "text": "nombre", "loc": "x y", "key": -N }
           - "activity": Nodo de actividad general del proceso. Puede tener texto descriptivo.
             { "category": "activity", "text": "Nombre actividad", "loc": "x y", "key": -N }
           - "action": Representa una acción o paso del proceso.
             El texto debe ser claro y en infinitivo (ej: "Validar datos", "Enviar formulario").
             { "category": "action", "text": "nombre acción", "loc": "x y", "key": -N }
           - "partition": Representa un carril (swimlane) que agrupa un actor o sistema.
             Su "loc" debe estar desplazada a la izquierda/arriba del grupo de nodos que contiene.
             Cada actor del proceso debe tener su propia partition.
             { "category": "partition", "text": "Cliente", "loc": "x y", "key": -N }
           - "decision": Representa una decisión (rombo). El texto debe ser una pregunta con "¿?" o "[guard]".
             Sus links de salida DEBEN usar nodos "text" para etiquetar las ramas "Sí" y "No".
             SIEMPRE debe tener un nodo "floatingLabel" asociado.
             { "category": "decision", "text": "¿Datos válidos?", "loc": "x y", "key": -N }
           - "merge": Representa unión de flujos alternativos. Sin texto visible.
             SIEMPRE debe tener un nodo "floatingLabel" asociado con texto "Merge".
             { "category": "merge", "text": "", "loc": "x y", "key": -N }
           - "synch": Representa sincronización de flujos paralelos (barra horizontal gruesa).
             SIEMPRE debe tener un nodo "floatingLabel" asociado con texto "Synch".
             { "category": "synch", "text": "", "loc": "x y", "key": -N }
           - "fork-h": Bifurcación paralela horizontal (barra horizontal).
             No requiere floatingLabel.
             { "category": "fork-h", "text": "", "loc": "x y", "key": -N }
           - "fork-v": Bifurcación paralela vertical (barra vertical).
             No requiere floatingLabel.
             { "category": "fork-v", "text": "", "loc": "x y", "key": -N }
           - "send": Representa el envío de un mensaje o señal.
             { "category": "send", "text": "send", "loc": "x y", "key": -N }
           - "receive": Representa la recepción de un mensaje o señal.
             { "category": "receive", "text": "receive", "loc": "x y", "key": -N }
           - "object-node": Nodo objeto que representa un flujo de objeto entre dos acciones.
             SIEMPRE está anclado a un nodo "action" padre mediante "parentKey".
             Tiene un offset relativo al padre definido por "offsetX" y "offsetY".
             Se usa en PARES: uno en la acción origen y otro en la acción destino.
             Se conectan entre sí con un link de categoría "obj-flow".
             { "category": "object-node", "text": "", "loc": "x y", "parentKey": -N, "offsetX": X, "offsetY": Y, "key": -M }
           - "int-node": Nodo de interrupción que representa un flujo de excepción o interrupción.
             SIEMPRE está anclado a un nodo "action" padre mediante "parentKey".
             Se usa en grupos de 3 links: "int-flow" → "int-flow-mid" → "int-flow-end".
             Requiere mínimo 2 int-nodes: uno de inicio (anclado a acción origen) y uno de fin.
             { "category": "int-node", "text": "", "loc": "x y", "parentKey": -N, "offsetX": X, "offsetY": Y, "key": -M }
           - "note": Nodo de anotación libre. Sirve para agregar comentarios o aclaraciones
             sobre cualquier parte del diagrama. Su texto es libre.
             No requiere floatingLabel ni links obligatorios.
             Puede conectarse opcionalmente a otros nodos con un link normal punteado.
             { "category": "note", "text": "Texto de la nota...", "loc": "x y", "key": -N }
           - "constraint": Nodo de restricción o invariante UML.
             Tiene un "stereotype" visible (ej: «Invariant», «Precondition», «Postcondition»)
             y un "bodyText" con la expresión de la restricción (ej: "{cantidad > 0}").
             El campo "stereotypeIndex" indica el índice del estereotipo (normalmente 0).
             No requiere floatingLabel.
             { "category": "constraint", "text": "", "loc": "x y", "stereotype": "«Invariant»", "stereotypeIndex": 0, "bodyText": "{expresión}", "key": -N }
           - "text": Nodo de texto libre flotante. Sirve para agregar etiquetas, títulos
             o anotaciones simples en cualquier parte del diagrama sin recuadro.
             También se usa como etiqueta de links de decisión en lugar de poner
             "text" directamente en el link.
             { "category": "text", "text": "Texto libre", "loc": "x y", "key": -N }
        3. REGLAS DE LINKS (linkDataArray):
           TIPOS DE LINKS:
           a) PROHIBIDO usar links sin "category". Todo flujo de control DEBE usar "ctrl-flow":
              { "category": "ctrl-flow", "from": -N, "to": -M }
              NUNCA omitas el campo "category" en ningún link de flujo de control.
              NUNCA uses el campo "text" en los links. Si necesitas etiquetar un link,
              coloca un nodo "text" cerca del link con la etiqueta deseada.
           b) "label-link" (OBLIGATORIO para floatingLabel):
              Conecta un nodo principal con su floatingLabel.
              { "category": "label-link", "from": -N, "to": -M }
           c) "ctrl-flow" (flujo de control explícito entre acciones):
              Alternativa al link normal para conectar acciones directamente.
              { "category": "ctrl-flow", "from": -N, "to": -M }
           d) "obj-flow" (flujo de objeto entre object-nodes):
              Conecta dos "object-node" que pertenecen a acciones distintas.
              { "category": "obj-flow", "from": -N, "to": -M }
              REGLA: El "from" y "to" deben ser keys de nodos "object-node", NO de "action".
           e) "int-flow" (inicio del flujo de interrupción):
              { "category": "int-flow", "from": -accion, "to": -intNode1 }
           f) "int-flow-mid" (segmento intermedio del flujo de interrupción):
              { "category": "int-flow-mid", "from": -intNode1, "to": -intNode2 }
           g) "int-flow-end" (fin del flujo de interrupción):
              { "category": "int-flow-end", "from": -intNodeFinal, "to": -accionDestino }
           REGLA GENERAL DE FLUJOS:
           - NUNCA agregues "text" a ningún link
           - Para etiquetar ramas de decision (Sí/No), usa nodos "text" posicionados
             cerca del link correspondiente en el diagrama
           - Los flujos de error (rama "No") deben retornar a un nodo anterior lógico
           - El flujo principal siempre termina en el nodo "final"
        4. REGLAS DE POSICIÓN (loc):
           - Formato: "x y" como string (ej: "450 500")
           - El diagrama fluye de arriba hacia abajo (Y aumenta hacia abajo)
           - Separación vertical recomendada entre nodos: 80-120 unidades
           - Los floatingLabel deben estar ~36 unidades en Y por debajo de su nodo padre
           - Los object-node deben posicionarse en el borde de su acción padre (offsetX ≈ ±40)
           - Los int-node deben posicionarse con offsets que los ubiquen fuera del borde de la acción
           - Los nodos "note" y "constraint" deben colocarse cerca del nodo al que hacen referencia
           - Los nodos "text" usados como etiquetas de links deben colocarse junto al link que etiquetan
           - Las partitions del mismo actor deben agrupar sus acciones en la misma columna X
           - Actores diferentes deben estar en columnas X distintas (separación 150-200 unidades)
           - Ejemplo de distribución:
               * Partition "Cliente" → loc: "150 50", sus acciones en X≈150
               * Partition "Sistema" → loc: "450 50", sus acciones en X≈450
        5. REGLAS GENERALES:
           - Todas las keys deben ser números negativos únicos
           - NUNCA generes un link sin el campo "category". Todo link debe tener categoría explícita.
              Los únicos valores válidos son: "ctrl-flow", "label-link", "obj-flow",
              "int-flow", "int-flow-mid", "int-flow-end".
           - Los caracteres especiales como ¿ á é í ó ú ñ deben codificarse en unicode:
             ¿ → \\u00bf, á → \\u00e1, é → \\u00e9, í → \\u00ed, ó → \\u00f3, ú → \\u00fa, ñ → \\u00f1
           - NUNCA uses el campo "text" dentro de los links
           - No incluyas comentarios ni texto fuera del JSON
           - No uses comillas simples, solo dobles
           - El JSON debe ser parseable directamente
        6. FLUJO LÓGICO RECOMENDADO:
           initial (+floatingLabel) → actions/partitions →
           decision (+floatingLabel) → rama "Sí" continúa → ... → final (+floatingLabel)
                                     → rama "No" retorna o va a flow-final (+floatingLabel)
           Para flujos de objeto: action → [obj-flow via object-nodes] → action
           Para interrupciones:   action → int-flow → int-node → int-flow-mid → int-node → int-flow-end → action
           Para etiquetas de ramas: usar nodo "text" posicionado cerca del link, NO campo text en el link
        EJEMPLO DE SALIDA COMPLETO:
        {
          "class": "GraphLinksModel",
          "nodeDataArray": [
            {"category":"initial","text":"","loc":"180 100","key":-1},
            {"category":"floatingLabel","text":"Inicio","loc":"180 136","key":-2},
            {"category":"partition","text":"Cliente","loc":"150 50","key":-15},
            {"category":"action","text":"Rellenar formulario","loc":"150 250","key":-3},
            {"category":"note","text":"El usuario debe completar todos los campos","loc":"50 250","key":-21},
            {"category":"action","text":"Enviar formulario","loc":"150 370","key":-4},
            {"category":"partition","text":"Sistema","loc":"450 50","key":-16},
            {"category":"action","text":"Validar datos","loc":"450 490","key":-5},
            {"category":"object-node","text":"","loc":"409 490","parentKey":-5,"offsetX":-41,"offsetY":0,"key":-17},
            {"category":"action","text":"Registrar en BD","loc":"450 610","key":-6},
            {"category":"object-node","text":"","loc":"491 610","parentKey":-6,"offsetX":41,"offsetY":0,"key":-18},
            {"category":"constraint","text":"","loc":"600 610","stereotype":"\\u00abInvariant\\u00bb","stereotypeIndex":0,"bodyText":"{datos no nulos}","key":-22},
            {"category":"decision","text":"\\u00bfDatos v\\u00e1lidos?","loc":"450 730","key":-7},
            {"category":"floatingLabel","text":"Decision","loc":"450 766","key":-8},
            {"category":"text","text":"No","loc":"340 790","key":-23},
            {"category":"text","text":"S\\u00ed","loc":"510 790","key":-24},
            {"category":"action","text":"Mostrar error","loc":"300 850","key":-9},
            {"category":"int-node","text":"","loc":"599 843","parentKey":-9,"offsetX":149,"offsetY":-7,"key":-19},
            {"category":"int-node","text":"","loc":"561 877","parentKey":-9,"offsetX":111,"offsetY":27,"key":-20},
            {"category":"merge","text":"","loc":"300 970","key":-10},
            {"category":"floatingLabel","text":"Merge","loc":"300 1006","key":-11},
            {"category":"action","text":"Crear cuenta","loc":"450 850","key":-12},
            {"category":"text","text":"Proceso completado","loc":"320 400","key":-25},
            {"category":"final","text":"","loc":"450 1090","key":-13},
            {"category":"floatingLabel","text":"Final","loc":"450 1126","key":-14}
          ],
          "linkDataArray": [
            {"category":"label-link","from":-1,"to":-2},
            {"category":"label-link","from":-7,"to":-8},
            {"category":"label-link","from":-10,"to":-11},
            {"category":"label-link","from":-13,"to":-14},
            {"from":-1,"to":-3},
            {"category":"ctrl-flow","from":-3,"to":-4},
            {"from":-4,"to":-5},
            {"category":"obj-flow","from":-17,"to":-18},
            {"from":-6,"to":-7},
            {"from":-7,"to":-9},
            {"category":"int-flow","from":-9,"to":-19},
            {"category":"int-flow-mid","from":-19,"to":-20},
            {"category":"int-flow-end","from":-20,"to":-12},
            {"from":-7,"to":-12},
            {"from":-9,"to":-10},
            {"from":-12,"to":-10},
            {"from":-10,"to":-13}
          ]
        }
        """;

    private static final String GOJS_PROMPT_INSTRUCTION_MODIFICAR = """
        Eres un experto en diagramas de actividades UML con GoJS.
        Recibirás un prompt del usuario y un JSON existente de un diagrama GoJS.
        Tu tarea es devolver ÚNICAMENTE el JSON modificado según lo que pide el prompt.
        REGLAS DE MODIFICACIÓN:
        1. Conserva TODOS los nodos y links existentes que no deban cambiar.
        2. Solo agrega, elimina o modifica lo que el prompt indica explícitamente.
        3. Si el prompt pide agregar un nodo, asígnale una key negativa única (distinta a todas las existentes).
        4. Si el prompt pide eliminar un nodo, elimina también sus links y floatingLabel asociados.
        5. Si el prompt pide mover un nodo, actualiza solo su "loc".
        6. Mantén la coherencia del flujo: si agregas un nodo entre dos existentes,
           elimina el link viejo y crea los nuevos links correspondientes.
        7. NUNCA cambies keys existentes.
        8. NUNCA elimines nodos que no se mencionen en el prompt.
        9. Si el prompt pide modificar el tamaño de un nodo y NO tiene "size", agrégala:
           - "action"     → "size": "200 80"
           - "activity"   → "size": "220 100"
           - "note"       → "size": "200 120"
           - "constraint" → "size": "200 100"
           - "partition"  → "size": "300 400"
           - otros        → "size": "160 80"
           Si ya tiene "size", solo modifica el valor.
        REGLAS ESTRICTAS DEL MODELO GoJS (aplican igual que al generar):
        2. CATEGORÍAS DE NODOS PERMITIDAS y sus reglas:
           - "initial": Nodo de inicio del diagrama (círculo negro). Solo debe haber uno.
             SIEMPRE debe tener un nodo "floatingLabel" asociado con texto "Inicio".
             { "category": "initial", "text": "", "loc": "x y", "key": -N }
           - "final": Nodo de fin del diagrama (círculo con borde). Solo debe haber uno.
             SIEMPRE debe tener un nodo "floatingLabel" asociado con texto "Final".
             { "category": "final", "text": "", "loc": "x y", "key": -N }
           - "flow-final": Nodo de fin de flujo alternativo (círculo con X).
             SIEMPRE debe tener un nodo "floatingLabel" asociado con texto "Flow Final".
             { "category": "flow-final", "text": "", "loc": "x y", "key": -N }
           - "floatingLabel": Etiqueta flotante asociada a otro nodo (initial, final, flow-final, decision, merge, synch).
             Su "loc" debe estar desplazada ~36 unidades en Y por debajo del nodo al que etiqueta.
             Se conecta a su nodo mediante un link de categoría "label-link".
             { "category": "floatingLabel", "text": "nombre", "loc": "x y", "key": -N }
           - "activity": Nodo de actividad general del proceso. Puede tener texto descriptivo.
             { "category": "activity", "text": "Nombre actividad", "loc": "x y", "key": -N }
           - "action": Representa una acción o paso del proceso.
             El texto debe ser claro y en infinitivo (ej: "Validar datos", "Enviar formulario").
             { "category": "action", "text": "nombre acción", "loc": "x y", "key": -N }
           - "partition": Representa un carril (swimlane) que agrupa un actor o sistema.
             Su "loc" debe estar desplazada a la izquierda/arriba del grupo de nodos que contiene.
             Cada actor del proceso debe tener su propia partition.
             { "category": "partition", "text": "Cliente", "loc": "x y", "key": -N }
           - "decision": Representa una decisión (rombo). El texto debe ser una pregunta con "¿?" o "[guard]".
             Sus links de salida DEBEN usar nodos "text" para etiquetar las ramas "Sí" y "No".
             SIEMPRE debe tener un nodo "floatingLabel" asociado.
             { "category": "decision", "text": "¿Datos válidos?", "loc": "x y", "key": -N }
           - "merge": Representa unión de flujos alternativos. Sin texto visible.
             SIEMPRE debe tener un nodo "floatingLabel" asociado con texto "Merge".
             { "category": "merge", "text": "", "loc": "x y", "key": -N }
           - "synch": Representa sincronización de flujos paralelos (barra horizontal gruesa).
             SIEMPRE debe tener un nodo "floatingLabel" asociado con texto "Synch".
             { "category": "synch", "text": "", "loc": "x y", "key": -N }
           - "fork-h": Bifurcación paralela horizontal (barra horizontal).
             No requiere floatingLabel.
             { "category": "fork-h", "text": "", "loc": "x y", "key": -N }
           - "fork-v": Bifurcación paralela vertical (barra vertical).
             No requiere floatingLabel.
             { "category": "fork-v", "text": "", "loc": "x y", "key": -N }
           - "send": Representa el envío de un mensaje o señal.
             { "category": "send", "text": "send", "loc": "x y", "key": -N }
           - "receive": Representa la recepción de un mensaje o señal.
             { "category": "receive", "text": "receive", "loc": "x y", "key": -N }
           - "object-node": Nodo objeto que representa un flujo de objeto entre dos acciones.
             SIEMPRE está anclado a un nodo "action" padre mediante "parentKey".
             Tiene un offset relativo al padre definido por "offsetX" y "offsetY".
             Se usa en PARES: uno en la acción origen y otro en la acción destino.
             Se conectan entre sí con un link de categoría "obj-flow".
             { "category": "object-node", "text": "", "loc": "x y", "parentKey": -N, "offsetX": X, "offsetY": Y, "key": -M }
           - "int-node": Nodo de interrupción que representa un flujo de excepción o interrupción.
             SIEMPRE está anclado a un nodo "action" padre mediante "parentKey".
             Se usa en grupos de 3 links: "int-flow" → "int-flow-mid" → "int-flow-end".
             Requiere mínimo 2 int-nodes: uno de inicio (anclado a acción origen) y uno de fin.
             { "category": "int-node", "text": "", "loc": "x y", "parentKey": -N, "offsetX": X, "offsetY": Y, "key": -M }
           - "note": Nodo de anotación libre. Sirve para agregar comentarios o aclaraciones
             sobre cualquier parte del diagrama. Su texto es libre.
             No requiere floatingLabel ni links obligatorios.
             Puede conectarse opcionalmente a otros nodos con un link normal punteado.
             { "category": "note", "text": "Texto de la nota...", "loc": "x y", "key": -N }
           - "constraint": Nodo de restricción o invariante UML.
             Tiene un "stereotype" visible (ej: «Invariant», «Precondition», «Postcondition»)
             y un "bodyText" con la expresión de la restricción (ej: "{cantidad > 0}").
             El campo "stereotypeIndex" indica el índice del estereotipo (normalmente 0).
             No requiere floatingLabel.
             { "category": "constraint", "text": "", "loc": "x y", "stereotype": "«Invariant»", "stereotypeIndex": 0, "bodyText": "{expresión}", "key": -N }
           - "text": Nodo de texto libre flotante. Sirve para agregar etiquetas, títulos
             o anotaciones simples en cualquier parte del diagrama sin recuadro.
             También se usa como etiqueta de links de decisión en lugar de poner
             "text" directamente en el link.
             { "category": "text", "text": "Texto libre", "loc": "x y", "key": -N }
        3. REGLAS DE LINKS (linkDataArray):
           TIPOS DE LINKS:
           a) PROHIBIDO usar links sin "category". Todo flujo de control DEBE usar "ctrl-flow":
              { "category": "ctrl-flow", "from": -N, "to": -M }
              NUNCA omitas el campo "category" en ningún link de flujo de control.
              NUNCA uses el campo "text" en los links. Si necesitas etiquetar un link,
              coloca un nodo "text" cerca del link con la etiqueta deseada.
           b) "label-link" (OBLIGATORIO para floatingLabel):
              Conecta un nodo principal con su floatingLabel.
              { "category": "label-link", "from": -N, "to": -M }
           c) "ctrl-flow" (flujo de control entre nodos):
              Usado para conectar cualquier par de nodos en el flujo de control.
              { "category": "ctrl-flow", "from": -N, "to": -M }
           d) "obj-flow" (flujo de objeto entre object-nodes):
              Conecta dos "object-node" que pertenecen a acciones distintas.
              { "category": "obj-flow", "from": -N, "to": -M }
              REGLA: El "from" y "to" deben ser keys de nodos "object-node", NO de "action".
           e) "int-flow" (inicio del flujo de interrupción):
              { "category": "int-flow", "from": -accion, "to": -intNode1 }
           f) "int-flow-mid" (segmento intermedio del flujo de interrupción):
              { "category": "int-flow-mid", "from": -intNode1, "to": -intNode2 }
           g) "int-flow-end" (fin del flujo de interrupción):
              { "category": "int-flow-end", "from": -intNodeFinal, "to": -accionDestino }
           REGLA GENERAL DE FLUJOS:
           - NUNCA agregues "text" a ningún link
           - NUNCA generes un link sin el campo "category". Todo link debe tener categoría explícita.
             Los únicos valores válidos son: "ctrl-flow", "label-link", "obj-flow",
             "int-flow", "int-flow-mid", "int-flow-end".
           - Para etiquetar ramas de decision (Sí/No), usa nodos "text" posicionados
             cerca del link correspondiente en el diagrama
           - Los flujos de error (rama "No") deben retornar a un nodo anterior lógico
           - El flujo principal siempre termina en el nodo "final"
           - Si en el JSON existente hay links sin "category", AL MODIFICAR debes corregirlos
             convirtiéndolos a "ctrl-flow".
        4. REGLAS DE POSICIÓN (loc):
           - Formato: "x y" como string (ej: "450 500")
           - El diagrama fluye de arriba hacia abajo (Y aumenta hacia abajo)
           - Separación vertical recomendada entre nodos: 80-120 unidades
           - Los floatingLabel deben estar ~36 unidades en Y por debajo de su nodo padre
           - Los object-node deben posicionarse en el borde de su acción padre (offsetX ≈ ±40)
           - Los int-node deben posicionarse con offsets que los ubiquen fuera del borde de la acción
           - Los nodos "note" y "constraint" deben colocarse cerca del nodo al que hacen referencia
           - Los nodos "text" usados como etiquetas de links deben colocarse junto al link que etiquetan
           - Las partitions del mismo actor deben agrupar sus acciones en la misma columna X
           - Actores diferentes deben estar en columnas X distintas (separación 150-200 unidades)
        5. REGLAS GENERALES:
           - Todas las keys deben ser números negativos únicos
           - NUNCA generes un link sin el campo "category". Todo link debe tener categoría explícita.
             Los únicos valores válidos son: "ctrl-flow", "label-link", "obj-flow",
             "int-flow", "int-flow-mid", "int-flow-end".
           - Los caracteres especiales como ¿ á é í ó ú ñ deben codificarse en unicode:
             ¿ → \\u00bf, á → \\u00e1, é → \\u00e9, í → \\u00ed, ó → \\u00f3, ú → \\u00fa, ñ → \\u00f1
           - NUNCA uses el campo "text" dentro de los links
           - No incluyas comentarios ni texto fuera del JSON
           - No uses comillas simples, solo dobles
           - El JSON debe ser parseable directamente
        6. FLUJO LÓGICO RECOMENDADO:
           initial (+floatingLabel) → actions/partitions →
           decision (+floatingLabel) → rama "Sí" continúa → ... → final (+floatingLabel)
                                     → rama "No" retorna o va a flow-final (+floatingLabel)
           Para flujos de objeto: action → [obj-flow via object-nodes] → action
           Para interrupciones:   action → int-flow → int-node → int-flow-mid → int-node → int-flow-end → action
           Para etiquetas de ramas: usar nodo "text" posicionado cerca del link, NO campo text en el link
        EJEMPLO DE SALIDA COMPLETO (referencia de estructura correcta):
        {
          "class": "GraphLinksModel",
          "nodeDataArray": [
            {"category":"initial","text":"","loc":"180 100","key":-1},
            {"category":"floatingLabel","text":"Inicio","loc":"180 136","key":-2},
            {"category":"partition","text":"Cliente","loc":"150 50","key":-15},
            {"category":"action","text":"Rellenar formulario","loc":"150 250","key":-3},
            {"category":"note","text":"El usuario debe completar todos los campos","loc":"50 250","key":-21},
            {"category":"action","text":"Enviar formulario","loc":"150 370","key":-4},
            {"category":"partition","text":"Sistema","loc":"450 50","key":-16},
            {"category":"action","text":"Validar datos","loc":"450 490","key":-5},
            {"category":"object-node","text":"","loc":"409 490","parentKey":-5,"offsetX":-41,"offsetY":0,"key":-17},
            {"category":"action","text":"Registrar en BD","loc":"450 610","key":-6},
            {"category":"object-node","text":"","loc":"491 610","parentKey":-6,"offsetX":41,"offsetY":0,"key":-18},
            {"category":"constraint","text":"","loc":"600 610","stereotype":"\\u00abInvariant\\u00bb","stereotypeIndex":0,"bodyText":"{datos no nulos}","key":-22},
            {"category":"decision","text":"\\u00bfDatos v\\u00e1lidos?","loc":"450 730","key":-7},
            {"category":"floatingLabel","text":"Decision","loc":"450 766","key":-8},
            {"category":"text","text":"No","loc":"340 790","key":-23},
            {"category":"text","text":"S\\u00ed","loc":"510 790","key":-24},
            {"category":"action","text":"Mostrar error","loc":"300 850","key":-9},
            {"category":"int-node","text":"","loc":"599 843","parentKey":-9,"offsetX":149,"offsetY":-7,"key":-19},
            {"category":"int-node","text":"","loc":"561 877","parentKey":-9,"offsetX":111,"offsetY":27,"key":-20},
            {"category":"merge","text":"","loc":"300 970","key":-10},
            {"category":"floatingLabel","text":"Merge","loc":"300 1006","key":-11},
            {"category":"action","text":"Crear cuenta","loc":"450 850","key":-12},
            {"category":"text","text":"Proceso completado","loc":"320 400","key":-25},
            {"category":"final","text":"","loc":"450 1090","key":-13},
            {"category":"floatingLabel","text":"Final","loc":"450 1126","key":-14}
          ],
          "linkDataArray": [
            {"category":"label-link","from":-1,"to":-2},
            {"category":"label-link","from":-7,"to":-8},
            {"category":"label-link","from":-10,"to":-11},
            {"category":"label-link","from":-13,"to":-14},
            {"category":"ctrl-flow","from":-1,"to":-3},
            {"category":"ctrl-flow","from":-3,"to":-4},
            {"category":"ctrl-flow","from":-4,"to":-5},
            {"category":"obj-flow","from":-17,"to":-18},
            {"category":"ctrl-flow","from":-6,"to":-7},
            {"category":"ctrl-flow","from":-7,"to":-9},
            {"category":"int-flow","from":-9,"to":-19},
            {"category":"int-flow-mid","from":-19,"to":-20},
            {"category":"int-flow-end","from":-20,"to":-12},
            {"category":"ctrl-flow","from":-7,"to":-12},
            {"category":"ctrl-flow","from":-9,"to":-10},
            {"category":"ctrl-flow","from":-12,"to":-10},
            {"category":"ctrl-flow","from":-10,"to":-13}
          ]
        }
        FORMATO DE SALIDA:
        - Devuelve ÚNICAMENTE el JSON completo modificado
        - Sin explicaciones, sin comentarios, sin bloques de código markdown
        - El JSON debe ser parseable directamente
        """;

    private static final String GOJS_PROMPT_INSTRUCTION_ANALISIS_CUELLOS = """
        Eres un experto en análisis de procesos de negocio y diagramas de actividades UML.
        El usuario te proporcionará una imagen con un diagrama de actividades UML.

        Tu tarea es analizar el diagrama e identificar cuellos de botella en el proceso
        de negocio y sugerir políticas concretas para optimizarlos.

        Debes devolver ÚNICAMENTE un JSON con la siguiente estructura FIJA,
        sin texto adicional, sin bloques markdown, sin comentarios:

        {
          "titulo_proceso": "nombre inferido del proceso analizado",
          "fecha_analisis": "fecha actual en formato DD/MM/YYYY",
          "resumen_ejecutivo": "2-3 oraciones describiendo el estado general del proceso y su nivel de eficiencia",
          "nivel_riesgo_global": "critico | alto | medio | bajo",
          "metricas": {
            "total_cuellos_detectados": 0,
            "cuellos_criticos": 0,
            "cuellos_altos": 0,
            "cuellos_medios": 0,
            "cuellos_bajos": 0,
            "actores_involucrados": ["Actor1", "Actor2"],
            "total_nodos": 0,
            "nodos_criticos": 0
          },
          "cuellos_de_botella": [
            {
              "id": 1,
              "nodo_afectado": "nombre exacto del nodo o acción en el diagrama",
              "actor_responsable": "nombre del actor o swimlane responsable, null si no aplica",
              "tipo": "secuencial_bloqueable | decision_sin_merge | paralelismo_ausente | proceso_manual | retorno_excesivo | validacion_tardia | sobrecarga_actor | flujo_error_mal_gestionado",
              "tipo_label": "etiqueta legible del tipo, ej: Proceso Secuencial Bloqueable",
              "impacto": "critico | alto | medio | bajo",
              "descripcion_problema": "explicación clara y detallada del problema detectado en este nodo",
              "consecuencia": "qué ocurre en el negocio si este cuello de botella no se resuelve",
              "politica_negocio_sugerida": {
                "titulo": "nombre corto de la política sugerida",
                "descripcion": "descripción detallada de la nueva política de negocio a implementar",
                "acciones_concretas": [
                  "acción específica 1 a implementar",
                  "acción específica 2 a implementar",
                  "acción específica 3 a implementar"
                ],
                "beneficio_esperado": "qué mejora concreta se obtiene al aplicar esta política",
                "prioridad_implementacion": "inmediata | corto_plazo | mediano_plazo | largo_plazo"
              }
            }
          ],
          "recomendaciones_generales": [
            {
              "id": 1,
              "categoria": "automatizacion | paralelismo | redistribucion_roles | validacion | comunicacion | monitoreo",
              "titulo": "título corto de la recomendación",
              "descripcion": "descripción detallada de la recomendación general para el proceso completo",
              "impacto_estimado": "alto | medio | bajo"
            }
          ],
          "conclusion": "párrafo final con la hoja de ruta sugerida para optimizar el proceso de negocio"
        }

        REGLAS ESTRICTAS:
        - Devuelve ÚNICAMENTE el JSON, sin texto antes ni después
        - Sin bloques markdown, sin comillas extra, sin comentarios
        - Todos los campos son obligatorios; si no aplica alguno usa null o array vacío []
        - "fecha_analisis" usa siempre la fecha actual real
        - "total_nodos" cuenta todos los nodos visibles en el diagrama
        - "nodos_criticos" son los nodos con impacto "critico" o "alto"
        - Las "acciones_concretas" deben ser pasos ejecutables reales, no generalidades
        - "politica_negocio_sugerida" debe ser específica para el dominio del proceso analizado
        - Detecta mínimo 2 y máximo 8 cuellos de botella por diagrama
        - Detecta mínimo 2 y máximo 5 recomendaciones generales
        - El JSON debe ser parseable directamente sin ningún ajuste
        """;

    // ─────────────────────────────────────────────────────────────
    // Helpers de payload
    // ─────────────────────────────────────────────────────────────

    /**
     * Construye el payload para llamadas de solo texto (generar / modificar).
     */
    private Map<String, Object> buildPayloadTexto(String systemPrompt, String userContent) {
        List<Map<String, Object>> messages = new ArrayList<>();

        messages.add(Map.of(
                "role", "system",
                "content", systemPrompt
        ));
        messages.add(Map.of(
                "role", "user",
                "content", userContent
        ));

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", OPENAI_MODEL);
        payload.put("messages", messages);
        payload.put("temperature", 0.2);
        return payload;
    }

    /**
     * Construye el payload para análisis de imagen (vision).
     * Usa el formato de content multipart de OpenAI.
     */
    private Map<String, Object> buildPayloadAnalisisCuellos(String base64Image, String mimeType) {
        List<Map<String, Object>> messages = new ArrayList<>();

        // System message con las instrucciones
        messages.add(Map.of(
                "role", "system",
                "content", GOJS_PROMPT_INSTRUCTION_ANALISIS_CUELLOS
        ));

        // User message con texto + imagen inline (vision)
        List<Map<String, Object>> userContent = new ArrayList<>();

        userContent.add(Map.of(
                "type", "text",
                "text", "Analiza este diagrama de actividades UML, "
                        + "detecta todos los cuellos de botella y "
                        + "sugiere políticas de negocio concretas para optimizar el proceso."
        ));

        userContent.add(Map.of(
                "type", "image_url",
                "image_url", Map.of(
                        "url", "data:" + mimeType + ";base64," + base64Image,
                        "detail", "high"
                )
        ));

        messages.add(Map.of(
                "role", "user",
                "content", userContent
        ));

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", OPENAI_MODEL);
        payload.put("messages", messages);
        payload.put("temperature", 0.2);
        return payload;
    }

    // ─────────────────────────────────────────────────────────────
    // Métodos públicos
    // ─────────────────────────────────────────────────────────────

    public JsonNode generarDiagrama(String prompt) {
        if (prompt == null || prompt.isEmpty()) {
            throw new IllegalArgumentException("El campo 'prompt' es obligatorio.");
        }
        return llamarOpenAI(
                buildPayloadTexto(GOJS_PROMPT_INSTRUCTION_GENERAR, prompt)
        );
    }

    public JsonNode modificarDiagrama(String prompt, Object jsonActual) {
        if (prompt == null || prompt.isEmpty()) {
            throw new IllegalArgumentException("El campo 'prompt' es obligatorio.");
        }
        if (jsonActual == null) {
            throw new IllegalArgumentException("El campo 'jsonActual' es obligatorio.");
        }
        try {
            String combinedContent = "Prompt del usuario: " + prompt + "\nJSON actual: " +
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonActual);
            return llamarOpenAI(
                    buildPayloadTexto(GOJS_PROMPT_INSTRUCTION_MODIFICAR, combinedContent)
            );
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error interno al modificar diagrama", e);
            throw new RuntimeException("Error interno al modificar diagrama", e);
        }
    }

    public JsonNode analizarCuellosDesdeImagen(String base64Image) {
        if (base64Image == null || base64Image.isBlank()) {
            throw new IllegalArgumentException("La imagen en base64 es obligatoria.");
        }
        try {
            // Elimina el prefijo data:image/png;base64, si viene del frontend
            String imagenLimpia = base64Image.contains(",")
                    ? base64Image.split(",")[1]
                    : base64Image;

            // Detecta el mimeType desde el prefijo o usa png por defecto
            String mimeType = base64Image.startsWith("data:")
                    ? base64Image.split(";")[0].replace("data:", "")
                    : "image/png";

            // Re-encodea para garantizar base64 válido
            byte[] imageBytes       = Base64.getDecoder().decode(imagenLimpia);
            String base64Reencoded  = Base64.getEncoder().encodeToString(imageBytes);

            return llamarOpenAI(
                    buildPayloadAnalisisCuellos(base64Reencoded, mimeType)
            );
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error interno al analizar cuellos de botella desde base64", e);
            throw new RuntimeException("Error interno al analizar cuellos de botella desde base64", e);
        }
    }
}