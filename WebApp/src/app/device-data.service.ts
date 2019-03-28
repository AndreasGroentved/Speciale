import {Injectable} from '@angular/core';
import {TangleDeviceSpecification} from "./TangleDeviceSpecification";

@Injectable({
  providedIn: 'root'
})
export class DeviceDataService {

  constructor() {
  }

  id:string;
  deviceSpecification: TangleDeviceSpecification;
  addressTo:string;

}
