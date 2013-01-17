package com.tmall.top.push.mqtt;

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;

import org.apache.commons.lang.time.StopWatch;
import org.junit.Test;

import com.tmall.top.push.messages.MessageType;
import com.tmall.top.push.mqtt.connack.MqttConnectAckMessage;
import com.tmall.top.push.mqtt.connect.MqttConnectMessage;
import com.tmall.top.push.mqtt.disconnect.MqttDisconnectMessage;
import com.tmall.top.push.mqtt.publish.MqttPublishMessage;
import com.tmall.top.push.mqtt.publish.MqttPublishVariableHeader;

public class MqttMessageIOTest {

	private int total = 1000000;

	@Test
	public void get_string_byte_count_test() {
		assertEquals(5, MqttMessageIO.getByteCount("123"));
		assertEquals(5, MqttMessageIO.getByteCount("abc"));
		assertEquals(8, MqttMessageIO.getByteCount("中文"));
		assertEquals(11, MqttMessageIO.getByteCount("123中文"));
		assertEquals(14, MqttMessageIO.getByteCount("中文中文"));
	}

	@Test
	public void read_write_string_test() {
		byte[] bytes = new byte[1024];
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		buffer.position(0);

		String str = "abc-中文";
		MqttMessageIO.writeMqttString(buffer, str);
		assertEquals(12, buffer.position());

		buffer.position(0);
		MqttMessageIO.readMqttString(buffer);
		System.out.println(buffer.position());

		buffer.position(0);
		assertEquals(str, MqttMessageIO.readMqttString(buffer));
		assertEquals(12, buffer.position());
	}

	@Test
	public void read_write_connect_flags_test() {
		byte[] bytes = new byte[1024];
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		MqttConnectFlags flags1 = new MqttConnectFlags();
		flags1.CleanStart = true;
		flags1.Reserved1 = true;
		flags1.Reserved2 = true;
		flags1.Reserved3 = true;
		flags1.WillFlag = true;
		flags1.WillQos = MqttQos.ExactlyOnce;
		flags1.WillRetain = true;
		MqttMessageIO.writeConnectFlags(flags1, buffer);

		buffer.position(0);
		MqttConnectFlags flags2 = new MqttConnectFlags();
		MqttMessageIO.readConnectFlags(flags2, buffer);
		assertEquals(flags1.CleanStart, flags2.CleanStart);
		assertEquals(flags1.Reserved1, flags2.Reserved1);
		assertEquals(flags1.Reserved2, flags2.Reserved2);
		assertEquals(flags1.Reserved3, flags2.Reserved3);
		assertEquals(flags1.WillFlag, flags2.WillFlag);
		assertEquals(flags1.WillQos, flags2.WillQos);
		assertEquals(flags1.WillRetain, flags2.WillRetain);
	}

	@Test
	public void read_write_header_test() {
		byte[] bytes = new byte[1024];
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		MqttHeader header1 = new MqttHeader();
		header1.Duplicate = true;
		header1.MessageType = MqttMessageType.Publish;
		header1.Qos = MqttQos.ExactlyOnce;
		header1.RemainingLength = 100;// 1byte
		MqttMessageIO.writeHeader(header1, buffer);
		assertEquals(2, header1.Length);
		assertEquals(header1.Length, buffer.position());

		buffer.position(0);
		MqttHeader header2 = new MqttHeader();
		MqttMessageIO.readHeader(header2, buffer);
		assertEquals(header1.Duplicate, header2.Duplicate);
		assertEquals(header1.Length, header2.Length);
		assertEquals(header1.MessageType, header2.MessageType);
		assertEquals(header1.Qos, header2.Qos);
		assertEquals(header1.RemainingLength, header2.RemainingLength);
	}

	@Test
	public void read_write_publish_variable_header_need_qos_test() {
		byte[] bytes = new byte[1024];
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		MqttHeader header = new MqttHeader();
		header.Qos = MqttQos.AtLeastOnce;

		MqttPublishVariableHeader vHeader1 = new MqttPublishVariableHeader(
				header);
		vHeader1.MessageIdentifier = 10;
		vHeader1.TopicName = "abc中文";
		MqttMessageIO.writeVariableHeader(vHeader1, buffer);

		buffer.position(0);
		MqttPublishVariableHeader vHeader2 = new MqttPublishVariableHeader(
				header);
		MqttMessageIO.readVariableHeader(vHeader2, buffer);
		assertEquals(vHeader1.MessageIdentifier, vHeader2.MessageIdentifier);
		assertEquals(vHeader1.TopicName, vHeader2.TopicName);
	}

	@Test
	public void client_to_server_publish_parse_test() {
		byte[] bytes = new byte[1024];
		ByteBuffer buffer = ByteBuffer.wrap(bytes);

		MqttPublishMessage msg = new MqttPublishMessage();
		msg.messageType = MessageType.PUBLISH;// 1
		msg.to = "abc";// 8
		msg.bodyFormat = 5;// 1
		msg.remainingLength = 100;

		msg.Header.Qos = MqttQos.AtLeastOnce;
		msg.VariableHeader.TopicName = "abc";// 5
		msg.VariableHeader.MessageIdentifier = 10;// 2
		// 7+113=120 just 1byte
		// msg.Header.RemainingLength = MqttMessageIO
		// .getVariableHeaderWriteLength(msg.VariableHeader)// 7
		// + MessageIO.getFullMessageSize(msg.remainingLength);// 113

		MqttMessageIO.parseClientSending(msg, buffer);
		assertEquals(121, msg.Header.RemainingLength);
		assertEquals(123, MqttMessageIO.getFullMessageSize(msg));

		msg.clear();

		MqttMessageIO.parseServerReceiving(msg, buffer);
		assertEquals(MessageType.PUBLISH, msg.messageType);
		assertEquals("abc", msg.to);
		assertEquals(5, msg.bodyFormat);
		assertEquals(100, msg.remainingLength);

		assertEquals(121, msg.Header.RemainingLength);
		assertEquals(123, MqttMessageIO.getFullMessageSize(msg));

		assertEquals("abc", msg.VariableHeader.TopicName);
		assertEquals(10, msg.VariableHeader.MessageIdentifier);
	}

	@Test
	public void server_to_client_publish_parse_test() {
		byte[] bytes = new byte[1024];
		ByteBuffer buffer = ByteBuffer.wrap(bytes);

		MqttPublishMessage msg = new MqttPublishMessage();
		msg.messageType = MessageType.PUBLISH;
		msg.from = "abc";
		msg.bodyFormat = 5;
		msg.remainingLength = 100;

		msg.Header.Qos = MqttQos.AtLeastOnce;
		msg.VariableHeader.TopicName = "abc";
		msg.VariableHeader.MessageIdentifier = 10;
		MqttMessageIO.parseServerSending(msg, buffer);
		assertEquals(121, msg.Header.RemainingLength);
		assertEquals(123, MqttMessageIO.getFullMessageSize(msg));

		msg.clear();

		MqttMessageIO.parseClientReceiving(msg, buffer);
		assertEquals(MessageType.PUBLISH, msg.messageType);
		assertEquals("abc", msg.from);
		assertEquals(5, msg.bodyFormat);
		assertEquals(100, msg.remainingLength);

		assertEquals(121, msg.Header.RemainingLength);
		assertEquals(123, MqttMessageIO.getFullMessageSize(msg));
		assertEquals("abc", msg.VariableHeader.TopicName);
		assertEquals(10, msg.VariableHeader.MessageIdentifier);
	}

	@Test
	public void connect_parse_test() {
		byte[] bytes = new byte[1024];
		ByteBuffer buffer = ByteBuffer.wrap(bytes);

		MqttConnectMessage msg = new MqttConnectMessage();
		msg.VariableHeader.KeepAlive = 10;
		msg.VariableHeader.ConnectFlags.CleanStart = true;
		msg.VariableHeader.ConnectFlags.WillQos = MqttQos.ExactlyOnce;
		msg.VariableHeader.ConnectFlags.WillRetain = true;
		MqttMessageIO.parseClientSending(msg, buffer);
		msg.clear();

		assertEquals(MqttMessageType.Connect, MqttMessageIO.parseMessageType(buffer.get(0)));
		
		MqttMessageIO.parseServerReceiving(msg, buffer);
		assertEquals(10, msg.VariableHeader.KeepAlive);
		assertEquals(true, msg.VariableHeader.ConnectFlags.CleanStart);
		assertEquals(MqttQos.ExactlyOnce,
				msg.VariableHeader.ConnectFlags.WillQos);
		assertEquals(true, msg.VariableHeader.ConnectFlags.WillRetain);
	}

	@Test
	public void connack_parse_test() {
		byte[] bytes = new byte[1024];
		ByteBuffer buffer = ByteBuffer.wrap(bytes);

		MqttConnectAckMessage msg = new MqttConnectAckMessage();
		msg.VariableHeader.ReturnCode = MqttConnectReturnCode.IdentifierRejected;
		MqttMessageIO.parseServerSending(msg, buffer);
		msg.clear();

		assertEquals(MqttMessageType.ConnectAck, MqttMessageIO.parseMessageType(buffer.get(0)));
		
		MqttMessageIO.parseClientReceiving(msg, buffer);
		assertEquals(MqttConnectReturnCode.IdentifierRejected,
				msg.VariableHeader.ReturnCode);
	}

	@Test
	public void disconnect_parse_test() {
		byte[] bytes = new byte[1024];
		ByteBuffer buffer = ByteBuffer.wrap(bytes);

		MqttDisconnectMessage msg = new MqttDisconnectMessage();
		MqttMessageIO.parseClientSending(msg, buffer);
		msg.clear();
		
		assertEquals(MqttMessageType.Disconnect, MqttMessageIO.parseMessageType(buffer.get(0)));
		
		MqttMessageIO.parseServerReceiving(msg, buffer);
	}

	@Test
	public void parse_perf() {
		byte[] bytes = new byte[1024];
		ByteBuffer buffer = ByteBuffer.wrap(bytes);

		MqttPublishMessage msg = new MqttPublishMessage();
		msg.messageType = MessageType.PUBLISH;
		msg.from = "abcdefgh";// 8length ignore messageio effect abcdefgh
		msg.to = "abcdefgh";
		msg.remainingLength = 100;
		msg.Header.Qos = MqttQos.AtLeastOnce;
		msg.VariableHeader.TopicName = "abc";
		msg.VariableHeader.MessageIdentifier = 10;

		StopWatch watch = new StopWatch();
		watch.start();
		for (int i = 0; i < total; i++)
			MqttMessageIO.parseServerSending(msg, buffer);
		watch.stop();
		System.out
				.println(String.format("---- server write buffer %s cost %sms",
						total, watch.getTime()));

		watch.reset();
		watch.start();
		for (int i = 0; i < total; i++)
			MqttMessageIO.parseClientReceiving(msg, buffer);
		watch.stop();
		System.out
				.println(String.format("---- client read buffer %s cost %sms",
						total, watch.getTime()));

		watch.reset();
		watch.start();
		for (int i = 0; i < total; i++)
			MqttMessageIO.parseClientSending(msg, buffer);
		watch.stop();
		System.out
				.println(String.format("---- client write buffer %s cost %sms",
						total, watch.getTime()));

		msg.clear();
		watch.reset();
		watch.start();
		for (int i = 0; i < total; i++)
			MqttMessageIO.parseServerReceiving(msg, buffer);
		watch.stop();
		System.out
				.println(String.format("---- server read buffer %s cost %sms",
						total, watch.getTime()));
	}

	@Test
	public void write_variable_header_perf() throws Exception {
		byte[] bytes = new byte[1024];
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		MqttHeader header = new MqttHeader();
		header.Qos = MqttQos.AtLeastOnce;
		MqttPublishVariableHeader vHeader1 = new MqttPublishVariableHeader(
				header);
		vHeader1.MessageIdentifier = 10;
		vHeader1.TopicName = "abc中文";

		StopWatch watch = new StopWatch();
		watch.start();
		for (int i = 0; i < total; i++) {
			buffer.position(0);
			MqttMessageIO.writeVariableHeader(vHeader1, buffer);
		}
		watch.stop();
		System.out
				.println(String.format("---- writeVariableHeader %s cost %sms",
						total, watch.getTime()));
	}

	@Test
	public void read_variable_header_perf() throws Exception {
		byte[] bytes = new byte[1024];
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		MqttHeader header = new MqttHeader();
		header.Qos = MqttQos.AtLeastOnce;
		MqttPublishVariableHeader vHeader1 = new MqttPublishVariableHeader(
				header);
		vHeader1.MessageIdentifier = 10;
		vHeader1.TopicName = "abc中文";

		MqttMessageIO.writeVariableHeader(vHeader1, buffer);

		StopWatch watch = new StopWatch();
		watch.start();
		for (int i = 0; i < total; i++) {
			buffer.position(0);
			MqttMessageIO.readVariableHeader(vHeader1, buffer);
		}
		watch.stop();
		System.out
				.println(String.format("---- readVariableHeader %s cost %sms",
						total, watch.getTime()));

	}

}
