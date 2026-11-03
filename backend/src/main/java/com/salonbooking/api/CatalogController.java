package com.salonbooking.api;

import com.salonbooking.api.dto.ServiceResponse;
import com.salonbooking.api.dto.StylistResponse;
import com.salonbooking.repository.SalonServiceRepository;
import com.salonbooking.repository.StylistRepository;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CatalogController {

    private final StylistRepository stylistRepository;
    private final SalonServiceRepository salonServiceRepository;

    public CatalogController(StylistRepository stylistRepository, SalonServiceRepository salonServiceRepository) {
        this.stylistRepository = stylistRepository;
        this.salonServiceRepository = salonServiceRepository;
    }

    // GET /api/stylists - SALON-04 View Salon Stylists
    @GetMapping("/api/stylists")
    public List<StylistResponse> listStylists() {
        return stylistRepository.findAll(Sort.by("id")).stream().map(StylistResponse::from).toList();
    }

    // GET /api/services - SALON-03 Browse Salon Services
    @GetMapping("/api/services")
    public List<ServiceResponse> listServices() {
        return salonServiceRepository.findAll(Sort.by("id")).stream().map(ServiceResponse::from).toList();
    }
}
