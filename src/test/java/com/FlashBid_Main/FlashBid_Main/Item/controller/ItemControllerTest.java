package com.FlashBid_Main.FlashBid_Main.Item.controller;

import com.FlashBid_Main.FlashBid_Main.Item.dto.ItemRegistrationDto;
import com.FlashBid_Main.FlashBid_Main.Item.service.ItemService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
@SpringBootTest
@AutoConfigureMockMvc
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItemService itemService;


    @Nested
    @DisplayName("POST /api/items/register - 인증된 사용자")
    class RegisterEndpointWithAuthTest {

        @Test
        @DisplayName("물품 등록 성공 - 200 OK 및 성공 메시지 반환")
        void register_Success_Returns200() throws Exception {
            UserDetails testUser = User.withUsername("test@example.com")
                .password("password")
                .roles("USER")
                .build();

            when(itemService.registerItem(any(ItemRegistrationDto.class), any()))
                .thenReturn(1L);

            MockMultipartFile image = new MockMultipartFile(
                "images", "test.jpg", "image/jpeg", "test image".getBytes()
            );

            mockMvc.perform(multipart("/api/items/register")
                    .file(image)
                    .param("itemName", "테스트 상품")
                    .param("description", "테스트 상품 설명")
                    .param("startPrice", "10000")
                    .param("durationHour", "24")
                    .param("category", "ELECTRONICS")
                    .with(user("test@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().string("물품 등록 성공! ID: 1"));
        }

        @Test
        @DisplayName("물품 등록 성공 - 이미지 없이도 등록 가능")
        void register_WithoutImages_Success() throws Exception {
            when(itemService.registerItem(any(ItemRegistrationDto.class), any()))
                .thenReturn(2L);

            mockMvc.perform(multipart("/api/items/register")
                    .param("itemName", "이미지 없는 상품")
                    .param("description", "이미지 없는 상품 설명")
                    .param("startPrice", "5000")
                    .param("durationHour", "12")
                    .param("category", "ELECTRONICS")
                    .with(user("test@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().string("물품 등록 성공! ID: 2"));
        }

        @Test
        @DisplayName("물품 등록 실패 - IOException 발생 시 500 반환")
        void register_IOExceptionThrown_Returns500() throws Exception {
            when(itemService.registerItem(any(ItemRegistrationDto.class), any()))
                .thenThrow(new IOException("이미지 업로드 실패"));

            MockMultipartFile image = new MockMultipartFile(
                "images", "test.jpg", "image/jpeg", "test image".getBytes()
            );

            mockMvc.perform(multipart("/api/items/register")
                    .file(image)
                    .param("itemName", "테스트 상품")
                    .param("description", "테스트 상품 설명")
                    .param("startPrice", "10000")
                    .param("durationHour", "24")
                    .param("category", "ELECTRONICS")
                    .with(user("test@example.com").roles("USER")))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("등록 실패: 이미지 업로드 실패"));
        }

        @Test
        @DisplayName("multipart/form-data 바인딩 검증 - 여러 이미지 전송")
        void register_MultipleImages_Binding() throws Exception {
            when(itemService.registerItem(any(ItemRegistrationDto.class), any()))
                .thenReturn(3L);

            MockMultipartFile image1 = new MockMultipartFile(
                "images", "image1.jpg", "image/jpeg", "image 1".getBytes()
            );
            MockMultipartFile image2 = new MockMultipartFile(
                "images", "image2.jpg", "image/jpeg", "image 2".getBytes()
            );
            MockMultipartFile image3 = new MockMultipartFile(
                "images", "image3.jpg", "image/jpeg", "image 3".getBytes()
            );

            mockMvc.perform(multipart("/api/items/register")
                    .file(image1)
                    .file(image2)
                    .file(image3)
                    .param("itemName", "다중 이미지 상품")
                    .param("description", "여러 이미지가 있는 상품")
                    .param("startPrice", "30000")
                    .param("durationHour", "48")
                    .param("category", "ELECTRONICS")
                    .with(user("test@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().string("물품 등록 성공! ID: 3"));
        }

        @Test
        @DisplayName("다양한 durationHour 값 테스트")
        void register_DifferentDurationHours() throws Exception {
            when(itemService.registerItem(any(ItemRegistrationDto.class), any()))
                .thenReturn(4L);

            mockMvc.perform(multipart("/api/items/register")
                    .param("itemName", "단기 경매 상품")
                    .param("description", "1시간 경매")
                    .param("startPrice", "1000")
                    .param("durationHour", "1")
                    .param("category", "ELECTRONICS")
                    .with(user("test@example.com").roles("USER")))
                .andExpect(status().isOk());

            mockMvc.perform(multipart("/api/items/register")
                    .param("itemName", "장기 경매 상품")
                    .param("description", "168시간(1주일) 경매")
                    .param("startPrice", "100000")
                    .param("durationHour", "168")
                    .param("category", "ELECTRONICS")
                    .with(user("test@example.com").roles("USER")))
                .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("POST /api/items/register - 미인증 사용자")
    class RegisterEndpointWithoutAuthTest {

        @Test
        @DisplayName("인증되지 않은 요청 시 접근 거부")
        void register_Unauthorized_Returns401Or403() throws Exception {
            MockMultipartFile image = new MockMultipartFile(
                "images", "test.jpg", "image/jpeg", "test image".getBytes()
            );

            mockMvc.perform(multipart("/api/items/register")
                    .file(image)
                    .param("itemName", "테스트 상품")
                    .param("description", "테스트 상품 설명")
                    .param("startPrice", "10000")
                    .param("durationHour", "24")
                    .param("category", "ELECTRONICS"))
                .andExpect(status().is4xxClientError());
        }
    }
}
