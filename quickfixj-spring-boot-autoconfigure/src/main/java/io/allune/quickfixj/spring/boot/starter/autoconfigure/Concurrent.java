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
package io.allune.quickfixj.spring.boot.starter.autoconfigure;

import lombok.Data;

/**
 * Defines the threading model that the {@link quickfix.Connector} should use.
 *
 * @author Eduardo Sanchez-Ros
 */
@Data
public class Concurrent {

	/**
	 * Whether the connector to create should use a separate thread per session ({@code enabled=true}) or a
	 * single thread ({@code enabled=false}) to process messages.
	 */
	private boolean enabled = false;

	/**
	 * Whether the connector to create should use a default {@link quickfix.ExecutorFactory}
	 * ({@code useDefaultExecutorFactory=true}).
	 */
	private boolean useDefaultExecutorFactory = false;

	/**
	 * Queue capacity. An unbounded capacity does not increase the pool and therefore
	 * ignores the "max-size" property.
	 */
	private int queueCapacity = Integer.MAX_VALUE;

	/**
	 * Core number of threads.
	 */
	private int corePoolSize = 8;

	/**
	 * Maximum allowed number of threads. If tasks are filling up the queue, the pool
	 * can expand up to that size to accommodate the load. Ignored if the queue is
	 * unbounded.
	 */
	private int maxPoolSize = Integer.MAX_VALUE;

	/**
	 * Whether core threads are allowed to time out. This enables dynamic growing and
	 * shrinking of the pool.
	 */
	private boolean allowCoreThreadTimeOut = true;

	/**
	 * Time limit for which threads may remain idle before being terminated.
	 */
	private int keepAliveSeconds = 60;

	/**
	 * Whether the executor should wait for scheduled tasks to complete on shutdown.
	 */
	private boolean waitForTasksToCompleteOnShutdown = false;

	/**
	 * Maximum time the executor should wait for remaining tasks to complete.
	 */
	private int awaitTerminationSeconds = 0;

	/**
	 * Prefix to use for the names of newly created threads.
	 */
	private String threadNamePrefix = "QuickFixJ Spring Boot Starter thread-";
}
