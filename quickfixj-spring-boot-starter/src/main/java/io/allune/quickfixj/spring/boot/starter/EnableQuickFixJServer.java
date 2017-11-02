package io.allune.quickfixj.spring.boot.starter;

import io.allune.quickfixj.spring.boot.starter.autoconfigure.server.QuickFixJServerMarkerConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Sets up a Fix Acceptor (Server), configures the connection to accept connections from a Fix Initiator (Client), handles the
 * connection's lifecycle and processes messages for all sessions
 *
 * @author Eduardo Sanchez-Ros
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(QuickFixJServerMarkerConfiguration.class)
public @interface EnableQuickFixJServer {
}
