/*
 * SGP - servico-usuarios
 */
package com.sgp.servico_usuarios.domain.repository;

import com.sgp.servico_usuarios.domain.entity.Usuario;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 *
 * @author diego-casemiro
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long>, JpaSpecificationExecutor<Usuario> {

    Page<Usuario> findAllByDeletadoEmIsNull(Pageable pageable);

    Optional<Usuario> findByIdAndDeletadoEmIsNull(Long id);

    Optional<Usuario> findByKeycloakIdAndDeletadoEmIsNull(UUID keycloakId);

    boolean existsByEmail(String email);

    boolean existsByCpf(String cpf);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByCpfAndIdNot(String cpf, Long id);
}
