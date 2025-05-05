package com.userrolemgmt;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.userrolemgmt.dao.EventDAO; // Importa tu nueva clase

public class RoleEventConsumerFunction {

    private static final Gson gson = new Gson();
    private EventDAO eventDAO; // Agregamos esta propiedad

    public RoleEventConsumerFunction() {
        this.eventDAO = new EventDAO(); // Inicializa en el constructor
    }

    @FunctionName("RoleEventConsumer")
    public void run(
            @EventGridTrigger(name = "event") String content,
            final ExecutionContext context) {

        Logger logger = context.getLogger();
        logger.info("Role Event Grid trigger function processed an event: " + content);

        long eventId = -1;

        try {
            // Parsear el evento
            JsonObject eventJson = JsonParser.parseString(content).getAsJsonObject();
            String eventType = eventJson.get("eventType").getAsString();
            String subject = eventJson.get("subject").getAsString();

            // Registrar el evento en el Event Store
            eventId = eventDAO.storeEvent(eventType, subject, content);
            logger.info("Evento registrado en Event Store con ID: " + eventId);

            // Procesar según el tipo de evento
            if (eventType.equals("RoleCreated")) {
                // Lógica específica para cuando se crea un rol
                logger.info("Procesando evento de creación de rol");

                // Simular actualización de caché de permisos
                logger.info("Actualizando caché de permisos para el nuevo rol");

                // Marca el evento como procesado exitosamente
                eventDAO.markEventProcessed(eventId, true, null);

            } else if (eventType.equals("RoleUpdated")) {
                // Lógica para roles actualizados...
                logger.info("Procesando evento de actualización de rol");
                eventDAO.markEventProcessed(eventId, true, null);

            } else if (eventType.equals("RoleDeleted")) {
                // Lógica para roles eliminados...
                logger.info("Procesando evento de eliminación de rol");
                eventDAO.markEventProcessed(eventId, true, null);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error procesando el evento: " + e.getMessage(), e);

            try {
                // Si tenemos un ID de evento, marcar como fallido
                if (eventId != -1) {
                    eventDAO.markEventProcessed(eventId, false, e.getMessage());
                }
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Error al actualizar el estado del evento: " + ex.getMessage(), ex);
            }
        }
    }
}