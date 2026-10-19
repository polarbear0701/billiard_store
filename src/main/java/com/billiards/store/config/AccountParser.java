package com.billiards.store.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.billiards.store.common.enumerated.Role;

/**
 * Parses the APP_ACCOUNTS setting: entries separated by ';', each written as {@code username:ROLE:password}.
 * The password is everything after the second ':', so it may contain ':' but not ';'.
 * Example: {@code bob:STAFF:pass1;carol:STAFF:pass2}
 */
public final class AccountParser {

	public record Account(String username, Role role, String password) {}

	private static final int MIN_PASSWORD_LENGTH = 8;

	private AccountParser() {}

	/**
	 * @param raw the raw setting value; null or blank means no extra accounts
	 * @return the parsed accounts in declaration order
	 * @throws IllegalArgumentException if an entry is malformed, has an unknown role or a short password,
	 *         or a username appears twice
	 */
	public static List<Account> parse(String raw) {
		List<Account> accounts = new ArrayList<>();
		if (raw == null || raw.isBlank()) {
			return accounts;
		}
		Set<String> seen = new HashSet<>();
		for (String entry : raw.split(";")) {
			if (entry.isBlank()) {
				continue;
			}
			String[] parts = entry.trim().split(":", 3);
			if (parts.length != 3 || parts[0].isBlank()) {
				throw new IllegalArgumentException(
					"Invalid APP_ACCOUNTS entry (expected username:ROLE:password)"
				);
			}
			String username = parts[0].trim();
			Role role;
			try {
				role = Role.valueOf(parts[1].trim().toUpperCase());
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException(
					"Unknown role '" + parts[1].trim() + "' for account '" + username + "'"
				);
			}
			if (parts[2].length() < MIN_PASSWORD_LENGTH) {
				throw new IllegalArgumentException(
					"Password for account '" + username + "' must be at least " + MIN_PASSWORD_LENGTH + " characters"
				);
			}
			if (!seen.add(username)) {
				throw new IllegalArgumentException("Duplicate account '" + username + "'");
			}
			accounts.add(new Account(username, role, parts[2]));
		}
		return accounts;
	}
}
