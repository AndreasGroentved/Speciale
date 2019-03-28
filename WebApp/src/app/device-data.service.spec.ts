import { TestBed } from '@angular/core/testing';

import { DeviceDataService } from './device-data.service';

describe('DeviceDataService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: DeviceDataService = TestBed.get(DeviceDataService);
    expect(service).toBeTruthy();
  });
});
