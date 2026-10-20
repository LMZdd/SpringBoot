package com.ccsw.tutorial.loan;

import com.ccsw.tutorial.client.ClientService;
import com.ccsw.tutorial.client.model.Client;
import com.ccsw.tutorial.client.model.ClientDto;
import com.ccsw.tutorial.game.GameService;
import com.ccsw.tutorial.game.model.Game;
import com.ccsw.tutorial.game.model.GameDto;
import com.ccsw.tutorial.loan.model.Loan;
import com.ccsw.tutorial.loan.model.LoanDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoanTest {

    public static final Long GAME_ID = 1L;
    public static final Long CLIENT_ID = 1L;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private GameService gameService;

    @Mock
    private ClientService clientService;

    @InjectMocks
    private LoanServiceImpl loanService;

    @Test
    public void saveWithEndDateBeforeStartDateShouldThrowException() {

        when(gameService.get(GAME_ID)).thenReturn(mock(Game.class));
        when(clientService.get(CLIENT_ID)).thenReturn(mock(Client.class));

        GameDto gameDto = new GameDto();
        gameDto.setId(GAME_ID);

        ClientDto clientDto = new ClientDto();
        clientDto.setId(CLIENT_ID);

        LoanDto loan = new LoanDto();
        loan.setGame(gameDto);
        loan.setClient(clientDto);
        loan.setStartDate(LocalDate.of(2026, 10, 10));
        loan.setEndDate(LocalDate.of(2026, 10, 5));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> loanService.save(null, loan));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("La fecha de fin no puede ser anterior a la fecha de inicio", exception.getReason());
        verify(loanRepository, never()).save(any());
    }

    @Test
    public void saveWithMoreThanFourteenDaysShouldThrowException() {

        when(gameService.get(GAME_ID)).thenReturn(mock(Game.class));
        when(clientService.get(CLIENT_ID)).thenReturn(mock(Client.class));

        GameDto gameDto = new GameDto();
        gameDto.setId(GAME_ID);

        ClientDto clientDto = new ClientDto();
        clientDto.setId(CLIENT_ID);

        LoanDto loan = new LoanDto();
        loan.setGame(gameDto);
        loan.setClient(clientDto);
        loan.setStartDate(LocalDate.of(2026, 10, 1));
        loan.setEndDate(LocalDate.of(2026, 10, 16));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> loanService.save(null, loan));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("El periodo de préstamo no puede ser superior a 14 días", exception.getReason());
        verify(loanRepository, never()).save(any());
    }

    @Test
    public void saveWithGameAlreadyLoanedShouldThrowException() {

        Game game = mock(Game.class);
        when(game.getId()).thenReturn(GAME_ID);
        when(gameService.get(GAME_ID)).thenReturn(game);

        when(clientService.get(CLIENT_ID)).thenReturn(mock(Client.class));

        Loan other = mock(Loan.class);
        when(other.getId()).thenReturn(99L);
        when(loanRepository.findAll(any(Specification.class))).thenReturn(List.of(other));

        GameDto gameDto = new GameDto();
        gameDto.setId(GAME_ID);

        ClientDto clientDto = new ClientDto();
        clientDto.setId(CLIENT_ID);

        LoanDto loan = new LoanDto();
        loan.setGame(gameDto);
        loan.setClient(clientDto);
        loan.setStartDate(LocalDate.of(2026, 10, 1));
        loan.setEndDate(LocalDate.of(2026, 10, 5));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> loanService.save(null, loan));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("El juego ya está prestado en alguno de los días del periodo", exception.getReason());
        verify(loanRepository, never()).save(any());
    }

    @Test
    public void saveWithClientWithTwoLoansSameDayShouldThrowException() {

        Game game = mock(Game.class);
        when(game.getId()).thenReturn(GAME_ID);
        when(gameService.get(GAME_ID)).thenReturn(game);

        Client client = mock(Client.class);
        when(client.getId()).thenReturn(CLIENT_ID);
        when(clientService.get(CLIENT_ID)).thenReturn(client);

        Loan first = mock(Loan.class);
        when(first.getId()).thenReturn(98L);
        when(first.getStartDate()).thenReturn(LocalDate.of(2026, 10, 1));
        when(first.getEndDate()).thenReturn(LocalDate.of(2026, 10, 10));

        Loan second = mock(Loan.class);
        when(second.getId()).thenReturn(99L);
        when(second.getStartDate()).thenReturn(LocalDate.of(2026, 10, 1));
        when(second.getEndDate()).thenReturn(LocalDate.of(2026, 10, 10));

        // 1ª llamada (juego libre) -> vacío; 2ª llamada (préstamos del cliente) -> dos solapados
        when(loanRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList(), List.of(first, second));

        GameDto gameDto = new GameDto();
        gameDto.setId(GAME_ID);

        ClientDto clientDto = new ClientDto();
        clientDto.setId(CLIENT_ID);

        LoanDto loan = new LoanDto();
        loan.setGame(gameDto);
        loan.setClient(clientDto);
        loan.setStartDate(LocalDate.of(2026, 10, 3));
        loan.setEndDate(LocalDate.of(2026, 10, 5));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> loanService.save(null, loan));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("El cliente ya tiene 2 juegos prestados el día 2026-10-03", exception.getReason());
        verify(loanRepository, never()).save(any());
    }
}
