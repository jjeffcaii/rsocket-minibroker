import {Component, OnInit} from '@angular/core';
import {RpcService} from '../rpc.service';
import {Router} from '@angular/router';
import * as _ from 'lodash';

@Component({
  selector: 'app-console',
  templateUrl: './console.component.html',
  styleUrls: ['./console.component.css']
})
export class ConsoleComponent implements OnInit {

  you = '';
  target = '';
  method = '';
  args = '';

  constructor(private rpc: RpcService, private router: Router) {
  }

  ngOnInit() {
    const pc = this.rpc.getPeerController();
    if (_.isUndefined(pc) || _.isNull(pc)) {
      this.router.navigateByUrl('/login').then();
    } else {
      this.you = pc.getName();
    }
  }

  execute() {
    const call = {
      args: JSON.parse(`[${this.args}]`) as [any],
      method: this.method,
    };
    this.rpc.getPeerController().invoke(this.target, call)
      .then(value => {
        alert(value);
      }, reason => {
        alert(reason);
      });
  }


}
