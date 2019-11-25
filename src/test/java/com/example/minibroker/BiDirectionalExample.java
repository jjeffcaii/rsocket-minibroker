package com.example.minibroker;

import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * BiDirectionalExample
 *
 * @author jeffsky
 * @since 2019-11-22
 */
@Slf4j
public class BiDirectionalExample {

  public static void main(String[] args) {
    RSocketFactory.receive()
        .acceptor(
            (setup, sendingSocket) -> {

              // 服务器请求客户端
              sendingSocket
                  .requestResponse(DefaultPayload.create("Hello Golang! I'm JAVA"))
                  .subscribeOn(Schedulers.elastic())
                  .doOnNext(payload -> log.info("got response: {}", payload.getDataUtf8()))
                  .subscribe();

              return Mono.empty();
            })
        .transport(TcpServerTransport.create(7878))
        .start()
        .block()
        .onClose()
        .block();
  }
}
