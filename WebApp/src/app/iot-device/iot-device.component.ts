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

  private id: string;
  private messages: [DeviceMessage];
  private device: DeviceSpecification;
  private modules: Array<DeviceResource> = [];

  ngOnInit() {
    this.id = this.route.snapshot.paramMap.get('id');
    console.log(this.id);
    this.webService.getDevice(this.id, device => {
      this.device = device;
      this.modules = device.deviceResources;
    });
    this.webService.getMessages(this.id, messages => {
      this.messages = messages;
    });
  }


}
