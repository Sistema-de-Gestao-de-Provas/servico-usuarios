/*
 * SGP - servico-usuarios
 */
package com.sgp.servico_usuarios.domain.repository;

import com.sgp.servico_usuarios.domain.entity.Materia;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 *
 * @author diego-casemiro
 */
public interface MateriaRepository extends JpaRepository<Materia, Long>, JpaSpecificationExecutor<Materia> {

    Page<Materia> findAllByDeletadoEmIsNull(Pageable pageable);

    Optional<Materia> findByIdAndDeletadoEmIsNull(Long id);

    List<Materia> findAllByIdInAndDeletadoEmIsNull(Set<Long> ids);

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);
}
