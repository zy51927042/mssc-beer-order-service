package guru.sfg.beer.order.service.web.mappers;

import guru.sfg.beer.order.service.domain.Customer;
import guru.sfg.brewery.model.CustomerDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {DateMapper.class,BeerOrderMapper.class})
public interface CustomerMapper {
    @Mapping(target = "name", source = "customerName")
    CustomerDto customerToCustomerDto(Customer customer);
}
