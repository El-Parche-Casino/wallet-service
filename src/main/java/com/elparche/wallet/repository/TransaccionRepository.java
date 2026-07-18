package com.elparche.wallet.repository;

import com.elparche.wallet.model.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    long countByFechaHoraGreaterThanEqual(LocalDateTime desde);

    List<Transaccion> findAllByOrderByFechaHoraDesc(org.springframework.data.domain.Pageable pageable);

    List<Transaccion> findByUsernameOrderByFechaHoraDesc(String username);

    List<Transaccion> findByUsernameAndJuegoTipoOrderByFechaHoraDesc(
            String username, String juegoTipo);
}