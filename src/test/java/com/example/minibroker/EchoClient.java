package com.example.minibroker;

import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.DefaultPayload;
import lombok.extern.slf4j.Slf4j;

/**
 * EchoClient
 *
 * @author jeffsky
 * @since 2019-11-22
 */
@Slf4j
public class EchoClient {

  public static void main(String[] args) {
    RSocket client =
        RSocketFactory.connect()
            .setupPayload(DefaultPayload.create("Hi"))
            .transport(TcpClientTransport.create("127.0.0.1", 7878))
            .start()
            .block();
    Payload response = client.requestResponse(DefaultPayload.create("HelloWorld!")).block();
    log.info("got response: {}", response.getDataUtf8());
    client.dispose();
    client.onClose().block();
  }
}
