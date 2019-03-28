import {Component, OnInit} from '@angular/core';
import {DeviceDataService} from "../device-data.service";
import {DeviceResource} from "../DeviceResource";

@Component({
  selector: 'app-stranger-device',
  templateUrl: './stranger-device.component.html',
  styleUrls: ['./stranger-device.component.css']
})

export class StrangerDeviceComponent implements OnInit {

  id: string;
  modules: Array<DeviceResource>;

  constructor(private ds: DeviceDataService) {
  }

  ngOnInit() {
    this.id = this.ds.id;
    console.log(this.ds.deviceSpecification);
    let a = this.ds.deviceSpecification.deviceSpecification.deviceResources;
    a.forEach(value =>
      value.resourceMethods = value.resourceMethods.filter(value1 => value1.methodType != "GET")
    );
    this.modules = this.ds.deviceSpecification.deviceSpecification.deviceResources;

  }


}
