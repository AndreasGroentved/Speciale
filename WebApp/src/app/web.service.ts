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

  getDeviceValueFromPath(deviceId: string, path: string, callback: (val: string) => (void)) {
    this.http.get(this.serverUrl + "/device/" + deviceId + "/" + path).subscribe(results => {
      callback(results.toString());
    });
  }

  getDevice(deviceID, callback: (device: DeviceSpecification) => (void)) {
    this.http.get("http://localhost:4567/device/" + deviceID).subscribe(results => {
      let device: DeviceSpecification = results as DeviceSpecification;
      callback(device)
    });
  }


}
