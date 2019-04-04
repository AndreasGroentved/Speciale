import {Component, OnInit} from '@angular/core';
import {WebService} from '../web.service';
import {DeviceSpecification} from '../DeviceSpecification';
import {TangleDeviceSpecification} from '../TangleDeviceSpecification';
import {Router} from '@angular/router';
import {DeviceDataService} from '../device-data.service';
import {DeviceSpecificationToAddressPair} from '../DeviceSpecificationToAddressPair';

@Component({
  selector: 'app-tangle-devices',
  templateUrl: './tangle-devices.component.html',
  styleUrls: ['./tangle-devices.component.css']
})

export class TangleDevicesComponent implements OnInit {

  constructor(private webService: WebService, private router: Router, private deviceService: DeviceDataService) {
  }

  unPermissionedDevices: Array<DeviceSpecificationToAddressPair> = [];
  permissionedDevices: Array<DeviceSpecificationToAddressPair> = [];


  ngOnInit() {
    this.webService.getUnpermissionedTangleDevices(unpermissioned => {
      this.webService.getPermissionedTangleDevices(permissioned => {
        this.unPermissionedDevices = unpermissioned.filter(d => permissioned.find(hest => hest.address === d.address
          && hest.tangleDeviceSpecification.deviceSpecification.id === d.tangleDeviceSpecification.deviceSpecification.id) == null);
        this.permissionedDevices = permissioned;
      });
    });
  }

  getCapabilities(device: DeviceSpecification) {
    return device.deviceResources.map(value => ' ' + value.path.toString()).toString();
  }

  navigateToStrangerDevice(deviceId, deviceSpecification: TangleDeviceSpecification, addressTo) {
    this.deviceService.deviceSpecification = deviceSpecification;
    this.deviceService.addressTo = addressTo;
    this.deviceService.id = deviceId;
    this.router.navigateByUrl('/stranger');
  }


  navigateToInfo(deviceId, deviceSpecification: TangleDeviceSpecification, addressTo) {
    this.deviceService.deviceSpecification = deviceSpecification;
    this.deviceService.addressTo = addressTo;
    this.deviceService.id = deviceId;
    this.router.navigateByUrl('/tangle/' + deviceId);
    /*this.router.navigate(['/tangle/' + deviceId], {
      queryParams: {device: deviceSpecification},
      queryParamsHandling: "merge",state: {device:deviceSpecification}
    });*/
  }

}
