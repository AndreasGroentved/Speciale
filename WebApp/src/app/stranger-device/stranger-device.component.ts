import {Component, OnInit} from '@angular/core';
import {DeviceDataService} from '../device-data.service';
import {DeviceResource} from '../DeviceResource';
import {DeviceMessage} from '../DeviceMessage';
import {WebService} from '../web.service';

@Component({
  selector: 'app-stranger-device',
  templateUrl: './stranger-device.component.html',
  styleUrls: ['./stranger-device.component.css']
})

export class StrangerDeviceComponent implements OnInit {

  id: string;
  messageChainID: string;
  modules: Array<DeviceResource>;
  messages: Array<DeviceMessage>;
  collapsed = true;

  constructor(private ds: DeviceDataService, private webservice: WebService) {
  }

  ngOnInit() {
    this.id = this.ds.id;
    const a = this.ds.deviceSpecification.deviceSpecification.deviceResources;
    a.forEach(value =>
      value.resourceMethods = value.resourceMethods.filter(value1 => value1.methodType !== 'GET')
    );
    this.modules = this.ds.deviceSpecification.deviceSpecification.deviceResources;
    this.webservice.getMessages(this.id, (messages => {
      this.messages = messages;
    }));
    this.webservice.getMessageChainID(this.id, (value) => {
      this.messageChainID = value;
    });
  }
}
