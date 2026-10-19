package com.billiards.store;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.billiards.store.common.enumerated.Role;
import com.billiards.store.config.AccountParser;

class AccountParserTest {

    @Test
    void blankMeansNoExtraAccounts() {
        assertThat(AccountParser.parse(null)).isEmpty();
        assertThat(AccountParser.parse("")).isEmpty();
        assertThat(AccountParser.parse("  ")).isEmpty();
    }

    @Test
    void parsesSeveralAccountsAndKeepsColonsInPasswords() {
        var accounts = AccountParser.parse("bob:STAFF:pass-word-1; carol:admin:a:b:c:d:e:f ;");
        assertThat(accounts).hasSize(2);
        assertThat(accounts.get(0)).isEqualTo(new AccountParser.Account("bob", Role.STAFF, "pass-word-1"));
        assertThat(accounts.get(1)).isEqualTo(new AccountParser.Account("carol", Role.ADMIN, "a:b:c:d:e:f"));
    }

    @Test
    void rejectsBadConfigurationSoTheAppFailsFast() {
        assertThatThrownBy(() -> AccountParser.parse("bob:STAFF")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AccountParser.parse(":STAFF:long-enough-pass")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AccountParser.parse("bob:BOSS:long-enough-pass"))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Unknown role");
        assertThatThrownBy(() -> AccountParser.parse("bob:STAFF:short"))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("at least 8");
        assertThatThrownBy(() -> AccountParser.parse("bob:STAFF:long-enough-pass;bob:ADMIN:other-long-pass"))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Duplicate");
    }

    @Test
    void errorMessagesNeverEchoPasswords() {
        assertThatThrownBy(() -> AccountParser.parse("bob:BOSS:super-secret-pw"))
            .hasMessageNotContaining("super-secret-pw");
        assertThatThrownBy(() -> AccountParser.parse("bob:STAFF:tiny"))
            .hasMessageNotContaining("tiny");
    }
}
