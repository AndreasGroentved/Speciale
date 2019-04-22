import {Injectable} from '@angular/core';
import {TangleDeviceSpecification} from "./TangleDeviceSpecification";
import {TangleDeviceSpecificationPairToMessageId} from "./TangleDeviceSpecificationPairToMessageId";

@Injectable({
  providedIn: 'root'
})
//For sharing data between modules
export class DeviceDataService {

  constructor() {
  }

  id:string;
  deviceSpecification: TangleDeviceSpecification;
  tangleDeviceSpecificationPairToMessageId:TangleDeviceSpecificationPairToMessageId;
  addressTo:string;

}
