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

  getDevices(callback: (device: [Device]) => (void)) {
    console.log("get");
    this.http.get(this.serverUrl + "/devices").subscribe(results => {
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
    console.log(postValue);
    let postMessage = new PostMessage(postValue);
    console.log(postMessage);
    console.log(JSON.stringify(postMessage));
    this.http.post(this.serverUrl + "/device/" + deviceId + "/" + path, JSON.stringify(postMessage)).subscribe(results => {
      callback(results);
    });
  }

  getDeviceValueFromPath(deviceId: string, path: string, callback: (val) => (void)) {
    console.log(this.serverUrl + "/device/" + deviceId + "/" + path);
    this.http.get(this.serverUrl + "/device/" + deviceId + "/" + path).subscribe(results => {
      console.log(results);
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
