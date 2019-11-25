import {Component, OnInit} from '@angular/core';
import {Peer} from '../models';
import {Router} from '@angular/router';
import {RpcService} from '../rpc.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  peer: Peer = {
    name: '',
  };

  constructor(private router: Router, private rpc: RpcService) {
  }

  ngOnInit() {
  }

  async start() {
    const pc = await this.rpc.connect(this.peer);
    this.rpc.bindPeerController(pc);
    await this.router.navigateByUrl(`/console/${this.peer.name}`);
  }

}
