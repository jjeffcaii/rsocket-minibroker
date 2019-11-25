package com.example.minibroker;

import com.example.minibroker.misc.JSON;
import com.example.minibroker.pojo.ForwardInfo;
import com.example.minibroker.pojo.Peer;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import io.rsocket.AbstractRSocket;
import io.rsocket.Payload;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.server.WebsocketServerTransport;
import io.rsocket.util.DefaultPayload;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * App
 *
 * @author jeffsky
 * @since 2019-11-22
 */
@Slf4j
public class App {

  private static final int DEFAULT_PORT = 8080;

  public static void main(String[] args) {
    final PosixParser parser = new PosixParser();
    final Options options =
        new Options()
            .addOption("p", "port", true, "listen port. (default: " + DEFAULT_PORT + ")")
            .addOption("h", "help", false, "print usage.");
    final HelpFormatter helpFormatter = new HelpFormatter();
    final CommandLine cmd;
    try {
      cmd = parser.parse(options, args);
    } catch (ParseException e) {
      helpFormatter.printHelp("minibroker", options);
      return;
    }
    if (cmd.hasOption("h")) {
      helpFormatter.printHelp("minibroker", options);
      return;
    }
    final App app =
        new App(Integer.parseInt(cmd.getOptionValue("port", String.valueOf(DEFAULT_PORT))));
    app.serve();
  }

  private int port;
  private final ConcurrentMap<String, Peer> peers = new ConcurrentHashMap<>();

  public App(int port) {
    this.port = port;
  }

  private void serve() {
    RSocketFactory.receive()
        .acceptor(
            (setup, socket) -> {
              final Peer peer = JSON.parse(setup.getDataUtf8(), Peer.class);
              Preconditions.checkArgument(
                  StringUtils.isNotBlank(peer.getName()), "missing peer name!");
              peer.setSocket(socket);
              Preconditions.checkArgument(
                  this.peers.putIfAbsent(peer.getName(), peer) == null,
                  "duplicated peer %s",
                  peer.getName());
              log.info("welcome: {}!", peer.getName());
              socket
                  .onClose()
                  .doFinally(
                      signalType -> {
                        this.peers.remove(peer.getName());
                        log.info("goodbye: {}!", peer.getName());
                      })
                  .subscribeOn(Schedulers.elastic())
                  .subscribe();
              return Mono.just(new ForwardRSocket(peer));
            })
        .transport(WebsocketServerTransport.create(this.port))
        .start()
        .doOnSuccess(it -> log.info("+++++ mini broker start success! +++++"))
        .block()
        .onClose()
        .doOnSuccess(it -> log.info("+++++ mini broker stopped! +++++"))
        .block();
  }

  class ForwardRSocket extends AbstractRSocket {

    final Peer source;

    ForwardRSocket(Peer source) {
      this.source = source;
    }

    @Override
    public Mono<Payload> requestResponse(Payload input) {
      final ForwardInfo forwardInfo = JSON.parse(input.getMetadataUtf8(), ForwardInfo.class);
      forwardInfo.setSource(this.source.getName());
      log.info("parse forward: {}", forwardInfo);
      final Peer peer = App.this.peers.get(forwardInfo.getTarget());
      if (peer == null) {
        final String err =
            JSON.stringify(
                ImmutableMap.builder()
                    .put("success", false)
                    .put("result", "no such peer " + forwardInfo.getTarget() + "!")
                    .build());
        return Mono.just(DefaultPayload.create(err));
      }
      return peer.getSocket()
          .requestResponse(input)
          .doOnSuccess(
              it ->
                  log.info(
                      "forward result: data={}, metadata={}",
                      it.getDataUtf8(),
                      it.getMetadataUtf8()));
    }
  }
}
