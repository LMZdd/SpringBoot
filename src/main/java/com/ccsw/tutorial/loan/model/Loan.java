package com.ccsw.tutorial.loan.model;

import com.ccsw.tutorial.client.model.Client;
import com.ccsw.tutorial.game.model.Game;
import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * @author ccsw
 */
@Entity
@Table(name = "loan")
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    /**
     * @return id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id new value of {@link #getId}.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return client
     */
    public Client getClient() {
        return client;
    }

    /**
     * @param client new value of {@link #getClient()}.
     */
    public void setClient(Client client) {
        this.client = client;
    }

    /**
     * @return client
     */
    public Game getGame() {
        return game;
    }

    /**
     * @param game new value of {@link #getGame()}.
     */
    public void setGame(Game game) {
        this.game = game;
    }

    /**
     * @return client
     */
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * @param startDate new value of {@link #getStartDate()}.
     */
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    /**
     * @return client
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * @param endDate new value of {@link #getEndDate()}.
     */
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
