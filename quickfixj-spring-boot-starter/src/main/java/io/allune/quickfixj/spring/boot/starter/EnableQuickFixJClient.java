package io.allune.quickfixj.spring.boot.starter;

import io.allune.quickfixj.spring.boot.starter.autoconfigure.client.QuickFixJClientMarkerConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Sets up a Fix Initiator (Client), configures the connection to communicate to a Fix Acceptor (Server), handles the
 * connection's lifecycle and processes messages for all sessions
 *
 * @author Eduardo Sanchez-Ros
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(QuickFixJClientMarkerConfiguration.class)
public @interface EnableQuickFixJClient {
}
