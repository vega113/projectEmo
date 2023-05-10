import { TestBed } from '@angular/core/testing';

import { SuggestedActionsService } from './suggested-actions.service';

describe('SuggestedActionsService', () => {
  let service: SuggestedActionsService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SuggestedActionsService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
