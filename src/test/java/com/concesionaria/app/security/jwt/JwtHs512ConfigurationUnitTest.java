package com.concesionaria.app.security.jwt;

import static com.concesionaria.app.security.SecurityUtils.JWT_ALGORITHM;
import static org.assertj.core.api.Assertions.assertThat;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

class JwtHs512ConfigurationUnitTest {

    private static final String DEV_SAMPLE_BASE64_SECRET =
        "ZGV2ZWxvcG1lbnQtb25seS1oczUxMi1qd3Qtc2VjcmV0LWR1bW15LWNoYW5nZS1tZS02NC1ieXRlcy1taW5pbXVt";

    @Test
    void devSampleSecretSupportsHs512JwtEncoding() {
        byte[] keyBytes = Base64.getDecoder().decode(DEV_SAMPLE_BASE64_SECRET);
        SecretKey secretKey = new SecretKeySpec(keyBytes, JWT_ALGORITHM.getName());
        NimbusJwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
        JwtClaimsSet claims = JwtClaimsSet.builder().subject("admin").issuedAt(Instant.now()).build();

        String token = encoder.encode(JwtEncoderParameters.from(JwsHeader.with(JWT_ALGORITHM).build(), claims)).getTokenValue();
        String header = new String(Base64.getUrlDecoder().decode(token.split("\\.")[0]), StandardCharsets.UTF_8);

        assertThat(keyBytes).hasSizeGreaterThanOrEqualTo(64);
        assertThat(header).contains("\"alg\":\"HS512\"");
    }
}
