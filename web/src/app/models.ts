import {ReactiveSocket} from 'rsocket-types';

export class Peer {
  name: string;
}

export class Call {
  method: string;
  args: [any];
}

export class Metadata {
  source?: string;
  target: string;
}

export class PeerController {

  private socket: ReactiveSocket<String, String>;

  constructor(private you: Peer) {
  }

  getName(): string {
    return this.you.name;
  }

  setSocket(socket: ReactiveSocket<String, String>) {
    this.socket = socket;
  }

  invoke<T>(target: string, call: Call): Promise<T> {
    return new Promise<T>((resolve, reject) => {
      const metadata: Metadata = {target: target};
      this.socket
        .requestResponse({
          data: JSON.stringify(call),
          metadata: JSON.stringify(metadata),
        })
        .then(data => {
          console.log('got:', data);
          const v = JSON.parse(data.data + '');
          if (v.success) {
            resolve(v.result as T);
          } else {
            reject(new Error(v.result + ''));
          }
        }, err => {
          console.log('invoke failed:', err);
          reject(err);
        });
    });
  }

  moo(nickname: string): string {
    return '         (__) \n' +
      '         (oo) \n' +
      '   /------\\/ \n' +
      '  / |    ||   \n' +
      ' *  /\\---/\\ \n' +
      '    ~~   ~~   \n' +
      '...."Have you mooed today, ' + nickname + '?"...';
  }

}
