package com.ironlegacy.dao;

import com.ironlegacy.modelo.Usuario;

import java.util.Optional;

public interface UsuarioDao {

    Optional<Usuario> buscarPorNombreUsuario(String nombreUsuario);
}
