package com.digital.magazine.subscription.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import com.digital.magazine.common.exception.DuplicateAddressException;
import com.digital.magazine.common.exception.UserNotFoundException;
import com.digital.magazine.subscription.dto.SaveAddressRequestDto;
import com.digital.magazine.subscription.dto.UserAddressResponseDto;
import com.digital.magazine.subscription.entity.UserAddress;
import com.digital.magazine.subscription.service.impl.UserAddressServiceImpl;
import com.digital.magazine.subscription.repository.UserAddressRepository;
import com.digital.magazine.user.entity.User;
import com.digital.magazine.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserAddressServiceImplTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private UserAddressRepository addressRepo;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserAddressServiceImpl service;

    private User user;

    @BeforeEach
    void setup() {
        user = User.builder()
                .id(1L)
                .name("Test User")
                .email("user@test.com")
                .build();

        when(authentication.getName()).thenReturn("user@test.com");
    }

    // ✅ saveAddress success
    @Test
    void saveAddress_success() {

		SaveAddressRequestDto dto = SaveAddressRequestDto.builder()
                .name("Home")
                .addressLine("Street 1")
                .city("Chennai")
                .state("TN")
                .pincode("600001")
                .mobile("9999999999")
                .defaultAddress(true)
                .build();

        when(userRepo.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(addressRepo.existsByUserAndAddressLineAndCityAndStateAndPincode(
                any(), any(), any(), any(), any()))
                .thenReturn(false);

        UserAddress saved = UserAddress.builder()
                .id(10L)
                .user(user)
                .name("Home")
                .defaultAddress(true)
                .build();

        when(addressRepo.save(any())).thenReturn(saved);

        UserAddressResponseDto response = service.saveAddress(dto, authentication);

        assertNotNull(response);
        assertEquals("Home", response.getName());
        verify(addressRepo).save(any(UserAddress.class));
    }

    // ❌ saveAddress → user not found
    @Test
    void saveAddress_userNotFound() {

        when(userRepo.findByEmail("user@test.com"))
                .thenReturn(Optional.empty());

        SaveAddressRequestDto dto = SaveAddressRequestDto.builder()
                .addressLine("Street")
                .city("Chennai")
                .state("TN")
                .pincode("600001")
                .build();

        assertThrows(
                UserNotFoundException.class,
                () -> service.saveAddress(dto, authentication)
        );
    }

    // ❌ saveAddress → duplicate address
    @Test
    void saveAddress_duplicateAddress() {

        when(userRepo.findByEmail("user@test.com"))
                .thenReturn(Optional.of(user));

        when(addressRepo.existsByUserAndAddressLineAndCityAndStateAndPincode(
                any(), any(), any(), any(), any()))
                .thenReturn(true);

        SaveAddressRequestDto dto = SaveAddressRequestDto.builder()
                .addressLine("Street 1")
                .city("Chennai")
                .state("TN")
                .pincode("600001")
                .build();

        assertThrows(
                DuplicateAddressException.class,
                () -> service.saveAddress(dto, authentication)
        );
    }

    // ✅ default address → reset old defaults
    @Test
    void saveAddress_defaultAddress_resetsOld() {

        when(userRepo.findByEmail("user@test.com"))
                .thenReturn(Optional.of(user));

        when(addressRepo.existsByUserAndAddressLineAndCityAndStateAndPincode(
                any(), any(), any(), any(), any()))
                .thenReturn(false);

        UserAddress old = UserAddress.builder()
                .id(1L)
                .defaultAddress(true)
                .build();

        when(addressRepo.findByUser(user))
                .thenReturn(List.of(old));

        when(addressRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        SaveAddressRequestDto dto = SaveAddressRequestDto.builder()
                .addressLine("New Street")
                .city("Chennai")
                .state("TN")
                .pincode("600002")
                .defaultAddress(true)
                .build();

        service.saveAddress(dto, authentication);

        assertFalse(old.isDefaultAddress());
        verify(addressRepo).saveAll(anyList());
    }

    // ✅ getMyAddresses success
    @Test
    void getMyAddresses_success() {

        when(userRepo.findByEmail("user@test.com"))
                .thenReturn(Optional.of(user));

        when(addressRepo.findByUser(user))
                .thenReturn(List.of(
                        UserAddress.builder().id(1L).name("Home").build(),
                        UserAddress.builder().id(2L).name("Office").build()
                ));

        List<UserAddressResponseDto> result =
                service.getMyAddresses(authentication);

        assertEquals(2, result.size());
    }

    // ❌ getMyAddresses → user not found
    @Test
    void getMyAddresses_userNotFound() {

        when(userRepo.findByEmail("user@test.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> service.getMyAddresses(authentication)
        );
    }
}
