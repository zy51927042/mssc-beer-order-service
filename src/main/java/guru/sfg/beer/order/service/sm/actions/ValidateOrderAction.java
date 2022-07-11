package guru.sfg.beer.order.service.sm.actions;

import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.services.BeerOrderManagerImpl;
import guru.sfg.beer.order.service.web.mappers.BeerOrderMapper;
import guru.sfg.brewery.model.BeerOrderDto;
import guru.sfg.brewery.model.events.ValidateOrderRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class ValidateOrderAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum>  {
    private final BeerOrderRepository beerOrderRepository;
    private final BeerOrderMapper beerOrderMapper;

    private final JmsTemplate jmsTemplate;
    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> stateContext) {
        String orderId = (String) stateContext.getMessageHeader(BeerOrderManagerImpl.ORDER_ID_HEADER);
        BeerOrder beerOrder= beerOrderRepository.findById(UUID.fromString(orderId)).get();
        BeerOrderDto beerOrderDto = beerOrderMapper.beerOrderToDto(beerOrder);

        jmsTemplate.convertAndSend(JmsConfig.VALIDATE_ORDER_QUEUE,
                ValidateOrderRequest.builder().beerOrderDto(beerOrderDto).build());
        log.debug("Send Validation request to queue for order id" + orderId);
    }
}
