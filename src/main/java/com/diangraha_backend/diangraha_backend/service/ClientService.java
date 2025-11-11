package com.diangraha_backend.diangraha_backend.service;
import com.diangraha_backend.diangraha_backend.dto.*;
import com.diangraha_backend.diangraha_backend.entity.Brand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.diangraha_backend.diangraha_backend.repository.ClientRepository;
import com.diangraha_backend.diangraha_backend.entity.Client;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientService {
    private final ClientRepository clientRepository;
    private final FileStorageService fileStorageService;

    public List<ClientResponse> findAll(){
        return clientRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ClientResponse create(ClientRequest request, MultipartFile imageUrl) throws IOException {
            Client client = new Client();
            client.setName(request.getName());

            if (imageUrl != null && !imageUrl.isEmpty()) {
                String logoPath = fileStorageService.storeFile(imageUrl, "clients");
                client.setImageUrl(logoPath);
            }

            Client saved = clientRepository.save(client);
            return mapToResponse(saved);
        }

    public ClientResponse findById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Brand not found"));
        return mapToResponse(client);
    }

    public ClientResponse update(Long id, ClientRequest request, MultipartFile imageUrl) throws IOException {
            Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Brand not found"));

        client.setName(request.getName());

        if (imageUrl != null && !imageUrl.isEmpty()) {
            String logoPath = fileStorageService.storeFile(imageUrl, "clients");
            client.setImageUrl(logoPath);
        }

        Client updated = clientRepository.save(client);
        return mapToResponse(updated);
    }
    public void delete(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new RuntimeException("Brand not found");
        }
        clientRepository.deleteById(id);
    }

    private ClientResponse mapToResponse(Client client) {
        return ClientResponse.builder()
                .id(client.getId())
                .name(client.getName())
                .imageUrl(client.getImageUrl())
                .createdAt(client.getCreatedAt())
                .build();
    }
}
