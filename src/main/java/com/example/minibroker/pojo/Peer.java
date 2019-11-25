package com.example.minibroker.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.rsocket.RSocket;
import lombok.Data;

/**
 * Peer
 *
 * @author jeffsky
 * @since 2019-11-22
 */
@Data
public class Peer {
  private String name;
  @JsonIgnore private RSocket socket;
}
