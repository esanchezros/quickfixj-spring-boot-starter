//package io.allune.quickfixj.spring.boot.starter.template;
//
//import java.io.IOException;
//
//import quickfix.FieldNotFound;
//import quickfix.Message;
//
//public class HeaderFieldExtractor implements FieldExtractor<Message.Header> {
//
//	@Override
//	public Message.Header extractField(Message message, int fieldTag) throws IOException {
//		Message.Header header = response.getHeader();
//		try {
//			return message.getHeader().getField().getString(fieldTag);
//		} catch (FieldNotFound fieldNotFound) {
//			throw new FieldNotFoundException("Field with ID " + fieldTag + " not found in");
//		}
//		return header;
//	}
//}
