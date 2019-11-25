import {Injectable} from '@angular/core';
import {RSocketClient, Utf8Encoders} from 'rsocket-core';
import RSocketWebSocketClient from 'rsocket-websocket-client';
import {Call, Metadata, Peer, PeerController} from './models';
import {Payload} from 'rsocket-types';
import {Single} from 'rsocket-flowable';

@Injectable({
  providedIn: 'root'
})
export class RpcService {

  private you: PeerController;

  constructor() {
  }

  getPeerController(): PeerController {
    return this.you;
  }

  bindPeerController(pc: PeerController) {
    this.you = pc;
  }

  connect(you: Peer): Promise<PeerController> {
    return new Promise<PeerController>((resolve, reject) => {
      const pc = new PeerController(you);
      const setup = {
        keepAlive: 5000,
        lifetime: 60000,
        dataMimeType: 'text/plain',
        metadataMimeType: 'text/plain',
        data: JSON.stringify(you),
      };
      const client = new RSocketClient({
        transport: new RSocketWebSocketClient({
          url: 'ws://127.0.0.1:8080/'
        }, Utf8Encoders),
        setup: setup,
        responder: {
          requestResponse(payload: Payload<string, string>): Single<Payload<string, string>> {
            const call: Call = JSON.parse(payload.data);
            const result = (pc as any)[call.method](...call.args);
            const info = `Oops! Someone call my method #${call.method}!`;
            alert(info);
            return Single.of({
              data: JSON.stringify({
                success: true,
                result: result,
              }),
            });
          }
        }
      });
      client.connect()
        .then(preparedSocket => {
          pc.setSocket(preparedSocket);
          resolve(pc);
        }, e => {
          reject(e);
        });
    });
  }

}
