package cl.veterinary;

import java.util.Optional;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.BindingName;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import cl.veterinary.model.User;
import cl.veterinary.service.UserService;


public class UserFunction {

    private static final ApplicationContext context =
            new SpringApplicationBuilder(SpringBootAzureApp.class).run();

    private final UserService userService =
            context.getBean(UserService.class);



    

    @FunctionName("saveUser")
    public HttpResponseMessage saveCustomer(
            @HttpTrigger(name = "req",
                    methods = {HttpMethod.POST},
                    authLevel = AuthorizationLevel.FUNCTION,
                    route = "saveUser")
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Procesando solicitud saveUser...");

        try {

            String requestBody = request.getBody().orElse("");
            ObjectMapper mapper = new ObjectMapper();
            User user = mapper.readValue(requestBody, User.class);

            User saved = userService.saveUser(user);

            return request
                .createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json") 
                .body(saved)
                .build();

        } catch (Exception e) {
            context.getLogger().severe("Error al guardar usuario: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al guardar cliente")
                    .build();
        }
    }

    @FunctionName("updateUser")
    public HttpResponseMessage updateCustomer(
            @HttpTrigger(
                    name = "req",
                    methods = {HttpMethod.PUT},
                    authLevel = AuthorizationLevel.FUNCTION,
                    route = "updateUser/{id}") // id por ruta
            HttpRequestMessage<Optional<String>> request,
            @BindingName("id") String id,
            final ExecutionContext context) {

        context.getLogger().info("Procesando solicitud updateUser con ID: " + id);

        try {
            Long userId = Long.parseLong(id);

            // Parsear el JSON recibido
            String requestBody = request.getBody().orElse("");
            ObjectMapper mapper = new ObjectMapper();
            User updatedData = mapper.readValue(requestBody, User.class);

            // Buscar si el cliente existe
            Optional<User> existingOpt = userService.findUserById(userId);
            if (existingOpt.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .body("Usuario con ID " + id + " no encontrado.")
                        .build();
            }

            User existing = existingOpt.get();

            // Actualizar campos
            existing.setId(userId);
            existing.setNombre(updatedData.getNombre());
            existing.setApellidoPaterno(updatedData.getApellidoPaterno());
            existing.setApellidoMaterno(updatedData.getApellidoMaterno());
            existing.setRut(updatedData.getRut());
            existing.setDireccion(updatedData.getDireccion());
            existing.setCelular(updatedData.getCelular());
            existing.setEmail(updatedData.getEmail());
            existing.setActivo(updatedData.getActivo());
            existing.setPassword(updatedData.getPassword());
            existing.setRol(updatedData.getRol());

            // Guardar cambios
            User updated = userService.updateUser(existing);

            return request
                .createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json") 
                .body(updated)
                .build();

        } catch (NumberFormatException e) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("ID inválido: debe ser numérico.")
                    .build();
        } catch (Exception e) {
            context.getLogger().severe("Error al actualizar usuario: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar cliente.")
                    .build();
        }
    }

    @FunctionName("deleteUser")
    public HttpResponseMessage deleteCustomer(
            @HttpTrigger(
                    name = "req",
                    methods = {HttpMethod.DELETE},
                    authLevel = AuthorizationLevel.FUNCTION,
                    route = "deleteUser/{id}") // ID por ruta
            HttpRequestMessage<Optional<String>> request,
            @BindingName("id") String id,
            final ExecutionContext context) {

        context.getLogger().info("Procesando solicitud deleteUser con ID: " + id);

        try {
            Long userId = Long.parseLong(id);

            // Buscar si existe
            Optional<User> existing = userService.findUserById(userId);
            if (existing.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .body(new User())
                        .build();
            }

            // Eliminar
            userService.deleteUser(userId);

            return request
                .createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json") 
                .body("Usuario eliminado")
                .build();

        } catch (NumberFormatException e) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("ID inválido: debe ser numérico.")
                    .build();
        } catch (Exception e) {
            context.getLogger().severe("Error al eliminar cliente: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar usuario.")
                    .build();
        }
    }



}
