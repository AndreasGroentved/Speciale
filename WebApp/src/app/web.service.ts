import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Device} from "./Device";
import {DeviceSpecification} from "./DeviceSpecification";

@Injectable({
  providedIn: 'root'
})
export class WebService {
  serverUrl = "http://localhost:4567"; //TODO

  constructor(private http: HttpClient) {

  }

  httpOptions = {
    headers: new HttpHeaders({
      'Content-Type': 'application/json',
    })
  };

  getDevices(callback: (device: [Device]) => (void)) {
    console.log("get");
    this.http.get(this.serverUrl + "/devices").subscribe(results => {
      console.log(results);
      let devices: [Device] = results as [Device];
      callback(devices)
    });
  }

  getOnTime(from: string = "0", to: string = Date.now() + "", id: string, callback: (val: string) => (void)) {
    this.http.get(this.serverUrl + "/device/" + id + "/time ? from=" + from + "&to" + to).subscribe(results => {
      return results as Map<string, string>;
    });
  }

  postDeviceValue(deviceId: string, path: string, postValue: string, callback: (val: string) => (void)) {
    this.http.get(this.serverUrl + "/device/" + deviceId + "/" + path).subscribe(results => {
      return results.toString();
    });
  }

  getDeviceValueFromPath(deviceId: string, path: string, callback: (val) => (void)) {
    this.http.get(this.serverUrl + "/device/" + deviceId + "/" + path).subscribe(results => {
      callback(results);
    });
  }

  getDevice(deviceID, callback: (device: DeviceSpecification) => (void)) {
    this.http.get(this.serverUrl + "/device/" + deviceID).subscribe(results => {
      let device: DeviceSpecification = results as DeviceSpecification;
      device.deviceResources = device.deviceResources.filter(a => a.path != "time");
      callback(device)
    });
  }


}
