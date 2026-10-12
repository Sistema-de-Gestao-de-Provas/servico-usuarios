/*
 * SGP - servico-usuarios
 */
package com.sgp.servico_usuarios.domain.repository.specification;

import com.sgp.servico_usuarios.domain.entity.Usuario;
import com.sgp.servico_usuarios.domain.enums.Permissao;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

/**
 *
 * @author diego-casemiro
 */
public final class UsuarioSpecification {

    private UsuarioSpecification() {
    }

    public static Specification<Usuario> filtrar(String nome, String email, String cpf, Permissao permissao,
            Long universidadeId) {
        return (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();
            predicados.add(cb.isNull(root.get("deletadoEm")));

            if (StringUtils.hasText(nome)) {
                predicados.add(cb.like(cb.lower(root.get("nome")), "%" + nome.trim().toLowerCase() + "%"));
            }
            if (StringUtils.hasText(email)) {
                predicados.add(cb.like(cb.lower(root.get("email")), "%" + email.trim().toLowerCase() + "%"));
            }
            if (StringUtils.hasText(cpf)) {
                predicados.add(cb.equal(root.get("cpf"), cpf.replaceAll("\\D", "")));
            }
            if (permissao != null) {
                predicados.add(cb.equal(root.get("permissao"), permissao));
            }
            if (universidadeId != null) {
                predicados.add(cb.equal(root.get("universidade").get("id"), universidadeId));
            }
            return cb.and(predicados.toArray(Predicate[]::new));
        };
    }
}
