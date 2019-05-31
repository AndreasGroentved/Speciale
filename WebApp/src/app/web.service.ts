import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Device} from './Device';
import {DeviceSpecification} from './DeviceSpecification';
import {Procuration} from './Procuration';
import {DeviceSpecificationToAddressPair} from './DeviceSpecificationToAddressPair';
import {TangleDeviceSpecification} from './TangleDeviceSpecification';
import {DeviceDataService} from './device-data.service';
import {DeviceMessage} from './DeviceMessage';
import {Login} from "./Login";
import {LoginServiceService} from "./login-service.service";

@Injectable({
  providedIn:'root'
})
export class WebService {
  serverUrl = 'http://localhost:4567'; //TODO

  httpOptions = {
    headers:new HttpHeaders({
      'Content-Type':'application/json', 'Authorization':`unset`
    })
  };

  constructor(private http: HttpClient, private ds: DeviceDataService, private ls: LoginServiceService) {
    if (ls.token != "") {
      this.validateToken(ls.token);
      this.setHeader(ls.token);
    }
  }

  isLoggedIn() {
    return this.ls.token != "";
  }

  validateToken(token: string) {
    this.http.post(this.serverUrl + '/validate', JSON.stringify({"token":token}), this.httpOptions).subscribe(results => {
      let token = results["result"] as string;
      if (token == "false") {
        this.ls.setLogin("");
        return
      }
    });
  }

  setHeader(token: string) {
    this.httpOptions = {
      headers:new HttpHeaders({
        'Content-Type':'application/json', 'Authorization':token
      })
    };
  }


  login(userName: string, password: string, callback: (a: string) => (void)) {
    let login = new Login(userName, password);
    this.http.post(this.serverUrl + '/login', JSON.stringify(login), this.httpOptions).subscribe(results => {
      let token = results["result"] as string;
      if (token.length == 0) callback(""); else {
        this.setHeader(token);
        this.ls.setLogin(token);
        callback(token);
      }
    });
  }


  register(userName: string, password: string, callback: (a: string) => (void)) {
    let login = new Login(userName, password);
    this.http.post(this.serverUrl + '/register', JSON.stringify(login), this.httpOptions).subscribe(results => {
      if (!results.hasOwnProperty('error')) {
        return "user created"
      } else {
        return "failed creating user"
      }
    });
  }

  getDevices(registered: boolean, callback: (device: [Device]) => (void)) {
    this.httpOptions.headers.append("moon", "yolo");
    this.http.get(this.serverUrl + '/device?registered=' + registered, this.httpOptions).subscribe(results => {
      let devices: [Device] = results['result'] as [Device];
      callback(devices);
    });
  }

  addRemoveDevice(deviceId: string, add: boolean, callback: (val) => (void)) {
    if (add) {
      this.http.put(this.serverUrl + '/device/' + deviceId, '', this.httpOptions).subscribe(results => {
        callback(results['result']);
      });
    } else {
      this.http.delete(this.serverUrl + '/device/' + deviceId, this.httpOptions).subscribe(results => {
        callback(results['result']);
      });
    }
  }

  getAllDevices(callback: (device: [Device]) => (void)) {
    this.http.get(this.serverUrl + '/device', this.httpOptions).subscribe(results => {
      let devices: [Device] = results['result'] as [Device];
      callback(devices);
    });
  }

  getOnTime(id: string, callback: (val) => (void), from: string = '0', to: string = (Date.now() + '')) {
    this.http.get(this.serverUrl + '/time/' + id + '?from=' + from + '&to=' + to, this.httpOptions).subscribe(results => {
      callback(results);
    });
  }

  postDeviceValue(deviceId: string, isOwned: boolean = true, path: string, messageChainID: string = '', postValue, callback: (val) => (void)) {
    for (var key in postValue) {
      postValue[key] = postValue[key].toString();
    }

    if (isOwned) {
      this.http.post(this.serverUrl + '/device/' + deviceId + '/' + path, JSON.stringify(postValue), this.httpOptions).subscribe(results => {
        callback(results['result']);
      });
    } else {
      let post = {
        deviceID:deviceId,
        type:'POST',
        path:path,
        addressTo:this.ds.addressTo,
        params:postValue,
        messageChainID:messageChainID,
        time:new Date().getTime()
      };
      this.http.post(this.serverUrl + '/tangle/permissioned/devices', JSON.stringify(post), this.httpOptions).subscribe(results => {
        callback(results['result']);
      });
    }
  }

  getDeviceValueFromPath(deviceId: string, path: string, callback: (val) => (void)) {
    console.log(this.serverUrl + '/device/' + deviceId + '/' + path);
    this.http.get(this.serverUrl + '/device/' + deviceId + '/' + path, this.httpOptions).subscribe(results => {
      callback(results['result']);
    });
  }

  getDevice(deviceID, callback: (device: DeviceSpecification) => (void)) {
    this.http.get(this.serverUrl + '/device/' + deviceID, this.httpOptions).subscribe(results => {
      let device: DeviceSpecification = results['result'] as DeviceSpecification;
      callback(device);
    });
  }

  updateRules(rules: string, callback: (string) => (void)) {
    this.http.post(this.serverUrl + '/rule', JSON.stringify({rules:rules}), this.httpOptions).subscribe(results => {
      callback(results);
    });
  }

  requestDevice(addressTo: string, deviceId: string, fromDate: Date, toDate: Date, deviceSpec: TangleDeviceSpecification, callback: (val) => (void)) {
    this.http.post(this.serverUrl + '/tangle/unpermissioned/devices/procuration', this.getProcObj(addressTo, deviceSpec, deviceId, fromDate, toDate), this.httpOptions).subscribe(value => {
      callback(value['result'] as [DeviceSpecification]);
    });
  }

  private getProcObj(addressTo: string, deviceSpec: TangleDeviceSpecification, deviceId: string, fromDate: Date, toDate: Date) {
    return JSON.stringify({
      addressTo:addressTo,
      specification:JSON.stringify(deviceSpec),
      deviceId:deviceId,
      dateFrom:fromDate.getTime().toString(),
      dateTo:toDate.getTime().toString()
    });
  }

  getUnpermissionedTangleDevices(callback: (devices: [DeviceSpecificationToAddressPair]) => (void)) {
    this.http.get(this.serverUrl + '/tangle/unpermissioned/devices', this.httpOptions).subscribe(value => {
      if (value == null) return;
      callback(value['result'] as [DeviceSpecificationToAddressPair]);
    });
  }

  getPermissionedTangleDevices(callback: (devices: [DeviceSpecificationToAddressPair]) => (void)) {
    this.http.get(this.serverUrl + '/tangle/permissioned/devices', this.httpOptions).subscribe(value => {
      callback(value['result'] as [DeviceSpecificationToAddressPair]);
    });
  }


  getRules(callback: (string) => (void)) {
    this.http.get(this.serverUrl + '/rule', this.httpOptions).subscribe(value => {
      callback(value['result']);
    });
  }

  getReceivedPendingProcurations(callback: (string) => (void)) {
    this.http.get(this.serverUrl + '/device/procurations/received/pending', this.httpOptions).subscribe(value => {
      callback(value['result'] as [Procuration]);
    });
  }

  getReceivedAcceptedProcurations(callback: (string) => (void)) {
    this.http.get(this.serverUrl + '/device/procurations/received/accepted', this.httpOptions).subscribe(value => {
      callback(value['result'] as Procuration[]);
    });
  }

  getReceivedExpiredProcurations(callback: (string) => (void)) {
    this.http.get(this.serverUrl + '/device/procurations/received/expired', this.httpOptions).subscribe(value => {
      callback(value['result'] as [Procuration]);
    });
  }

  getSentPendingProcurations(callback: (string) => (void)) {
    this.http.get(this.serverUrl + '/device/procurations/sent/pending', this.httpOptions).subscribe(value => {
      callback(value['result'] as [Procuration]);
    });
  }

  getSentAcceptedProcurations(callback: (string) => (void)) {
    this.http.get(this.serverUrl + '/device/procurations/sent/accepted', this.httpOptions).subscribe(value => {
      callback(value['result'] as [Procuration]);
    });
  }

  getSentExpiredProcurations(callback: (string) => (void)) {
    this.http.get(this.serverUrl + '/device/procurations/sent/expired', this.httpOptions).subscribe(value => {
      callback(value['result'] as [Procuration]);
    });
  }

  acceptProcuration(messageChainID: string) {
    this.http.put(this.serverUrl + '/device/procuration/received/' + messageChainID + '/accept', '', this.httpOptions).subscribe(value => {
      console.log(value);
    });
  }

  rejectProcuration(messageChainID: string) {
    this.http.put(this.serverUrl + '/device/procuration/received/' + messageChainID + '/reject', '', this.httpOptions).subscribe(value => {
    });
  }

  getMessages(deviceID: string, callback: ([Message]) => (void)) {
    this.http.get(this.serverUrl + '/tangle/messages/' + deviceID, this.httpOptions).subscribe(value => {
      callback(value['result'] as [DeviceMessage]);
    });
  }

  getMessageChainID(deviceID: string, callback: (string) => (void)) {
    this.http.get(this.serverUrl + '/tangle/messagechainid/' + deviceID, this.httpOptions).subscribe(value => {
      callback(value['result'] as string);
    });
  }
}
