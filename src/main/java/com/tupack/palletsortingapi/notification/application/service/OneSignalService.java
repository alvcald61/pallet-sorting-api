package com.tupack.palletsortingapi.notification.application.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio para el envío de push notifications utilizando la API de OneSignal.
 * <p>
 * OneSignal es un servicio de terceros que gestiona el envío de notificaciones
 * push a múltiples plataformas (Web, iOS, Android) a través de una API REST unificada.
 * </p>
 * <p>
 * Este servicio encapsula toda la lógica de comunicación con la API de OneSignal,
 * manejando la autenticación, construcción de payloads y envío de notificaciones.
 * </p>
 *
 * <h2>Configuración Requerida</h2>
 * <p>
 * En application.yml:
 * <pre>
 * application:
 *   onesignal:
 *     app-id: "tu-app-id-de-onesignal"
 *     rest-api-key: "tu-rest-api-key-de-onesignal"
 * </pre>
 * </p>
 *
 * <h2>Flujo de Notificaciones</h2>
 * <ol>
 *   <li>Backend llama a sendPushNotification() o sendPushNotificationToMultiple()</li>
 *   <li>Se construye el payload JSON según especificación de OneSignal</li>
 *   <li>Se hace HTTP POST a la API de OneSignal</li>
 *   <li>OneSignal busca los dispositivos asociados al external_user_id</li>
 *   <li>OneSignal envía la notificación a todos los dispositivos del usuario</li>
 * </ol>
 *
 * @author TUPACK Development Team
 * @version 1.0
 * @since 2026-02-17
 * @see <a href="https://documentation.onesignal.com/reference/create-notification">OneSignal API Documentation</a>
 */
@Service
@Slf4j
public class OneSignalService {

    /**
     * URL de la API de OneSignal para crear notificaciones.
     * Endpoint oficial de OneSignal (User Model, compatible con SDK v10+).
     */
    private static final String ONESIGNAL_API_URL = "https://api.onesignal.com/notifications?c=push";

    /**
     * ID de la aplicación en OneSignal.
     * Se obtiene del dashboard de OneSignal en Settings → Keys & IDs.
     */
    @Value("${application.onesignal.app-id}")
    private String appId;

    /**
     * REST API Key de OneSignal.
     * Clave secreta para autenticación en la API.
     * Se obtiene del dashboard de OneSignal en Settings → Keys & IDs.
     * <p>
     * <strong>IMPORTANTE:</strong> Esta clave es privada y nunca debe exponerse
     * en el frontend o en repositorios públicos.
     * </p>
     */
    @Value("${application.onesignal.rest-api-key}")
    private String restApiKey;

    /**
     * Cliente HTTP para realizar peticiones a la API de OneSignal.
     */
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Envía una push notification a un único usuario utilizando su external_user_id.
     * <p>
     * El external_user_id es el ID del usuario en nuestra base de datos,
     * que se registró previamente en OneSignal desde el frontend con OneSignal.login().
     * </p>
     * <p>
     * OneSignal automáticamente envía la notificación a TODOS los dispositivos
     * registrados de ese usuario (Chrome, Safari, iPhone, Android, etc.).
     * </p>
     *
     * <h3>Ejemplo de Uso:</h3>
     * <pre>
     * Map&lt;String, Object&gt; data = Map.of(
     *     "entityType", "ORDER",
     *     "entityId", "123"
     * );
     *
     * oneSignalService.sendPushNotification(
     *     "456",                    // userId
     *     "Orden Aprobada",        // title
     *     "Tu orden #123...",      // message
     *     data                     // additional data
     * );
     * </pre>
     *
     * @param userId  ID del usuario (external_user_id en OneSignal). Debe corresponder
     *                al ID del usuario en la tabla users de nuestra BD.
     * @param title   Título de la notificación. Se muestra prominentemente en el dispositivo.
     * @param message Mensaje completo de la notificación. Proporciona detalles del evento.
     * @param data    Datos adicionales en formato clave-valor (opcional). Estos datos
     *                se envían al dispositivo y pueden usarse para navegación o lógica personalizada.
     *                Ejemplo: {"entityType": "ORDER", "entityId": "123"}
     *
     * @throws IllegalStateException si restApiKey no está configurado (warning, no exception)
     *
     * @see #sendPushNotificationToMultiple(List, String, String, Map)
     */
    public void sendPushNotification(String userId, String title, String message, Map<String, Object> data) {
        try {
            if (restApiKey == null || restApiKey.isEmpty()) {
                log.warn("OneSignal REST API key not configured. Skipping push notification.");
                return;
            }

            Map<String, Object> aliases = new HashMap<>();
            aliases.put("external_id", List.of(userId));

            Map<String, Object> payload = new HashMap<>();
            payload.put("app_id", appId);
            payload.put("include_aliases", aliases);
            payload.put("target_channel", "push");

            Map<String, String> headings = new HashMap<>();
            headings.put("en", title);
            payload.put("headings", headings);

            Map<String, String> contents = new HashMap<>();
            contents.put("en", message);
            payload.put("contents", contents);

            if (data != null && !data.isEmpty()) {
                payload.put("data", data);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Key " + restApiKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            restTemplate.postForObject(ONESIGNAL_API_URL, request, Map.class);
            log.info("Push notification sent to user: {}", userId);
        } catch (Exception e) {
            log.error("Error sending push notification to user {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Envía una push notification a múltiples usuarios simultáneamente.
     * <p>
     * Esta es la versión optimizada para envíos masivos. En lugar de hacer
     * N llamadas HTTP individuales, hace una sola petición a OneSignal con
     * múltiples external_user_ids.
     * </p>
     * <p>
     * Útil para escenarios como:
     * - Notificar a todos los ADMINs cuando se crea una orden
     * - Broadcast de alertas del sistema
     * - Notificaciones grupales por rol o departamento
     * </p>
     *
     * <h3>Ejemplo de Uso:</h3>
     * <pre>
     * List&lt;String&gt; adminIds = Arrays.asList("1", "2", "3");
     *
     * Map&lt;String, Object&gt; data = Map.of(
     *     "entityType", "ORDER",
     *     "entityId", "123",
     *     "priority", "HIGH"
     * );
     *
     * oneSignalService.sendPushNotificationToMultiple(
     *     adminIds,                      // Lista de user IDs
     *     "Nueva Orden Pendiente",       // title
     *     "Orden #123 requiere...",     // message
     *     data                           // additional data
     * );
     * </pre>
     *
     * <h3>Comportamiento de OneSignal:</h3>
     * <p>
     * OneSignal itera sobre cada external_user_id y envía la notificación
     * a TODOS los dispositivos registrados de cada usuario. Si un usuario
     * tiene 3 dispositivos (iPhone, Android, Chrome), recibirá 3 notificaciones.
     * </p>
     *
     * @param userIds Lista de IDs de usuarios (external_user_ids en OneSignal).
     *                Cada ID debe corresponder a un usuario válido en la BD.
     *                No debe estar vacía.
     * @param title   Título de la notificación. Mismo para todos los usuarios.
     * @param message Mensaje de la notificación. Mismo para todos los usuarios.
     * @param data    Datos adicionales opcionales en formato clave-valor.
     *                Se envían a todos los usuarios. Si necesitas datos
     *                personalizados por usuario, debes hacer llamadas individuales.
     *
     * @throws IllegalStateException si restApiKey no está configurado (warning, no exception)
     *
     * @see #sendPushNotification(String, String, String, Map)
     */
    public void sendPushNotificationToMultiple(List<String> userIds, String title, String message, Map<String, Object> data) {
        try {
            if (restApiKey == null || restApiKey.isEmpty()) {
                log.warn("OneSignal REST API key not configured. Skipping push notification.");
                return;
            }

            Map<String, Object> aliases = new HashMap<>();
            aliases.put("external_id", userIds);

            Map<String, Object> payload = new HashMap<>();
            payload.put("app_id", appId);
            payload.put("include_aliases", aliases);
            payload.put("target_channel", "push");

            Map<String, String> headings = new HashMap<>();
            headings.put("en", title);
            payload.put("headings", headings);

            Map<String, String> contents = new HashMap<>();
            contents.put("en", message);
            payload.put("contents", contents);

            if (data != null && !data.isEmpty()) {
                payload.put("data", data);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Key " + restApiKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            restTemplate.postForObject(ONESIGNAL_API_URL, request, Map.class);
            log.info("Push notification sent to {} users", userIds.size());
        } catch (Exception e) {
            log.error("Error sending push notification to multiple users: {}", e.getMessage());
        }
    }
}
