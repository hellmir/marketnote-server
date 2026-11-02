package com.personal.marketnote.common.domain.ipaddress;

import com.personal.marketnote.common.domain.exception.illegalargument.invalidvalue.InvalidIpAddressException;
import com.personal.marketnote.common.domain.exception.illegalargument.novalue.IpAddressNoValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IpAddressTest {

    @Nested
    @DisplayName("of(String) 팩토리 메서드")
    class OfFactory {

        @Test
        @DisplayName("유효한 IPv4(192.168.1.1)로 생성 시 정상 생성된다")
        void shouldCreateIpAddressWithIpv4() {
            IpAddress ipAddress = IpAddress.of("192.168.1.1");

            assertThat(ipAddress.getValue()).isEqualTo("192.168.1.1");
        }

        @Test
        @DisplayName("유효한 IPv4 경계값(0.0.0.0)으로 생성 시 정상 생성된다")
        void shouldCreateIpAddressWithIpv4Zero() {
            IpAddress ipAddress = IpAddress.of("0.0.0.0");

            assertThat(ipAddress.getValue()).isEqualTo("0.0.0.0");
        }

        @Test
        @DisplayName("유효한 IPv4 경계값(255.255.255.255)으로 생성 시 정상 생성된다")
        void shouldCreateIpAddressWithIpv4Max() {
            IpAddress ipAddress = IpAddress.of("255.255.255.255");

            assertThat(ipAddress.getValue()).isEqualTo("255.255.255.255");
        }

        @Test
        @DisplayName("유효한 IPv6 루프백(::1)으로 생성 시 정상 생성된다")
        void shouldCreateIpAddressWithIpv6Loopback() {
            IpAddress ipAddress = IpAddress.of("::1");

            assertThat(ipAddress.getValue()).isEqualTo("::1");
        }

        @Test
        @DisplayName("유효한 IPv6 압축형(2001:db8:85a3::8a2e:370:7334)으로 생성 시 정상 생성된다")
        void shouldCreateIpAddressWithIpv6Compressed() {
            IpAddress ipAddress = IpAddress.of("2001:db8:85a3::8a2e:370:7334");

            assertThat(ipAddress.getValue()).isEqualTo("2001:db8:85a3::8a2e:370:7334");
        }

        @Test
        @DisplayName("유효한 IPv6 풀 무축약(2001:0db8:85a3:0000:0000:8a2e:0370:7334)으로 생성 시 정상 생성된다")
        void shouldCreateIpAddressWithIpv6FullForm() {
            IpAddress ipAddress = IpAddress.of("2001:0db8:85a3:0000:0000:8a2e:0370:7334");

            assertThat(ipAddress.getValue()).isEqualTo("2001:0db8:85a3:0000:0000:8a2e:0370:7334");
        }

        @Test
        @DisplayName("유효한 IPv6 모두 0(::)로 생성 시 정상 생성된다")
        void shouldCreateIpAddressWithIpv6AllZero() {
            IpAddress ipAddress = IpAddress.of("::");

            assertThat(ipAddress.getValue()).isEqualTo("::");
        }

        @Test
        @DisplayName("유효한 IPv4-mapped IPv6(::ffff:192.168.1.1)로 생성 시 정상 생성된다")
        void shouldCreateIpAddressWithIpv4MappedIpv6() {
            IpAddress ipAddress = IpAddress.of("::ffff:192.168.1.1");

            assertThat(ipAddress.getValue()).isEqualTo("::ffff:192.168.1.1");
        }

        @Test
        @DisplayName("IPv4 옥텟 범위 초과(256.1.1.1)로 생성 시 InvalidIpAddressException을 던진다")
        void shouldThrowWhenIpv4OctetOverflow() {
            assertThatThrownBy(() -> IpAddress.of("256.1.1.1"))
                    .isInstanceOf(InvalidIpAddressException.class);
        }

        @Test
        @DisplayName("IPv4 선행 0(192.168.01.1)으로 생성 시 InvalidIpAddressException을 던진다")
        void shouldThrowWhenIpv4LeadingZero() {
            assertThatThrownBy(() -> IpAddress.of("192.168.01.1"))
                    .isInstanceOf(InvalidIpAddressException.class);
        }

        @Test
        @DisplayName("IPv6 연속 콜론 3개(2001:::1)로 생성 시 InvalidIpAddressException을 던진다")
        void shouldThrowWhenIpv6TripleColon() {
            assertThatThrownBy(() -> IpAddress.of("2001:::1"))
                    .isInstanceOf(InvalidIpAddressException.class);
        }

        @Test
        @DisplayName("IPv6 비허용 문자(gggg::1)로 생성 시 InvalidIpAddressException을 던진다")
        void shouldThrowWhenIpv6InvalidHex() {
            assertThatThrownBy(() -> IpAddress.of("gggg::1"))
                    .isInstanceOf(InvalidIpAddressException.class);
        }

        @Test
        @DisplayName("길이 상한(45자) 초과 입력으로 생성 시 InvalidIpAddressException을 던진다")
        void shouldThrowWhenLengthExceedsLimit() {
            String tooLong = "a".repeat(100);

            assertThatThrownBy(() -> IpAddress.of(tooLong))
                    .isInstanceOf(InvalidIpAddressException.class);
        }

        @Test
        @DisplayName("IPv4 옥텟 개수 부족(192.168.1)으로 생성 시 InvalidIpAddressException을 던진다")
        void shouldThrowWhenIpv4OctetMissing() {
            assertThatThrownBy(() -> IpAddress.of("192.168.1"))
                    .isInstanceOf(InvalidIpAddressException.class);
        }

        @Test
        @DisplayName("잘못된 형식(abc.def.ghi.jkl)으로 생성 시 InvalidIpAddressException을 던진다")
        void shouldThrowWhenInvalidFormat() {
            assertThatThrownBy(() -> IpAddress.of("abc.def.ghi.jkl"))
                    .isInstanceOf(InvalidIpAddressException.class);
        }

        @Test
        @DisplayName("호스트명(example.com)으로 생성 시 InvalidIpAddressException을 던진다")
        void shouldThrowWhenHostname() {
            assertThatThrownBy(() -> IpAddress.of("example.com"))
                    .isInstanceOf(InvalidIpAddressException.class);
        }

        @Test
        @DisplayName("null로 생성 시 IpAddressNoValueException을 던진다")
        void shouldThrowWhenNull() {
            assertThatThrownBy(() -> IpAddress.of(null))
                    .isInstanceOf(IpAddressNoValueException.class);
        }

        @Test
        @DisplayName("빈 문자열로 생성 시 IpAddressNoValueException을 던진다")
        void shouldThrowWhenEmpty() {
            assertThatThrownBy(() -> IpAddress.of(""))
                    .isInstanceOf(IpAddressNoValueException.class);
        }

        @Test
        @DisplayName("공백 문자열로 생성 시 IpAddressNoValueException을 던진다")
        void shouldThrowWhenBlank() {
            assertThatThrownBy(() -> IpAddress.of("   "))
                    .isInstanceOf(IpAddressNoValueException.class);
        }
    }

    @Nested
    @DisplayName("equals/hashCode")
    class EqualityContract {

        @Test
        @DisplayName("같은 값이면 동등하다")
        void shouldBeEqualWhenSameValue() {
            IpAddress a = IpAddress.of("192.168.1.1");
            IpAddress b = IpAddress.of("192.168.1.1");

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("다른 값이면 동등하지 않다")
        void shouldNotBeEqualWhenDifferentValue() {
            IpAddress a = IpAddress.of("192.168.1.1");
            IpAddress b = IpAddress.of("192.168.1.2");

            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("null과 비교 시 동등하지 않다")
        void shouldNotBeEqualToNull() {
            IpAddress ipAddress = IpAddress.of("192.168.1.1");

            assertThat(ipAddress).isNotEqualTo(null);
        }

        @Test
        @DisplayName("다른 타입과 비교 시 동등하지 않다")
        void shouldNotBeEqualToOtherType() {
            IpAddress ipAddress = IpAddress.of("192.168.1.1");

            assertThat(ipAddress).isNotEqualTo("192.168.1.1");
        }
    }

    @Nested
    @DisplayName("toString PII 마스킹")
    class ToStringMasking {

        @Test
        @DisplayName("toString은 원본 IPv4 값을 노출하지 않는다")
        void shouldNotLeakRawIpv4InToString() {
            IpAddress ipAddress = IpAddress.of("192.168.1.1");

            assertThat(ipAddress.toString()).doesNotContain("192.168.1.1");
            assertThat(ipAddress.toString()).contains("***");
        }

        @Test
        @DisplayName("toString은 원본 IPv6 값을 노출하지 않는다")
        void shouldNotLeakRawIpv6InToString() {
            IpAddress ipAddress = IpAddress.of("2001:db8:85a3::8a2e:370:7334");

            assertThat(ipAddress.toString()).doesNotContain("2001");
            assertThat(ipAddress.toString()).contains("***");
        }
    }
}
