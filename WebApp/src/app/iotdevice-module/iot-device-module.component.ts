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
  @Input() ownedDevice: boolean;
  name = "loading...";
  private getModules: Array<ParameterNameToType> = [];
  private postModules: Array<ParameterNameToType> = [];

  getValues: Map<string, string> = new Map();
  postValues: Map<string, string> = new Map();
  formData = {};


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

  postUpdate() {
    console.log(this.formData);
    this.postModules.forEach(value => {
      if (!this.formData.hasOwnProperty(value.name)) {
        try {
          this.formData[value.name] = this.getValues.get(value.name);
        } catch (e) {
          console.log("can't assign value");
          //NOOOOOOOOOOOOOO!!!!!!!!!!
        }
      }
    });
    let ws = this.webService;
    console.log(this.formData);
    let outer = this;
    ws.postDeviceValue(this.deviceId, this.ownedDevice, this.deviceResource.path, this.formData, val => {
      try {
        Object.keys(val).forEach(key => {
          outer.getValues.set(key, val[key]);
        });
      } catch (e) {

      }
    })
  }

  addGetValue(getMethod: string) {
    this.webService.getDeviceValueFromPath(this.deviceId, this.deviceResource.path, val => {
      this.deviceResource.resourceMethods.filter(value => value.methodType == "GET").map(value => {
        try {
          let a = value.parameters[getMethod];
          this.getValues.set(getMethod, val);
        } catch (e) {
        }
      });
    })
  }

  addPostValue(parameterNameToType: ParameterNameToType) {
    this.postValues.set(parameterNameToType.name, parameterNameToType.type);
  }

}
