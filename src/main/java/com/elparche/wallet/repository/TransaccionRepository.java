package com.elparche.wallet.repository;

import com.elparche.wallet.model.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    List<Transaccion> findByUsernameOrderByFechaHoraDesc(String username);

    List<Transaccion> findByUsernameAndJuegoTipoOrderByFechaHoraDesc(
            String username, String juegoTipo);
}