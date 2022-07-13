package guru.sfg.beer.order.service.web.controllers;

import guru.sfg.beer.order.service.services.CustomerService;
import guru.sfg.brewery.model.CustomerList;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {
    private final CustomerService customerService;
    @GetMapping
    public CustomerList listCustomers() {
        return customerService.listCustomers();
    }
}
