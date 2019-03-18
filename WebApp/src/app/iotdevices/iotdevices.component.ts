import {Component, OnInit} from '@angular/core';
import {WebService} from "../web.service";
import {Device} from "../Device";

@Component({
  selector: 'iotdevices',
  templateUrl: './iotdevices.component.html',
  styleUrls: ['./iotdevices.component.css']
})
export class IotdevicesComponent implements OnInit {
  private registeredDevices: Array<Device> = [];
  private unregisteredDevices: Array<Device> = [];


  constructor(private webService: WebService) {

  }

  ngOnInit() {
    this.updateDeviceList()
  }

  updateDeviceList() {
    this.webService.getDevices(true, devices => {
      this.registeredDevices = devices
    });
    this.webService.getDevices(false, devices => {
      this.unregisteredDevices = devices
    })
  }

  registerDevice(id: string) {
    this.webService.addRemoveDevice(id, true, val => {
      if (!val.hasOwnProperty("error")) {
        console.log("device registered");
        this.updateDeviceList();
      } else {
        console.log("error registering device")
      }
    })
  }

  unregisterDevice(id: string) {
    this.webService.addRemoveDevice(id, false, val => {
      if (!val.hasOwnProperty("error")) {
        console.log("device unregistered");
        this.updateDeviceList();
      } else {
        console.log("error unregistering device")
      }
    })
  }

}
