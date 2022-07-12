package guru.sfg.beer.order.service.services;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.brewery.model.BeerOrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BeerOrderManagerImpl implements BeerOrderManager {

    public static final String ORDER_ID_HEADER = "ORDER_ID_HEADER";
    private final StateMachineFactory<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachineFactory;

    private final BeerOrderRepository beerOrderRepository;

    private final BeerOrderStateChangeInterceptor beerOrderStateChangeInterceptor;

    @Transactional
    @Override
    public BeerOrder newBeerOrder(BeerOrder beerOrder) {
        beerOrder.setId(null);
        beerOrder.setOrderStatus(BeerOrderStatusEnum.NEW);
        BeerOrder savedBeerOrder = beerOrderRepository.save(beerOrder);
        sendBeerOrderEvent(savedBeerOrder, BeerOrderEventEnum.VALIDATE_ORDER);
        return savedBeerOrder;
    }

    @Override
    @Transactional
    public void processValidationResult(UUID beerOrderId, Boolean isValid) {
        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(beerOrderId);
        beerOrderOptional.ifPresentOrElse(beerOrder -> {
            if (isValid){
                sendBeerOrderEvent(beerOrder,BeerOrderEventEnum.VALIDATION_PASSED);

                BeerOrder validateOrder = beerOrderRepository.findById(beerOrderId).get();
                sendBeerOrderEvent(validateOrder,BeerOrderEventEnum.ALLOCATE_ORDER);
            }else {
                sendBeerOrderEvent(beerOrder,BeerOrderEventEnum.VALIDATION_FAILED);
            }
        },() -> log.error("Order Not Found Id:"+ beerOrderId));


    }

    @Override
    public void beerOrderAllocationPassed(BeerOrderDto beerOrderDto) {
        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(beerOrderDto.getId());
        beerOrderOptional.ifPresentOrElse(beerOrder -> {
            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_SUCCESS);
            updateAllocatedQty(beerOrderDto);
        }, () -> log.error("Order Not Found Id:" + beerOrderDto.getId()));

    }

    @Override
    public void beerOrderAllocationPendingInventory(BeerOrderDto beerOrderDto) {
        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(beerOrderDto.getId());
        beerOrderOptional.ifPresentOrElse( beerOrder -> {
                    sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_NO_INVENTORY);
                    updateAllocatedQty(beerOrderDto);
                }
                ,()->log.error("Order Not Found Id:" + beerOrderDto.getId()));

    }

    private void updateAllocatedQty(BeerOrderDto beerOrderDto) {
        Optional<BeerOrder> allocatedOrderOptional = beerOrderRepository.findById(beerOrderDto.getId());

        allocatedOrderOptional.ifPresentOrElse(
                allocatedOrder -> {
                    allocatedOrder.getBeerOrderLines().forEach(beerOrderLine -> {
                        beerOrderDto.getBeerOrderLines().forEach(beerOrderLineDto -> {
                            if (beerOrderLine.getId().equals(beerOrderLineDto.getId())) {
                                beerOrderLine.setQuantityAllocated(beerOrderLineDto.getQuantityAllocated());
                            }
                        });
                    });
                    beerOrderRepository.saveAndFlush(allocatedOrder);
                },() -> log.error("Order Not Found Id:" + beerOrderDto.getId()));

    }

    @Override
    public void beerOrderAllocationFailed(BeerOrderDto beerOrderDto) {
        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(beerOrderDto.getId());
        beerOrderOptional.ifPresentOrElse(beerOrder -> {
            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_FAILED);
        },() -> log.error("Order Not Found Id:" + beerOrderDto.getId()));

    }

    @Override
    public void beerOrderPickedUp(UUID beerOrderId) {
        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(beerOrderId);
        beerOrderOptional.ifPresentOrElse(beerOrder -> {
            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.BEERORDER_PICKED_UP);
        },() -> log.error("Order Not Found Id:" + beerOrderId));
    }


    private void sendBeerOrderEvent(BeerOrder beerOrder, BeerOrderEventEnum eventEnum) {
        StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> sm = build(beerOrder);

        Message msg = MessageBuilder.withPayload(eventEnum)
                .setHeader(ORDER_ID_HEADER,beerOrder.getId().toString())
                .build();
        sm.sendEvent(msg);

    }

    private StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> build(BeerOrder beerOrder) {
        StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> sm = stateMachineFactory.getStateMachine(beerOrder.getId());

        sm.stop();

        sm.getStateMachineAccessor()
                .doWithAllRegions(sma -> {
                    sma.addStateMachineInterceptor(beerOrderStateChangeInterceptor);
                    sma.resetStateMachine(new DefaultStateMachineContext<>(beerOrder.getOrderStatus(),null,null,null));

                });
        sm.start();
        return sm;
    }
}
