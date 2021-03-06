import {Component, Input, OnInit} from '@angular/core';
import {DeviceResource} from '../DeviceResource';
import {WebService} from '../web.service';
import {ModuleService} from '../module.service';
import {ParameterNameToType} from '../ParameterNameToType';

@Component({
  selector: 'app-iot-module',
  templateUrl: './iot-device-module.component.html',
  styleUrls: ['./iot-device-module.component.css']
})
export class IotDeviceModuleComponent implements OnInit {
  //todo array
  @Input() deviceResource: DeviceResource;
  @Input() deviceId: string;
  @Input() ownedDevice: boolean;
  @Input() messageChainID: string;

  name = 'loading...';
  private getModules: Array<ParameterNameToType> = [];
  private postModules: Array<ParameterNameToType> = [];

  getValues: Map<string, string> = new Map();
  postValues: Map<string, string> = new Map();
  formData = {};

  constructor(private webService: WebService, private moduleService: ModuleService) {
  }

  ngOnInit() {
    const ws = this.webService;
    const ms = this.moduleService;
    this.name = this.deviceResource.path;
    this.getModules = ms.getModuleInputTypes(this.deviceResource, 'GET');
    this.postModules = ms.getModuleInputTypes(this.deviceResource, 'POST');
    this.getModules.forEach(value => this.addGetValue(value.name));
    this.postModules.forEach(value => this.addPostValue(value));
  }

  getFilteredGetModules() {
    return this.getModules.filter(m => this.checkForName(m.name));
  }

  checkForName(name: string) {
    return this.postModules.find(p =>
      p.name === name) == null;
  }

  postUpdate() {
    this.postModules.forEach(value => {
      if (!this.formData.hasOwnProperty(value.name)) {
        try {
          this.formData[value.name] = this.getValues.get(value.name);
        } catch (e) {
          console.log('cant assign value');
          // NOOOOOOOOOOOOOO!!!!!!!!!!
        }
      }
    });
    const ws = this.webService;
    const outer = this;
    ws.postDeviceValue(this.deviceId, this.ownedDevice, this.deviceResource.path, this.messageChainID, this.formData, val => {
      try {
        Object.keys(val).forEach(key => {
          this.formData[key] = val[key];
        });
      } catch (e) {
      }
    });
  }

  addGetValue(getMethod: string) {
    this.webService.getDeviceValueFromPath(this.deviceId, this.deviceResource.path, val => {
      this.deviceResource.resourceMethods.filter(value => value.methodType === 'GET').map(value => {
        try {
          this.getValues.set(getMethod, val);
          this.formData[getMethod] = val;
        } catch (e) {
        }
      });
    });

  }

  addPostValue(parameterNameToType: ParameterNameToType) {
    this.postValues.set(parameterNameToType.name, parameterNameToType.type);
  }

}
