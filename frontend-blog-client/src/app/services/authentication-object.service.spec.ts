import { TestBed } from '@angular/core/testing';

import { AuthenticationObjectService } from './authentication-object.service';

describe('ComponentCommunicationService', () => {
  let service: AuthenticationObjectService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AuthenticationObjectService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
