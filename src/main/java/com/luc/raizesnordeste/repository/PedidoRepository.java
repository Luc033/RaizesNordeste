package com.luc.raizesnordeste.repository;

import com.luc.raizesnordeste.domain.entity.Pedido;
import com.luc.raizesnordeste.domain.enums.CanalPedido;
import com.luc.raizesnordeste.domain.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PedidoRepository   extends JpaRepository<Pedido, UUID> {

    List<Pedido> findByStatusAndCriadoEmIsBefore(Status status, LocalDateTime desde);
    List<Pedido> findByCanalPedido(CanalPedido canal);

}
