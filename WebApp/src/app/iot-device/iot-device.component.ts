import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {WebService} from '../web.service';
import {DeviceSpecification} from '../DeviceSpecification';
import {DeviceResource} from '../DeviceResource';
import {DeviceMessage} from '../DeviceMessage';

@Component({
  selector: 'app-iot-device',
  templateUrl: './iot-device.component.html',
  styleUrls: ['./iot-device.component.css']
})
export class IotDeviceComponent implements OnInit {

  constructor(private route: ActivatedRoute, private router: Router, private webService: WebService) {
  }

  id: string;
  messages: [DeviceMessage];
  device: DeviceSpecification;
  modules: Array<DeviceResource> = [];
  collapsed: boolean = true;

  ngOnInit() {
    this.id = this.route.snapshot.paramMap.get('id');
    this.webService.getDevice(this.id, device => {
      this.device = device;
      this.modules = device.deviceResources;
    });
    this.webService.getMessages(this.id, messages => {
      this.messages = messages;
    });
  }


}
