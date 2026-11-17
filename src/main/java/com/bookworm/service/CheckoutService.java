package com.bookworm.service;

import com.bookworm.dto.DTOs.*;
import com.bookworm.model.Address;
import com.bookworm.model.GiftPoints;
import com.bookworm.model.User;
import com.bookworm.repository.AddressRepository;
import com.bookworm.repository.GiftPointsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CheckoutService {

    private final AddressRepository addressRepository;
    private final GiftPointsRepository giftPointsRepository;

    public CheckoutService(AddressRepository addressRepository,
                           GiftPointsRepository giftPointsRepository) {
        this.addressRepository = addressRepository;
        this.giftPointsRepository = giftPointsRepository;
    }

    public List<AddressDto> getUserAddresses(User user) {
        return addressRepository.findByUserId(user.getId()).stream()
                .map(this::mapToAddressDto)
                .toList();
    }

    @Transactional
    public AddressDto createAddress(User user, AddressRequest req) {
        Address address = new Address();
        address.setUser(user);
        address.setRecipientName(req.recipientName());
        address.setPhone(req.phone());
        address.setStreet(req.street());
        address.setCity(req.city());
        address.setState(req.state());
        address.setPostalCode(req.postalCode());
        address.setCountry(req.country() != null ? req.country() : "India");
        address.setIsDefault(req.isDefault() != null ? req.isDefault() : false);

        address = addressRepository.save(address);
        return mapToAddressDto(address);
    }

    public GiftPointsSummaryDto getGiftPointsSummary(User user) {
        GiftPoints gp = giftPointsRepository.findByUserId(user.getId())
                .orElseGet(() -> giftPointsRepository.save(new GiftPoints(user, 100)));

        return new GiftPointsSummaryDto(
                gp.getAvailablePoints(),
                gp.getRedemptionValueInINR(),
                gp.getConversionRate(),
                Math.min(gp.getAvailablePoints(), 500)
        );
    }

    public AddressDto mapToAddressDto(Address a) {
        return new AddressDto(
                a.getId(),
                a.getRecipientName(),
                a.getPhone(),
                a.getStreet(),
                a.getCity(),
                a.getState(),
                a.getPostalCode(),
                a.getCountry(),
                a.getIsDefault()
        );
    }
}
