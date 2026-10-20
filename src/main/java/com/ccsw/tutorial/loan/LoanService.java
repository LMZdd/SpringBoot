package com.ccsw.tutorial.loan;

import com.ccsw.tutorial.loan.model.Loan;
import com.ccsw.tutorial.loan.model.LoanDto;
import com.ccsw.tutorial.loan.model.LoanSearchDto;
import org.springframework.data.domain.Page;

import java.time.LocalDate;

/**
 * @author ccsw
 *
 */
public interface LoanService {

    /**
     * Recupera un {@link Loan} a través de su ID
     *
     * @param id PK de la entidad
     * @return {@link Loan}
     */
    Loan get(Long id);

    /**
     * Método para recuperar un listado paginado y filtrado de {@link Loan}
     *
     * @param dto dto de búsqueda
     * @param idGame PK del juego (opcional)
     * @param idClient PK del cliente (opcional)
     * @param date fecha que debe estar dentro del periodo del préstamo (opcional)
     * @return {@link Page} de {@link Loan}
     */
    Page<Loan> findPage(LoanSearchDto dto, Long idGame, Long idClient, LocalDate date);

    /**
     * Método para crear o actualizar un {@link Loan}, aplicando las reglas de negocio
     *
     * @param id PK de la entidad
     * @param dto datos de la entidad
     */
    void save(Long id, LoanDto dto) throws Exception;

    /**
     * Método para borrar un {@link Loan}
     *
     * @param id PK de la entidad
     */
    void delete(Long id) throws Exception;

}