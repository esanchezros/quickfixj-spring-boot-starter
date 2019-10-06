package io.allune.quickfixj.spring.boot.starter.template;

import org.springframework.util.Assert;
import quickfix.DataDictionary;
import quickfix.DataDictionaryProvider;
import quickfix.FixVersions;
import quickfix.LogUtil;
import quickfix.Message;
import quickfix.MessageUtils;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.field.ApplVerID;

public class QuickFixJTemplate implements QuickFixJOperations {

    private SessionLookupHandler sessionLookupHandler;

    public QuickFixJTemplate() {
        this.sessionLookupHandler = new DefaultSessionLookupHandler();
    }

    public QuickFixJTemplate(SessionLookupHandler sessionLookupHandler) {
        this.sessionLookupHandler = sessionLookupHandler;
    }

    @Override
    public boolean send(Message message, SessionID sessionID) {
        Assert.notNull(message, "'message' must not be null");
        Assert.notNull(sessionID, "'sessionID' must not be null");

        Session session = sessionLookupHandler.lookupBySessionID(sessionID);
        if (session == null) {
            throw new SessionNotFoundException("Session not found: " + sessionID.toString());
        }

        // TODO: Make configurable
        DataDictionaryProvider dataDictionaryProvider = session.getDataDictionaryProvider();
        if (dataDictionaryProvider != null) {
            try {
                ApplVerID applVerID = getApplicationVersionID(session);
                DataDictionary applicationDataDictionary = dataDictionaryProvider.getApplicationDataDictionary(applVerID);
                applicationDataDictionary.validate(message, true);
            } catch (Exception e) {
                LogUtil.logThrowable(sessionID, "Message failed validation: " + e.getMessage(), e);
                throw new MessageValidationException("Message failed validation: " + e.getMessage(), e);
            }
        }

        return session.send(message);
    }

    @Override
    public boolean send(Message message, String qualifier) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean send(Message message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean send(Message message, String senderCompID, String targetCompID) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean send(Message message, String senderCompID, String targetCompID, String qualifier) {
        throw new UnsupportedOperationException();
    }

    // TODO: Extract to DefaultMessageUtils  <- think of better naming
    private ApplVerID getApplicationVersionID(Session session) {
        String beginString = session.getSessionID().getBeginString();
        if (FixVersions.BEGINSTRING_FIXT11.equals(beginString)) {
            return new ApplVerID(ApplVerID.FIX50);
        } else {
            return MessageUtils.toApplVerID(beginString);
        }
    }

//
//    /**
//     * Send a message to the session specified by the provided target company
//     * ID. The sender company ID is provided as an argument rather than from the
//     * message.
//     *
//     * @param message a FIX message
//     * @param senderCompID the sender's company ID
//     * @param targetCompID the target's company ID
//     * @return true is send was successful, false otherwise
//     * @throws SessionNotFound if session could not be located
//     */
//    public static boolean sendToTarget(Message message, String senderCompID, String targetCompID)
//            throws SessionNotFound {
//        return sendToTarget(message, senderCompID, targetCompID, "");
//    }
//
//    /**
//     * Send a message to the session specified by the provided target company
//     * ID. The sender company ID is provided as an argument rather than from the
//     * message. The session qualifier is used to distinguish sessions with the
//     * same target identifiers.
//     *
//     * @param message a FIX message
//     * @param senderCompID the sender's company ID
//     * @param targetCompID the target's company ID
//     * @param qualifier a session qualifier
//     * @return true is send was successful, false otherwise
//     * @throws SessionNotFound if session could not be located
//     */
//    public static boolean sendToTarget(Message message, String senderCompID, String targetCompID,
//                                       String qualifier) throws SessionNotFound {
//        try {
//            return sendToTarget(message,
//                    new SessionID(message.getHeader().getString(BeginString.FIELD), senderCompID,
//                            targetCompID, qualifier));
//        } catch (final SessionNotFound e) {
//            throw e;
//        } catch (final Exception e) {
//            throw new SessionException(e);
//        }
//    }
//
//    /**
//     * Send a message to the session specified by the provided session ID.
//     *
//     * @param message a FIX message
//     * @param sessionID the target SessionID
//     * @return true is send was successful, false otherwise
//     * @throws SessionNotFound if session could not be located
//     */
//    public static boolean sendToTarget(Message message, SessionID sessionID) throws SessionNotFound {
//        final Session session = lookupSession(sessionID);
//        if (session == null) {
//            throw new SessionNotFound();
//        }
//        message.setSessionID(sessionID);
//        return session.send(message);
//    }

}
