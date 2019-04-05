import {Component, OnInit} from '@angular/core';
import {WebService} from '../web.service';
import {Device} from '../Device';

@Component({
  selector: 'iotdevices',
  templateUrl: './iotdevices.component.html',
  styleUrls: ['./iotdevices.component.css']
})
export class IotdevicesComponent implements OnInit {
  registeredDevices: Array<Device> = [];
  unregisteredDevices: Array<Device> = [];


  constructor(private webService: WebService) {

  }

  ngOnInit() {
    this.updateDeviceList();
  }

  updateDeviceList() {
    this.webService.getDevices(true, devices => {
      this.registeredDevices = devices;
    });
    this.webService.getDevices(false, devices => {
      this.unregisteredDevices = devices;
    });
  }

  registerDevice(id: string) {
    this.webService.addRemoveDevice(id, true, val => {
        this.updateDeviceList();
    });
  }

  unregisterDevice(id: string) {
    this.webService.addRemoveDevice(id, false, val => {
      if (!val.hasOwnProperty('error')) {
        this.updateDeviceList();
      } else {
        console.log('error unregistering device');
      }
    });
  }

}
