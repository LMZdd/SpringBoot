package com.ccsw.tutorial.client;

import com.ccsw.tutorial.client.model.Client;
import com.ccsw.tutorial.client.model.ClientDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientTest {
    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientServiceImpl clientService;

    public static final String CLIENT_NAME = "NOM1";
    public static final Long EXISTS_CLIENT_ID = 1L;
    public static final String EXISTING_NAME = "REP_NOM";

    @Test
    public void findAllShouldReturnAllClients() {

        List<Client> list = new ArrayList<>();
        list.add(mock(Client.class));

        when(clientRepository.findAll()).thenReturn(list);

        List<Client> clients = clientService.findAll();

        assertNotNull(clients);
        assertEquals(1, clients.size());
    }

    @Test
    public void saveNotExistsClientIdShouldInsert() throws Exception {

        ClientDto clientDto = new ClientDto();
        clientDto.setName(CLIENT_NAME);

        ArgumentCaptor<Client> client = ArgumentCaptor.forClass(Client.class);

        clientService.save(null, clientDto);

        verify(clientRepository).save(client.capture());

        assertEquals(CLIENT_NAME, client.getValue().getName());
    }

    @Test
    public void saveExistsClientIdShouldUpdate() throws Exception {

        ClientDto clientDto = new ClientDto();
        clientDto.setName(CLIENT_NAME);

        Client client = mock(Client.class);
        when(clientRepository.findById(EXISTS_CLIENT_ID)).thenReturn(Optional.of(client));

        clientService.save(EXISTS_CLIENT_ID, clientDto);

        verify(clientRepository).save(client);
    }

    @Test
    public void deleteExistsClientIdShouldDelete() throws Exception {

        Client client = mock(Client.class);
        when(clientRepository.findById(EXISTS_CLIENT_ID)).thenReturn(Optional.of(client));

        clientService.delete(EXISTS_CLIENT_ID);

        verify(clientRepository).deleteById(EXISTS_CLIENT_ID);
    }

    @Test
    public void saveWithExistingNameShouldThrowException() throws Exception {
        ClientDto clientDto = new ClientDto();
        clientDto.setName(EXISTING_NAME);

        when(clientRepository.countByName(EXISTING_NAME)).thenReturn(1L);

        assertThrows(Exception.class, () -> clientService.save(null, clientDto));

        verify(clientRepository, never()).save(any());
    }
}