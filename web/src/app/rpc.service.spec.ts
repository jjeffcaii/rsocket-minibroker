import {TestBed} from '@angular/core/testing';

import {RpcService} from './rpc.service';

describe('RpcService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: RpcService = TestBed.get(RpcService);
    expect(service).toBeTruthy();
  });
});
