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
  formdata = {};


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
    console.log(this.formdata);
    this.postModules.forEach(value => {
      if (!this.formdata.hasOwnProperty(value.name)) {
        try {
          console.log("ahh yeah");
          console.log(value.name);
          this.formdata[value.name] = this.getValues.get(value.name);
        } catch (e) {
          console.log("can't assign value");
          //NOOOOOOOOOOOOOO!!!!!!!!!!
        }
      }
    });
    let ws = this.webService;
    console.log(this.formdata);
    let outer = this;
    ws.postDeviceValue(this.deviceId, this.deviceResource.path, this.formdata, val => {
      console.log(val);
      Object.keys(val).forEach(function (key) {
        outer.getValues.set(key, val[key]);
        console.log(key, val[key]);
      });

      console.log(this.getValues);
    })
  }

  addGetValue(getMethod: string) {
    this.webService.getDeviceValueFromPath(this.deviceId, this.deviceResource.path, val => {
      this.deviceResource.resourceMethods.filter(value => value.methodType == "GET").map(value => {
        try {
          console.log(value.parameters);
          let a = value.parameters[getMethod];
          console.log(a);
          this.getValues.set(getMethod, val[getMethod]);
        } catch (e) {
        }
      });
    })
  }

  addPostValue(parameterNameToType: ParameterNameToType) {
    this.postValues.set(parameterNameToType.name, parameterNameToType.type);
  }

}
