package com.example.minibroker;

import io.rsocket.AbstractRSocket;
import io.rsocket.Payload;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * FlowControlServer
 *
 * @author jeffsky
 * @since 2019-11-22
 */
public class FlowControlServer {

  public static void main(String[] args) {
    RSocketFactory.receive()
        .acceptor(
            (setup, sendingSocket) ->
                Mono.just(
                    new AbstractRSocket() {
                      @Override
                      public Flux<Payload> requestStream(Payload payload) {
                        return Flux.range(0, 10)
                            .map(
                                n ->
                                    DefaultPayload.create(
                                        String.format("%s_%04d", payload.getDataUtf8(), n)));
                      }
                    }))
        .transport(TcpServerTransport.create(7878))
        .start()
        .block()
        .onClose()
        .block();
  }
}
