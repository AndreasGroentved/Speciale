import {Component, Input, OnInit} from '@angular/core';
import {DeviceResource} from "../DeviceResource";

@Component({
  selector: 'app-iot-module',
  templateUrl: './iot-device-module.component.html',
  styleUrls: ['./iot-device-module.component.css']
})
export class IotDeviceModuleComponent implements OnInit {
  @Input() deviceResource: DeviceResource;

  private name = "loading...";

  constructor() {
  }

  ngOnInit() {
    console.log(this.deviceResource);
    this.name = this.deviceResource.path;
  }

}
