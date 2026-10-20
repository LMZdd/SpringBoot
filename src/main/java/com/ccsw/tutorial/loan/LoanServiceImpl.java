package com.ccsw.tutorial.loan;

import com.ccsw.tutorial.client.ClientService;
import com.ccsw.tutorial.client.model.Client;
import com.ccsw.tutorial.common.criteria.SearchCriteria;
import com.ccsw.tutorial.game.GameService;
import com.ccsw.tutorial.game.model.Game;
import com.ccsw.tutorial.loan.model.Loan;
import com.ccsw.tutorial.loan.model.LoanDto;
import com.ccsw.tutorial.loan.model.LoanSearchDto;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * @author ccsw
 */
@Service
@Transactional
public class LoanServiceImpl implements LoanService {
    public static final long MAX_LOAN_DAYS = 14;
    public static final long MAX_LOANS_PER_CLIENT_AND_DAY = 2;

    @Autowired
    LoanRepository loanRepository;

    @Autowired
    GameService gameService;

    @Autowired
    ClientService clientService;

    /**
     * {@inheritDoc}
     */
    @Override
    public Loan get(Long id) {

        return this.loanRepository.findById(id).orElse(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<Loan> findPage(LoanSearchDto dto, Long idGame, Long idClient, LocalDate date) {

        Specification<Loan> spec = new LoanSpecification(new SearchCriteria("game.id", ":", idGame)).and(new LoanSpecification(new SearchCriteria("client.id", ":", idClient)))
                .and(new LoanSpecification(new SearchCriteria("startDate", "<=", date))).and(new LoanSpecification(new SearchCriteria("endDate", ">=", date)));

        return this.loanRepository.findAll(spec, dto.getPageable().getPageable());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void save(Long id, LoanDto dto) throws Exception {

        Loan loan;

        if (id == null) {
            loan = new Loan();
        } else {
            loan = this.get(id);
            if (loan == null) {
                throw new Exception("Not exists");
            }
        }

        Game game = this.gameService.get(dto.getGame().getId());
        Client client = this.clientService.get(dto.getClient().getId());

        this.validate(id, game, client, dto.getStartDate(), dto.getEndDate());

        BeanUtils.copyProperties(dto, loan, "id", "game", "client");
        loan.setGame(game);
        loan.setClient(client);

        this.loanRepository.save(loan);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(Long id) throws Exception {

        if (this.get(id) == null) {
            throw new Exception("Not exists");
        }

        this.loanRepository.deleteById(id);
    }

    private void validate(Long id, Game game, Client client, LocalDate start, LocalDate end) {

        if (game == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El juego es obligatorio y debe existir");
        }

        if (client == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El cliente es obligatorio y debe existir");
        }

        if (start == null || end == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Las fechas de inicio y fin son obligatorias");
        }

        if (end.isBefore(start)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha de fin no puede ser anterior a la fecha de inicio");
        }

        if (ChronoUnit.DAYS.between(start, end) > MAX_LOAN_DAYS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El periodo de préstamo no puede ser superior a " + MAX_LOAN_DAYS + " días");
        }

        this.validateGameAvailable(id, game, start, end);
        this.validateClientLimit(id, client, start, end);
    }

    /**
     * El mismo juego no puede estar prestado a dos clientes en un mismo día
     */
    private void validateGameAvailable(Long id, Game game, LocalDate start, LocalDate end) {

        Specification<Loan> spec = this.overlapping(start, end).and(new LoanSpecification(new SearchCriteria("game.id", ":", game.getId())));

        boolean busy = this.loanRepository.findAll(spec).stream().anyMatch(l -> !l.getId().equals(id));

        if (busy) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El juego ya está prestado en alguno de los días del periodo");
        }
    }

    /**
     * Un cliente no puede tener más de 2 juegos prestados en un mismo día
     */
    private void validateClientLimit(Long id, Client client, LocalDate start, LocalDate end) {

        Specification<Loan> spec = this.overlapping(start, end).and(new LoanSpecification(new SearchCriteria("client.id", ":", client.getId())));

        List<Loan> clientLoans = this.loanRepository.findAll(spec).stream().filter(l -> !l.getId().equals(id)).toList();

        for (LocalDate day = start; !day.isAfter(end); day = day.plusDays(1)) {
            final LocalDate currentDay = day;

            long loansThatDay = clientLoans.stream().filter(l -> !l.getStartDate().isAfter(currentDay) && !l.getEndDate().isBefore(currentDay)).count();

            if (loansThatDay >= MAX_LOANS_PER_CLIENT_AND_DAY) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El cliente ya tiene " + MAX_LOANS_PER_CLIENT_AND_DAY + " juegos prestados el día " + currentDay);
            }
        }
    }

    /**
     * Préstamos que solapan con el rango [start, end]: startDate <= end y endDate >= start
     */
    private Specification<Loan> overlapping(LocalDate start, LocalDate end) {

        return new LoanSpecification(new SearchCriteria("startDate", "<=", end)).and(new LoanSpecification(new SearchCriteria("endDate", ">=", start)));
    }
}
