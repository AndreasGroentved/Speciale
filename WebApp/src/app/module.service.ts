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
    let parameters:Array<ParameterNameToType> = [];
    d.resourceMethods.filter(value => value.methodType == methodType)
      .forEach(value =>
        value.parameters.forEach((value, key) => parameters.push(new ParameterNameToType(value, key)))
      );
    return parameters;
  }

}
