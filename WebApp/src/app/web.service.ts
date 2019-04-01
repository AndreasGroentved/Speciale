import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Device} from './Device';
import {DeviceSpecification} from './DeviceSpecification';
import {Procuration} from './Procuration';
import {DeviceSpecificationToAddressPair} from './DeviceSpecificationToAddressPair';
import {TangleDeviceSpecification} from './TangleDeviceSpecification';
import {DeviceDataService} from './device-data.service';
import {DeviceMessage} from './DeviceMessage';

@Injectable({
  providedIn: 'root'
})
export class WebService {
  serverUrl = 'http://localhost:4567'; //TODO

  constructor(private http: HttpClient, private ds: DeviceDataService) {

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

  postDeviceValue(deviceId: string, isOwned: boolean = true, path: string, messageChainID: string = '', postValue, callback: (val) => (void)) {
    for (var key in postValue) {
      postValue[key] = postValue[key].toString();
    }

    if (isOwned) {
      this.http.post(this.serverUrl + '/device/' + deviceId + '/' + path, JSON.stringify(postValue)).subscribe(results => {
        callback(results['result']);
      });
    } else {
      console.log("aaaaaa");
      console.log(messageChainID);
      let post = {
        deviceID: deviceId,
        type: 'POST',
        path: path,
        addressTo: this.ds.addressTo,
        params: postValue,
        messageChainID: messageChainID
      };
      console.log(JSON.stringify(post));
      this.http.post(this.serverUrl + '/tangle/permissioned/devices', JSON.stringify(post)).subscribe(results => {
        callback(results['result']);
      });
    }
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
      callback(results['errors']);
    });
  }

  requestDevice(addressTo: string, deviceId: string, fromDate: Date, toDate: Date, deviceSpec: TangleDeviceSpecification, callback: (val) => (void)) {
    this.http.post(this.serverUrl + '/tangle/unpermissioned/devices/procuration', JSON.stringify({
      addressTo: addressTo,
      specification: JSON.stringify(deviceSpec),
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
    });
  }

  getPermissionedTangleDevices(callback: (devices: [DeviceSpecificationToAddressPair]) => (void)) {
    this.http.get(this.serverUrl + '/tangle/permissioned/devices').subscribe(value => {
      console.log(value);
      callback(value['result'] as [DeviceSpecificationToAddressPair]);
    });
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

  getMessages(deviceID: string, callback: ([Message]) => (void)) {
    this.http.get(this.serverUrl + '/tangle/messages/' + deviceID).subscribe(value => {
      console.log(value);
      callback(value as [DeviceMessage]);
    });
  }

  getMessageChainID(deviceID: string, callback: (string) => (void)) {
    this.http.get(this.serverUrl + '/tangle/messagechainid/' + deviceID).subscribe(value => {
      console.log(value);
      callback(value as string);
    });
  }
}
