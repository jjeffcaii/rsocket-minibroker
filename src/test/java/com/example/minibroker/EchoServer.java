package com.example.minibroker;

import io.rsocket.AbstractRSocket;
import io.rsocket.Payload;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.server.TcpServerTransport;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class EchoServer {

  public static void main(String[] args) {
    RSocketFactory.receive()
        .acceptor(
            (setup, sendingSocket) ->
                Mono.just(
                    new AbstractRSocket() {
                      @Override
                      public Mono<Payload> requestResponse(Payload payload) {
                        log.info("rcv: {}", payload.getDataUtf8());
                        return Mono.just(payload);
                      }
                    }))
        .transport(TcpServerTransport.create(7878))
        .start()
        .block()
        .onClose()
        .block();
  }
}
