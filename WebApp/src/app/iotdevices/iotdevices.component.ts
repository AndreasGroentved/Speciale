import {Component, OnInit} from '@angular/core';
import {WebService} from "../web.service";
import {Device} from "../Device";

@Component({
  selector: 'iotdevices',
  templateUrl: './iotdevices.component.html',
  styleUrls: ['./iotdevices.component.css']
})
export class IotdevicesComponent implements OnInit {
  private devices: Array<Device> = [];


  constructor(private webService: WebService) {

  }


  ngOnInit() {
    this.webService.getDevices(devices => {
      this.devices = devices
    })

  }

}
