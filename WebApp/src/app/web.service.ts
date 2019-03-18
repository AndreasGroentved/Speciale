import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Device} from "./Device";
import {DeviceSpecification} from "./DeviceSpecification";
import {PostMessage} from "./PostMessage";

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

  getDevices(registered: boolean, callback: (device: [Device]) => (void)) {
    this.http.get(this.serverUrl + "/device?registered=" + registered).subscribe(results => {
      console.log(results);
      let devices: [Device] = results as [Device];
      callback(devices)
    });
  }

  addRemoveDevice(deviceId: string, add: boolean, callback: (val) => (void)) {
    if (add) {
      this.http.put(this.serverUrl + "/device/" + deviceId, "").subscribe(results => {
        callback(results)
      });
    } else {
      this.http.delete(this.serverUrl + "/device/" + deviceId).subscribe(results => {
        callback(results)
      });
    }
  }

  getAllDevices(callback: (device: [Device]) => (void)) {
    this.http.get(this.serverUrl + "/device").subscribe(results => {
      console.log(results);
      let devices: [Device] = results as [Device];
      callback(devices)
    });
  }

  getOnTime(from: string = "0", to: string = Date.now() + "", id: string, callback: (val: Map<string, string>) => (void)) {
    this.http.get(this.serverUrl + "/device/" + id + "/time ? from=" + from + "&to=" + to).subscribe(results => {
      callback(results as Map<string, string>);
    });
  }

  postDeviceValue(deviceId: string, path: string, postValue, callback: (val) => (void)) {
    let postMessage = new PostMessage(postValue);
    this.http.post(this.serverUrl + "/device/" + deviceId + "/" + path, JSON.stringify(postMessage)).subscribe(results => {
      callback(results);
    });
  }

  getDeviceValueFromPath(deviceId: string, path: string, callback: (val) => (void)) {
    console.log(this.serverUrl + "/device/" + deviceId + "/" + path);
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

  updateRules(rules: string, callback: (string) => (void)) {
    this.http.post(this.serverUrl + "/rule", JSON.stringify({rules: rules})).subscribe(results => {
      callback(results);
    });
  }

  getRules(callback: (string) => (void)) {
    this.http.get(this.serverUrl + "/rule").subscribe(value => {
      callback(value);
    });
  }

}
