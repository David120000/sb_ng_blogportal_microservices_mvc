import { TestBed } from '@angular/core/testing';

import { PostsCacheService } from './posts-cache.service';

describe('PostsCacheService', () => {
  let service: PostsCacheService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PostsCacheService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
