/*
 * SGP - servico-usuarios
 */
package com.sgp.servico_usuarios.domain.repository;

import com.sgp.servico_usuarios.domain.entity.Universidade;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 *
 * @author diego-casemiro
 */
public interface UniversidadeRepository extends JpaRepository<Universidade, Long>, JpaSpecificationExecutor<Universidade> {

    Optional<Universidade> findByIdAndDeletadoEmIsNull(Long id);
}
