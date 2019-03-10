import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Device} from "./Device";
import {DeviceSpecification} from "./DeviceSpecification";

@Injectable({
  providedIn: 'root'
})
export class WebService {
  serverUrl = "10.126.87.11"; //TODO
  constructor(private http: HttpClient) {

  }

  httpOptions = {
    headers: new HttpHeaders({
      'Content-Type': 'application/json',
    })
  };

  getDevices(callback: (device: [Device]) => (void)) {
    this.http.get("http://localhost:4567/devices").subscribe(results => {
      console.log(results);
      let devices: [Device] = results as [Device];

      callback(devices)
    });
  }


  getDevice(deviceID, callback: (device: DeviceSpecification) => (void)) {
    this.http.get("http://localhost:4567/device/" + deviceID).subscribe(results => {
      let device: DeviceSpecification = results as DeviceSpecification;
      callback(device)
    });
  }


}
