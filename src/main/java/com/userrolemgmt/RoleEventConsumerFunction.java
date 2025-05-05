package com.userrolemgmt;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import java.util.logging.Logger;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class RoleEventConsumerFunction {
    
    private static final Gson gson = new Gson();
    
    @FunctionName("RoleEventConsumer")
    public void run(
        @EventGridTrigger(name = "event") String content,
        final ExecutionContext context) {
        
        Logger logger = context.getLogger();
        logger.info("Role Event Grid trigger function processed an event: " + content);
        
        try {
            // Parsear el evento
            JsonObject eventJson = JsonParser.parseString(content).getAsJsonObject();
            String eventType = eventJson.get("eventType").getAsString();
            
            // Procesar según el tipo de evento
            if (eventType.equals("RoleCreated")) {
                // Lógica específica para cuando se crea un rol
                logger.info("Procesando evento de creación de rol");
                // Por ejemplo, podrías actualizar alguna caché
            } else if (eventType.equals("RoleUpdated")) {
                // Lógica específica para cuando se actualiza un rol
                logger.info("Procesando evento de actualización de rol");
            } else if (eventType.equals("RoleDeleted")) {
                // Lógica específica para cuando se elimina un rol
                logger.info("Procesando evento de eliminación de rol");
            }
            
            // Puedes agregar más tipos de eventos según sea necesario
            
        } catch (Exception e) {
            logger.severe("Error procesando el evento: " + e.getMessage());
        }
    }
}