/*
 * SGP - servico-usuarios
 */
package com.sgp.servico_usuarios.domain.repository;

import com.sgp.servico_usuarios.domain.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 *
 * @author diego-casemiro
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long>, JpaSpecificationExecutor<Usuario> {
}
