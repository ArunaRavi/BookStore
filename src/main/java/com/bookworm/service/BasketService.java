package com.bookworm.service;

import com.bookworm.dto.DTOs.*;
import com.bookworm.model.*;
import com.bookworm.repository.AddressRepository;
import com.bookworm.repository.BasketRepository;
import com.bookworm.repository.GiftPointsRepository;
import com.bookworm.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class BasketService {

    private final BasketRepository basketRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;
    private final GiftPointsRepository giftPointsRepository;
    private final CatalogService catalogService;

    public BasketService(BasketRepository basketRepository,
                         ProductRepository productRepository,
                         AddressRepository addressRepository,
                         GiftPointsRepository giftPointsRepository,
                         CatalogService catalogService) {
        this.basketRepository = basketRepository;
        this.productRepository = productRepository;
        this.addressRepository = addressRepository;
        this.giftPointsRepository = giftPointsRepository;
        this.catalogService = catalogService;
    }

    public Basket getOrCreateBasket(User user) {
        return basketRepository.findByUserId(user.getId())
                .orElseGet(() -> basketRepository.save(new Basket(user)));
    }

    public BasketResponse getBasketResponse(User user) {
        Basket basket = getOrCreateBasket(user);
        return mapToBasketResponse(basket);
    }

    @Transactional
    public BasketResponse addToBasket(User user, AddBasketItemRequest req) {
        Basket basket = getOrCreateBasket(user);
        Product product = productRepository.findById(req.productId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + req.productId()));

        Optional<BasketItem> existing = basket.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(product.getId()))
                .findFirst();

        int qtyToAdd = (req.quantity() != null && req.quantity() > 0) ? req.quantity() : 1;

        if (existing.isPresent()) {
            BasketItem item = existing.get();
            item.setQuantity(item.getQuantity() + qtyToAdd);
        } else {
            BasketItem newItem = new BasketItem(basket, product, qtyToAdd, product.getPrice());
            basket.getItems().add(newItem);
        }

        basket = basketRepository.save(basket);
        return mapToBasketResponse(basket);
    }

    @Transactional
    public BasketResponse updateItemQuantity(User user, String itemId, int quantity) {
        Basket basket = getOrCreateBasket(user);
        BasketItem item = basket.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Basket item not found with id: " + itemId));

        if (quantity <= 0) {
            basket.getItems().remove(item);
        } else {
            item.setQuantity(quantity);
        }

        basket = basketRepository.save(basket);
        return mapToBasketResponse(basket);
    }

    @Transactional
    public BasketResponse removeItem(User user, String itemId) {
        return updateItemQuantity(user, itemId, 0);
    }

    @Transactional
    public BasketResponse setDeliveryAddress(User user, String addressId) {
        Basket basket = getOrCreateBasket(user);
        Address address = addressRepository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Address not found or does not belong to user"));

        basket.setSelectedDeliveryAddress(address);
        basket = basketRepository.save(basket);
        return mapToBasketResponse(basket);
    }

    @Transactional
    public BasketResponse redeemPoints(User user, int pointsToRedeem) {
        Basket basket = getOrCreateBasket(user);
        GiftPoints giftPoints = giftPointsRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("No gift points account found"));

        if (pointsToRedeem > giftPoints.getAvailablePoints()) {
            throw new IllegalArgumentException("Insufficient gift points. Available: " + giftPoints.getAvailablePoints());
        }

        BigDecimal discount = giftPoints.getPointValueInInr().multiply(BigDecimal.valueOf(pointsToRedeem));
        if (discount.compareTo(basket.getSubtotal()) > 0) {
            throw new IllegalArgumentException("Redemption amount cannot exceed basket subtotal.");
        }

        basket.setGiftPointsRedeemed(pointsToRedeem);
        basket.setDiscountAmount(discount);
        basket = basketRepository.save(basket);
        return mapToBasketResponse(basket);
    }

    public BasketResponse mapToBasketResponse(Basket b) {
        var items = b.getItems().stream().map(item -> new BasketItemDto(
                item.getId(),
                catalogService.mapToSummaryDto(item.getProduct()),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getLineTotal()
        )).toList();

        AddressDto addrDto = b.getSelectedDeliveryAddress() != null 
                ? new AddressDto(
                b.getSelectedDeliveryAddress().getId(),
                b.getSelectedDeliveryAddress().getRecipientName(),
                b.getSelectedDeliveryAddress().getPhone(),
                b.getSelectedDeliveryAddress().getStreet(),
                b.getSelectedDeliveryAddress().getCity(),
                b.getSelectedDeliveryAddress().getState(),
                b.getSelectedDeliveryAddress().getPostalCode(),
                b.getSelectedDeliveryAddress().getCountry(),
                b.getSelectedDeliveryAddress().getIsDefault()
        ) : null;

        return new BasketResponse(
                b.getId(),
                b.getTotalItemCount(),
                items,
                b.getSubtotal(),
                b.getDiscountAmount(),
                b.getGiftPointsRedeemed(),
                b.getDeliveryFee(),
                b.getTotalAmount(),
                b.getCurrency(),
                b.getCurrencySymbol(),
                addrDto
        );
    }
}
