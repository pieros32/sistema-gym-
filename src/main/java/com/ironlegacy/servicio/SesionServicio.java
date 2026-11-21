package com.ironlegacy.servicio;

import com.ironlegacy.dao.UsuarioDao;
import com.ironlegacy.modelo.Usuario;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SesionServicio {

    private static final String MENSAJE_CREDENCIALES = "Usuario o clave incorrectos.";

    private final UsuarioDao usuarioDao;
    private final PasswordEncoder codificadorClaves;

    public SesionServicio(UsuarioDao usuarioDao, PasswordEncoder codificadorClaves) {
        this.usuarioDao = usuarioDao;
        this.codificadorClaves = codificadorClaves;
    }

    /** Usuario y clave con el mismo mensaje de error, para no revelar qué usuarios existen. */
    public Usuario autenticar(String nombreUsuario, String clave) {
        if (estaVacio(nombreUsuario) || estaVacio(clave)) {
            throw new NegocioException("Ingresa usuario y clave.");
        }
        return usuarioDao.buscarPorNombreUsuario(nombreUsuario.trim())
                .filter(usuario -> usuario.activo() && codificadorClaves.matches(clave, usuario.claveHash()))
                .orElseThrow(() -> new NegocioException(MENSAJE_CREDENCIALES));
    }

    private static boolean estaVacio(String texto) {
        return texto == null || texto.isBlank();
    }
}
