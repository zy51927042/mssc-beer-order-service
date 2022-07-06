package guru.sfg.beer.order.service.services.beer;

import guru.sfg.beer.order.service.services.beer.model.BeerDto;

import java.util.Optional;
import java.util.UUID;

public interface BeerService {
    Optional<BeerDto> getBeerDtoByUpc(String upc);

    Optional<BeerDto> getBeerDtoById(UUID uuid);
}
