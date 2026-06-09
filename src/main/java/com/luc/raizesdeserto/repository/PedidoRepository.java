package com.luc.raizesdeserto.repository;

import com.luc.raizesdeserto.domain.entity.Pedido;
import com.luc.raizesdeserto.domain.enums.CanalPedido;
import com.luc.raizesdeserto.domain.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PedidoRepository   extends JpaRepository<Pedido, UUID> {

    List<Pedido> findByStatusAndCriadoEmAfter(Status status, LocalDateTime desde);
    List<Pedido> findByCanalPedido(CanalPedido canal);

}
