package com.ironlegacy.config;

import com.ironlegacy.modelo.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Sin sesión redirige a login. Con sesión y sin permiso para la ruta responde 403.
 * La matriz de permisos vive en Rol; aquí solo se traduce ruta → caso de uso.
 */
@Component
public class InterceptorRoles implements HandlerInterceptor {

    private static final List<ReglaCasoDeUso> REGLAS = List.of(
            new ReglaCasoDeUso("/socios/registro", "CU-02"),
            new ReglaCasoDeUso("/membresias/renovar", "CU-03"),
            new ReglaCasoDeUso("/acceso/", "CU-04"),
            new ReglaCasoDeUso("/socios/buscar", "CU-05"),
            new ReglaCasoDeUso("/pagos/historial", "CU-07"),
            new ReglaCasoDeUso("/membresias/por-vencer", "CU-08"),
            new ReglaCasoDeUso("/reportes", "CU-09"));

    private record ReglaCasoDeUso(String prefijoRuta, String casoDeUso) {
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        Usuario usuario = usuarioEnSesion(request.getSession(false));
        if (usuario == null) {
            response.sendRedirect("/login");
            return false;
        }

        String ruta = request.getRequestURI();
        Optional<ReglaCasoDeUso> regla = REGLAS.stream()
                .filter(r -> ruta.startsWith(r.prefijoRuta()))
                .findFirst();

        if (regla.isPresent() && !usuario.tienePermiso(regla.get().casoDeUso())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
    }

    private static Usuario usuarioEnSesion(HttpSession sesion) {
        return sesion == null ? null : (Usuario) sesion.getAttribute(ClavesSesion.USUARIO);
    }
}
