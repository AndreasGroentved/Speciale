import {Injectable} from '@angular/core';
import {DeviceResource} from "./DeviceResource";
import {ParameterNameToType} from "./ParameterNameToType";

@Injectable({
  providedIn: 'root'
})
export class ModuleService {

  constructor() {
  }

  getModuleInputTypes(d: DeviceResource, methodType: string): Array<ParameterNameToType> {//Methodtype = GET or POST
    let parameters: Array<ParameterNameToType> = [];
    d.resourceMethods.filter(value1 =>
      value1.methodType == methodType).forEach(value2 =>
      Object.keys(value2.parameters).forEach((key: string) =>
        parameters.push(new ParameterNameToType(key,value2.parameters[key]))));
    return parameters;
  }

}
