import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Device} from './Device';
import {DeviceSpecification} from './DeviceSpecification';
import {Procuration} from './Procuration';
import {TangleDeviceSpecification} from "./TangleDeviceSpecification";
import {DeviceSpecificationToAddressPair} from "./DeviceSpecificationToAddressPair";

@Injectable({
  providedIn: 'root'
})
export class WebService {
  serverUrl = 'http://localhost:4567'; //TODO

  constructor(private http: HttpClient) {

  }

  httpOptions = {
    headers: new HttpHeaders({
      'Content-Type': 'application/json',
    })
  };

  getDevices(registered: boolean, callback: (device: [Device]) => (void)) {
    this.http.get(this.serverUrl + '/device?registered=' + registered).subscribe(results => {
      console.log(results);
      let devices: [Device] = results['result'] as [Device];
      callback(devices);
    });
  }

  addRemoveDevice(deviceId: string, add: boolean, callback: (val) => (void)) {
    if (add) {
      this.http.put(this.serverUrl + '/device/' + deviceId, '').subscribe(results => {
        callback(results['result']);
      });
    } else {
      this.http.delete(this.serverUrl + '/device/' + deviceId).subscribe(results => {
        callback(results['result']);
      });
    }
  }

  getAllDevices(callback: (device: [Device]) => (void)) {
    this.http.get(this.serverUrl + '/device').subscribe(results => {
      console.log(results);
      let devices: [Device] = results['result'] as [Device];
      callback(devices);
    });
  }

  getOnTime(from: string = '0', to: string = Date.now() + '', id: string, callback: (val: Map<string, string>) => (void)) {
    this.http.get(this.serverUrl + '/device/' + id + '/time ? from=' + from + '&to=' + to).subscribe(results => {
      callback(results['result'] as Map<string, string>);
    });
  }

  postDeviceValue(deviceId: string, path: string, postValue, callback: (val) => (void)) {
    for (var key in postValue) {
      postValue[key] = postValue[key].toString();
    }
    /*    let postMessage = new PostMessage(postValue);*/
    console.log('yo');
    console.log(JSON.stringify(postMessage));
    console.log(path);

    this.http.post(this.serverUrl + '/device/' + deviceId + '/' + path, JSON.stringify(postValue)).subscribe(results => {
      callback(results['result']);
    });
  }

  getDeviceValueFromPath(deviceId: string, path: string, callback: (val) => (void)) {
    console.log(this.serverUrl + '/device/' + deviceId + '/' + path);
    this.http.get(this.serverUrl + '/device/' + deviceId + '/' + path).subscribe(results => {
      callback(results['result']);
    });
  }

  getDevice(deviceID, callback: (device: DeviceSpecification) => (void)) {
    this.http.get(this.serverUrl + '/device/' + deviceID).subscribe(results => {
      let device: DeviceSpecification = results['result'] as DeviceSpecification;
      device.deviceResources = device.deviceResources.filter(a => a.path != 'time');
      callback(device);
    });
  }

  updateRules(rules: string, callback: (string) => (void)) {
    this.http.post(this.serverUrl + '/rule', JSON.stringify({rules: rules})).subscribe(results => {
      callback(results['result']);
    });
  }

  requestDevice(addressTo: string, deviceId: string, fromDate: Date, toDate: Date, callback: (val) => (void)) {
    this.http.post(this.serverUrl + '/tangle/unpermissioned/devices/procuration', JSON.stringify({
      addressTo: addressTo,
      deviceId: deviceId,
      dateFrom: fromDate.getTime().toString(),
      dateTo: toDate.getTime().toString()
    })).subscribe(value => {
      callback(value['result'] as [DeviceSpecification]);
    });
  }

  getUnpermissionedTangleDevices(callback: (devices: [DeviceSpecificationToAddressPair]) => (void)) {
    this.http.get(this.serverUrl + '/tangle/unpermissioned/devices').subscribe(value => {
      console.log(value);
      if (value == null) return;
      callback(value['result'] as [DeviceSpecificationToAddressPair]);
    })
  }

  getPermissionedTangleDevices(callback: (devices: [DeviceSpecificationToAddressPair]) => (void)) {
    /*   this.http.get(this.serverUrl + 'tangle/permissioned/devices').subscribe(value => {
         callback(value['result'] as [DeviceSpecification]);
       })*/
  }


  getRules(callback: (string) => (void)) {
    this.http.get(this.serverUrl + '/rule').subscribe(value => {
      console.log(value);
      callback(value['result']);
    });
  }

  getPendingProcurations(callback: (string) => (void)) {
    this.http.get(this.serverUrl + '/device/procurations/pending').subscribe(value => {
      console.log(value);
      callback(value as [Procuration]);
    });
  }

  getAcceptedProcurations(callback: (string) => (void)) {
    this.http.get(this.serverUrl + '/device/procurations/accepted').subscribe(value => {
      console.log(value as Procuration[]);
      callback(value as Procuration[]);
    });
  }

  getExpiredProcurations(callback: (string) => (void)) {
    this.http.get(this.serverUrl + '/device/procurations/expired').subscribe(value => {
      console.log(value);
      callback(value as [Procuration]);
    });
  }

  acceptProcuration(messageChainID: string) {
    this.http.put(this.serverUrl + '/device/procuration/' + messageChainID + '/accept', '').subscribe(value => {
      console.log(value);
    });
  }

  rejectProcuration(messageChainID: string) {
    this.http.put(this.serverUrl + '/device/procuration/' + messageChainID + '/reject', '').subscribe(value => {
      console.log(value);
    });
  }

}
