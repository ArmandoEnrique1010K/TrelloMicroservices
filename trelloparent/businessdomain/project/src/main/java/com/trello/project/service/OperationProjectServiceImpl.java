package com.trello.project.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trello.project.entities.Operation;
import com.trello.project.enums.OperationStatus;
import com.trello.project.enums.OperationType;
import com.trello.project.exception.BusinessRuleException;
import com.trello.project.repositories.OperationRepository;

@Service
public class OperationProjectServiceImpl implements OperationProjectService {

    private final OperationRepository operationRepository;

    public OperationProjectServiceImpl(
            OperationRepository operationRepository) {
        this.operationRepository = operationRepository;
    }

    @Override
    public Optional<Operation> findOperationById(UUID operationId) {
        Optional<Operation> operation = operationRepository.findById(operationId);
        return operation;
    }

    @Override
    public Operation saveOperation(Operation operation) {
        return operationRepository.save(operation);
    }

    @Override
    public Optional<Operation> findOperationByInvitationIdAndType(UUID operationId, OperationType operationType) {
        return operationRepository.findByInvitationIdAndType(operationId, operationType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void compensateOperation(UUID operationId) {

        // 1. Buscar la operacion
        // Lanzar excepción OperationNotFoundException
        Operation operation = operationRepository.findById(operationId).orElseThrow(
                BusinessRuleException::new);

        // 2. Verificar estado y tipo
        // La operación ya fue compensada.
        // No se debe compensar dos veces.
        if (operation.getStatus() == OperationStatus.COMPENSATED) {
            return;
        }

        // Solo se compensan operaciones completadas.
        if (operation.getStatus() != OperationStatus.COMPLETED) {
            // throw new OperationCannotBeCompensatedException();
            throw new BusinessRuleException();
        }

        // 3. Marcar la operación como COMPENSATED.
        // Se actualiza el estado únicamente después
        // de revertir correctamente los cambios de negocio.
        operation.setStatus(OperationStatus.COMPENSATED);

        // 4. Realizar el guardado
        operationRepository.save(operation);
    }

}
