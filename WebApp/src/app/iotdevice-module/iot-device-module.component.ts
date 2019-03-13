import {Component, Input, OnInit} from '@angular/core';
import {DeviceResource} from "../DeviceResource";
import {WebService} from "../web.service";
import {ModuleService} from "../module.service";
import {ParameterNameToType} from "../ParameterNameToType";

@Component({
  selector: 'app-iot-module',
  templateUrl: './iot-device-module.component.html',
  styleUrls: ['./iot-device-module.component.css']
})
export class IotDeviceModuleComponent implements OnInit {
  @Input() deviceResource: DeviceResource;
  @Input() deviceId: string;
  private name = "loading...";
  private getModules: Array<ParameterNameToType> = [];
  private postModules: Array<ParameterNameToType> = [];

  private getValues: Map<string, string> = new Map();
  private postValues: Map<string, string> = new Map();

  constructor(private webService: WebService, private moduleService: ModuleService) {
  }

  ngOnInit() {
    let ws = this.webService;
    let ms = this.moduleService;
    console.log(this.deviceResource);
    this.name = this.deviceResource.path;
    this.getModules = ms.getModuleInputTypes(this.deviceResource, "GET");
    this.postModules = ms.getModuleInputTypes(this.deviceResource, "POST");
    this.getModules.forEach(value => this.addGetValue(value.name));
    this.postModules.forEach(value => this.addPostValue(value))
  }

  isBoolean(value: string) {
    return value == "Boolean"
  }

  isString(value: string) {
    return value == "String";
  }

  isInteger(value: string) {
    return value == "Integer";
  }

  isFloat(value: string) {
    return value == "Float";
  }

  postUpdate(key: string, value: string) {
    let ws = this.webService;
    ws.postDeviceValue(this.deviceId, this.deviceResource.path, value, val => {
      console.log(val);
    })
  }

  addGetValue(getMethod: string) {
    this.webService.getDeviceValueFromPath(this.deviceId, this.deviceResource.path, val => {
      this.getValues.set(val, this.deviceResource.path);
    })
  }

  addPostValue(parameterNameToType: ParameterNameToType) {
    this.postValues.set(parameterNameToType.name, parameterNameToType.type);
  }

}
