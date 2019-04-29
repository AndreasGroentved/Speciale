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
      let devices: [Device] = results['result'] as [Device];
      callback(devices);
    });
  }

  getOnTime(id: string, callback: (val) => (void), from: string = '0', to: string = (Date.now() + '')) {
    this.http.get(this.serverUrl + '/time/' + id + '?from=' + from + '&to=' + to).subscribe(results => {
      callback(results);
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
      let post = {
        deviceID: deviceId, type: 'POST', path: path, addressTo: this.ds.addressTo,
        params: postValue, messageChainID: messageChainID, time: new Date().getTime()
      };
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
      callback(device);
    });
  }

  updateRules(rules: string, callback: (string) => (void)) {
    this.http.post(this.serverUrl + '/rule', JSON.stringify({rules: rules})).subscribe(results => {
      callback(results);
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
      if (value == null) return;
      callback(value['result'] as [DeviceSpecificationToAddressPair]);
    });
  }

  getPermissionedTangleDevices(callback: (devices: [DeviceSpecificationToAddressPair]) => (void)) {
    this.http.get(this.serverUrl + '/tangle/permissioned/devices').subscribe(value => {
      callback(value['result'] as [DeviceSpecificationToAddressPair]);
    });
  }


  getRules(callback: (string) => (void)) {
    this.http.get(this.serverUrl + '/rule').subscribe(value => {
      callback(value['result']);
    });
  }

  getReceivedPendingProcurations(callback: (string) => (void)) {
    this.http.get(this.serverUrl + '/device/procurations/received/pending').subscribe(value => {
      callback(value['result'] as [Procuration]);
    });
  }

  getReceivedAcceptedProcurations(callback: (string) => (void)) {
    this.http.get(this.serverUrl + '/device/procurations/received/accepted').subscribe(value => {
      callback(value['result'] as Procuration[]);
    });
  }

  getReceivedExpiredProcurations(callback: (string) => (void)) {
    this.http.get(this.serverUrl + '/device/procurations/received/expired').subscribe(value => {
      callback(value['result'] as [Procuration]);
    });
  }

  getSentPendingProcurations(callback: (string) => (void)) {
    this.http.get(this.serverUrl + '/device/procurations/sent/pending').subscribe(value => {
      callback(value['result'] as [Procuration]);
    });
  }

  getSentAcceptedProcurations(callback: (string) => (void)) {
    this.http.get(this.serverUrl + '/device/procurations/sent/accepted').subscribe(value => {
      callback(value['result'] as [Procuration]);
    });
  }

  getSentExpiredProcurations(callback: (string) => (void)) {
    this.http.get(this.serverUrl + '/device/procurations/sent/expired').subscribe(value => {
      callback(value['result'] as [Procuration]);
    });
  }

  acceptProcuration(messageChainID: string) {
    this.http.put(this.serverUrl + '/device/procuration/received/' + messageChainID + '/accept', '').subscribe(value => {
      console.log(value);
    });
  }

  rejectProcuration(messageChainID: string) {
    this.http.put(this.serverUrl + '/device/procuration/received/' + messageChainID + '/reject', '').subscribe(value => {
    });
  }

  getMessages(deviceID: string, callback: ([Message]) => (void)) {
    this.http.get(this.serverUrl + '/tangle/messages/' + deviceID).subscribe(value => {
      callback(value['result'] as [DeviceMessage]);
    });
  }

  getMessageChainID(deviceID: string, callback: (string) => (void)) {
    this.http.get(this.serverUrl + '/tangle/messagechainid/' + deviceID).subscribe(value => {
      callback(value['result'] as string);
    });
  }
}
