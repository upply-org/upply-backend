package com.upply.socialLink;

import com.upply.profile.socialLink.*;
import com.upply.profile.socialLink.dto.SocialLinkMapper;
import com.upply.profile.socialLink.dto.SocialLinkRequest;
import com.upply.profile.socialLink.dto.SocialLinkResponse;
import com.upply.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SocialLinkMapper unit tests")
class SocialLinkMapperTest {

    @InjectMocks
    private SocialLinkMapper socialLinkMapper;

    private SocialLinkRequest testSocialLinkRequest;
    private SocialLink testSocialLink;

    @BeforeEach
    void setUp() {
        testSocialLinkRequest = new SocialLinkRequest(
                "https://github.com/user",
                SocialType.GITHUB
        );

        testSocialLink = SocialLink.builder()
                .id(1L)
                .url("https://github.com/user")
                .socialType(SocialType.GITHUB)
                .build();
    }

    @Test
    @DisplayName("toSocialLink should map SocialLinkRequest to SocialLink with all fields")
    void shouldMapSocialLinkRequestToSocialLinkWithAllFields() {
        SocialLink result = socialLinkMapper.toSocialLink(testSocialLinkRequest);

        assertNotNull(result);
        assertEquals("https://github.com/user", result.getUrl());
        assertEquals(SocialType.GITHUB, result.getSocialType());
    }

    @Test
    @DisplayName("toSocialLink should map SocialLinkRequest with different social types")
    void shouldMapSocialLinkRequestWithDifferentSocialTypes() {
        SocialLinkRequest linkedInRequest = new SocialLinkRequest(
                "https://linkedin.com/in/user",
                SocialType.LINKEDIN
        );

        SocialLink result = socialLinkMapper.toSocialLink(linkedInRequest);

        assertNotNull(result);
        assertEquals("https://linkedin.com/in/user", result.getUrl());
        assertEquals(SocialType.LINKEDIN, result.getSocialType());
    }

    @Test
    @DisplayName("toSocialLink should map SocialLinkRequest with Twitter social type")
    void shouldMapSocialLinkRequestWithTwitter() {
        SocialLinkRequest twitterRequest = new SocialLinkRequest(
                "https://twitter.com/user",
                SocialType.TWITTER
        );

        SocialLink result = socialLinkMapper.toSocialLink(twitterRequest);

        assertNotNull(result);
        assertEquals("https://twitter.com/user", result.getUrl());
        assertEquals(SocialType.TWITTER, result.getSocialType());
    }

    @Test
    @DisplayName("toSocialLink should map SocialLinkRequest with Portfolio social type")
    void shouldMapSocialLinkRequestWithPortfolio() {
        SocialLinkRequest portfolioRequest = new SocialLinkRequest(
                "https://portfolio.example.com",
                SocialType.PORTFOLIO
        );

        SocialLink result = socialLinkMapper.toSocialLink(portfolioRequest);

        assertNotNull(result);
        assertEquals("https://portfolio.example.com", result.getUrl());
        assertEquals(SocialType.PORTFOLIO, result.getSocialType());
    }

    @Test
    @DisplayName("toSocialLink should map SocialLinkRequest with empty URL")
    void shouldMapSocialLinkRequestWithEmptyUrl() {
        SocialLinkRequest requestWithEmptyUrl = new SocialLinkRequest(
                "",
                SocialType.GITHUB
        );

        SocialLink result = socialLinkMapper.toSocialLink(requestWithEmptyUrl);

        assertNotNull(result);
        assertEquals("", result.getUrl());
        assertEquals(SocialType.GITHUB, result.getSocialType());
    }

    @Test
    @DisplayName("toSocialLink should map SocialLinkRequest with null URL")
    void shouldMapSocialLinkRequestWithNullUrl() {
        SocialLinkRequest requestWithNullUrl = new SocialLinkRequest(
                null,
                SocialType.GITHUB
        );

        SocialLink result = socialLinkMapper.toSocialLink(requestWithNullUrl);

        assertNotNull(result);
        assertNull(result.getUrl());
        assertEquals(SocialType.GITHUB, result.getSocialType());
    }

    @Test
    @DisplayName("toSocialLink should map SocialLinkRequest with OTHER social type")
    void shouldMapSocialLinkRequestWithOtherSocialType() {
        SocialLinkRequest otherRequest = new SocialLinkRequest(
                "https://example.com",
                SocialType.OTHER
        );

        SocialLink result = socialLinkMapper.toSocialLink(otherRequest);

        assertNotNull(result);
        assertEquals("https://example.com", result.getUrl());
        assertEquals(SocialType.OTHER, result.getSocialType());
    }

    @Test
    @DisplayName("toSocialLinkResponse should map SocialLink to SocialLinkResponse with all fields")
    void shouldMapSocialLinkToSocialLinkResponseWithAllFields() {
        SocialLinkResponse result = socialLinkMapper.toSocialLinkResponse(testSocialLink);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("https://github.com/user", result.url());
        assertEquals(SocialType.GITHUB, result.socialType());
    }

    @Test
    @DisplayName("toSocialLinkResponse should map SocialLink with different social types")
    void shouldMapSocialLinkWithDifferentSocialTypesToSocialLinkResponse() {
        SocialLink linkedInLink = SocialLink.builder()
                .id(2L)
                .url("https://linkedin.com/in/user")
                .socialType(SocialType.LINKEDIN)
                .build();

        SocialLinkResponse result = socialLinkMapper.toSocialLinkResponse(linkedInLink);

        assertNotNull(result);
        assertEquals(2L, result.id());
        assertEquals("https://linkedin.com/in/user", result.url());
        assertEquals(SocialType.LINKEDIN, result.socialType());
    }

    @Test
    @DisplayName("toSocialLinkResponse should map SocialLink with Facebook social type")
    void shouldMapSocialLinkWithFacebookToSocialLinkResponse() {
        SocialLink facebookLink = SocialLink.builder()
                .id(3L)
                .url("https://facebook.com/user")
                .socialType(SocialType.FACEBOOK)
                .build();

        SocialLinkResponse result = socialLinkMapper.toSocialLinkResponse(facebookLink);

        assertNotNull(result);
        assertEquals(3L, result.id());
        assertEquals("https://facebook.com/user", result.url());
        assertEquals(SocialType.FACEBOOK, result.socialType());
    }

    @Test
    @DisplayName("toSocialLinkResponse should map SocialLink with Instagram social type")
    void shouldMapSocialLinkWithInstagramToSocialLinkResponse() {
        SocialLink instagramLink = SocialLink.builder()
                .id(4L)
                .url("https://instagram.com/user")
                .socialType(SocialType.INSTAGRAM)
                .build();

        SocialLinkResponse result = socialLinkMapper.toSocialLinkResponse(instagramLink);

        assertNotNull(result);
        assertEquals(4L, result.id());
        assertEquals("https://instagram.com/user", result.url());
        assertEquals(SocialType.INSTAGRAM, result.socialType());
    }

    @Test
    @DisplayName("toSocialLinkResponse should map SocialLink with empty URL")
    void shouldMapSocialLinkWithEmptyUrlToSocialLinkResponse() {
        SocialLink linkWithEmptyUrl = SocialLink.builder()
                .id(5L)
                .url("")
                .socialType(SocialType.GITHUB)
                .build();

        SocialLinkResponse result = socialLinkMapper.toSocialLinkResponse(linkWithEmptyUrl);

        assertNotNull(result);
        assertEquals(5L, result.id());
        assertEquals("", result.url());
        assertEquals(SocialType.GITHUB, result.socialType());
    }

    @Test
    @DisplayName("toSocialLinkResponse should map SocialLink with null URL")
    void shouldMapSocialLinkWithNullUrlToSocialLinkResponse() {
        SocialLink linkWithNullUrl = SocialLink.builder()
                .id(6L)
                .url(null)
                .socialType(SocialType.GITHUB)
                .build();

        SocialLinkResponse result = socialLinkMapper.toSocialLinkResponse(linkWithNullUrl);

        assertNotNull(result);
        assertEquals(6L, result.id());
        assertNull(result.url());
        assertEquals(SocialType.GITHUB, result.socialType());
    }

    @Test
    @DisplayName("toSocialLinkResponse should map SocialLink with User relationship")
    void shouldMapSocialLinkWithUserToSocialLinkResponse() {
        User testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .build();

        testSocialLink.setUser(testUser);

        SocialLinkResponse result = socialLinkMapper.toSocialLinkResponse(testSocialLink);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("https://github.com/user", result.url());
        assertEquals(SocialType.GITHUB, result.socialType());
        // Note: User is not included in SocialLinkResponse, which is correct
    }

    @Test
    @DisplayName("toSocialLink should handle long URL")
    void shouldHandleLongUrl() {
        String longUrl = "https://" + "a".repeat(2000) + ".com";
        SocialLinkRequest requestWithLongUrl = new SocialLinkRequest(
                longUrl,
                SocialType.PORTFOLIO
        );

        SocialLink result = socialLinkMapper.toSocialLink(requestWithLongUrl);

        assertNotNull(result);
        assertEquals(longUrl, result.getUrl());
        assertEquals(SocialType.PORTFOLIO, result.getSocialType());
    }

    @Test
    @DisplayName("toSocialLinkResponse should map SocialLink with all SocialType enum values")
    void shouldMapSocialLinkWithAllSocialTypeEnumValues() {
        SocialType[] allTypes = SocialType.values();

        for (SocialType type : allTypes) {
            SocialLink link = SocialLink.builder()
                    .id(100L)
                    .url("https://example.com/" + type.name().toLowerCase())
                    .socialType(type)
                    .build();

            SocialLinkResponse result = socialLinkMapper.toSocialLinkResponse(link);

            assertNotNull(result);
            assertEquals(type, result.socialType());
            assertEquals("https://example.com/" + type.name().toLowerCase(), result.url());
        }
    }
}
