package guru.sfg.beer.order.service.services;

import guru.sfg.beer.order.service.domain.Customer;
import guru.sfg.beer.order.service.repositories.CustomerRepository;
import guru.sfg.beer.order.service.web.mappers.CustomerMapper;
import guru.sfg.brewery.model.CustomerList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    private final CustomerMapper mapper;
    @Override
    public CustomerList listCustomers() {
        List<Customer> customerList = customerRepository.findAll();

        return CustomerList
                .builder()
                .customers(customerList
                        .stream()
                        .map(mapper::customerToCustomerDto)
                        .collect(Collectors.toList()))
                .build();
    }
}
