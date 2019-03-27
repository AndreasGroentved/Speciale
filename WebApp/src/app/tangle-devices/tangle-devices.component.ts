import {Component, OnInit} from '@angular/core';
import {WebService} from "../web.service";
import {DeviceSpecification} from "../DeviceSpecification";
import {TangleDeviceSpecification} from "../TangleDeviceSpecification";

@Component({
  selector: 'app-tangle-devices',
  templateUrl: './tangle-devices.component.html',
  styleUrls: ['./tangle-devices.component.css']
})

export class TangleDevicesComponent implements OnInit {

  constructor(private webService: WebService,) {
  }

  unPermissionedDevices: Array<TangleDeviceSpecification> = [];
  permissionedDevices: Array<TangleDeviceSpecification> = [];


  ngOnInit() {
    this.webService.getUnpermissionedTangleDevices(devices => {
      this.unPermissionedDevices = devices;
    });
    this.webService.getPermissionedTangleDevices(devices => {
      this.permissionedDevices = devices;
    });
  }

  getCapabilities(device: DeviceSpecification) {
    return device.deviceResources.map(value => value.path.toString()).toString()
  }

}
