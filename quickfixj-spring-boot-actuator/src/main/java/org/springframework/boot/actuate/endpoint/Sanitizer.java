/*
 * Copyright 2017-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.boot.actuate.endpoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Strategy that should be used by endpoint implementations to sanitize potentially
 * sensitive keys.
 *
 * @author Christian Dupuis
 * @author Toshiaki Maki
 * @author Phillip Webb
 * @author Nicolas Lejeune
 * @author Stephane Nicoll
 * @author HaiTao Zhang
 * @author Chris Bono
 * @author David Good
 * @author Madhura Bhave
 * @since 2.0.0
 */

/**
 * Borrowed from https://github.com/spring-projects/spring-boot/blob/2.7.x/spring-boot-project/spring-boot-actuator/src/main/java/org/springframework/boot/actuate/endpoint/Sanitizer.java
 * as Sanitizer in Spring Boot 3 is not backward compatible with the version shipped in Spring Boot 2
 */
public class Sanitizer {

	private static final String[] REGEX_PARTS = { "*", "$", "^", "+" };

	private static final Set<String> DEFAULT_KEYS_TO_SANITIZE = new LinkedHashSet<>(
			Arrays.asList("password", "secret", "key", "token", ".*credentials.*", "vcap_services",
					"^vcap\\.services.*$", "sun.java.command", "^spring[._]application[._]json$"));

	private static final Set<String> URI_USERINFO_KEYS = new LinkedHashSet<>(
			Arrays.asList("uri", "uris", "url", "urls", "address", "addresses"));

	private static final Pattern URI_USERINFO_PATTERN = Pattern
		.compile("^\\[?[A-Za-z][A-Za-z0-9\\+\\.\\-]+://.+:(.*)@.+$");

	private Pattern[] keysToSanitize;

	private final List<SanitizingFunction> sanitizingFunctions = new ArrayList<>();

	static {
		DEFAULT_KEYS_TO_SANITIZE.addAll(URI_USERINFO_KEYS);
	}

	/**
	 * Create a new {@link Sanitizer} instance with a default set of keys to sanitize.
	 */
	public Sanitizer() {
		this(DEFAULT_KEYS_TO_SANITIZE.toArray(new String[0]));
	}

	/**
	 * Create a new {@link Sanitizer} instance with specific keys to sanitize.
	 * @param keysToSanitize the keys to sanitize
	 */
	public Sanitizer(String... keysToSanitize) {
		this(Collections.emptyList(), keysToSanitize);
	}

	/**
	 * Create a new {@link Sanitizer} instance with a default set of keys to sanitize and
	 * additional sanitizing functions.
	 * @param sanitizingFunctions the sanitizing functions to apply
	 * @since 2.6.0
	 */
	public Sanitizer(Iterable<SanitizingFunction> sanitizingFunctions) {
		this(sanitizingFunctions, DEFAULT_KEYS_TO_SANITIZE.toArray(new String[0]));
	}

	/**
	 * Create a new {@link Sanitizer} instance with specific keys to sanitize and
	 * additional sanitizing functions.
	 * @param sanitizingFunctions the sanitizing functions to apply
	 * @param keysToSanitize the keys to sanitize
	 * @since 2.6.0
	 */
	public Sanitizer(Iterable<SanitizingFunction> sanitizingFunctions, String... keysToSanitize) {
		sanitizingFunctions.forEach(this.sanitizingFunctions::add);
		this.sanitizingFunctions.add(getDefaultSanitizingFunction());
		setKeysToSanitize(keysToSanitize);
	}

	private SanitizingFunction getDefaultSanitizingFunction() {
		return (data) -> {
			Object sanitizedValue = sanitize(data.getKey(), data.getValue());
			return data.withValue(sanitizedValue);
		};
	}

	/**
	 * Set the keys that should be sanitized, overwriting any existing configuration. Keys
	 * can be simple strings that the property ends with or regular expressions.
	 * @param keysToSanitize the keys to sanitize
	 */
	public void setKeysToSanitize(String... keysToSanitize) {
		Assert.notNull(keysToSanitize, "KeysToSanitize must not be null");
		this.keysToSanitize = new Pattern[keysToSanitize.length];
		for (int i = 0; i < keysToSanitize.length; i++) {
			this.keysToSanitize[i] = getPattern(keysToSanitize[i]);
		}
	}

	/**
	 * Adds keys that should be sanitized. Keys can be simple strings that the property
	 * ends with or regular expressions.
	 * @param keysToSanitize the keys to sanitize
	 * @since 2.5.0
	 */
	public void keysToSanitize(String... keysToSanitize) {
		Assert.notNull(keysToSanitize, "KeysToSanitize must not be null");
		int existingKeys = this.keysToSanitize.length;
		this.keysToSanitize = Arrays.copyOf(this.keysToSanitize, this.keysToSanitize.length + keysToSanitize.length);
		for (int i = 0; i < keysToSanitize.length; i++) {
			this.keysToSanitize[i + existingKeys] = getPattern(keysToSanitize[i]);
		}
	}

	private Pattern getPattern(String value) {
		if (isRegex(value)) {
			return Pattern.compile(value, Pattern.CASE_INSENSITIVE);
		}
		return Pattern.compile(".*" + value + "$", Pattern.CASE_INSENSITIVE);
	}

	private boolean isRegex(String value) {
		for (String part : REGEX_PARTS) {
			if (value.contains(part)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Sanitize the given value if necessary.
	 * @param key the key to sanitize
	 * @param value the value
	 * @return the potentially sanitized value
	 */
	public Object sanitize(String key, Object value) {
		if (value == null) {
			return null;
		}
		for (Pattern pattern : this.keysToSanitize) {
			if (pattern.matcher(key).matches()) {
				if (keyIsUriWithUserInfo(pattern)) {
					return sanitizeUris(value.toString());
				}
				return SanitizableData.SANITIZED_VALUE;
			}
		}
		return value;
	}

	/**
	 * Sanitize the value from the given {@link SanitizableData} using the available
	 * {@link SanitizingFunction}s.
	 * @param data the sanitizable data
	 * @return the potentially updated data
	 * @since 2.6.0
	 */
	public Object sanitize(SanitizableData data) {
		Object value = data.getValue();
		if (value == null) {
			return null;
		}
		for (SanitizingFunction sanitizingFunction : this.sanitizingFunctions) {
			data = sanitizingFunction.apply(data);
			Object sanitizedValue = data.getValue();
			if (!value.equals(sanitizedValue)) {
				return sanitizedValue;
			}
		}
		return value;
	}

	private boolean keyIsUriWithUserInfo(Pattern pattern) {
		for (String uriKey : URI_USERINFO_KEYS) {
			if (pattern.matcher(uriKey).matches()) {
				return true;
			}
		}
		return false;
	}

	private Object sanitizeUris(String value) {
		return Arrays.stream(value.split(",")).map(this::sanitizeUri).collect(Collectors.joining(","));
	}

	private String sanitizeUri(String value) {
		Matcher matcher = URI_USERINFO_PATTERN.matcher(value);
		String password = matcher.matches() ? matcher.group(1) : null;
		if (password != null) {
			return StringUtils.replace(value, ":" + password + "@", ":" + SanitizableData.SANITIZED_VALUE + "@");
		}
		return value;
	}

}
