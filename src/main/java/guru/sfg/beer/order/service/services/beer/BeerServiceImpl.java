package guru.sfg.beer.order.service.services.beer;

import guru.sfg.beer.order.service.services.beer.model.BeerDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@ConfigurationProperties(prefix = "sfg.brewery", ignoreUnknownFields = false)
@Service
public class BeerServiceImpl implements BeerService {
    private final String BEER_UPC_PATH_V1 = "/api/v1/beerUpc/";
    private final String BEER_PATH_V1 = "/api/v1/beer/";

    private final RestTemplate restTemplate;
    private String beerServiceHost;

    public BeerServiceImpl(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public void setBeerServiceHost(String beerServiceHost) {
        this.beerServiceHost = beerServiceHost;
    }

    @Override
    public Optional<BeerDto> getBeerDtoByUpc(String upc) {
        log.debug("Calling Beer Service"+beerServiceHost+BEER_UPC_PATH_V1+upc);
        return Optional.of(restTemplate.getForObject(beerServiceHost+BEER_UPC_PATH_V1+upc,BeerDto.class));

    }

    @Override
    public Optional<BeerDto> getBeerDtoById(UUID uuid) {
        return Optional.of(restTemplate.getForObject(beerServiceHost+BEER_PATH_V1+uuid.toString(),BeerDto.class));
    }

}
