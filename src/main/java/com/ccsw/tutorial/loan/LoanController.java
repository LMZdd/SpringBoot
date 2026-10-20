package com.ccsw.tutorial.loan;

import com.ccsw.tutorial.loan.model.Loan;
import com.ccsw.tutorial.loan.model.LoanDto;
import com.ccsw.tutorial.loan.model.LoanSearchDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.stream.Collectors;

/**
 * @author ccsw
 */
@Tag(name = "Loan", description = "API of Loan")
@RequestMapping(value = "/loan")
@RestController
@CrossOrigin(origins = "*")
public class LoanController {
    @Autowired
    LoanService loanService;

    @Autowired
    ModelMapper mapper;

    /**
     * Método para recuperar un listado paginado y filtrado de {@link Loan}
     *
     * @param dto dto de búsqueda
     * @param idGame PK del juego
     * @param idClient PK del cliente
     * @param date fecha que debe estar dentro del periodo del préstamo
     * @return {@link Page} de {@link LoanDto}
     */
    @Operation(summary = "Find Page", description = "Method that return a filtered page of Loans")
    @RequestMapping(path = "", method = RequestMethod.POST)
    public Page<LoanDto> findPage(@RequestBody LoanSearchDto dto, @RequestParam(value = "idGame", required = false) Long idGame, @RequestParam(value = "idClient", required = false) Long idClient,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        Page<Loan> page = this.loanService.findPage(dto, idGame, idClient, date);

        return new PageImpl<>(page.getContent().stream().map(e -> mapper.map(e, LoanDto.class)).collect(Collectors.toList()), page.getPageable(), page.getTotalElements());
    }

    /**
     * Método para crear o actualizar un {@link Loan}
     *
     * @param id PK de la entidad
     * @param dto datos de la entidad
     */
    @Operation(summary = "Save or Update", description = "Method that saves or updates a Loan")
    @RequestMapping(path = { "", "/{id}" }, method = RequestMethod.PUT)
    public void save(@PathVariable(name = "id", required = false) Long id, @RequestBody LoanDto dto) throws Exception {

        this.loanService.save(id, dto);
    }

    /**
     * Método para borrar un {@link Loan}
     *
     * @param id PK de la entidad
     */
    @Operation(summary = "Delete", description = "Method that deletes a Loan")
    @RequestMapping(path = "/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable("id") Long id) throws Exception {

        this.loanService.delete(id);
    }
}
