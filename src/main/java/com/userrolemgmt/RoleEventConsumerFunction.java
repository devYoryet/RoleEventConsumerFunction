package com.userrolemgmt;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.userrolemgmt.dao.EventDAO;

public class RoleEventConsumerFunction {

    private static final Gson gson = new Gson();
    private final EventDAO eventDAO;

    public RoleEventConsumerFunction() {
        this.eventDAO = new EventDAO();
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

            logger.info("Evento parseado correctamente - Tipo: " + eventType + ", Asunto: " + subject);

            try {
                // Registrar el evento en el Event Store
                logger.info("Intentando registrar evento en Event Store");
                eventId = eventDAO.storeEvent(eventType, subject, content);
                logger.info("Evento registrado exitosamente en Event Store con ID: " + eventId);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error al registrar evento en Event Store: " + e.getMessage(), e);
                return; // Terminar la función
            }

            // Procesar según el tipo de evento
            if (eventType.equals("RoleCreated")) {
                try {
                    // Lógica específica para cuando se crea un rol
                    logger.info("Procesando evento de creación de rol");

                    // Simular actualización de caché de permisos
                    logger.info("Actualizando caché de permisos para el nuevo rol");

                    // Marca el evento como procesado exitosamente
                    eventDAO.markEventProcessed(eventId, true, null);
                    logger.info("Evento de creación de rol procesado exitosamente");
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Error al procesar evento RoleCreated: " + e.getMessage(), e);
                    if (eventId != -1) {
                        try {
                            eventDAO.markEventProcessed(eventId, false, e.getMessage());
                        } catch (Exception ex) {
                            logger.log(Level.SEVERE, "Error al marcar evento como fallido: " + ex.getMessage(), ex);
                        }
                    }
                    return; // Terminar la función
                }

            } else if (eventType.equals("RoleUpdated")) {
                try {
                    // Lógica para roles actualizados...
                    logger.info("Procesando evento de actualización de rol");
                    eventDAO.markEventProcessed(eventId, true, null);
                    logger.info("Evento de actualización de rol procesado exitosamente");
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Error al procesar evento RoleUpdated: " + e.getMessage(), e);
                    if (eventId != -1) {
                        try {
                            eventDAO.markEventProcessed(eventId, false, e.getMessage());
                        } catch (Exception ex) {
                            logger.log(Level.SEVERE, "Error al marcar evento como fallido: " + ex.getMessage(), ex);
                        }
                    }
                    return; // Terminar la función
                }

            } else if (eventType.equals("RoleDeleted")) {
                try {
                    // Lógica para roles eliminados...
                    logger.info("Procesando evento de eliminación de rol");
                    eventDAO.markEventProcessed(eventId, true, null);
                    logger.info("Evento de eliminación de rol procesado exitosamente");
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Error al procesar evento RoleDeleted: " + e.getMessage(), e);
                    if (eventId != -1) {
                        try {
                            eventDAO.markEventProcessed(eventId, false, e.getMessage());
                        } catch (Exception ex) {
                            logger.log(Level.SEVERE, "Error al marcar evento como fallido: " + ex.getMessage(), ex);
                        }
                    }
                    return; // Terminar la función
                }
            }

            // Si todo salió bien
            logger.info("Evento procesado exitosamente");

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error general procesando el evento: " + e.getMessage(), e);

            if (eventId != -1) {
                try {
                    eventDAO.markEventProcessed(eventId, false, "Error general: " + e.getMessage());
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "Error al marcar evento como fallido: " + ex.getMessage(), ex);
                }
            }
        }
    }
}